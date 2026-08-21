import { describe, expect, it, vi } from "vitest";
import { EMPTY_CHIZHIK_RESOURCE_DIAGNOSTICS } from "../src/chizhik-resource-diagnostics";
import { runChizhikSchemaCanary } from "../src/chizhik-schema-canary";

const PAGE_URL = new URL("https://chizhik.club/catalog/chay-kofe--264C39224/");
const HD87_RESOURCE =
  "https://app.chizhik.club/delivery/api/catalog/v3/stores/HD87/categories/drinks/products";

const STORES = [
  {
    sapId: "HD87",
    longitude: 37.83372708,
    latitude: 55.76833314,
    active: true,
    name: "Москва",
    locality: "Москва",
  },
  {
    sapId: "HD88",
    longitude: 37.80898339,
    latitude: 55.39666279,
    active: true,
    name: "Домодедово",
    locality: "Домодедово",
  },
] as const;

function clientWithPayload(payload: unknown) {
  return {
    listStores: vi.fn(async () => ({ status: "ok" as const, stores: STORES })),
    searchStore: vi.fn(async () => ({
      status: "received" as const,
      httpStatus: 200,
      contentType: "application/json; charset=utf-8",
      payload,
    })),
  };
}

describe("runChizhikSchemaCanary", () => {
  it("uses one browser-evidenced validated store and emits only approved candidate schema keys", async () => {
    const client = clientWithPayload({
      products: [
        {
          plu: 123456,
          name: "Secret product name",
          prices: { regular: "129.99", discount: "99.99" },
          is_available: true,
          stock_limit: "7",
          uom: "шт",
          property_clarification: "0.5 л",
          secretSkuValue: { nested: true },
          sku123: "dynamic-key-must-not-leak",
          promotion: { label: "secret promo" },
        },
      ],
      requestId: "SECRET-REQUEST-ID",
      SECRET_DYNAMIC_KEY: "must-not-leak",
    });

    const result = await runChizhikSchemaCanary({
      client,
      pageUrl: PAGE_URL,
      resourceUrls: [HD87_RESOURCE],
    });

    expect(client.listStores).toHaveBeenCalledTimes(1);
    expect(client.searchStore).toHaveBeenCalledWith({ sapId: "HD87", query: "кола", limit: 1 });
    expect(result).toEqual({
      status: "pass",
      httpStatus: 200,
      contentType: "application/json; charset=utf-8",
      rootType: "object",
      schema: [
        { path: "$", type: "object", fields: { products: "array" } },
        { path: "$.products", type: "array" },
        {
          path: "$.products[]",
          type: "object",
          fields: {
            plu: "number",
            name: "string",
            prices: "object",
            is_available: "boolean",
            stock_limit: "string",
            uom: "string",
            property_clarification: "string",
          },
        },
        {
          path: "$.products[].prices",
          type: "object",
          fields: { regular: "string" },
        },
      ],
    });

    const serialized = JSON.stringify(result);
    expect(serialized).not.toContain("Secret product name");
    expect(serialized).not.toContain("129.99");
    expect(serialized).not.toContain("99.99");
    expect(serialized).not.toContain("SECRET-REQUEST-ID");
    expect(serialized).not.toContain("secretSkuValue");
    expect(serialized).not.toContain("sku123");
    expect(serialized).not.toContain("promotion");
    expect(serialized).not.toContain("discount");
    expect(serialized).not.toContain("SECRET_DYNAMIC_KEY");
  });

  it("fails closed without exactly one evidenced store and never searches", async () => {
    for (const resourceUrls of [
      [] as string[],
      ["https://app.chizhik.club/delivery/api/catalog/v3/stores/UNKNOWN/search"],
      [
        HD87_RESOURCE,
        "https://app.chizhik.club/delivery/api/catalog/v2/stores/HD88/categories/drinks/products",
      ],
      ["https://app.chizhik.club.evil.example/delivery/api/catalog/v3/stores/HD87/search"],
    ]) {
      const client = clientWithPayload({ products: [] });
      await expect(
        runChizhikSchemaCanary({ client, pageUrl: PAGE_URL, resourceUrls }),
      ).resolves.toEqual({
        status: "missing-context",
        diagnostics: EMPTY_CHIZHIK_RESOURCE_DIAGNOSTICS,
      });
      expect(client.searchStore).not.toHaveBeenCalled();
    }
  });

  it("rejects a non-official page origin before any retailer request", async () => {
    const client = clientWithPayload({ products: [] });

    await expect(
      runChizhikSchemaCanary({
        client,
        pageUrl: new URL("https://chizhik.club.evil.example/catalog"),
        resourceUrls: [HD87_RESOURCE],
      }),
    ).resolves.toEqual({ status: "wrong-origin" });

    expect(client.listStores).not.toHaveBeenCalled();
    expect(client.searchStore).not.toHaveBeenCalled();
  });

  it("fails closed when store discovery or delivery search is unavailable", async () => {
    const discoveryUnavailable = {
      listStores: vi.fn(async () => ({ status: "unavailable" as const, stores: [] as const })),
      searchStore: vi.fn(),
    };
    await expect(
      runChizhikSchemaCanary({
        client: discoveryUnavailable,
        pageUrl: PAGE_URL,
        resourceUrls: [HD87_RESOURCE],
      }),
    ).resolves.toEqual({ status: "stores-unavailable" });
    expect(discoveryUnavailable.searchStore).not.toHaveBeenCalled();

    const searchUnavailable = {
      listStores: vi.fn(async () => ({ status: "ok" as const, stores: STORES })),
      searchStore: vi.fn(async () => ({ status: "unavailable" as const })),
    };
    await expect(
      runChizhikSchemaCanary({
        client: searchUnavailable,
        pageUrl: PAGE_URL,
        resourceUrls: [HD87_RESOURCE],
      }),
    ).resolves.toEqual({ status: "search-unavailable" });
  });
});
