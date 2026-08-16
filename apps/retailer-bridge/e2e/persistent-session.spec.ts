import { mkdtemp, readFile, rm } from "node:fs/promises";
import { tmpdir } from "node:os";
import { join, resolve } from "node:path";
import { chromium, expect, test, type BrowserContext, type Page, type Worker } from "@playwright/test";

const bridgeRoot = resolve(process.cwd(), "../retailer-bridge");
const extensionPath = resolve(bridgeRoot, "dist");
const fixtureDir = resolve(bridgeRoot, "test/fixtures");

async function openPerekrestokFixture(): Promise<{
  context: BrowserContext;
  page: Page;
  worker: Worker;
  userDataDir: string;
}> {
  const userDataDir = await mkdtemp(join(tmpdir(), "zg-retailer-bridge-persistent-"));
  const context = await chromium.launchPersistentContext(userDataDir, {
    channel: "chromium",
    headless: true,
    args: [
      `--disable-extensions-except=${extensionPath}`,
      `--load-extension=${extensionPath}`,
    ],
  });

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
  await expect.poll(() => page.locator("html").getAttribute("data-zg-bridge-count")).toBe("1");

  const worker = context.serviceWorkers()[0] ?? (await context.waitForEvent("serviceworker"));
  return { context, page, worker, userDataDir };
}

async function storedObservations(worker: Worker): Promise<unknown[]> {
  return worker.evaluate(async () => {
    const current = await chrome.storage.local.get("zg.latestObservations");
    return current["zg.latestObservations"] ?? [];
  });
}

async function insertProduct(page: Page, sku: string, name: string, price: string): Promise<void> {
  await page.evaluate(
    ({ productSku, productName, productPrice }) => {
      const catalog = document.querySelector(".catalog-content");
      if (!catalog) throw new Error("catalog fixture is missing");
      catalog.insertAdjacentHTML(
        "beforeend",
        `<article class="product-card">
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
        </article>`,
      );
    },
    { productSku: sku, productName: name, productPrice: price },
  );
}

test("refreshes observations after same-document SPA catalog navigation", async () => {
  const { context, page, worker, userDataDir } = await openPerekrestokFixture();

  try {
    expect(JSON.stringify(await storedObservations(worker))).toContain('"fulfillmentContextId":"656"');

    await page.evaluate(() => {
      history.pushState({}, "", "/cat/fixture-spa-second");
      document.querySelector(".catalog-content")?.replaceChildren();
    });

    await expect.poll(() => storedObservations(worker)).toEqual([]);

    await insertProduct(page, "5512345", "Новый SPA продукт", "249,50");

    await expect.poll(() => page.locator("html").getAttribute("data-zg-bridge-status")).toBe("ok");
    await expect.poll(() => storedObservations(worker)).toHaveLength(1);

    const serialized = JSON.stringify(await storedObservations(worker));
    expect(serialized).toContain('"fulfillmentContextId":"656"');
    expect(serialized).toContain('"sku":"5512345"');
    expect(serialized).toContain('"priceMinor":24950');
    expect(serialized).toContain('"sourceReference":"https://www.perekrestok.ru/cat/fixture-spa-second"');
    expect(serialized).not.toContain('"sku":"4408829"');
  } finally {
    await context.close();
    await rm(userDataDir, { recursive: true, force: true });
  }
});

test("invalidates old store observations before recollecting a new fulfillment context", async () => {
  const { context, page, worker, userDataDir } = await openPerekrestokFixture();

  try {
    expect(JSON.stringify(await storedObservations(worker))).toContain('"fulfillmentContextId":"656"');

    await page.evaluate(async () => {
      document.querySelector(".catalog-content")?.replaceChildren();
      await fetch("/api/customer/1.4.1.0/shop/777?session=SECRET_SECOND_CONTEXT#fragment");
    });

    await expect.poll(() => storedObservations(worker)).toEqual([]);

    await insertProduct(page, "6612345", "Продукт нового магазина", "349,90");

    await expect.poll(() => page.locator("html").getAttribute("data-zg-bridge-status")).toBe("ok");
    await expect.poll(() => storedObservations(worker)).toHaveLength(1);

    const serialized = JSON.stringify(await storedObservations(worker));
    expect(serialized).toContain('"fulfillmentContextId":"777"');
    expect(serialized).toContain('"sku":"6612345"');
    expect(serialized).toContain('"priceMinor":34990');
    expect(serialized).not.toContain('"fulfillmentContextId":"656"');
    expect(serialized).not.toContain('"sku":"4408829"');
    expect(serialized).not.toContain("SECRET_SECOND_CONTEXT");
  } finally {
    await context.close();
    await rm(userDataDir, { recursive: true, force: true });
  }
});

test("keeps only the new context when SPA navigation and store change overlap", async () => {
  const { context, page, worker, userDataDir } = await openPerekrestokFixture();

  try {
    expect(JSON.stringify(await storedObservations(worker))).toContain('"fulfillmentContextId":"656"');

    await page.evaluate(async () => {
      history.pushState({}, "", "/cat/fixture-spa-new-store");
      document.querySelector(".catalog-content")?.replaceChildren();
      await fetch("/api/customer/1.4.1.0/shop/777?session=SECRET_COMBINED_CONTEXT#fragment");
    });

    await expect.poll(() => storedObservations(worker)).toEqual([]);
    await insertProduct(page, "7712345", "SPA продукт нового магазина", "459,90");

    await expect.poll(() => page.locator("html").getAttribute("data-zg-bridge-status")).toBe("ok");
    await expect.poll(() => storedObservations(worker)).toHaveLength(1);

    const serialized = JSON.stringify(await storedObservations(worker));
    expect(serialized).toContain('"fulfillmentContextId":"777"');
    expect(serialized).toContain('"sku":"7712345"');
    expect(serialized).toContain('"sourceReference":"https://www.perekrestok.ru/cat/fixture-spa-new-store"');
    expect(serialized).not.toContain('"fulfillmentContextId":"656"');
    expect(serialized).not.toContain('"sku":"4408829"');
    expect(serialized).not.toContain("SECRET_COMBINED_CONTEXT");
  } finally {
    await context.close();
    await rm(userDataDir, { recursive: true, force: true });
  }
});
