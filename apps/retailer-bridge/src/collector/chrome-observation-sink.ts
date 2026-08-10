import type { BrowserObservation } from "../model/browser-observation";
import type { ObservationSink } from "./browser-observation-collector";

export type RuntimeSendMessage = (message: unknown) => Promise<unknown> | unknown;

export function createChromeObservationSink(
  sendMessage: RuntimeSendMessage,
): ObservationSink {
  return async (observations: BrowserObservation[]) => {
    await sendMessage({
      type: "ZG_STORE_OBSERVATIONS",
      observations,
    });
  };
}
