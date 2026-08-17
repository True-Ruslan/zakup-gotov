import { mkdtemp, rm } from "node:fs/promises";
import { tmpdir } from "node:os";
import { join, resolve } from "node:path";
import { chromium, expect, test } from "@playwright/test";

const bridgeRoot = resolve(process.cwd(), "../retailer-bridge");
const extensionPath = resolve(bridgeRoot, "dist");
const pageHtml = "<!doctype html><html><body><main>Chizhik catalog fixture</main></body></html>";
const shopsEndpoint = "https://app.chizhik.club/api/v1/shops/";

async function launchBridge() {
  const userDataDir = await mkdtemp(join(tmpdir(), "zg-retailer-bridge-chizhik-"));
  const context = await chromium.launchPersistentContext(userDataDir, {
    channel: "chromium",
    headless: true,
    args: [
      `--disable-extensions-except=${extensionPath}`,
      `--load-extension=${extensionPath}`,
    ],
  });
  return { context, userDataDir };
}

test("actively discovers Chizhik stores once through normal page-origin CORS without creating offers", async () => {
  const { context, userDataDir } = await launchBridge();
  let shopsRequests = 0;

  try {
    await context.route("https://chizhik.club/**", async (route) => {
      await route.fulfill({
        status: 200,
        contentType: "text/html; charset=utf-8",
        body: pageHtml,
      });
    });
    await context.route(shopsEndpoint, async (route) => {
      shopsRequests += 1;
      await route.fulfill({
        status: 200,
        headers: {
          "access-control-allow-origin": "https://chizhik.club",
          "content-type": "application/json; charset=utf-8",
        },
        body: JSON.stringify([
          {
            id: 26504,
            sap_id: "HD87",
            lon: 37.83372708,
            lat: 55.76833314,
            status: 1,
            name: "Москва, Саянская ул., Дом 11Б",
            locality: "Москва",
            secret_like_field: "MUST_NOT_PERSIST",
          },
        ]),
      });
    });

    const page = await context.newPage();
    await page.goto("https://chizhik.club/catalog/chay-kofe--264C39224/");

    await expect
      .poll(() => page.locator("html").getAttribute("data-zg-bridge-status"))
      .toBe("observation-only");
    await expect.poll(() => page.locator("html").getAttribute("data-zg-bridge-count")).toBe("0");
    expect(shopsRequests).toBe(1);

    const worker = context.serviceWorkers()[0] ?? (await context.waitForEvent("serviceworker"));
    const stored = await worker.evaluate(async () => chrome.storage.local.get("zg.latestObservations"));
    expect(stored["zg.latestObservations"] ?? []).toEqual([]);
    expect(JSON.stringify(stored)).not.toContain("MUST_NOT_PERSIST");
  } finally {
    await context.close();
    await rm(userDataDir, { recursive: true, force: true });
  }
});

test("fails closed when active Chizhik store discovery is blocked", async () => {
  const { context, userDataDir } = await launchBridge();

  try {
    await context.route("https://chizhik.club/**", async (route) => {
      await route.fulfill({
        status: 200,
        contentType: "text/html; charset=utf-8",
        body: pageHtml,
      });
    });
    await context.route(shopsEndpoint, async (route) => {
      await route.fulfill({
        status: 403,
        headers: {
          "access-control-allow-origin": "https://chizhik.club",
          "content-type": "text/plain; charset=utf-8",
        },
        body: "blocked",
      });
    });

    const page = await context.newPage();
    await page.goto("https://chizhik.club/catalog/chay-kofe--264C39224/");

    await expect
      .poll(() => page.locator("html").getAttribute("data-zg-bridge-status"))
      .toBe("missing-context");
    await expect.poll(() => page.locator("html").getAttribute("data-zg-bridge-count")).toBe("0");
  } finally {
    await context.close();
    await rm(userDataDir, { recursive: true, force: true });
  }
});
