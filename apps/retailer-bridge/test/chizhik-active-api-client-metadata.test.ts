import { describe, expect, it, vi } from "vitest";
import { createChizhikActiveApiClient } from "../src/chizhik-active-api-client";

describe("ChizhikActiveApiClient delivery-search metadata", () => {
  it("returns only HTTP status and content type alongside the opaque successful payload", async () => {
    const payload = { secretProduct: "must-remain-opaque" };
    const client = createChizhikActiveApiClient(
      vi.fn(async () =>
        new Response(JSON.stringify(payload), {
          status: 200,
          headers: { "content-type": "application/json; charset=utf-8" },
        }),
      ),
    );

    await expect(
      client.searchStore({ sapId: "HD87", query: "кола", limit: 1 }),
    ).resolves.toEqual({
      status: "received",
      httpStatus: 200,
      contentType: "application/json; charset=utf-8",
      payload,
    });
  });
});
