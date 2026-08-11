const PYATEROCHKA_PAGE_HOSTS = new Set(["5ka.ru", "www.5ka.ru"]);
const PYATEROCHKA_SERVICE_ORIGIN = "https://5d.5ka.ru";
const PYATEROCHKA_STORE_RESOURCE_PATH = /^\/api\/catalog\/v2\/stores\/[A-Za-z0-9_-]+(?:\/|$)/;

function isOfficialPyaterochkaPage(pageUrl: URL): boolean {
  return pageUrl.protocol === "https:" && PYATEROCHKA_PAGE_HOSTS.has(pageUrl.hostname);
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

    return null;
  } catch {
    return null;
  }
}
