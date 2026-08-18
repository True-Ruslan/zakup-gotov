import { describe, expect, it, vi } from "vitest";
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
  it("uses one browser-evidenced validated store and emits structural schema only", async () => {
    const client = clientWithPayload({
      products: [
        {
          sku: "SECRET-SKU-123",
          name: "Secret product name",
          price: 12999,
          available: true,
          promotion: { label: "secret promo" },
          "123456": "dynamic-key-must-not-leak",
        },
      ],
      requestId: "SECRET-REQUEST-ID",
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
        { path: "$", type: "object", fields: { products: "array", requestId: "string" } },
        { path: "$.products", type: "array" },
        {
          path: "$.products[]",
          type: "object",
          fields: {
            sku: "string",
            name: "string",
            price: "number",
            available: "boolean",
            promotion: "object",
          },
        },
        {
          path: "$.products[].promotion",
          type: "object",
          fields: { label: "string" },
        },
      ],
    });

    const serialized = JSON.stringify(result);
    expect(serialized).not.toContain("SECRET-SKU-123");
    expect(serialized).not.toContain("Secret product name");
    expect(serialized).not.toContain("12999");
    expect(serialized).not.toContain("secret promo");
    expect(serialized).not.toContain("SECRET-REQUEST-ID");
    expect(serialized).not.toContain("123456");
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
      ).resolves.toEqual({ status: "missing-context" });
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
