import type { BrowserAvailability } from "../model/browser-observation";
import type {
  AdapterResult,
  RetailerBrowserAdapter,
} from "./retailer-browser-adapter";

const RETAILER_ID = "perekrestok";
const SOURCE_PROVIDER_ID = "perekrestok-browser";
const ADAPTER_VERSION = "1";
const SHOP_RESOURCE_PATH = /^\/api\/customer\/[^/]+\/shop\/(\d+)\/?$/;
const PRODUCT_SKU_SUFFIX = /-(\d+)\/?$/;

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === "object" && value !== null && !Array.isArray(value);
}

function nonBlankString(value: unknown): string | null {
  return typeof value === "string" && value.trim().length > 0 ? value.trim() : null;
}

function scalarId(value: unknown): string | null {
  if (typeof value === "number" && Number.isFinite(value)) {
    return String(value);
  }
  return nonBlankString(value);
}

function mapAvailability(value: unknown): BrowserAvailability {
  const normalized = typeof value === "string" ? value.trim().toLowerCase() : "";
  if (["many", "few", "low", "available", "in_stock", "instock"].includes(normalized)) {
    return "AVAILABLE";
  }
  if (["none", "unavailable", "out_of_stock", "outofstock"].includes(normalized)) {
    return "UNAVAILABLE";
  }
  return "UNKNOWN";
}

type ProductCandidate = Readonly<{
  sku: string;
  productName: string;
  priceMinor: number;
  availability: BrowserAvailability;
}>;

type Evidence = {
  readonly contexts: Set<string>;
  readonly products: ProductCandidate[];
};

function productCandidate(value: Record<string, unknown>): ProductCandidate | null {
  const masterData = value.masterData;
  const priceTag = value.priceTag;
  if (!isRecord(masterData) || !isRecord(priceTag)) {
    return null;
  }

  const sku = nonBlankString(masterData.plu);
  const productName = nonBlankString(value.title);
  const price = priceTag.price;
  if (
    !sku ||
    !productName ||
    typeof price !== "number" ||
    !Number.isInteger(price) ||
    price < 0
  ) {
    return null;
  }

  return {
    sku,
    productName,
    priceMinor: price,
    availability: mapAvailability(value.balanceState),
  };
}

function collectEvidence(value: unknown, evidence: Evidence): void {
  if (Array.isArray(value)) {
    value.forEach((item) => collectEvidence(item, evidence));
    return;
  }
  if (!isRecord(value)) {
    return;
  }

  const selectedShopId = scalarId(value.selectedShopId);
  if (selectedShopId) evidence.contexts.add(selectedShopId);

  const shopId = scalarId(value.shopId);
  if (shopId) evidence.contexts.add(shopId);

  if (isRecord(value.shop)) {
    const nestedShopId = scalarId(value.shop.id);
    if (nestedShopId) evidence.contexts.add(nestedShopId);
  }

  const product = productCandidate(value);
  if (product) {
    evidence.products.push(product);
  }

  Object.values(value).forEach((child) => collectEvidence(child, evidence));
}

function parseStructuredState(document: Document): {
  evidence: Evidence;
  parsedCount: number;
  malformedCount: number;
} {
  const evidence: Evidence = { contexts: new Set<string>(), products: [] };
  let parsedCount = 0;
  let malformedCount = 0;

  const scripts = document.querySelectorAll(
    'script[type="application/json"], script#__NEXT_DATA__, script[type="application/ld+json"]',
  );
  scripts.forEach((script) => {
    const text = script.textContent?.trim();
    if (!text) return;
    try {
      const state: unknown = JSON.parse(text);
      parsedCount += 1;
      collectEvidence(state, evidence);
    } catch {
      malformedCount += 1;
    }
  });

  return { evidence, parsedCount, malformedCount };
}

function collectResourceContexts(
  resourceUrls: readonly string[],
  pageUrl: URL,
  evidence: Evidence,
): void {
  resourceUrls.forEach((rawUrl) => {
    try {
      const resourceUrl = new URL(rawUrl, pageUrl);
      if (resourceUrl.origin !== pageUrl.origin) return;

      const match = resourceUrl.pathname.match(SHOP_RESOURCE_PATH);
      if (match?.[1]) {
        evidence.contexts.add(match[1]);
      }
    } catch {
      // Ignore malformed runtime resource names and fail closed below if no context remains.
    }
  });
}

function parsePriceMinor(text: string | null): number | null {
  if (!text) return null;

  const normalized = text.replace(/[\u00a0\u202f]/g, " ").trim();
  const match = normalized.match(/(\d[\d ]*)(?:[,.](\d{1,2}))?/);
  if (!match?.[1]) return null;

  const rubles = Number(match[1].replace(/\s/g, ""));
  const kopecks = match[2] ? Number(match[2].padEnd(2, "0")) : 0;
  if (!Number.isSafeInteger(rubles) || !Number.isSafeInteger(kopecks)) return null;

  const priceMinor = rubles * 100 + kopecks;
  return Number.isSafeInteger(priceMinor) && priceMinor >= 0 ? priceMinor : null;
}

function collectDomProducts(document: Document, pageUrl: URL, evidence: Evidence): void {
  const productsBySku = new Map<string, ProductCandidate>();

  document.querySelectorAll<HTMLElement>(".product-card").forEach((card) => {
    const titleLink = card.querySelector<HTMLAnchorElement>(
      ".product-card__title-link[href], .product-card__link[href]",
    );
    const href = titleLink?.getAttribute("href");
    if (!href) return;

    let sku: string | null = null;
    try {
      const productUrl = new URL(href, pageUrl);
      if (productUrl.origin !== pageUrl.origin) return;
      sku = productUrl.pathname.match(PRODUCT_SKU_SUFFIX)?.[1] ?? null;
    } catch {
      return;
    }
    if (!sku) return;

    const productName =
      nonBlankString(card.querySelector(".product-card__title-link")?.textContent) ??
      nonBlankString(card.querySelector(".product-card__title")?.textContent);
    const priceMinor = parsePriceMinor(
      card.querySelector(".product-card__price .price-new, .price-new")?.textContent ?? null,
    );
    if (!productName || priceMinor === null) return;

    productsBySku.set(sku, {
      sku,
      productName,
      priceMinor,
      availability: "UNKNOWN",
    });
  });

  evidence.products.push(...productsBySku.values());
}

export const perekrestokBrowserAdapter: RetailerBrowserAdapter = {
  adapterId: "perekrestok-browser-v1",
  retailerId: RETAILER_ID,

  supports(url: URL): boolean {
    return url.protocol === "https:" && url.hostname === "www.perekrestok.ru";
  },

  collect({ document, url, observedAt, resourceUrls = [] }): AdapterResult {
    const { evidence, parsedCount, malformedCount } = parseStructuredState(document);
    collectResourceContexts(resourceUrls, url, evidence);
    if (evidence.products.length === 0) {
      collectDomProducts(document, url, evidence);
    }

    if (parsedCount === 0 && malformedCount > 0) {
      return { status: "malformed-state", observations: [] };
    }
    if (evidence.contexts.size !== 1) {
      return { status: "missing-context", observations: [] };
    }
    if (evidence.products.length === 0) {
      return { status: "missing-product", observations: [] };
    }

    const fulfillmentContextId = [...evidence.contexts][0];
    return {
      status: "ok",
      observations: evidence.products.map((product) => ({
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
