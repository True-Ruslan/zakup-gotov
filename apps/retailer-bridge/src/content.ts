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

function publishDiagnostics(status: string, observationCount: number): void {
  document.documentElement.dataset.zgBridgeStatus = status;
  document.documentElement.dataset.zgBridgeCount = String(observationCount);
}

function currentFirstPartyResourceUrls(): string[] {
  const urls = new Set<string>();
  performance.getEntriesByType("resource").forEach((entry) => {
    try {
      const resourceUrl = new URL(entry.name, location.href);
      if (resourceUrl.origin !== location.origin) return;
      urls.add(`${resourceUrl.origin}${resourceUrl.pathname}`);
    } catch {
      // Ignore malformed performance entries; adapter fails closed if context is absent.
    }
  });
  return [...urls];
}

void collector
  .collect(
    document,
    new URL(location.href),
    new Date().toISOString(),
    currentFirstPartyResourceUrls(),
  )
  .then(async (result) => {
    if (result.status !== "ok") {
      await clearObservations();
    }
    publishDiagnostics(result.status, result.observationCount);
  })
  .catch(async () => {
    await clearObservations();
    publishDiagnostics("internal-error", 0);
  });
