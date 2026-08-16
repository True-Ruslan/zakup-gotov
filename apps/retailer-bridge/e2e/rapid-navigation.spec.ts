import { mkdtemp, readFile, rm } from "node:fs/promises";
import { tmpdir } from "node:os";
import { join, resolve } from "node:path";
import { chromium, expect, test, type Worker } from "@playwright/test";

const bridgeRoot = resolve(process.cwd(), "../retailer-bridge");
const extensionPath = resolve(bridgeRoot, "dist");
const fixtureDir = resolve(bridgeRoot, "test/fixtures");

async function storedObservations(worker: Worker): Promise<unknown[]> {
  return worker.evaluate(async () => {
    const current = await chrome.storage.local.get("zg.latestObservations");
    return current["zg.latestObservations"] ?? [];
  });
}

test("ignores a fulfillment response that started before the latest SPA boundary", async () => {
  const userDataDir = await mkdtemp(join(tmpdir(), "zg-retailer-bridge-rapid-navigation-"));
  let releaseDelayedShopResponse: () => void = () => undefined;
  const delayedShopResponse = new Promise<void>((resolve) => {
    releaseDelayedShopResponse = resolve;
  });

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
      const url = new URL(route.request().url());
      if (/^\/api\/customer\/1\.4\.1\.0\/shop\/(656|777)$/.test(url.pathname)) {
        if (url.searchParams.get("delayed") === "true") {
          await delayedShopResponse;
        }
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

    const worker = context.serviceWorkers()[0] ?? (await context.waitForEvent("serviceworker"));
    expect(JSON.stringify(await storedObservations(worker))).toContain('"fulfillmentContextId":"656"');

    await page.evaluate(() => {
      history.pushState({}, "", "/cat/fixture-first-transition");
      void fetch(
        "/api/customer/1.4.1.0/shop/656?delayed=true&session=SECRET_OLD_ROUTE#fragment",
      ).then(() => {
        document.documentElement.dataset.zgDelayedShopDone = "true";
      });
    });

    await expect.poll(() => storedObservations(worker)).toEqual([]);

    await page.evaluate(() => {
      history.pushState({}, "", "/cat/fixture-latest-transition");
      const catalog = document.querySelector(".catalog-content");
      if (!catalog) throw new Error("catalog fixture is missing");
      catalog.innerHTML = `<article class="product-card">
        <a class="product-card__link" href="/cat/1768/p/sanitized-product-8812345"></a>
        <div class="product-card__content">
          <div class="product-card__title-wrapper">
            <div class="product-card__title">
              <a class="product-card__title-link" href="/cat/1768/p/sanitized-product-8812345">Продукт последнего SPA route</a>
            </div>
          </div>
          <div class="product-card__pricing">
            <div class="product-card__price"><span class="price-new">569,90 ₽</span></div>
          </div>
        </div>
      </article>`;
    });

    releaseDelayedShopResponse();
    await expect.poll(() => page.locator("html").getAttribute("data-zg-delayed-shop-done")).toBe("true");
    await page.waitForTimeout(300);

    expect(await storedObservations(worker)).toEqual([]);
    expect(await page.locator("html").getAttribute("data-zg-bridge-status")).toBe("refreshing");

    await page.evaluate(async () => {
      await fetch("/api/customer/1.4.1.0/shop/777?session=SECRET_FRESH_ROUTE#fragment");
    });

    await expect.poll(() => page.locator("html").getAttribute("data-zg-bridge-status")).toBe("ok");
    await expect.poll(() => storedObservations(worker)).toHaveLength(1);

    const serialized = JSON.stringify(await storedObservations(worker));
    expect(serialized).toContain('"fulfillmentContextId":"777"');
    expect(serialized).toContain('"sku":"8812345"');
    expect(serialized).toContain('"priceMinor":56990');
    expect(serialized).toContain('"sourceReference":"https://www.perekrestok.ru/cat/fixture-latest-transition"');
    expect(serialized).not.toContain('"fulfillmentContextId":"656"');
    expect(serialized).not.toContain("SECRET_OLD_ROUTE");
    expect(serialized).not.toContain("SECRET_FRESH_ROUTE");
  } finally {
    releaseDelayedShopResponse();
    await context.close();
    await rm(userDataDir, { recursive: true, force: true });
  }
});
