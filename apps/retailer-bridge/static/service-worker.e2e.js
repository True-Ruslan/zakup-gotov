const OBSERVATIONS_KEY = "zg.latestObservations";
const DELAY_NEXT_STORE_KEY = "zg.e2e.delayNextObservationStore";
const STORE_PENDING_KEY = "zg.e2e.observationStorePending";
const RELEASE_STORE_KEY = "zg.e2e.releaseObservationStore";

let latestRevision = 0;
let latestObservations = [];

function validRevision(value) {
  return Number.isSafeInteger(value) && value > 0;
}

chrome.runtime.onMessage.addListener((message) => {
  if (
    message?.type !== "ZG_STORE_OBSERVATIONS" ||
    !Array.isArray(message.observations) ||
    !validRevision(message.revision)
  ) {
    return;
  }

  const revision = message.revision;
  if (revision < latestRevision) {
    return Promise.resolve();
  }

  latestRevision = revision;
  latestObservations = message.observations;

  return (async () => {
    const controls = await chrome.storage.local.get(DELAY_NEXT_STORE_KEY);
    const delayed =
      message.observations.length > 0 &&
      controls[DELAY_NEXT_STORE_KEY] === true;

    if (delayed) {
      await chrome.storage.local.remove([DELAY_NEXT_STORE_KEY, RELEASE_STORE_KEY]);
      await chrome.storage.local.set({ [STORE_PENDING_KEY]: true });

      await new Promise((resolve) => {
        const listener = (changes, areaName) => {
          if (areaName !== "local" || changes[RELEASE_STORE_KEY]?.newValue !== true) {
            return;
          }
          chrome.storage.onChanged.removeListener(listener);
          resolve();
        };
        chrome.storage.onChanged.addListener(listener);
      });
    }

    if (revision === latestRevision) {
      await chrome.storage.local.set({
        [OBSERVATIONS_KEY]: message.observations,
      });
    }

    if (delayed) {
      await chrome.storage.local.remove([STORE_PENDING_KEY, RELEASE_STORE_KEY]);
    }

    if (revision !== latestRevision) {
      await chrome.storage.local.set({
        [OBSERVATIONS_KEY]: latestObservations,
      });
    }
  })();
});
