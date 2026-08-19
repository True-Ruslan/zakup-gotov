import { describe, expect, it, vi } from "vitest";
import {
  CHIZHIK_SHOPS_ENDPOINT,
  createChizhikActiveApiClient,
} from "../src/chizhik-active-api-client";

describe("ChizhikActiveApiClient", () => {
  it("uses only the fixed first-party shops endpoint with ordinary CORS semantics and an abort deadline", async () => {
    const fetcher = vi.fn(async () =>
      new Response(
        JSON.stringify([
          {
            id: 26504,
            sap_id: "HD87",
            lon: 37.83372708,
            lat: 55.76833314,
            status: 1,
            name: "Москва, Саянская ул., Дом 11Б",
            locality: "Москва",
            working_hours: "с 09:00 до 21:00",
            average_rating: 3.4,
            ignored_secret_like_field: "must-not-project",
          },
        ]),
        { status: 200, headers: { "content-type": "application/json; charset=utf-8" } },
      ),
    );

    const client = createChizhikActiveApiClient(fetcher);
    const result = await client.listStores();

    expect(fetcher).toHaveBeenCalledTimes(1);
    expect(fetcher).toHaveBeenCalledWith(CHIZHIK_SHOPS_ENDPOINT, {
      method: "GET",
      mode: "cors",
      credentials: "same-origin",
      headers: { Accept: "application/json, text/plain, */*" },
      signal: expect.any(AbortSignal),
    });
    const requestSignal = fetcher.mock.calls[0]?.[1]?.signal;
    expect(requestSignal).toBeInstanceOf(AbortSignal);
    expect(requestSignal?.aborted).toBe(false);
    expect(result).toEqual({
      status: "ok",
      stores: [
        {
          sapId: "HD87",
          longitude: 37.83372708,
          latitude: 55.76833314,
          active: true,
          name: "Москва, Саянская ул., Дом 11Б",
          locality: "Москва",
        },
      ],
    });
  });

  it("fails closed for non-JSON, non-2xx, malformed rows, and impossible coordinates", async () => {
    const cases: Array<Response> = [
      new Response("blocked", { status: 403, headers: { "content-type": "text/plain" } }),
      new Response("<html></html>", {
        status: 200,
        headers: { "content-type": "text/html" },
      }),
      new Response(JSON.stringify([{ sap_id: "HD87", lat: 55.7 }]), {
        status: 200,
        headers: { "content-type": "application/json" },
      }),
      new Response(
        JSON.stringify([
          {
            sap_id: "HD87",
            lon: 37.8,
            lat: 155.7,
            status: 1,
            name: "Invalid latitude",
            locality: "Москва",
          },
        ]),
        { status: 200, headers: { "content-type": "application/json" } },
      ),
      new Response(
        JSON.stringify([
          {
            sap_id: "HD87",
            lon: 237.8,
            lat: 55.7,
            status: 1,
            name: "Invalid longitude",
            locality: "Москва",
          },
        ]),
        { status: 200, headers: { "content-type": "application/json" } },
      ),
    ];

    for (const response of cases) {
      const client = createChizhikActiveApiClient(vi.fn(async () => response));
      await expect(client.listStores()).resolves.toEqual({ status: "unavailable", stores: [] });
    }
  });

  it("fails closed on transport exceptions without exposing exception details", async () => {
    const client = createChizhikActiveApiClient(
      vi.fn(async () => {
        throw new Error("sensitive transport details");
      }),
    );

    await expect(client.listStores()).resolves.toEqual({ status: "unavailable", stores: [] });
  });

  it("builds one fixed store-scoped delivery search request from a validated sap id", async () => {
    const opaquePayload = { schema: "not-yet-accepted" };
    const fetcher = vi.fn(async () =>
      new Response(JSON.stringify(opaquePayload), {
        status: 200,
        headers: { "content-type": "application/json; charset=utf-8" },
      }),
    );
    const client = createChizhikActiveApiClient(fetcher);

    const result = await client.searchStore({ sapId: "HD87", query: "молоко" });

    expect(fetcher).toHaveBeenCalledTimes(1);
    expect(fetcher).toHaveBeenCalledWith(
      "https://app.chizhik.club/delivery/api/catalog/v3/stores/HD87/search?mode=store&include_restrict=true&q=%D0%BC%D0%BE%D0%BB%D0%BE%D0%BA%D0%BE&limit=12",
      {
        method: "GET",
        mode: "cors",
        credentials: "same-origin",
        headers: { Accept: "application/json, text/plain, */*" },
        signal: expect.any(AbortSignal),
      },
    );
    expect(result).toEqual({
      status: "received",
      httpStatus: 200,
      contentType: "application/json; charset=utf-8",
      payload: opaquePayload,
    });
  });

  it("rejects unsafe store ids, blank queries, and unbounded limits before transport", async () => {
    const fetcher = vi.fn(async () =>
      new Response("{}", { status: 200, headers: { "content-type": "application/json" } }),
    );
    const client = createChizhikActiveApiClient(fetcher);

    await expect(client.searchStore({ sapId: "../shops", query: "молоко" })).resolves.toEqual({
      status: "unavailable",
    });
    await expect(client.searchStore({ sapId: "HD87", query: "   " })).resolves.toEqual({
      status: "unavailable",
    });
    await expect(
      client.searchStore({ sapId: "HD87", query: "молоко", limit: 0 }),
    ).resolves.toEqual({ status: "unavailable" });
    await expect(
      client.searchStore({ sapId: "HD87", query: "молоко", limit: 51 }),
    ).resolves.toEqual({ status: "unavailable" });

    expect(fetcher).not.toHaveBeenCalled();
  });

  it("keeps delivery search payload opaque and fails closed for non-json or transport failures", async () => {
    const responses = [
      new Response("blocked", { status: 403, headers: { "content-type": "text/plain" } }),
      new Response("<html></html>", {
        status: 200,
        headers: { "content-type": "text/html" },
      }),
    ];

    for (const response of responses) {
      const client = createChizhikActiveApiClient(vi.fn(async () => response));
      await expect(client.searchStore({ sapId: "HD87", query: "молоко" })).resolves.toEqual({
        status: "unavailable",
      });
    }

    const client = createChizhikActiveApiClient(
      vi.fn(async () => {
        throw new Error("transport details must not escape");
      }),
    );
    await expect(client.searchStore({ sapId: "HD87", query: "молоко" })).resolves.toEqual({
      status: "unavailable",
    });
  });
});
