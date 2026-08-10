import { perekrestokBrowserAdapter } from "./adapters/perekrestok-browser-adapter";
import { BrowserObservationCollector } from "./collector/browser-observation-collector";
import {
  createChromeObservationClearer,
  createChromeObservationSink,
} from "./collector/chrome-observation-sink";

const sendMessage = (message: unknown) => chrome.runtime.sendMessage(message);
const sink = createChromeObservationSink(sendMessage);
const clearObservations = createChromeObservationClearer(sendMessage);
const collector = new BrowserObservationCollector([perekrestokBrowserAdapter], sink);
const firstPartyResourceUrls = new Set<string>();

let collectionInFlight = false;
let collectionPending = false;
let collectionSucceeded = false;
let resourceObserver: PerformanceObserver | null = null;
let domObserver: MutationObserver | null = null;

function publishDiagnostics(status: string, observationCount: number): void {
  document.documentElement.dataset.zgBridgeStatus = status;
  document.documentElement.dataset.zgBridgeCount = String(observationCount);
}

function rememberFirstPartyResource(rawUrl: string): boolean {
  try {
    const resourceUrl = new URL(rawUrl, location.href);
    if (resourceUrl.origin !== location.origin) return false;

    const canonical = `${resourceUrl.origin}${resourceUrl.pathname}`;
    const previousSize = firstPartyResourceUrls.size;
    firstPartyResourceUrls.add(canonical);
    return firstPartyResourceUrls.size !== previousSize;
  } catch {
    return false;
  }
}

function rememberResourceEntries(entries: readonly PerformanceEntry[]): boolean {
  let changed = false;
  entries.forEach((entry) => {
    changed = rememberFirstPartyResource(entry.name) || changed;
  });
  return changed;
}

async function collectCurrentPage(): Promise<void> {
  if (collectionSucceeded) return;
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
      [...firstPartyResourceUrls],
    );

    if (result.status === "ok") {
      collectionSucceeded = true;
      resourceObserver?.disconnect();
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
    if (collectionPending && !collectionSucceeded) {
      collectionPending = false;
      void collectCurrentPage();
    }
  }
}

function requestCollection(): void {
  if (collectionSucceeded) return;
  if (collectionInFlight) {
    collectionPending = true;
    return;
  }
  void collectCurrentPage();
}

resourceObserver = new PerformanceObserver((list) => {
  if (rememberResourceEntries(list.getEntries())) {
    requestCollection();
  }
});
resourceObserver.observe({ type: "resource", buffered: true });

domObserver = new MutationObserver((mutations) => {
  if (mutations.some((mutation) => mutation.addedNodes.length > 0 || mutation.removedNodes.length > 0)) {
    requestCollection();
  }
});
domObserver.observe(document.documentElement, { childList: true, subtree: true });

rememberResourceEntries(performance.getEntriesByType("resource"));
requestCollection();
