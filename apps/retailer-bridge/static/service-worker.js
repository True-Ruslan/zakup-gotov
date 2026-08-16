const OBSERVATIONS_KEY = "zg.latestObservations";

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
    if (revision !== latestRevision) {
      return;
    }

    await chrome.storage.local.set({
      [OBSERVATIONS_KEY]: message.observations,
    });

    // A newer lifecycle message may have arrived while the storage write was
    // in flight. Repair the key to the newest accepted payload before this
    // older handler resolves.
    if (revision !== latestRevision) {
      await chrome.storage.local.set({
        [OBSERVATIONS_KEY]: latestObservations,
      });
    }
  })();
});
