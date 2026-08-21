import { describe, expect, it } from "vitest";
import { createChizhikResourceDiagnosticsTracker } from "../src/chizhik-resource-diagnostics";

const PAGE_URL = new URL("https://chizhik.club/catalog/search?q=%D0%BA%D0%BE%D0%BB%D0%B0");

describe("Chizhik resource diagnostics", () => {
  it("reports only fixed route-family presence without retaining raw URL or store identifiers", () => {
    const tracker = createChizhikResourceDiagnosticsTracker();

    tracker.observe(
      "https://app.chizhik.club/delivery/api/catalog/v3/stores/SECRET87/search?mode=store&q=%D0%BA%D0%BE%D0%BB%D0%B0",
      PAGE_URL,
    );
    tracker.observe(
      "https://app.chizhik.club/delivery/api/catalog/v4/stores/SECRET99/search?mode=store&q=%D0%BA%D0%BE%D0%BB%D0%B0",
      PAGE_URL,
    );
    tracker.observe("https://app.chizhik.club/api/v1/shops/", PAGE_URL);
    tracker.observe("https://chizhik.club/api/delivery/status", PAGE_URL);
    tracker.observe("https://tracker.example/SECRET87", PAGE_URL);

    const snapshot = tracker.snapshot();
    expect(snapshot).toEqual({
      appOriginSeen: true,
      deliveryApiSeen: true,
      deliveryCatalogSeen: true,
      storeScopedV2V3Seen: true,
      storeScopedOtherVersionSeen: true,
      pageOriginDeliverySeen: true,
    });

    const serialized = JSON.stringify(snapshot);
    expect(serialized).not.toContain("SECRET87");
    expect(serialized).not.toContain("SECRET99");
    expect(serialized).not.toContain("tracker.example");
    expect(serialized).not.toContain("/delivery/");
  });

  it("stays all-false for malformed and unrelated resources", () => {
    const tracker = createChizhikResourceDiagnosticsTracker();
    tracker.observe("not a url", PAGE_URL);
    tracker.observe("https://example.com/catalog/v3/stores/SECRET/search", PAGE_URL);

    expect(tracker.snapshot()).toEqual({
      appOriginSeen: false,
      deliveryApiSeen: false,
      deliveryCatalogSeen: false,
      storeScopedV2V3Seen: false,
      storeScopedOtherVersionSeen: false,
      pageOriginDeliverySeen: false,
    });
  });
});
