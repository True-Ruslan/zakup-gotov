// @vitest-environment jsdom

import { describe, expect, it, vi } from "vitest";
import { BrowserObservationCollector } from "../src/collector/browser-observation-collector";
import type { RetailerBrowserAdapter } from "../src/adapters/retailer-browser-adapter";

const OBSERVED_AT = "2026-08-10T11:00:00Z";

function adapterReturning(observation: Record<string, unknown>): RetailerBrowserAdapter {
  return {
    adapterId: "fixture",
    retailerId: "perekrestok",
    supports: () => true,
    collect: () => ({
      status: "ok",
      observations: [observation],
    }),
  } as RetailerBrowserAdapter;
}

describe("BrowserObservationCollector", () => {
  it("projects adapter output onto the allow-listed normalized contract", async () => {
    const sink = vi.fn().mockResolvedValue(undefined);
    const adapter = adapterReturning({
      schemaVersion: 1,
      retailerId: "perekrestok",
      sourceProviderId: "perekrestok-browser",
      sourceMode: "BROWSER_BRIDGE",
      fulfillmentContextId: "shop-1",
      sku: "3431579",
      productName: "Молоко",
      priceMinor: 8999,
      currencyCode: "RUB",
      availability: "AVAILABLE",
      observedAt: OBSERVED_AT,
      sourceReference: "https://www.perekrestok.ru/cat/1?token=secret#fragment",
      adapterVersion: "1",
      cookie: "SECRET_COOKIE",
      authorization: "Bearer secret",
      address: "Secret street",
    });

    const collector = new BrowserObservationCollector([adapter], sink);
    const result = await collector.collect(
      document.implementation.createHTMLDocument(),
      new URL("https://www.perekrestok.ru/cat/1?token=secret#fragment"),
      OBSERVED_AT,
    );

    expect(result).toEqual({ status: "ok", observationCount: 1 });
    expect(sink).toHaveBeenCalledTimes(1);
    expect(sink.mock.calls[0][0]).toEqual([
      {
        schemaVersion: 1,
        retailerId: "perekrestok",
        sourceProviderId: "perekrestok-browser",
        sourceMode: "BROWSER_BRIDGE",
        fulfillmentContextId: "shop-1",
        sku: "3431579",
        productName: "Молоко",
        priceMinor: 8999,
        currencyCode: "RUB",
        availability: "AVAILABLE",
        observedAt: OBSERVED_AT,
        sourceReference: "https://www.perekrestok.ru/cat/1",
        adapterVersion: "1",
      },
    ]);
  });

  it("fails closed when no adapter supports the page", async () => {
    const sink = vi.fn();
    const adapter = {
      adapterId: "fixture",
      retailerId: "perekrestok",
      supports: () => false,
      collect: vi.fn(),
    } as unknown as RetailerBrowserAdapter;

    const result = await new BrowserObservationCollector([adapter], sink).collect(
      document.implementation.createHTMLDocument(),
      new URL("https://example.com/"),
      OBSERVED_AT,
    );

    expect(result).toEqual({ status: "unsupported-page", observationCount: 0 });
    expect(sink).not.toHaveBeenCalled();
  });

  it.each([
    ["blank fulfillment context", { fulfillmentContextId: "" }],
    ["negative price", { priceMinor: -1 }],
    ["fractional minor-unit price", { priceMinor: 89.99 }],
    ["invalid observation time", { observedAt: "not-an-instant" }],
  ])("rejects %s", async (_name, override) => {
    const sink = vi.fn();
    const adapter = adapterReturning({
      schemaVersion: 1,
      retailerId: "perekrestok",
      sourceProviderId: "perekrestok-browser",
      sourceMode: "BROWSER_BRIDGE",
      fulfillmentContextId: "shop-1",
      sku: "3431579",
      productName: "Молоко",
      priceMinor: 8999,
      currencyCode: "RUB",
      availability: "UNKNOWN",
      observedAt: OBSERVED_AT,
      sourceReference: "https://www.perekrestok.ru/cat/1",
      adapterVersion: "1",
      ...override,
    });

    const result = await new BrowserObservationCollector([adapter], sink).collect(
      document.implementation.createHTMLDocument(),
      new URL("https://www.perekrestok.ru/cat/1"),
      OBSERVED_AT,
    );

    expect(result).toEqual({ status: "invalid-observation", observationCount: 0 });
    expect(sink).not.toHaveBeenCalled();
  });

  it("propagates a fail-closed adapter status without persisting data", async () => {
    const sink = vi.fn();
    const adapter = {
      adapterId: "fixture",
      retailerId: "perekrestok",
      supports: () => true,
      collect: () => ({ status: "missing-context", observations: [] }),
    } as RetailerBrowserAdapter;

    const result = await new BrowserObservationCollector([adapter], sink).collect(
      document.implementation.createHTMLDocument(),
      new URL("https://www.perekrestok.ru/cat/1"),
      OBSERVED_AT,
    );

    expect(result).toEqual({ status: "missing-context", observationCount: 0 });
    expect(sink).not.toHaveBeenCalled();
  });
});
