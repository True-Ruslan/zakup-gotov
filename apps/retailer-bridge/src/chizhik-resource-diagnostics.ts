const CHIZHIK_PAGE_ORIGIN = "https://chizhik.club";
const CHIZHIK_APP_ORIGIN = "https://app.chizhik.club";
const DELIVERY_API_PREFIX = "/delivery/api/";
const DELIVERY_CATALOG_PREFIX = "/delivery/api/catalog/";
const ACCEPTED_STORE_PATH =
  /^\/delivery\/api\/catalog\/v(?:2|3)\/stores\/[A-Za-z0-9_-]{1,32}(?:\/|$)/;
const ANY_NUMERIC_STORE_PATH =
  /^\/delivery\/api\/catalog\/v(\d{1,2})\/stores\/[A-Za-z0-9_-]{1,64}(?:\/|$)/;
const CATEGORIES_INOUT_PATH =
  /^\/delivery\/api\/catalog\/v\d{1,2}\/categories\/inout(?:\/|$)/;
const DELIVERY_ORDERS_PREFIX = "/delivery/api/orders/";
const PAGE_ORIGIN_DELIVERY_PATH = /^\/(?:api\/)?delivery(?:\/|$)/;

export type ChizhikResourceDiagnosticsSnapshot = Readonly<{
  appOriginSeen: boolean;
  deliveryApiSeen: boolean;
  deliveryCatalogSeen: boolean;
  deliveryOrdersSeen: boolean;
  storeScopedV2V3Seen: boolean;
  storeScopedOtherVersionSeen: boolean;
  storeScopedCategoriesInoutSeen: boolean;
  pageOriginDeliverySeen: boolean;
}>;

const EMPTY_SNAPSHOT: ChizhikResourceDiagnosticsSnapshot = {
  appOriginSeen: false,
  deliveryApiSeen: false,
  deliveryCatalogSeen: false,
  deliveryOrdersSeen: false,
  storeScopedV2V3Seen: false,
  storeScopedOtherVersionSeen: false,
  storeScopedCategoriesInoutSeen: false,
  pageOriginDeliverySeen: false,
};

export const EMPTY_CHIZHIK_RESOURCE_DIAGNOSTICS = Object.freeze({ ...EMPTY_SNAPSHOT });

export function createChizhikResourceDiagnosticsTracker() {
  const state = { ...EMPTY_SNAPSHOT };

  return {
    observe(rawUrl: string, pageUrl: URL): void {
      if (pageUrl.origin !== CHIZHIK_PAGE_ORIGIN) return;

      try {
        const resourceUrl = new URL(rawUrl, pageUrl);

        if (
          resourceUrl.origin === pageUrl.origin &&
          PAGE_ORIGIN_DELIVERY_PATH.test(resourceUrl.pathname)
        ) {
          state.pageOriginDeliverySeen = true;
        }

        if (resourceUrl.origin !== CHIZHIK_APP_ORIGIN) return;
        state.appOriginSeen = true;

        if (!resourceUrl.pathname.startsWith(DELIVERY_API_PREFIX)) return;
        state.deliveryApiSeen = true;

        if (resourceUrl.pathname.startsWith(DELIVERY_ORDERS_PREFIX)) {
          state.deliveryOrdersSeen = true;
        }

        if (!resourceUrl.pathname.startsWith(DELIVERY_CATALOG_PREFIX)) return;
        state.deliveryCatalogSeen = true;

        if (ACCEPTED_STORE_PATH.test(resourceUrl.pathname)) {
          state.storeScopedV2V3Seen = true;
          return;
        }

        const version = resourceUrl.pathname.match(ANY_NUMERIC_STORE_PATH)?.[1];
        if (version && version !== "2" && version !== "3") {
          state.storeScopedOtherVersionSeen = true;
        }

        if (
          CATEGORIES_INOUT_PATH.test(resourceUrl.pathname) &&
          !!resourceUrl.searchParams.get("store_id")
        ) {
          state.storeScopedCategoriesInoutSeen = true;
        }
      } catch {
        // Malformed resource names are intentionally ignored.
      }
    },

    snapshot(): ChizhikResourceDiagnosticsSnapshot {
      return { ...state };
    },
  };
}
