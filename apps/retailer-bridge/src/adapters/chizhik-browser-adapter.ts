import {
  createChizhikActiveApiClient,
  type ChizhikActiveApiClient,
  type ChizhikStoreDiscoveryResult,
} from "../chizhik-active-api-client";
import { fulfillmentContextResource } from "../resource-observation-policy";
import type {
  AdapterResult,
  RetailerBrowserAdapter,
} from "./retailer-browser-adapter";

const RETAILER_ID = "chizhik";
const OFFICIAL_PAGE_HOST = "chizhik.club";
const CONTEXT_PREFIX = `${RETAILER_ID}:`;

function isOfficialPageUrl(url: URL): boolean {
  return url.protocol === "https:" && url.hostname === OFFICIAL_PAGE_HOST;
}

function evidencedStoreIds(
  resourceUrls: readonly string[],
  pageUrl: URL,
  validStoreIds: ReadonlySet<string>,
): Set<string> {
  const contexts = new Set<string>();

  for (const rawUrl of resourceUrls) {
    const resource = fulfillmentContextResource(rawUrl, pageUrl);
    if (!resource?.contextKey.startsWith(CONTEXT_PREFIX)) continue;

    const sapId = resource.contextKey.slice(CONTEXT_PREFIX.length);
    if (validStoreIds.has(sapId)) contexts.add(sapId);
  }

  return contexts;
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

    async collect({ url, resourceUrls = [] }): Promise<AdapterResult> {
      storeDiscovery ??= client.listStores();
      const result = await storeDiscovery;
      if (result.status !== "ok" || result.stores.length === 0) {
        return { status: "missing-context", observations: [] };
      }

      const validStoreIds = new Set(result.stores.map((store) => store.sapId));
      const contexts = evidencedStoreIds(resourceUrls, url, validStoreIds);
      if (contexts.size !== 1) {
        return { status: "missing-context", observations: [] };
      }

      // Phase D2 now has one browser-evidenced store context validated against the
      // active first-party directory. Product search remains intentionally disabled
      // until live response schema and price-unit evidence are accepted in #169.
      return { status: "observation-only", observations: [] };
    },
  };
}

export const chizhikBrowserAdapter = createChizhikBrowserAdapter();
