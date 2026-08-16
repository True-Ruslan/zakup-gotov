import { mkdtemp, readFile, rm } from "node:fs/promises";
import { tmpdir } from "node:os";
import { join, resolve } from "node:path";
import { chromium, expect, test, type Page, type Worker } from "@playwright/test";

const bridgeRoot = resolve(process.cwd(), "../retailer-bridge");
const extensionPath = resolve(bridgeRoot, "dist-e2e");
const fixtureDir = resolve(bridgeRoot, "test/fixtures");
const observationsKey = "zg.latestObservations";
const delayNextStoreKey = "zg.e2e.delayNextObservationStore";
const storePendingKey = "zg.e2e.observationStorePending";
const releaseStoreKey = "zg.e2e.releaseObservationStore";

async function storedObservations(worker: Worker): Promise<unknown[]> {
  return worker.evaluate(async (key) => {
    const current = await chrome.storage.local.get(key);
    return current[key] ?? [];
  }, observationsKey);
}

async function replaceCatalogProduct(
  page: Page,
  sku: string,
  name: string,
  price: string,
): Promise<void> {
  await page.evaluate(
    ({ productSku, productName, productPrice }) => {
      const catalog = document.querySelector(".catalog-content");
      if (!catalog) throw new Error("catalog fixture is missing");
      catalog.innerHTML = `<article class="product-card">
        <a class="product-card__link" href="/cat/1768/p/sanitized-product-${productSku}"></a>
        <div class="product-card__content">
          <div class="product-card__title-wrapper">
            <div class="product-card__title">
              <a class="product-card__title-link" href="/cat/1768/p/sanitized-product-${productSku}">${productName}</a>
            </div>
          </div>
          <div class="product-card__pricing">
            <div class="product-card__price"><span class="price-new">${productPrice} ₽</span></div>
          </div>
        </div>
      </article>`;
    },
    { productSku: sku, productName: name, productPrice: price },
  );
}

test("does not republish an obsolete collection after a newer SPA boundary", async () => {
  const userDataDir = await mkdtemp(join(tmpdir(), "zg-retailer-bridge-inflight-"));
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
      if (/^\/api\/customer\/1\.4\.1\.0\/shop\/(656|777)$/.test(pathname)) {
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

    await worker.evaluate(
      async ({ delayKey, pendingKey, releaseKey }) => {
        await chrome.storage.local.remove([pendingKey, releaseKey]);
        await chrome.storage.local.set({ [delayKey]: true });
      },
      { delayKey: delayNextStoreKey, pendingKey: storePendingKey, releaseKey: releaseStoreKey },
    );

    await page.evaluate(async () => {
      history.pushState({}, "", "/cat/inflight-old-route");
      document.querySelector(".catalog-content")?.replaceChildren();
      await fetch("/api/customer/1.4.1.0/shop/656?session=SECRET_OLD_COLLECTION#fragment");
    });
    await expect.poll(() => storedObservations(worker)).toEqual([]);

    await replaceCatalogProduct(page, "9912345", "Устаревший in-flight продукт", "619,90");
    await expect.poll(() =>
      worker.evaluate(async (key) => {
        const state = await chrome.storage.local.get(key);
        return state[key] === true;
      }, storePendingKey),
    ).toBe(true);

    await page.evaluate(() => {
      history.pushState({}, "", "/cat/inflight-latest-route");
      document.querySelector(".catalog-content")?.replaceChildren();
    });
    await replaceCatalogProduct(page, "9923456", "Актуальный in-flight продукт", "729,90");

    await worker.evaluate(async (key) => {
      await chrome.storage.local.set({ [key]: true });
    }, releaseStoreKey);
    await expect.poll(() =>
      worker.evaluate(async (key) => {
        const state = await chrome.storage.local.get(key);
        return state[key] === true;
      }, storePendingKey),
    ).toBe(false);

    // The collection that started for the previous route must never become current
    // after the newer SPA lifecycle boundary has already invalidated it.
    expect(await storedObservations(worker)).toEqual([]);
    expect(await page.locator("html").getAttribute("data-zg-bridge-status")).toBe("refreshing");

    await page.evaluate(async () => {
      await fetch("/api/customer/1.4.1.0/shop/777?session=SECRET_LATEST_COLLECTION#fragment");
    });

    await expect.poll(() => page.locator("html").getAttribute("data-zg-bridge-status")).toBe("ok");
    await expect.poll(() => storedObservations(worker)).toHaveLength(1);

    const serialized = JSON.stringify(await storedObservations(worker));
    expect(serialized).toContain('"fulfillmentContextId":"777"');
    expect(serialized).toContain('"sku":"9923456"');
    expect(serialized).toContain('"priceMinor":72990');
    expect(serialized).toContain('"sourceReference":"https://www.perekrestok.ru/cat/inflight-latest-route"');
    expect(serialized).not.toContain('"sku":"9912345"');
    expect(serialized).not.toContain("SECRET_OLD_COLLECTION");
    expect(serialized).not.toContain("SECRET_LATEST_COLLECTION");
  } finally {
    await context.close();
    await rm(userDataDir, { recursive: true, force: true });
  }
});
