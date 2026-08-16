import type {
  AdapterResult,
  RetailerBrowserAdapter,
} from "./retailer-browser-adapter";

const RETAILER_ID = "chizhik";
const OFFICIAL_PAGE_HOST = "chizhik.club";
const PUBLIC_CATALOG_ORIGIN = "https://app.chizhik.club";
const PUBLIC_CATALOG_PATH = /^\/api\/v1\/catalog\/unauthorized\/(?:categories|products)\/?$/;

function isOfficialPageUrl(url: URL): boolean {
  return url.protocol === "https:" && url.hostname === OFFICIAL_PAGE_HOST;
}

function hasObservedPublicCatalogResource(resourceUrls: readonly string[]): boolean {
  return resourceUrls.some((rawUrl) => {
    try {
      const resourceUrl = new URL(rawUrl);
      return (
        resourceUrl.origin === PUBLIC_CATALOG_ORIGIN &&
        PUBLIC_CATALOG_PATH.test(resourceUrl.pathname)
      );
    } catch {
      return false;
    }
  });
}

export const chizhikBrowserAdapter: RetailerBrowserAdapter = {
  adapterId: "chizhik-browser-discovery-v1",
  retailerId: RETAILER_ID,

  supports(url: URL): boolean {
    return isOfficialPageUrl(url);
  },

  collect({ resourceUrls = [] }): AdapterResult {
    if (!hasObservedPublicCatalogResource(resourceUrls)) {
      return { status: "missing-context", observations: [] };
    }

    // Phase B is intentionally observation-only: a sanitized catalog pathname proves
    // browser-side reachability but carries neither a trusted fulfillment context nor
    // enough product evidence to create an offer observation.
    return { status: "observation-only", observations: [] };
  },
};
