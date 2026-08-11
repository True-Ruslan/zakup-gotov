import { describe, expect, it } from "vitest";
import { retailerBrowserAdapters } from "../src/adapters/retailer-browser-adapters";

describe("retailerBrowserAdapters", () => {
  it("registers each supported retailer exactly once", () => {
    expect(retailerBrowserAdapters.map((adapter) => adapter.retailerId)).toEqual([
      "perekrestok",
      "pyaterochka",
    ]);

    expect(new Set(retailerBrowserAdapters.map((adapter) => adapter.retailerId)).size).toBe(
      retailerBrowserAdapters.length,
    );
    expect(new Set(retailerBrowserAdapters.map((adapter) => adapter.adapterId)).size).toBe(
      retailerBrowserAdapters.length,
    );
  });
});
