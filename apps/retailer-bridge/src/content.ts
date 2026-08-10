import { perekrestokBrowserAdapter } from "./adapters/perekrestok-browser-adapter";
import { BrowserObservationCollector } from "./collector/browser-observation-collector";
import { createChromeObservationSink } from "./collector/chrome-observation-sink";

const sink = createChromeObservationSink((message) => chrome.runtime.sendMessage(message));
const collector = new BrowserObservationCollector([perekrestokBrowserAdapter], sink);

function publishDiagnostics(status: string, observationCount: number): void {
  document.documentElement.dataset.zgBridgeStatus = status;
  document.documentElement.dataset.zgBridgeCount = String(observationCount);
}

void collector
  .collect(document, new URL(location.href), new Date().toISOString())
  .then((result) => publishDiagnostics(result.status, result.observationCount))
  .catch(() => publishDiagnostics("internal-error", 0));
