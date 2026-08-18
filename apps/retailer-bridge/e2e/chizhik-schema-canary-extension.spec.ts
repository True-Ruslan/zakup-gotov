import { mkdtemp, rm } from "node:fs/promises";
import { tmpdir } from "node:os";
import { join, resolve } from "node:path";
import { chromium, expect, test } from "@playwright/test";

const bridgeRoot = resolve(process.cwd(), "../retailer-bridge");
const extensionPath = resolve(bridgeRoot, "dist");
const shopsEndpoint = "https://app.chizhik.club/api/v1/shops/";
const contextResource =
  "https://app.chizhik.club/delivery/api/catalog/v3/stores/HD87/categories/drinks/products";

const stores = [
  {
    id: 26504,
    sap_id: "HD87",
    lon: 37.83372708,
    lat: 55.76833314,
    status: 1,
    name: "Москва, Саянская ул., Дом 11Б",
    locality: "Москва",
  },
];

const secretPayload = {
  products: [
    {
      sku: "SECRET-SKU-123",
      name: "Secret product name",
      price: 12999,
      available: true,
      promotion: { label: "secret promo" },
    },
  ],
  requestId: "SECRET-REQUEST-ID",
};

test("runs the Chizhik D2 schema canary only on user invocation and renders sanitized evidence", async () => {
  const userDataDir = await mkdtemp(join(tmpdir(), "zg-retailer-bridge-canary-"));
  const context = await chromium.launchPersistentContext(userDataDir, {
    channel: "chromium",
    headless: true,
    args: [
      `--disable-extensions-except=${extensionPath}`,
      `--load-extension=${extensionPath}`,
    ],
  });

  let searchRequests = 0;
  try {
    await context.route("https://chizhik.club/**", (route) =>
      route.fulfill({
        status: 200,
        contentType: "text/html; charset=utf-8",
        body: "<!doctype html><html><body><main>Chizhik catalog fixture</main></body></html>",
      }),
    );
    await context.route(shopsEndpoint, (route) =>
      route.fulfill({
        status: 200,
        headers: {
          "access-control-allow-origin": "https://chizhik.club",
          "content-type": "application/json; charset=utf-8",
        },
        body: JSON.stringify(stores),
      }),
    );
    await context.route("https://app.chizhik.club/delivery/api/catalog/**", async (route) => {
      const url = new URL(route.request().url());
      const isCanarySearch = url.pathname.endsWith("/search") && url.searchParams.get("q") === "кола";
      if (isCanarySearch) searchRequests += 1;
      await route.fulfill({
        status: 200,
        headers: {
          "access-control-allow-origin": "https://chizhik.club",
          "content-type": "application/json; charset=utf-8",
        },
        body: JSON.stringify(isCanarySearch ? secretPayload : { products: [] }),
      });
    });

    const page = await context.newPage();
    await page.goto("https://chizhik.club/catalog/chay-kofe--264C39224/");
    await page.evaluate(async (resourceUrl) => {
      await fetch(resourceUrl, {
        method: "GET",
        mode: "cors",
        credentials: "same-origin",
        headers: { Accept: "application/json, text/plain, */*" },
      });
    }, contextResource);

    await expect
      .poll(() => page.locator("html").getAttribute("data-zg-bridge-status"))
      .toBe("observation-only");
    expect(searchRequests).toBe(0);

    const worker = context.serviceWorkers()[0] ?? (await context.waitForEvent("serviceworker"));
    const extensionId = new URL(worker.url()).host;
    const popup = await context.newPage();
    await popup.goto(`chrome-extension://${extensionId}/popup.html`);
    await page.bringToFront();

    await popup.getByRole("button", { name: "Run sanitized Chizhik canary" }).click();
    const evidence = popup.locator("#evidence");
    await expect(evidence).toContainText("CHIZHIK_D2 status=PASS search_http_status=200");
    await expect(evidence).toContainText('"products":"array"');
    await expect(evidence).toContainText('"sku":"string"');
    await expect(evidence).toContainText('"name":"string"');
    await expect(evidence).toContainText('"price":"number"');
    await expect(evidence).toContainText('"available":"boolean"');
    expect(searchRequests).toBe(1);

    const rendered = await evidence.textContent();
    expect(rendered).not.toContain("HD87");
    expect(rendered).not.toContain("SECRET-SKU-123");
    expect(rendered).not.toContain("Secret product name");
    expect(rendered).not.toContain("12999");
    expect(rendered).not.toContain("secret promo");
    expect(rendered).not.toContain("SECRET-REQUEST-ID");
  } finally {
    await context.close();
    await rm(userDataDir, { recursive: true, force: true });
  }
});
