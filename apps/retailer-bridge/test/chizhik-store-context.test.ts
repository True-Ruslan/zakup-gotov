import { describe, expect, it } from "vitest";
import { resolveChizhikEvidencedStoreId } from "../src/chizhik-store-context";

const PAGE_URL = new URL("https://chizhik.club/catalog/chay-kofe--264C39224/");
const VALID_STORE_IDS = new Set(["HD87", "HD88"]);
const HD87_RESOURCE =
  "https://app.chizhik.club/delivery/api/catalog/v3/stores/HD87/search?mode=store&q=cola";
const HD87_ALT_RESOURCE =
  "https://app.chizhik.club/delivery/api/catalog/v2/stores/HD87/categories/drinks/products";
const HD88_RESOURCE =
  "https://app.chizhik.club/delivery/api/catalog/v3/stores/HD88/search?mode=store&q=cola";

describe("resolveChizhikEvidencedStoreId", () => {
  it("returns null when no resources are supplied", () => {
    expect(resolveChizhikEvidencedStoreId([], PAGE_URL, VALID_STORE_IDS)).toBeNull();
  });

  it("resolves the sap_id from exactly one evidenced, validated resource", () => {
    expect(
      resolveChizhikEvidencedStoreId([HD87_RESOURCE], PAGE_URL, VALID_STORE_IDS),
    ).toBe("HD87");
  });

  it("deduplicates repeated or differently-shaped resources for the same store", () => {
    expect(
      resolveChizhikEvidencedStoreId(
        [HD87_RESOURCE, HD87_RESOURCE, HD87_ALT_RESOURCE],
        PAGE_URL,
        VALID_STORE_IDS,
      ),
    ).toBe("HD87");
  });

  it("fails closed when the evidenced sap_id is absent from the validated directory", () => {
    expect(
      resolveChizhikEvidencedStoreId(
        ["https://app.chizhik.club/delivery/api/catalog/v3/stores/UNKNOWN/search"],
        PAGE_URL,
        VALID_STORE_IDS,
      ),
    ).toBeNull();
  });

  it("fails closed when two different validated stores are both evidenced", () => {
    expect(
      resolveChizhikEvidencedStoreId(
        [HD87_RESOURCE, HD88_RESOURCE],
        PAGE_URL,
        VALID_STORE_IDS,
      ),
    ).toBeNull();
  });

  it("fails closed when the validated directory is empty", () => {
    expect(
      resolveChizhikEvidencedStoreId([HD87_RESOURCE], PAGE_URL, new Set()),
    ).toBeNull();
  });

  it("ignores resources that do not project a fulfillment context at all", () => {
    expect(
      resolveChizhikEvidencedStoreId(
        [
          "https://app.chizhik.club/api/v1/catalog/unauthorized/products/",
          "https://app.chizhik.club/delivery/api/profile/v1/me",
          "not a url",
        ],
        PAGE_URL,
        VALID_STORE_IDS,
      ),
    ).toBeNull();
  });

  it("ignores a foreign-origin resource even when it carries a validated sap_id", () => {
    expect(
      resolveChizhikEvidencedStoreId(
        ["https://app.chizhik.club.evil.example/delivery/api/catalog/v3/stores/HD87/search"],
        PAGE_URL,
        VALID_STORE_IDS,
      ),
    ).toBeNull();
  });

  it("ignores context resources from a different retailer's contextKey namespace", () => {
    expect(
      resolveChizhikEvidencedStoreId(
        ["https://www.perekrestok.ru/api/customer/1.4.1.0/shop/HD87"],
        PAGE_URL,
        VALID_STORE_IDS,
      ),
    ).toBeNull();
  });

  it("resolves the single valid store even when mixed with unrelated and unknown resources", () => {
    expect(
      resolveChizhikEvidencedStoreId(
        [
          "https://app.chizhik.club/api/v1/shops/",
          "https://app.chizhik.club/delivery/api/catalog/v3/stores/UNKNOWN/search",
          HD87_RESOURCE,
          "not a url",
        ],
        PAGE_URL,
        VALID_STORE_IDS,
      ),
    ).toBe("HD87");
  });
});
