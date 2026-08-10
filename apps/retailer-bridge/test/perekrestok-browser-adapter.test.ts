// @vitest-environment jsdom

import { readFileSync } from "node:fs";
import { resolve } from "node:path";
import { describe, expect, it } from "vitest";
import { perekrestokBrowserAdapter } from "../src/adapters/perekrestok-browser-adapter";

const OBSERVED_AT = "2026-08-10T11:00:00Z";
const PAGE_URL = new URL("https://www.perekrestok.ru/cat/1?session=secret#fragment");

function fixture(name: string): Document {
  const path = resolve(process.cwd(), "../retailer-bridge/test/fixtures", name);
  return new DOMParser().parseFromString(readFileSync(path, "utf8"), "text/html");
}

describe("perekrestokBrowserAdapter", () => {
  it("extracts store-scoped PLU, minor-unit price and availability from structured state", () => {
    const result = perekrestokBrowserAdapter.collect({
      document: fixture("perekrestok-product-state.html"),
      url: PAGE_URL,
      observedAt: OBSERVED_AT,
    });

    expect(result.status).toBe("ok");
    if (result.status !== "ok") return;

    expect(result.observations).toEqual([
      expect.objectContaining({
        schemaVersion: 1,
        retailerId: "perekrestok",
        sourceProviderId: "perekrestok-browser",
        sourceMode: "BROWSER_BRIDGE",
        fulfillmentContextId: "shop-moscow-001",
        sku: "3431579",
        priceMinor: 8999,
        currencyCode: "RUB",
        availability: "AVAILABLE",
        observedAt: OBSERVED_AT,
        adapterVersion: "1",
      }),
      expect.objectContaining({
        sku: "3431580",
        priceMinor: 10999,
        availability: "UNAVAILABLE",
      }),
      expect.objectContaining({
        sku: "3431581",
        priceMinor: 7999,
        availability: "UNKNOWN",
      }),
    ]);
  });

  it("extracts current live DOM products with store context from first-party resource paths", () => {
    const result = perekrestokBrowserAdapter.collect({
      document: fixture("perekrestok-live-dom-state.html"),
      url: new URL("https://www.perekrestok.ru/cat/mc/25/gotovaa-eda"),
      observedAt: OBSERVED_AT,
      resourceUrls: [
        "https://www.perekrestok.ru/api/customer/1.4.1.0/basket?session=secret",
        "https://www.perekrestok.ru/api/customer/1.4.1.0/shop/656?address=secret#fragment",
      ],
    });

    expect(result.status).toBe("ok");
    if (result.status !== "ok") return;

    expect(result.observations).toEqual([
      expect.objectContaining({
        fulfillmentContextId: "656",
        sku: "4408829",
        productName: "Санитизированный готовый продукт",
        priceMinor: 19999,
        currencyCode: "RUB",
        availability: "UNKNOWN",
      }),
    ]);
  });

  it("requires a unique store context before emitting product observations", () => {
    expect(
      perekrestokBrowserAdapter.collect({
        document: fixture("perekrestok-missing-context.html"),
        url: PAGE_URL,
        observedAt: OBSERVED_AT,
      }),
    ).toEqual({ status: "missing-context", observations: [] });
  });

  it("fails closed on malformed structured state when no valid state remains", () => {
    expect(
      perekrestokBrowserAdapter.collect({
        document: fixture("perekrestok-malformed-state.html"),
        url: PAGE_URL,
        observedAt: OBSERVED_AT,
      }),
    ).toEqual({ status: "malformed-state", observations: [] });
  });

  it("rejects product records with noninteger or negative minor-unit price", () => {
    const document = new DOMParser().parseFromString(
      `<!doctype html><script type="application/json">${JSON.stringify({
        shop: { id: "shop-moscow-001" },
        products: [
          {
            masterData: { plu: "bad-1" },
            title: "Bad fractional",
            priceTag: { price: 89.99 },
            balanceState: "many",
          },
          {
            masterData: { plu: "bad-2" },
            title: "Bad negative",
            priceTag: { price: -1 },
            balanceState: "many",
          },
        ],
      })}</script>`,
      "text/html",
    );

    expect(
      perekrestokBrowserAdapter.collect({ document, url: PAGE_URL, observedAt: OBSERVED_AT }),
    ).toEqual({ status: "missing-product", observations: [] });
  });

  it("supports only the official Perekrestok HTTPS origin", () => {
    expect(perekrestokBrowserAdapter.supports(new URL("https://www.perekrestok.ru/cat/1"))).toBe(true);
    expect(perekrestokBrowserAdapter.supports(new URL("http://www.perekrestok.ru/cat/1"))).toBe(false);
    expect(perekrestokBrowserAdapter.supports(new URL("https://example.com/cat/1"))).toBe(false);
  });
});
