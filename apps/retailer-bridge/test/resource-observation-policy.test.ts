import { describe, expect, it } from "vitest";
import {
  canonicalObservedResourceUrl,
  fulfillmentContextResource,
} from "../src/resource-observation-policy";

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

  it("allows only path-only Chizhik public catalog and store-scoped delivery catalog evidence", () => {
    const page = new URL("https://chizhik.club/deeplink?action_type=to_screen");

    expect(
      canonicalObservedResourceUrl(
        "https://app.chizhik.club/api/v1/catalog/unauthorized/categories/?store=SECRET#fragment",
        page,
      ),
    ).toBe("https://app.chizhik.club/api/v1/catalog/unauthorized/categories/");
    expect(
      canonicalObservedResourceUrl(
        "https://app.chizhik.club/api/v1/catalog/unauthorized/products/?lat=SECRET&lon=SECRET",
        page,
      ),
    ).toBe("https://app.chizhik.club/api/v1/catalog/unauthorized/products/");
    expect(
      canonicalObservedResourceUrl(
        "https://app.chizhik.club/delivery/api/catalog/v3/stores/HD87/search?mode=store&q=SECRET#fragment",
        page,
      ),
    ).toBe("https://app.chizhik.club/delivery/api/catalog/v3/stores/HD87/search");
    expect(
      canonicalObservedResourceUrl(
        "https://app.chizhik.club/delivery/api/catalog/v2/stores/HD87/categories/drinks/products?token=SECRET",
        page,
      ),
    ).toBe(
      "https://app.chizhik.club/delivery/api/catalog/v2/stores/HD87/categories/drinks/products",
    );

    expect(
      canonicalObservedResourceUrl(
        "https://app.chizhik.club/api/v1/profile/me",
        page,
      ),
    ).toBeNull();
    expect(
      canonicalObservedResourceUrl(
        "https://app.chizhik.club/delivery/api/profile/v1/me",
        page,
      ),
    ).toBeNull();
    expect(
      canonicalObservedResourceUrl(
        "https://app.chizhik.club/delivery/api/catalog/v3/stores/HD87/../../profile/me",
        page,
      ),
    ).toBeNull();
    expect(
      canonicalObservedResourceUrl(
        "https://app.chizhik.club/delivery/api/catalog/v3/stores/%2Fprofile/search",
        page,
      ),
    ).toBeNull();
    expect(
      canonicalObservedResourceUrl(
        "https://app.chizhik.club/api/v1/catalog/unauthorized/products/",
        new URL("https://chizhik.club.evil.example/"),
      ),
    ).toBeNull();
    expect(
      canonicalObservedResourceUrl(
        "https://app.chizhik.club.evil.example/delivery/api/catalog/v3/stores/HD87/search",
        page,
      ),
    ).toBeNull();
  });
});

describe("fulfillmentContextResource", () => {
  it("projects only the Perekrestok shop id and sanitized canonical URL", () => {
    const page = new URL("https://www.perekrestok.ru/cat/1");

    expect(
      fulfillmentContextResource(
        "https://www.perekrestok.ru/api/customer/1.4.1.0/shop/656?session=SECRET#fragment",
        page,
      ),
    ).toEqual({
      contextKey: "perekrestok:656",
      canonicalUrl: "https://www.perekrestok.ru/api/customer/1.4.1.0/shop/656",
    });
    expect(
      fulfillmentContextResource("https://www.perekrestok.ru/api/catalog/products", page),
    ).toBeNull();
  });

  it("projects only the official Pyaterochka store path without query/hash", () => {
    const page = new URL("https://5ka.ru/catalog/fixture");

    expect(
      fulfillmentContextResource(
        "https://5d.5ka.ru/api/catalog/v2/stores/ZG001/products?token=SECRET#fragment",
        page,
      ),
    ).toEqual({
      contextKey: "pyaterochka:ZG001",
      canonicalUrl: "https://5d.5ka.ru/api/catalog/v2/stores/ZG001/products",
    });
    expect(
      fulfillmentContextResource("https://5d.5ka.ru/api/profile/v1/me", page),
    ).toBeNull();
  });

  it("projects an exact Chizhik delivery catalog store path as fulfillment context", () => {
    const page = new URL("https://chizhik.club/catalog/test");

    expect(
      fulfillmentContextResource(
        "https://app.chizhik.club/delivery/api/catalog/v3/stores/HD87/search?mode=store&q=SECRET",
        page,
      ),
    ).toEqual({
      contextKey: "chizhik:HD87",
      canonicalUrl: "https://app.chizhik.club/delivery/api/catalog/v3/stores/HD87/search",
    });
    expect(
      fulfillmentContextResource(
        "https://app.chizhik.club/delivery/api/catalog/v2/stores/HD87/categories/drinks/products?token=SECRET",
        page,
      ),
    ).toEqual({
      contextKey: "chizhik:HD87",
      canonicalUrl:
        "https://app.chizhik.club/delivery/api/catalog/v2/stores/HD87/categories/drinks/products",
    });
  });

  it("does not promote public Chizhik catalog resources or unsafe delivery paths", () => {
    const page = new URL("https://chizhik.club/");

    expect(
      fulfillmentContextResource(
        "https://app.chizhik.club/api/v1/catalog/unauthorized/products/?store=SECRET",
        page,
      ),
    ).toBeNull();
    expect(
      fulfillmentContextResource(
        "https://app.chizhik.club/delivery/api/catalog/v3/stores/%2Fprofile/search",
        page,
      ),
    ).toBeNull();
    expect(
      fulfillmentContextResource(
        "https://app.chizhik.club.evil.example/delivery/api/catalog/v3/stores/HD87/search",
        page,
      ),
    ).toBeNull();
  });
});
