const PEREKRESTOK_PAGE_HOST = "www.perekrestok.ru";
const PEREKRESTOK_SHOP_RESOURCE_PATH = /^\/api\/customer\/[^/]+\/shop\/(\d+)\/?$/;
const PYATEROCHKA_PAGE_HOSTS = new Set(["5ka.ru", "www.5ka.ru"]);
const PYATEROCHKA_SERVICE_ORIGIN = "https://5d.5ka.ru";
const PYATEROCHKA_STORE_RESOURCE_PATH = /^\/api\/catalog\/v2\/stores\/([A-Za-z0-9_-]+)(?:\/|$)/;
const CHIZHIK_PAGE_HOST = "chizhik.club";
const CHIZHIK_CATALOG_ORIGIN = "https://app.chizhik.club";
const CHIZHIK_PUBLIC_CATALOG_RESOURCE_PATH = /^\/api\/v1\/catalog\/unauthorized\/(?:categories|products)\/?$/;

export type FulfillmentContextResource = Readonly<{
  contextKey: string;
  canonicalUrl: string;
}>;

function isOfficialPerekrestokPage(pageUrl: URL): boolean {
  return pageUrl.protocol === "https:" && pageUrl.hostname === PEREKRESTOK_PAGE_HOST;
}

function isOfficialPyaterochkaPage(pageUrl: URL): boolean {
  return pageUrl.protocol === "https:" && PYATEROCHKA_PAGE_HOSTS.has(pageUrl.hostname);
}

function isOfficialChizhikPage(pageUrl: URL): boolean {
  return pageUrl.protocol === "https:" && pageUrl.hostname === CHIZHIK_PAGE_HOST;
}

export function canonicalObservedResourceUrl(rawUrl: string, pageUrl: URL): string | null {
  try {
    const resourceUrl = new URL(rawUrl, pageUrl);

    if (resourceUrl.origin === pageUrl.origin) {
      return `${resourceUrl.origin}${resourceUrl.pathname}`;
    }

    if (
      isOfficialPyaterochkaPage(pageUrl) &&
      resourceUrl.origin === PYATEROCHKA_SERVICE_ORIGIN &&
      PYATEROCHKA_STORE_RESOURCE_PATH.test(resourceUrl.pathname)
    ) {
      return `${resourceUrl.origin}${resourceUrl.pathname}`;
    }

    if (
      isOfficialChizhikPage(pageUrl) &&
      resourceUrl.origin === CHIZHIK_CATALOG_ORIGIN &&
      CHIZHIK_PUBLIC_CATALOG_RESOURCE_PATH.test(resourceUrl.pathname)
    ) {
      return `${resourceUrl.origin}${resourceUrl.pathname}`;
    }

    return null;
  } catch {
    return null;
  }
}

export function fulfillmentContextResource(
  rawUrl: string,
  pageUrl: URL,
): FulfillmentContextResource | null {
  const canonicalUrl = canonicalObservedResourceUrl(rawUrl, pageUrl);
  if (!canonicalUrl) return null;

  try {
    const resourceUrl = new URL(canonicalUrl);

    if (isOfficialPerekrestokPage(pageUrl) && resourceUrl.origin === pageUrl.origin) {
      const contextId = resourceUrl.pathname.match(PEREKRESTOK_SHOP_RESOURCE_PATH)?.[1];
      return contextId
        ? { contextKey: `perekrestok:${contextId}`, canonicalUrl }
        : null;
    }

    if (
      isOfficialPyaterochkaPage(pageUrl) &&
      resourceUrl.origin === PYATEROCHKA_SERVICE_ORIGIN
    ) {
      const contextId = resourceUrl.pathname.match(PYATEROCHKA_STORE_RESOURCE_PATH)?.[1];
      return contextId
        ? { contextKey: `pyaterochka:${contextId}`, canonicalUrl }
        : null;
    }

    // Chizhik Phase B deliberately does not infer fulfillment context from
    // catalog query parameters because they are stripped before retention.
    return null;
  } catch {
    return null;
  }
}
