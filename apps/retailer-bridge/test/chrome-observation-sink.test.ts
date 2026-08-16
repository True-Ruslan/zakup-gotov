import { describe, expect, it, vi } from "vitest";
import {
  createChromeObservationClearer,
  createChromeObservationSink,
} from "../src/collector/chrome-observation-sink";
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

const revision = 1_786_880_000_000_001;

describe("Chrome observation messaging", () => {
  it("sends normalized observations with their lifecycle revision", async () => {
    const sendMessage = vi.fn().mockResolvedValue(undefined);
    const sink = createChromeObservationSink(sendMessage);

    await sink([observation], revision);

    expect(sendMessage).toHaveBeenCalledExactlyOnceWith({
      type: "ZG_STORE_OBSERVATIONS",
      revision,
      observations: [observation],
    });
  });

  it("invalidates stale observations at the same lifecycle revision boundary", async () => {
    const sendMessage = vi.fn().mockResolvedValue(undefined);
    const clear = createChromeObservationClearer(sendMessage);

    await clear(revision);

    expect(sendMessage).toHaveBeenCalledExactlyOnceWith({
      type: "ZG_STORE_OBSERVATIONS",
      revision,
      observations: [],
    });
  });

  it.each([0, -1, Number.NaN, Number.POSITIVE_INFINITY, 1.5])(
    "rejects invalid observation revision %s before messaging",
    async (invalidRevision) => {
      const sendMessage = vi.fn().mockResolvedValue(undefined);
      const sink = createChromeObservationSink(sendMessage);

      await expect(sink([observation], invalidRevision)).rejects.toThrow(
        "observation revision must be a positive safe integer",
      );
      expect(sendMessage).not.toHaveBeenCalled();
    },
  );
});
