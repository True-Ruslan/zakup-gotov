import { describe, expect, it } from "vitest";
import { canonicalObservedResourceUrl } from "../src/resource-observation-policy";

describe("canonicalObservedResourceUrl", () => {
  it("keeps same-origin pathname evidence and strips query/hash", () => {
    expect(
      canonicalObservedResourceUrl(
        "https://www.perekrestok.ru/api/customer/1.4.1.0/shop/656?session=SECRET#fragment",
        new URL("https://www.perekrestok.ru/cat/1"),
      ),
    ).toBe("https://www.perekrestok.ru/api/customer/1.4.1.0/shop/656");
  });

  it("allows only the Pyaterochka catalog service store path for official 5ka pages", () => {
    const page = new URL("https://5ka.ru/catalog/fixture");

    expect(
      canonicalObservedResourceUrl(
        "https://5d.5ka.ru/api/catalog/v2/stores/ZG001/products?session=SECRET#fragment",
        page,
      ),
    ).toBe("https://5d.5ka.ru/api/catalog/v2/stores/ZG001/products");

    expect(
      canonicalObservedResourceUrl("https://5d.5ka.ru/api/profile/v1/me", page),
    ).toBeNull();
    expect(
      canonicalObservedResourceUrl(
        "https://5d.5ka.ru.evil.example/api/catalog/v2/stores/ZG001/products",
        page,
      ),
    ).toBeNull();
    expect(
      canonicalObservedResourceUrl(
        "https://5d.5ka.ru/api/catalog/v2/stores/ZG001/products",
        new URL("https://www.perekrestok.ru/cat/1"),
      ),
    ).toBeNull();
  });
});
