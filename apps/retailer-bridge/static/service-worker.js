chrome.runtime.onMessage.addListener((message) => {
  if (message?.type !== "ZG_STORE_OBSERVATIONS" || !Array.isArray(message.observations)) {
    return;
  }

  return chrome.storage.local.set({
    "zg.latestObservations": message.observations,
  });
});
