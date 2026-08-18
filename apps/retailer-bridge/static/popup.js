const REQUEST_TYPE = "zg-chizhik-schema-canary";
const RESULT_TYPE = "zg-chizhik-schema-canary-result";
const FALLBACK = "CHIZHIK_D2 status=UNAVAILABLE";

const runButton = document.getElementById("run");
const evidence = document.getElementById("evidence");

async function runCanary() {
  if (!(runButton instanceof HTMLButtonElement) || !(evidence instanceof HTMLElement)) return;

  runButton.disabled = true;
  evidence.textContent = "CHIZHIK_D2 status=RUNNING";
  try {
    const [tab] = await chrome.tabs.query({ active: true, currentWindow: true });
    if (!Number.isInteger(tab?.id)) {
      evidence.textContent = FALLBACK;
      return;
    }

    const response = await chrome.tabs.sendMessage(tab.id, { type: REQUEST_TYPE });
    if (
      !response ||
      response.type !== RESULT_TYPE ||
      typeof response.evidence !== "string" ||
      !response.evidence.startsWith("CHIZHIK_D2 status=")
    ) {
      evidence.textContent = FALLBACK;
      return;
    }

    evidence.textContent = response.evidence;
  } catch {
    evidence.textContent = FALLBACK;
  } finally {
    runButton.disabled = false;
  }
}

runButton?.addEventListener("click", () => {
  void runCanary();
});
