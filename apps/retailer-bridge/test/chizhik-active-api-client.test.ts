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

  it("fails closed for non-JSON, non-2xx, and malformed store payloads", async () => {
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
});
