import { retailerBrowserAdapters } from "./adapters/retailer-browser-adapters";
import { BrowserObservationCollector } from "./collector/browser-observation-collector";
import {
  createChromeObservationClearer,
  createChromeObservationSink,
} from "./collector/chrome-observation-sink";
import type { BrowserObservation } from "./model/browser-observation";
import {
  canonicalObservedResourceUrl,
  fulfillmentContextResource,
} from "./resource-observation-policy";

const sendMessage = (message: unknown) => chrome.runtime.sendMessage(message);
const storeObservations = createChromeObservationSink(sendMessage);
const clearObservations = createChromeObservationClearer(sendMessage);
const observedResourceUrls = new Set<string>();
const fulfillmentContextSignalStartTimes = new Map<string, number>();

let currentFulfillmentContextKey: string | null = null;
let currentFulfillmentContextSignalStartTime = Number.NEGATIVE_INFINITY;
const sink = async (observations: BrowserObservation[]): Promise<void> => {
  await storeObservations(observations);

  const contexts = new Set(
    observations.map(
      (observation) => `${observation.retailerId}:${observation.fulfillmentContextId}`,
    ),
  );
  currentFulfillmentContextKey = contexts.size === 1 ? [...contexts][0] : null;
  currentFulfillmentContextSignalStartTime = currentFulfillmentContextKey
    ? (fulfillmentContextSignalStartTimes.get(currentFulfillmentContextKey) ?? Number.NEGATIVE_INFINITY)
    : Number.NEGATIVE_INFINITY;

  if (currentFulfillmentContextKey) {
    const retainedSignalTime = currentFulfillmentContextSignalStartTime;
    fulfillmentContextSignalStartTimes.clear();
    fulfillmentContextSignalStartTimes.set(currentFulfillmentContextKey, retainedSignalTime);
  }
};
const collector = new BrowserObservationCollector(retailerBrowserAdapters, sink);

let collectionInFlight = false;
let collectionPending = false;
let collectionSucceeded = false;
let refreshInFlight: Promise<void> | null = null;
let resourceObserver: PerformanceObserver | null = null;
let domObserver: MutationObserver | null = null;

function publishDiagnostics(status: string, observationCount: number): void {
  document.documentElement.dataset.zgBridgeStatus = status;
  document.documentElement.dataset.zgBridgeCount = String(observationCount);
}

function canonicalPageReference(url: URL): string {
  return `${url.origin}${url.pathname}`;
}

function observeDomChanges(): void {
  domObserver?.observe(document.documentElement, { childList: true, subtree: true });
}

function finishRefresh(reset: Promise<void>): void {
  if (refreshInFlight !== reset) return;
  refreshInFlight = null;

  if (collectionPending && !collectionSucceeded) {
    collectionPending = false;
    void collectCurrentPage();
  }
}

function startLifecycleRefresh(): void {
  collectionSucceeded = false;
  collectionPending = false;
  observeDomChanges();
  publishDiagnostics("refreshing", 0);

  if (refreshInFlight) return;

  const reset = clearObservations().catch(() => {
    publishDiagnostics("internal-error", 0);
  });
  refreshInFlight = reset;
  void reset.finally(() => finishRefresh(reset));
}

type ResourceMemoryResult = Readonly<{
  changed: boolean;
  contextChanged: boolean;
}>;

function rememberAllowedResource(rawUrl: string, startTime: number): ResourceMemoryResult {
  const pageUrl = new URL(location.href);
  const canonical = canonicalObservedResourceUrl(rawUrl, pageUrl);
  if (!canonical) return { changed: false, contextChanged: false };

  const context = fulfillmentContextResource(rawUrl, pageUrl);
  if (currentFulfillmentContextKey && !context) {
    return { changed: false, contextChanged: false };
  }

  if (context) {
    const previousSignalTime = fulfillmentContextSignalStartTimes.get(context.contextKey);
    if (previousSignalTime === undefined || startTime > previousSignalTime) {
      fulfillmentContextSignalStartTimes.set(context.contextKey, startTime);
    }

    if (
      currentFulfillmentContextKey &&
      context.contextKey !== currentFulfillmentContextKey
    ) {
      if (startTime <= currentFulfillmentContextSignalStartTime) {
        return { changed: false, contextChanged: false };
      }

      observedResourceUrls.clear();
      observedResourceUrls.add(context.canonicalUrl);
      currentFulfillmentContextKey = context.contextKey;
      currentFulfillmentContextSignalStartTime = startTime;
      fulfillmentContextSignalStartTimes.clear();
      fulfillmentContextSignalStartTimes.set(context.contextKey, startTime);
      startLifecycleRefresh();
      return { changed: true, contextChanged: true };
    }

    if (context.contextKey === currentFulfillmentContextKey) {
      currentFulfillmentContextSignalStartTime = Math.max(
        currentFulfillmentContextSignalStartTime,
        startTime,
      );
    }
  }

  const previousSize = observedResourceUrls.size;
  observedResourceUrls.add(canonical);
  return {
    changed: observedResourceUrls.size !== previousSize,
    contextChanged: false,
  };
}

function rememberResourceEntries(entries: readonly PerformanceEntry[]): ResourceMemoryResult {
  let changed = false;
  let contextChanged = false;

  entries.forEach((entry) => {
    const result = rememberAllowedResource(entry.name, entry.startTime);
    changed = result.changed || changed;
    contextChanged = result.contextChanged || contextChanged;
  });

  return { changed, contextChanged };
}

function retainCurrentFulfillmentResource(pageUrl: URL): void {
  const currentContext = currentFulfillmentContextKey;
  const retained = currentContext
    ? [...observedResourceUrls].filter(
        (url) => fulfillmentContextResource(url, pageUrl)?.contextKey === currentContext,
      )
    : [];

  observedResourceUrls.clear();
  retained.forEach((url) => observedResourceUrls.add(url));
}

async function collectCurrentPage(): Promise<void> {
  if (collectionSucceeded) return;
  if (refreshInFlight) {
    collectionPending = true;
    return;
  }
  if (collectionInFlight) {
    collectionPending = true;
    return;
  }

  collectionInFlight = true;
  try {
    const result = await collector.collect(
      document,
      new URL(location.href),
      new Date().toISOString(),
      [...observedResourceUrls],
    );

    if (result.status === "ok") {
      collectionSucceeded = true;
      retainCurrentFulfillmentResource(new URL(location.href));
      domObserver?.disconnect();
    } else {
      await clearObservations();
    }
    publishDiagnostics(result.status, result.observationCount);
  } catch {
    await clearObservations();
    publishDiagnostics("internal-error", 0);
  } finally {
    collectionInFlight = false;
    if (collectionPending && !collectionSucceeded && !refreshInFlight) {
      collectionPending = false;
      void collectCurrentPage();
    }
  }
}

function requestCollection(): void {
  if (collectionSucceeded) return;
  if (refreshInFlight || collectionInFlight) {
    collectionPending = true;
    return;
  }
  void collectCurrentPage();
}

function handleSameDocumentNavigation(event: Event): void {
  if (!collectionSucceeded) return;

  const navigationEvent = event as Event & {
    destination?: { readonly sameDocument?: boolean; readonly url?: string };
  };
  if (!navigationEvent.destination?.sameDocument || !navigationEvent.destination.url) return;

  try {
    const currentUrl = new URL(location.href);
    const destinationUrl = new URL(navigationEvent.destination.url, currentUrl);
    if (canonicalPageReference(currentUrl) === canonicalPageReference(destinationUrl)) return;

    retainCurrentFulfillmentResource(destinationUrl);
    startLifecycleRefresh();
  } catch {
    // Ignore malformed navigation metadata; the existing page snapshot remains bounded.
  }
}

resourceObserver = new PerformanceObserver((list) => {
  const result = rememberResourceEntries(list.getEntries());
  if (result.contextChanged) return;
  if (result.changed) requestCollection();
});
resourceObserver.observe({ type: "resource", buffered: true });

domObserver = new MutationObserver((mutations) => {
  if (mutations.some((mutation) => mutation.addedNodes.length > 0 || mutation.removedNodes.length > 0)) {
    requestCollection();
  }
});
observeDomChanges();

const navigationTarget = (window as Window & { readonly navigation?: EventTarget }).navigation;
navigationTarget?.addEventListener("navigate", handleSameDocumentNavigation);

rememberResourceEntries(performance.getEntriesByType("resource"));
requestCollection();
