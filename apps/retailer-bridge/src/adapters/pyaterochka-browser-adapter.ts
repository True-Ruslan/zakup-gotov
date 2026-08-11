import type { BrowserAvailability } from "../model/browser-observation";
import type {
  AdapterResult,
  RetailerBrowserAdapter,
} from "./retailer-browser-adapter";

const RETAILER_ID = "pyaterochka";
const SOURCE_PROVIDER_ID = "pyaterochka-browser";
const ADAPTER_VERSION = "1";
const CATALOG_SERVICE_ORIGIN = "https://5d.5ka.ru";
const STORE_RESOURCE_PATH = /^\/api\/catalog\/v2\/stores\/([A-Za-z0-9_-]+)(?:\/|$)/;
const PRODUCT_PATH = /^\/product\/[^/?#]*--(\d+)\/?$/;

const OFFICIAL_PAGE_HOSTS = new Set(["5ka.ru", "www.5ka.ru"]);

type ProductCandidate = Readonly<{
  sku: string;
  productName: string;
  priceMinor: number;
  availability: BrowserAvailability;
}>;

function nonBlankString(value: string | null | undefined): string | null {
  if (typeof value !== "string") return null;
  const trimmed = value.trim();
  return trimmed.length > 0 ? trimmed : null;
}

function isOfficialPageUrl(url: URL): boolean {
  return url.protocol === "https:" && OFFICIAL_PAGE_HOSTS.has(url.hostname);
}

function productSkuFromHref(href: string, pageUrl: URL): string | null {
  try {
    const productUrl = new URL(href, pageUrl);
    if (!isOfficialPageUrl(productUrl)) return null;
    return productUrl.pathname.match(PRODUCT_PATH)?.[1] ?? null;
  } catch {
    return null;
  }
}

function rubPriceMinor(raw: string): number | null {
  const normalized = raw.replace(/[\u00a0\u202f]/g, " ").trim();
  const match = normalized.match(/^(\d[\d ]*)(?:[,.](\d{1,2}))?\s*₽$/);
  if (!match?.[1]) return null;

  const rubles = Number(match[1].replace(/\s/g, ""));
  const kopecks = match[2] ? Number(match[2].padEnd(2, "0")) : 0;
  if (!Number.isSafeInteger(rubles) || !Number.isSafeInteger(kopecks)) return null;

  const priceMinor = rubles * 100 + kopecks;
  return Number.isSafeInteger(priceMinor) && priceMinor >= 0 ? priceMinor : null;
}

function currentVisiblePriceMinor(container: Element): number | null {
  const text = container.textContent?.replace(/[\u00a0\u202f]/g, " ") ?? "";
  const matches = text.match(/\d[\d ]*(?:[,.]\d{1,2})?\s*₽/g) ?? [];
  const prices = matches
    .map(rubPriceMinor)
    .filter((price): price is number => price !== null);
  return prices.length > 0 ? prices[prices.length - 1] : null;
}

function productSkusIn(container: Element, pageUrl: URL): Set<string> {
  const skus = new Set<string>();
  container.querySelectorAll<HTMLAnchorElement>('a[href*="/product/"]').forEach((link) => {
    const href = link.getAttribute("href");
    if (!href) return;
    const sku = productSkuFromHref(href, pageUrl);
    if (sku) skus.add(sku);
  });
  return skus;
}

function productContainer(link: HTMLAnchorElement, sku: string, pageUrl: URL): Element | null {
  let current: Element | null = link;
  for (let depth = 0; current && depth < 7; depth += 1, current = current.parentElement) {
    if (!current.textContent?.includes("₽")) continue;
    const skus = productSkusIn(current, pageUrl);
    if (skus.size === 1 && skus.has(sku)) return current;
  }
  return null;
}

function collectDomProducts(document: Document, pageUrl: URL): ProductCandidate[] {
  const productsBySku = new Map<string, ProductCandidate>();

  document.querySelectorAll<HTMLAnchorElement>('a[href*="/product/"]').forEach((link) => {
    const href = link.getAttribute("href");
    if (!href) return;

    const sku = productSkuFromHref(href, pageUrl);
    if (!sku || productsBySku.has(sku)) return;

    const container = productContainer(link, sku, pageUrl);
    if (!container) return;

    const productName =
      nonBlankString(link.getAttribute("aria-label")) ??
      nonBlankString(link.textContent) ??
      [...container.querySelectorAll<HTMLAnchorElement>('a[href*="/product/"]')]
        .map((candidate) => nonBlankString(candidate.getAttribute("aria-label")) ?? nonBlankString(candidate.textContent))
        .find((candidate): candidate is string => candidate !== null) ??
      null;
    const priceMinor = currentVisiblePriceMinor(container);
    if (!productName || priceMinor === null) return;

    productsBySku.set(sku, {
      sku,
      productName,
      priceMinor,
      availability: "UNKNOWN",
    });
  });

  return [...productsBySku.values()];
}

function collectStoreContexts(resourceUrls: readonly string[]): Set<string> {
  const contexts = new Set<string>();

  resourceUrls.forEach((rawUrl) => {
    try {
      const resourceUrl = new URL(rawUrl);
      if (resourceUrl.origin !== CATALOG_SERVICE_ORIGIN) return;
      const context = resourceUrl.pathname.match(STORE_RESOURCE_PATH)?.[1];
      if (context) contexts.add(context);
    } catch {
      // Ignore malformed resource names and fail closed when no unique context remains.
    }
  });

  return contexts;
}

export const pyaterochkaBrowserAdapter: RetailerBrowserAdapter = {
  adapterId: "pyaterochka-browser-v1",
  retailerId: RETAILER_ID,

  supports(url: URL): boolean {
    return isOfficialPageUrl(url);
  },

  collect({ document, url, observedAt, resourceUrls = [] }): AdapterResult {
    const contexts = collectStoreContexts(resourceUrls);
    if (contexts.size !== 1) {
      return { status: "missing-context", observations: [] };
    }

    const products = collectDomProducts(document, url);
    if (products.length === 0) {
      return { status: "missing-product", observations: [] };
    }

    const fulfillmentContextId = [...contexts][0];
    return {
      status: "ok",
      observations: products.map((product) => ({
        schemaVersion: 1,
        retailerId: RETAILER_ID,
        sourceProviderId: SOURCE_PROVIDER_ID,
        sourceMode: "BROWSER_BRIDGE",
        fulfillmentContextId,
        sku: product.sku,
        productName: product.productName,
        priceMinor: product.priceMinor,
        currencyCode: "RUB",
        availability: product.availability,
        observedAt,
        sourceReference: url.toString(),
        adapterVersion: ADAPTER_VERSION,
      })),
    };
  },
};
