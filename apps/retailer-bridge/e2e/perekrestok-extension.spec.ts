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
