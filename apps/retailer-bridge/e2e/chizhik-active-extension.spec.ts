import { mkdtemp, rm } from "node:fs/promises";
import { tmpdir } from "node:os";
import { join, resolve } from "node:path";
import { chromium, expect, test, type BrowserContext, type Page } from "@playwright/test";

const bridgeRoot = resolve(process.cwd(), "../retailer-bridge");
const extensionPath = resolve(bridgeRoot, "dist");
const pageHtml = "<!doctype html><html><body><main>Chizhik catalog fixture</main></body></html>";
const shopsEndpoint = "https://app.chizhik.club/api/v1/shops/";
const hd87Search =
  "https://app.chizhik.club/delivery/api/catalog/v3/stores/HD87/search?mode=store&q=cola";
const hd88Search =
  "https://app.chizhik.club/delivery/api/catalog/v3/stores/HD88/search?mode=store&q=cola";
const unknownSearch =
  "https://app.chizhik.club/delivery/api/catalog/v3/stores/UNKNOWN/search?mode=store&q=cola";
const foreignOriginSearch =
  "https://app.chizhik.club.evil.example/delivery/api/catalog/v3/stores/HD87/search?mode=store&q=cola";

const stores = [
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
  {
    id: 26523,
    sap_id: "HD88",
    lon: 37.80898339,
    lat: 55.39666279,
    status: 1,
    name: "Домодедово, Вокзальная ул., Строение 2г",
    locality: "Домодедово",
  },
];

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

async function routeOfficialPage(context: BrowserContext): Promise<void> {
  await context.route("https://chizhik.club/**", async (route) => {
    await route.fulfill({
      status: 200,
      contentType: "text/html; charset=utf-8",
      body: pageHtml,
    });
  });
}

async function routeShops(context: BrowserContext, status = 200): Promise<() => number> {
  let requests = 0;
  await context.route(shopsEndpoint, async (route) => {
    requests += 1;
    if (status !== 200) {
      await route.fulfill({
        status,
        headers: {
          "access-control-allow-origin": "https://chizhik.club",
          "content-type": "text/plain; charset=utf-8",
        },
        body: "blocked",
      });
      return;
    }

    await route.fulfill({
      status: 200,
      headers: {
        "access-control-allow-origin": "https://chizhik.club",
        "content-type": "application/json; charset=utf-8",
      },
      body: JSON.stringify(stores),
    });
  });
  return () => requests;
}

async function routeDeliveryEvidence(context: BrowserContext): Promise<void> {
  await context.route("https://app.chizhik.club/delivery/api/catalog/**", async (route) => {
    await route.fulfill({
      status: 200,
      headers: {
        "access-control-allow-origin": "https://chizhik.club",
        "content-type": "application/json; charset=utf-8",
      },
      body: JSON.stringify({ products: [] }),
    });
  });
}

async function routeForeignDeliveryEvidence(context: BrowserContext): Promise<void> {
  await context.route("https://app.chizhik.club.evil.example/**", async (route) => {
    await route.fulfill({
      status: 200,
      headers: {
        "access-control-allow-origin": "https://chizhik.club",
        "content-type": "application/json; charset=utf-8",
      },
      body: JSON.stringify({ products: [] }),
    });
  });
}

async function emitDeliveryResource(page: Page, url: string): Promise<void> {
  await page.evaluate(async (resourceUrl) => {
    await fetch(resourceUrl, {
      method: "GET",
      mode: "cors",
      credentials: "same-origin",
      headers: { Accept: "application/json, text/plain, */*" },
    });
  }, url);
}

test("binds Chizhik to one browser-evidenced validated store without creating offers", async () => {
  const { context, userDataDir } = await launchBridge();

  try {
    await routeOfficialPage(context);
    const shopsRequests = await routeShops(context);
    await routeDeliveryEvidence(context);

    const page = await context.newPage();
    await page.goto("https://chizhik.club/catalog/chay-kofe--264C39224/");

    await expect
      .poll(() => page.locator("html").getAttribute("data-zg-bridge-status"))
      .toBe("missing-context");

    await emitDeliveryResource(page, hd87Search);

    await expect
      .poll(() => page.locator("html").getAttribute("data-zg-bridge-status"))
      .toBe("observation-only");
    await expect.poll(() => page.locator("html").getAttribute("data-zg-bridge-count")).toBe("0");
    expect(shopsRequests()).toBe(1);

    const worker = context.serviceWorkers()[0] ?? (await context.waitForEvent("serviceworker"));
    const stored = await worker.evaluate(async () => chrome.storage.local.get("zg.latestObservations"));
    expect(stored["zg.latestObservations"] ?? []).toEqual([]);
    expect(JSON.stringify(stored)).not.toContain("MUST_NOT_PERSIST");
  } finally {
    await context.close();
    await rm(userDataDir, { recursive: true, force: true });
  }
});

test("fails closed for an evidenced delivery store absent from the validated directory", async () => {
  const { context, userDataDir } = await launchBridge();

  try {
    await routeOfficialPage(context);
    await routeShops(context);
    await routeDeliveryEvidence(context);

    const page = await context.newPage();
    await page.goto("https://chizhik.club/catalog/chay-kofe--264C39224/");
    await emitDeliveryResource(page, unknownSearch);

    await expect
      .poll(() => page.locator("html").getAttribute("data-zg-bridge-status"))
      .toBe("missing-context");
    await expect.poll(() => page.locator("html").getAttribute("data-zg-bridge-count")).toBe("0");
  } finally {
    await context.close();
    await rm(userDataDir, { recursive: true, force: true });
  }
});

test("fails closed for a foreign-origin delivery resource even when it carries a validated store id", async () => {
  const { context, userDataDir } = await launchBridge();

  try {
    await routeOfficialPage(context);
    await routeShops(context);
    await routeForeignDeliveryEvidence(context);

    const page = await context.newPage();
    await page.goto("https://chizhik.club/catalog/chay-kofe--264C39224/");
    await emitDeliveryResource(page, foreignOriginSearch);

    await expect
      .poll(() => page.locator("html").getAttribute("data-zg-bridge-status"))
      .toBe("missing-context");
    await expect.poll(() => page.locator("html").getAttribute("data-zg-bridge-count")).toBe("0");
  } finally {
    await context.close();
    await rm(userDataDir, { recursive: true, force: true });
  }
});

test("fails closed when retained delivery evidence conflicts across validated stores", async () => {
  const { context, userDataDir } = await launchBridge();

  try {
    await routeOfficialPage(context);
    await routeShops(context);
    await routeDeliveryEvidence(context);

    const page = await context.newPage();
    await page.goto("https://chizhik.club/catalog/chay-kofe--264C39224/");
    await emitDeliveryResource(page, hd87Search);
    await expect
      .poll(() => page.locator("html").getAttribute("data-zg-bridge-status"))
      .toBe("observation-only");

    await emitDeliveryResource(page, hd88Search);

    await expect
      .poll(() => page.locator("html").getAttribute("data-zg-bridge-status"))
      .toBe("missing-context");
    await expect.poll(() => page.locator("html").getAttribute("data-zg-bridge-count")).toBe("0");
  } finally {
    await context.close();
    await rm(userDataDir, { recursive: true, force: true });
  }
});

test("fails closed when active Chizhik store discovery is blocked", async () => {
  const { context, userDataDir } = await launchBridge();

  try {
    await routeOfficialPage(context);
    await routeShops(context, 403);
    await routeDeliveryEvidence(context);

    const page = await context.newPage();
    await page.goto("https://chizhik.club/catalog/chay-kofe--264C39224/");
    await emitDeliveryResource(page, hd87Search);

    await expect
      .poll(() => page.locator("html").getAttribute("data-zg-bridge-status"))
      .toBe("missing-context");
    await expect.poll(() => page.locator("html").getAttribute("data-zg-bridge-count")).toBe("0");
  } finally {
    await context.close();
    await rm(userDataDir, { recursive: true, force: true });
  }
});
