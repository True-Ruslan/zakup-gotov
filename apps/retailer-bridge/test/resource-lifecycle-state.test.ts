import { describe, expect, it } from "vitest";
import {
  applySameDocumentNavigationReset,
  INITIAL_RESOURCE_LIFECYCLE_STATE,
  rememberAllowedResource,
  retainCurrentFulfillmentResource,
  type ResourceLifecycleState,
} from "../src/resource-lifecycle-state";

const CHIZHIK_PAGE = new URL("https://chizhik.club/catalog");
const CHIZHIK_STORE_A =
  "https://app.chizhik.club/delivery/api/catalog/v3/stores/store-a/";
const CHIZHIK_STORE_B =
  "https://app.chizhik.club/delivery/api/catalog/v3/stores/store-b/";

const PEREKRESTOK_PAGE = new URL("https://www.perekrestok.ru/cart");
const PEREKRESTOK_SHOP_A = "https://www.perekrestok.ru/api/customer/me/shop/111/";
const PEREKRESTOK_SHOP_B = "https://www.perekrestok.ru/api/customer/me/shop/222/";

const PYATEROCHKA_PAGE = new URL("https://5ka.ru/search");
const PYATEROCHKA_STORE_A = "https://5d.5ka.ru/api/catalog/v2/stores/store-a/";
const PYATEROCHKA_STORE_B = "https://5d.5ka.ru/api/catalog/v2/stores/store-b/";

function establishContext(
  pageUrl: URL,
  resourceUrl: string,
  startTime = 0,
): ResourceLifecycleState {
  const awaiting: ResourceLifecycleState = {
    ...INITIAL_RESOURCE_LIFECYCLE_STATE,
    awaitingFreshContext: true,
  };
  return rememberAllowedResource(awaiting, resourceUrl, pageUrl, startTime).state;
}

describe.each([
  { retailer: "chizhik", page: CHIZHIK_PAGE, storeA: CHIZHIK_STORE_A, storeB: CHIZHIK_STORE_B },
  {
    retailer: "perekrestok",
    page: PEREKRESTOK_PAGE,
    storeA: PEREKRESTOK_SHOP_A,
    storeB: PEREKRESTOK_SHOP_B,
  },
  {
    retailer: "pyaterochka",
    page: PYATEROCHKA_PAGE,
    storeA: PYATEROCHKA_STORE_A,
    storeB: PYATEROCHKA_STORE_B,
  },
])("same-document navigation lifecycle ($retailer)", ({ page, storeA }) => {
  it("discards an already-evidenced fulfillment context across a same-document navigation and re-arms a fresh-context wait", () => {
    const evidenced = establishContext(page, storeA);
    expect(evidenced.currentFulfillmentContextKey).not.toBeNull();

    const result = applySameDocumentNavigationReset(1_000);

    expect(result.awaitingFreshContext).toBe(true);
    expect(result.currentFulfillmentContextKey).toBeNull();
    expect(result.observedResourceUrls.size).toBe(0);
    expect(result.contextSignalFloorStartTime).toBe(1_000);
  });

  it("re-establishes context once the exact accepted resource re-fires after navigation", () => {
    const evidenced = establishContext(page, storeA);
    const afterNavigation = applySameDocumentNavigationReset(1_000);

    const reconfirmed = rememberAllowedResource(afterNavigation, storeA, page, 1_500);

    expect(reconfirmed.changed).toBe(true);
    expect(reconfirmed.state.currentFulfillmentContextKey).toBe(
      evidenced.currentFulfillmentContextKey,
    );
    expect(reconfirmed.state.awaitingFreshContext).toBe(false);
  });

  it("retainCurrentFulfillmentResource keeps only resources tied to the current context", () => {
    const evidenced = establishContext(page, storeA);
    const withExtra: ResourceLifecycleState = {
      ...evidenced,
      observedResourceUrls: new Set([...evidenced.observedResourceUrls, `${page.origin}/noise`]),
    };

    const retained = retainCurrentFulfillmentResource(withExtra, page);

    expect(retained.observedResourceUrls.has(storeA)).toBe(true);
    expect(retained.observedResourceUrls.has(`${page.origin}/noise`)).toBe(false);
  });
});

describe("rememberAllowedResource — floor and duplicate handling", () => {
  it("ignores a context resource observed before the signal floor", () => {
    const state: ResourceLifecycleState = {
      ...INITIAL_RESOURCE_LIFECYCLE_STATE,
      contextSignalFloorStartTime: 5_000,
    };

    const result = rememberAllowedResource(state, CHIZHIK_STORE_A, CHIZHIK_PAGE, 1_000);

    expect(result.changed).toBe(false);
    expect(result.state).toBe(state);
  });

  it("does not report a change for a duplicate already-observed resource", () => {
    const evidenced = establishContext(CHIZHIK_PAGE, CHIZHIK_STORE_A);

    const result = rememberAllowedResource(evidenced, CHIZHIK_STORE_A, CHIZHIK_PAGE, 10);

    expect(result.changed).toBe(false);
  });
});
