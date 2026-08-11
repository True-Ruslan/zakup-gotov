// @vitest-environment jsdom

import { readFileSync } from "node:fs";
import { resolve } from "node:path";
import { describe, expect, it } from "vitest";
import { pyaterochkaBrowserAdapter } from "../src/adapters/pyaterochka-browser-adapter";

const OBSERVED_AT = "2026-08-11T08:15:00Z";
const PAGE_URL = new URL("https://5ka.ru/catalog/sanitized--251C13239/?session=secret#fragment");

function fixture(name: string): Document {
  const path = resolve(process.cwd(), "../retailer-bridge/test/fixtures", name);
  return new DOMParser().parseFromString(readFileSync(path, "utf8"), "text/html");
}

describe("pyaterochkaBrowserAdapter", () => {
  it("extracts store-scoped visible products from official catalog DOM and service resource path", () => {
    const result = pyaterochkaBrowserAdapter.collect({
      document: fixture("pyaterochka-catalog-state.html"),
      url: PAGE_URL,
      observedAt: OBSERVED_AT,
      resourceUrls: [
        "https://analytics.example.test/api/catalog/v2/stores/EVIL/products",
        "https://5d.5ka.ru/api/catalog/v2/stores/3CRL/categories/251C13239/products?mode=delivery&cookie=SECRET#fragment",
      ],
    });

    expect(result.status).toBe("ok");
    if (result.status !== "ok") return;

    expect(result.observations).toEqual([
      expect.objectContaining({
        schemaVersion: 1,
        retailerId: "pyaterochka",
        sourceProviderId: "pyaterochka-browser",
        sourceMode: "BROWSER_BRIDGE",
        fulfillmentContextId: "3CRL",
        sku: "3165030",
        productName: "Санитизированный сыр 140г",
        priceMinor: 10599,
        currencyCode: "RUB",
        availability: "UNKNOWN",
        observedAt: OBSERVED_AT,
        adapterVersion: "1",
      }),
      expect.objectContaining({
        fulfillmentContextId: "3CRL",
        sku: "6788",
        productName: "Санитизированные макароны 450г",
        priceMinor: 5999,
        availability: "UNKNOWN",
        adapterVersion: "1",
      }),
    ]);
  });

  it("requires one unique official store context", () => {
    expect(
      pyaterochkaBrowserAdapter.collect({
        document: fixture("pyaterochka-catalog-state.html"),
        url: PAGE_URL,
        observedAt: OBSERVED_AT,
        resourceUrls: [
          "https://5d.5ka.ru/api/catalog/v2/stores/3CRL/products",
          "https://5d.5ka.ru/api/catalog/v2/stores/4ABC/products",
        ],
      }),
    ).toEqual({ status: "missing-context", observations: [] });
  });

  it("does not accept a lookalike service origin as fulfillment context", () => {
    expect(
      pyaterochkaBrowserAdapter.collect({
        document: fixture("pyaterochka-catalog-state.html"),
        url: PAGE_URL,
        observedAt: OBSERVED_AT,
        resourceUrls: [
          "https://5d.5ka.ru.evil.example/api/catalog/v2/stores/3CRL/products",
        ],
      }),
    ).toEqual({ status: "missing-context", observations: [] });
  });

  it("supports only official Pyaterochka HTTPS page origins", () => {
    expect(pyaterochkaBrowserAdapter.supports(new URL("https://5ka.ru/catalog/fixture"))).toBe(true);
    expect(pyaterochkaBrowserAdapter.supports(new URL("https://www.5ka.ru/product/fixture--1/"))).toBe(true);
    expect(pyaterochkaBrowserAdapter.supports(new URL("http://5ka.ru/catalog/fixture"))).toBe(false);
    expect(pyaterochkaBrowserAdapter.supports(new URL("https://5ka.ru.evil.example/catalog/fixture"))).toBe(false);
  });
});
