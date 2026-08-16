import type {
  AdapterFailureStatus,
  RetailerBrowserAdapter,
} from "../adapters/retailer-browser-adapter";
import type {
  BrowserAvailability,
  BrowserObservation,
} from "../model/browser-observation";

export type ObservationSink = (observations: BrowserObservation[]) => Promise<void>;

export type CollectorStatus = "ok" | AdapterFailureStatus | "invalid-observation";

export type CollectorResult = Readonly<{
  status: CollectorStatus;
  observationCount: number;
}>;

const AVAILABILITY = new Set<BrowserAvailability>([
  "AVAILABLE",
  "UNAVAILABLE",
  "UNKNOWN",
]);

function isNonBlank(value: unknown): value is string {
  return typeof value === "string" && value.trim().length > 0;
}

function isValidInstant(value: unknown): value is string {
  return typeof value === "string" && !Number.isNaN(Date.parse(value));
}

function canonicalSourceReference(url: URL): string {
  return `${url.origin}${url.pathname}`;
}

function projectObservation(
  raw: Record<string, unknown>,
  adapter: RetailerBrowserAdapter,
  pageUrl: URL,
  observedAt: string,
): BrowserObservation | null {
  if (
    raw.schemaVersion !== 1 ||
    raw.retailerId !== adapter.retailerId ||
    !isNonBlank(raw.retailerId) ||
    !isNonBlank(raw.sourceProviderId) ||
    raw.sourceMode !== "BROWSER_BRIDGE" ||
    !isNonBlank(raw.fulfillmentContextId) ||
    !isNonBlank(raw.sku) ||
    !isNonBlank(raw.productName) ||
    !Number.isInteger(raw.priceMinor) ||
    (raw.priceMinor as number) < 0 ||
    raw.currencyCode !== "RUB" ||
    !AVAILABILITY.has(raw.availability as BrowserAvailability) ||
    !isValidInstant(raw.observedAt) ||
    raw.observedAt !== observedAt ||
    !isValidInstant(observedAt) ||
    !isNonBlank(raw.adapterVersion)
  ) {
    return null;
  }

  return {
    schemaVersion: 1,
    retailerId: raw.retailerId,
    sourceProviderId: raw.sourceProviderId,
    sourceMode: "BROWSER_BRIDGE",
    fulfillmentContextId: raw.fulfillmentContextId,
    sku: raw.sku,
    productName: raw.productName,
    priceMinor: raw.priceMinor as number,
    currencyCode: "RUB",
    availability: raw.availability as BrowserAvailability,
    observedAt,
    sourceReference: canonicalSourceReference(pageUrl),
    adapterVersion: raw.adapterVersion,
  };
}

export class BrowserObservationCollector {
  constructor(
    private readonly adapters: readonly RetailerBrowserAdapter[],
    private readonly sink: ObservationSink,
  ) {}

  async collect(
    document: Document,
    url: URL,
    observedAt: string,
    resourceUrls: readonly string[] = [],
  ): Promise<CollectorResult> {
    const adapter = this.adapters.find((candidate) => candidate.supports(url));
    if (!adapter) {
      return { status: "unsupported-page", observationCount: 0 };
    }

    const result = adapter.collect({ document, url, observedAt, resourceUrls });
    if (result.status !== "ok") {
      return { status: result.status, observationCount: 0 };
    }
    if (result.observations.length === 0) {
      return { status: "invalid-observation", observationCount: 0 };
    }

    const observations: BrowserObservation[] = [];
    for (const raw of result.observations) {
      const projected = projectObservation(raw, adapter, url, observedAt);
      if (!projected) {
        return { status: "invalid-observation", observationCount: 0 };
      }
      observations.push(projected);
    }

    await this.sink(observations);
    return { status: "ok", observationCount: observations.length };
  }
}
