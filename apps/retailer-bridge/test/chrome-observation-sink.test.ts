import { describe, expect, it, vi } from "vitest";
import { createChromeObservationSink } from "../src/collector/chrome-observation-sink";
import type { BrowserObservation } from "../src/model/browser-observation";

const observation: BrowserObservation = {
  schemaVersion: 1,
  retailerId: "perekrestok",
  sourceProviderId: "perekrestok-browser",
  sourceMode: "BROWSER_BRIDGE",
  fulfillmentContextId: "shop-moscow-001",
  sku: "3431579",
  productName: "Молоко",
  priceMinor: 8999,
  currencyCode: "RUB",
  availability: "AVAILABLE",
  observedAt: "2026-08-10T11:00:00Z",
  sourceReference: "https://www.perekrestok.ru/cat/1",
  adapterVersion: "1",
};

describe("createChromeObservationSink", () => {
  it("sends only the normalized observation message contract", async () => {
    const sendMessage = vi.fn().mockResolvedValue(undefined);
    const sink = createChromeObservationSink(sendMessage);

    await sink([observation]);

    expect(sendMessage).toHaveBeenCalledExactlyOnceWith({
      type: "ZG_STORE_OBSERVATIONS",
      observations: [observation],
    });
  });
});
