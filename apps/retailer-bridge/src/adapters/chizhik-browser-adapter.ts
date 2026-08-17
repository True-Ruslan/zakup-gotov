import {
  createChizhikActiveApiClient,
  type ChizhikActiveApiClient,
  type ChizhikStoreDiscoveryResult,
} from "../chizhik-active-api-client";
import type {
  AdapterResult,
  RetailerBrowserAdapter,
} from "./retailer-browser-adapter";

const RETAILER_ID = "chizhik";
const OFFICIAL_PAGE_HOST = "chizhik.club";

function isOfficialPageUrl(url: URL): boolean {
  return url.protocol === "https:" && url.hostname === OFFICIAL_PAGE_HOST;
}

export function createChizhikBrowserAdapter(
  client: ChizhikActiveApiClient = createChizhikActiveApiClient(),
): RetailerBrowserAdapter {
  let storeDiscovery: Promise<ChizhikStoreDiscoveryResult> | null = null;

  return {
    adapterId: "chizhik-browser-active-v2",
    retailerId: RETAILER_ID,

    supports(url: URL): boolean {
      return isOfficialPageUrl(url);
    },

    async collect(): Promise<AdapterResult> {
      storeDiscovery ??= client.listStores();
      const result = await storeDiscovery;
      if (result.status !== "ok" || result.stores.length === 0) {
        return { status: "missing-context", observations: [] };
      }

      // Phase D1 proves active browser-context access and a valid store directory only.
      // Product offers remain fail-closed until a store-scoped delivery response is
      // independently evidenced and mapped in Phase D2.
      return { status: "observation-only", observations: [] };
    },
  };
}

export const chizhikBrowserAdapter = createChizhikBrowserAdapter();
