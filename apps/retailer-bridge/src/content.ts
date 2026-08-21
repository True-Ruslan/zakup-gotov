import { retailerBrowserAdapters } from "./adapters/retailer-browser-adapters";
import { createChizhikActiveApiClient } from "./chizhik-active-api-client";
import { createChizhikResourceDiagnosticsTracker } from "./chizhik-resource-diagnostics";
import { runChizhikSchemaCanary } from "./chizhik-schema-canary";
import {
  CHIZHIK_SCHEMA_CANARY_RESULT,
  createChizhikSchemaCanaryMessageHandler,
  isChizhikSchemaCanaryRequest,
} from "./chizhik-schema-canary-message";
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
const chizhikSchemaCanaryClient = createChizhikActiveApiClient();

function currentChizhikResourceDiagnostics() {
  const tracker = createChizhikResourceDiagnosticsTracker();
  const pageUrl = new URL(location.href);
  performance
    .getEntriesByType("resource")
    .forEach((entry) => tracker.observe(entry.name, pageUrl));
  return tracker.snapshot();
}

const handleChizhikSchemaCanaryMessage = createChizhikSchemaCanaryMessageHandler(() =>
  runChizhikSchemaCanary({
    client: chizhikSchemaCanaryClient,
    pageUrl: new URL(location.href),
    resourceUrls: [...observedResourceUrls],
    resourceDiagnostics: currentChizhikResourceDiagnostics(),
  }),
);

chrome.runtime.onMessage.addListener((message, _sender, sendResponse) => {
  if (!isChizhikSchemaCanaryRequest(message)) return false;

  void handleChizhikSchemaCanaryMessage(message)
    .then((response) => {
      if (response) sendResponse(response);
    })
    .catch(() => {
      sendResponse({
        type: CHIZHIK_SCHEMA_CANARY_RESULT,
        evidence: "CHIZHIK_D2 status=PROBE_ERROR",
      });
    });
  return true;
});

function nextObservationRevision(previous: number): number {
  const clockRevision = Math.floor((performance.timeOrigin + performance.now()) * 1_000);
  if (!Number.isSafeInteger(clockRevision) || clockRevision <= 0) {
    return previous + 1;
  }
  return Math.max(previous + 1, clockRevision);
}

let observationRevision = nextObservationRevision(0);
let activeCollectionRevision: number | null = null;
let currentFulfillmentContextKey: string | null = null;
let contextSignalFloorStartTime = Number.NEGATIVE_INFINITY;
let awaitingFreshContext = false;

const sink = async (observations: BrowserObservation[]): Promise<void> => {
  const revision = activeCollectionRevision;
  if (revision === null || revision !== observationRevision) {
    return;
  }

  await storeObservations(observations, revision);
  if (revision !== observationRevision) {
    return;
  }

  const contexts = new Set(
    observations.map(
      (observation) => `${observation.retailerId}:${observation.fulfillmentContextId}`,
    ),
  );
  currentFulfillmentContextKey = contexts.size === 1 ? [...contexts][0] : null;
  contextSignalFloorStartTime = performance.now();
  awaitingFreshContext = false;
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

  if (collectionPending && !collectionSucceeded && !awaitingFreshContext) {
    collectionPending = false;
    void collectCurrentPage();
  }
}

function startLifecycleRefresh(options: {
  awaitFreshContext: boolean;
  collectAfterReset: boolean;
  signalFloorStartTime?: number;
}): void {
  observationRevision = nextObservationRevision(observationRevision);
  collectionSucceeded = false;
  collectionPending = options.collectAfterReset;
  awaitingFreshContext = options.awaitFreshContext;
  if (options.signalFloorStartTime !== undefined) {
    contextSignalFloorStartTime = options.signalFloorStartTime;
  }
  observeDomChanges();
  publishDiagnostics("refreshing", 0);

  const revision = observationRevision;
  const reset = clearObservations(revision).catch(() => {
    if (revision === observationRevision) {
      publishDiagnostics("internal-error", 0);
    }
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

  if (context && startTime < contextSignalFloorStartTime) {
    return { changed: false, contextChanged: false };
  }

  if (context && awaitingFreshContext) {
    observedResourceUrls.clear();
    observedResourceUrls.add(context.canonicalUrl);
    currentFulfillmentContextKey = context.contextKey;
    contextSignalFloorStartTime = startTime;
    awaitingFreshContext = false;
    collectionPending = true;
    return { changed: true, contextChanged: false };
  }

  if (
    context &&
    currentFulfillmentContextKey &&
    context.contextKey !== currentFulfillmentContextKey
  ) {
    observedResourceUrls.clear();
    observedResourceUrls.add(context.canonicalUrl);
    currentFulfillmentContextKey = context.contextKey;
    contextSignalFloorStartTime = startTime;
    startLifecycleRefresh({
      awaitFreshContext: false,
      collectAfterReset: true,
    });
    return { changed: true, contextChanged: true };
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
  if (awaitingFreshContext) {
    collectionPending = true;
    return;
  }
  if (refreshInFlight) {
    collectionPending = true;
    return;
  }
  if (collectionInFlight) {
    collectionPending = true;
    return;
  }

  const revision = observationRevision;
  collectionInFlight = true;
  activeCollectionRevision = revision;
  try {
    const result = await collector.collect(
      document,
      new URL(location.href),
      new Date().toISOString(),
      [...observedResourceUrls],
    );

    if (revision !== observationRevision) {
      return;
    }

    if (result.status === "ok") {
      collectionSucceeded = true;
      retainCurrentFulfillmentResource(new URL(location.href));
      contextSignalFloorStartTime = performance.now();
      domObserver?.disconnect();
    } else {
      await clearObservations(revision);
    }
    publishDiagnostics(result.status, result.observationCount);
  } catch {
    if (revision === observationRevision) {
      await clearObservations(revision);
      publishDiagnostics("internal-error", 0);
    }
  } finally {
    if (activeCollectionRevision === revision) {
      activeCollectionRevision = null;
    }
    collectionInFlight = false;
    if (
      collectionPending &&
      !collectionSucceeded &&
      !refreshInFlight &&
      !awaitingFreshContext
    ) {
      collectionPending = false;
      void collectCurrentPage();
    }
  }
}

function requestCollection(): void {
  if (collectionSucceeded) return;
  if (awaitingFreshContext || refreshInFlight || collectionInFlight) {
    collectionPending = true;
    return;
  }
  void collectCurrentPage();
}

function handleSameDocumentNavigation(event: Event): void {
  if (
    !collectionSucceeded &&
    !awaitingFreshContext &&
    !refreshInFlight &&
    !collectionInFlight
  ) {
    return;
  }

  const navigationEvent = event as Event & {
    destination?: { readonly sameDocument?: boolean; readonly url?: string };
  };
  if (!navigationEvent.destination?.sameDocument || !navigationEvent.destination.url) return;

  try {
    const currentUrl = new URL(location.href);
    const destinationUrl = new URL(navigationEvent.destination.url, currentUrl);
    if (canonicalPageReference(currentUrl) === canonicalPageReference(destinationUrl)) return;

    observedResourceUrls.clear();
    startLifecycleRefresh({
      awaitFreshContext: true,
      collectAfterReset: false,
      signalFloorStartTime: performance.now(),
    });
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
collectionPending = true;
publishDiagnostics("refreshing", 0);
const initialReset = clearObservations(observationRevision).catch(() => {
  publishDiagnostics("internal-error", 0);
});
refreshInFlight = initialReset;
void initialReset.finally(() => finishRefresh(initialReset));