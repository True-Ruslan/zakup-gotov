import type { BrowserObservation } from "../model/browser-observation";

export type RuntimeSendMessage = (message: unknown) => Promise<unknown> | unknown;
export type ObservationRevision = number;

function requireRevision(revision: ObservationRevision): void {
  if (!Number.isSafeInteger(revision) || revision <= 0) {
    throw new Error("observation revision must be a positive safe integer");
  }
}

export function createChromeObservationSink(
  sendMessage: RuntimeSendMessage,
): (observations: BrowserObservation[], revision: ObservationRevision) => Promise<void> {
  return async (observations: BrowserObservation[], revision: ObservationRevision) => {
    requireRevision(revision);
    await sendMessage({
      type: "ZG_STORE_OBSERVATIONS",
      revision,
      observations,
    });
  };
}

export function createChromeObservationClearer(
  sendMessage: RuntimeSendMessage,
): (revision: ObservationRevision) => Promise<void> {
  return async (revision: ObservationRevision) => {
    requireRevision(revision);
    await sendMessage({
      type: "ZG_STORE_OBSERVATIONS",
      revision,
      observations: [],
    });
  };
}
