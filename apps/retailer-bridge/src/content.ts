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
  applySameDocumentNavigationReset,
  INITIAL_RESOURCE_LIFECYCLE_STATE,
  rememberAllowedResource as rememberAllowedResourceState,
  retainCurrentFulfillmentResource,
  type ResourceLifecycleState,
} from "./resource-lifecycle-state";

const sendMessage = (message: unknown) => chrome.runtime.sendMessage(message);
const storeObservations = createChromeObservationSink(sendMessage);
const clearObservations = createChromeObservationClearer(sendMessage);
let lifecycleState: ResourceLifecycleState = INITIAL_RESOURCE_LIFECYCLE_STATE;
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
    resourceUrls: [...lifecycleState.observedResourceUrls],
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
  lifecycleState = {
    ...lifecycleState,
    currentFulfillmentContextKey: contexts.size === 1 ? [...contexts][0] : null,
    contextSignalFloorStartTime: performance.now(),
    awaitingFreshContext: false,
  };
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

  if (collectionPending && !collectionSucceeded && !lifecycleState.awaitingFreshContext) {
    collectionPending = false;
    void collectCurrentPage();
  }
}

function startLifecycleRefresh(options: {
  awaitFreshContext: boolean;
  collectAfterReset: boolean;
}): void {
  observationRevision = nextObservationRevision(observationRevision);
  collectionSucceeded = false;
  collectionPending = options.collectAfterReset;
  lifecycleState = { ...lifecycleState, awaitingFreshContext: options.awaitFreshContext };
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
  const result = rememberAllowedResourceState(lifecycleState, rawUrl, pageUrl, startTime);
  lifecycleState = result.state;

  if (result.contextChanged) {
    startLifecycleRefresh({ awaitFreshContext: false, collectAfterReset: true });
  }

  return { changed: result.changed, contextChanged: result.contextChanged };
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

async function collectCurrentPage(): Promise<void> {
  if (collectionSucceeded) return;
  if (lifecycleState.awaitingFreshContext) {
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
      [...lifecycleState.observedResourceUrls],
    );

    if (revision !== observationRevision) {
      return;
    }

    if (result.status === "ok") {
      collectionSucceeded = true;
      lifecycleState = retainCurrentFulfillmentResource(lifecycleState, new URL(location.href));
      lifecycleState = { ...lifecycleState, contextSignalFloorStartTime: performance.now() };
      domObserver?.disconnect();
    } else {
      if (result.status === "observation-only") {
        // This adapter has confirmed a fulfillment context but intentionally
        // produces no observations (offer mapping disabled). No further DOM
        // mutation can change that outcome, so stop re-triggering collection
        // from them -- otherwise a collectCurrentPage() call is frequently
        // left in flight, racing with a same-document navigation reset (#169).
        domObserver?.disconnect();
      }
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
      !lifecycleState.awaitingFreshContext
    ) {
      collectionPending = false;
      void collectCurrentPage();
    }
  }
}

function requestCollection(): void {
  if (collectionSucceeded) return;
  if (lifecycleState.awaitingFreshContext || refreshInFlight || collectionInFlight) {
    collectionPending = true;
    return;
  }
  void collectCurrentPage();
}

function handleSameDocumentNavigation(event: Event): void {
  if (
    !collectionSucceeded &&
    !lifecycleState.awaitingFreshContext &&
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

    lifecycleState = applySameDocumentNavigationReset(performance.now());
    startLifecycleRefresh({ awaitFreshContext: true, collectAfterReset: false });
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