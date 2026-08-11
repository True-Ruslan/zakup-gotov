import { mkdtemp, readFile, rm } from "node:fs/promises";
import { tmpdir } from "node:os";
import { join, resolve } from "node:path";
import playwright from "../../web/node_modules/@playwright/test/index.js";

const { chromium, expect, test } = playwright;
const bridgeRoot = resolve(process.cwd(), "../retailer-bridge");
const extensionPath = resolve(bridgeRoot, "dist");
const fixtureDir = resolve(bridgeRoot, "test/fixtures");

async function fixtureFor(url: string): Promise<string> {
  const pathname = new URL(url).pathname;
  const name = pathname.includes("missing-context")
    ? "perekrestok-missing-context.html"
    : "perekrestok-product-state.html";
  return readFile(resolve(fixtureDir, name), "utf8");
}

test("stores only sanitized data and clears stale observations when context disappears", async () => {
  const userDataDir = await mkdtemp(join(tmpdir(), "zg-retailer-bridge-"));
  const context = await chromium.launchPersistentContext(userDataDir, {
    channel: "chromium",
    headless: true,
    args: [
      `--disable-extensions-except=${extensionPath}`,
      `--load-extension=${extensionPath}`,
    ],
  });

  try {
    await context.route("https://www.perekrestok.ru/**", async (route) => {
      await route.fulfill({
        status: 200,
        contentType: "text/html; charset=utf-8",
        body: await fixtureFor(route.request().url()),
      });
    });

    await context.addCookies([
      {
        name: "session",
        value: "SECRET_COOKIE",
        domain: "www.perekrestok.ru",
        path: "/",
        secure: true,
        sameSite: "Lax",
      },
    ]);
    await context.addInitScript(() => {
      try {
        localStorage.setItem("auth", "SECRET_LOCAL_STORAGE");
      } catch {
        // Some bootstrap documents do not expose storage; the target HTTPS page does.
      }
    });

    const page = await context.newPage();
    await page.goto("https://www.perekrestok.ru/cat/fixture-success");

    await expect.poll(() => page.locator("html").getAttribute("data-zg-bridge-status")).toBe("ok");
    await expect.poll(() => page.locator("html").getAttribute("data-zg-bridge-count")).toBe("3");

    const worker = context.serviceWorkers()[0] ?? (await context.waitForEvent("serviceworker"));
    const stored = await worker.evaluate(async () =>
      chrome.storage.local.get("zg.latestObservations"),
    );
    const serialized = JSON.stringify(stored);

    expect(serialized).toContain("3431579");
    expect(serialized).toContain("8999");
    expect(serialized).not.toContain("SECRET_COOKIE");
    expect(serialized).not.toContain("SECRET_LOCAL_STORAGE");
    expect(serialized).not.toContain("session=");

    await page.goto("https://www.perekrestok.ru/cat/fixture-missing-context");
    await expect.poll(() => page.locator("html").getAttribute("data-zg-bridge-status")).toBe("missing-context");

    await expect
      .poll(async () => {
        const current = await worker.evaluate(async () =>
          chrome.storage.local.get("zg.latestObservations"),
        );
        return current["zg.latestObservations"] ?? [];
      })
      .toEqual([]);
  } finally {
    await context.close();
    await rm(userDataDir, { recursive: true, force: true });
  }
});

test("collects current catalog DOM after async shop and DOM evidence resolve", async () => {
  const userDataDir = await mkdtemp(join(tmpdir(), "zg-retailer-bridge-live-dom-"));
  const context = await chromium.launchPersistentContext(userDataDir, {
    channel: "chromium",
    headless: true,
    args: [
      `--disable-extensions-except=${extensionPath}`,
      `--load-extension=${extensionPath}`,
    ],
  });

  try {
    const liveFixture = await readFile(
      resolve(fixtureDir, "perekrestok-live-dom-async-state.html"),
      "utf8",
    );

    await context.route("https://www.perekrestok.ru/**", async (route) => {
      const pathname = new URL(route.request().url()).pathname;
      if (pathname === "/api/customer/1.4.1.0/shop/656") {
        await route.fulfill({
          status: 200,
          contentType: "application/json; charset=utf-8",
          body: "{}",
        });
        return;
      }

      await route.fulfill({
        status: 200,
        contentType: "text/html; charset=utf-8",
        body: liveFixture,
      });
    });

    const page = await context.newPage();
    await page.goto("https://www.perekrestok.ru/cat/fixture-live-dom");

    await expect.poll(() => page.locator("html").getAttribute("data-zg-bridge-status")).toBe("ok");
    await expect.poll(() => page.locator("html").getAttribute("data-zg-bridge-count")).toBe("1");

    const worker = context.serviceWorkers()[0] ?? (await context.waitForEvent("serviceworker"));
    const stored = await worker.evaluate(async () =>
      chrome.storage.local.get("zg.latestObservations"),
    );
    const serialized = JSON.stringify(stored);

    expect(serialized).toContain("\"fulfillmentContextId\":\"656\"");
    expect(serialized).toContain("\"sku\":\"4408829\"");
    expect(serialized).toContain("\"priceMinor\":19999");
    expect(serialized).toContain("\"adapterVersion\":\"2\"");
    expect(serialized).not.toContain("SECRET_RESOURCE_QUERY");
    expect(serialized).not.toContain("session=");
  } finally {
    await context.close();
    await rm(userDataDir, { recursive: true, force: true });
  }
});

test("collects Pyaterochka catalog after async official service context and delayed DOM", async () => {
  const userDataDir = await mkdtemp(join(tmpdir(), "zg-retailer-bridge-pyaterochka-"));
  const context = await chromium.launchPersistentContext(userDataDir, {
    channel: "chromium",
    headless: true,
    args: [
      `--disable-extensions-except=${extensionPath}`,
      `--load-extension=${extensionPath}`,
    ],
  });

  try {
    const liveFixture = await readFile(
      resolve(fixtureDir, "pyaterochka-live-dom-async-state.html"),
      "utf8",
    );

    await context.route("https://5ka.ru/**", async (route) => {
      await route.fulfill({
        status: 200,
        contentType: "text/html; charset=utf-8",
        body: liveFixture,
      });
    });
    await context.route("https://5d.5ka.ru/**", async (route) => {
      await route.fulfill({
        status: 200,
        headers: { "access-control-allow-origin": "https://5ka.ru" },
        contentType: "application/json; charset=utf-8",
        body: "{}",
      });
    });

    await context.addCookies([
      {
        name: "session",
        value: "SECRET_PYATEROCHKA_COOKIE",
        domain: "5ka.ru",
        path: "/",
        secure: true,
        sameSite: "Lax",
      },
    ]);

    const page = await context.newPage();
    await page.goto("https://5ka.ru/catalog/fixture-live-dom");

    await expect.poll(() => page.locator("html").getAttribute("data-zg-bridge-status")).toBe("ok");
    await expect.poll(() => page.locator("html").getAttribute("data-zg-bridge-count")).toBe("1");

    const worker = context.serviceWorkers()[0] ?? (await context.waitForEvent("serviceworker"));
    const stored = await worker.evaluate(async () =>
      chrome.storage.local.get("zg.latestObservations"),
    );
    const serialized = JSON.stringify(stored);

    expect(serialized).toContain("\"retailerId\":\"pyaterochka\"");
    expect(serialized).toContain("\"sourceProviderId\":\"pyaterochka-browser\"");
    expect(serialized).toContain("\"fulfillmentContextId\":\"ZG001\"");
    expect(serialized).toContain("\"sku\":\"25113239\"");
    expect(serialized).toContain("\"priceMinor\":9999");
    expect(serialized).toContain("\"adapterVersion\":\"1\"");
    expect(serialized).not.toContain("SECRET_RESOURCE_QUERY");
    expect(serialized).not.toContain("SECRET_PYATEROCHKA_COOKIE");
    expect(serialized).not.toContain("session=");
  } finally {
    await context.close();
    await rm(userDataDir, { recursive: true, force: true });
  }
});
