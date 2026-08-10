# Perekrestok Browser Bridge Phase A Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build the first reproducible Chromium Manifest V3 browser-bridge path for Perekrestok that extracts one store-scoped product observation from first-party page state, normalizes it locally, proves browser credentials are not exported, and passes deterministic fixture plus Playwright extension tests.

**Architecture:** Add a new `apps/retailer-bridge` TypeScript workspace. A static Manifest V3 content script runs in Chrome's isolated world on Perekrestok pages, invokes a retailer-specific adapter against DOM/structured page state, then passes only normalized observations to a minimal extension service worker for local `chrome.storage.local` persistence. Phase A has no Zakup Gotov backend upload: proving local extraction, provenance, sanitization, extension lifecycle, and deterministic tests is the complete slice. Backend ingestion and the 20-item corpus are separate follow-up plans after Phase A live evidence passes.

**Tech Stack:** Node.js 24.18.1, pnpm 11.4.0, TypeScript 5.9.3, Vite 8.2.1, Vitest 4.1.10, jsdom 30.0.1, Playwright 1.62.1, Chromium Manifest V3.

## Global Constraints

- All executable behavior follows RED -> GREEN -> REFACTOR.
- Ordinary CI must never call live Perekrestok services.
- The first-party user performs login, location/store selection, and CAPTCHA manually when required.
- Do not export browser cookies, local/session storage values, authorization tokens, CAPTCHA artifacts, or precise street addresses.
- Do not add `cookies`, `webRequest`, `debugger`, proxy-control, or equivalent permissions to the extension.
- Content scripts use Manifest V3 `content_scripts` and the default isolated execution world.
- Raw full-page HTML is not sent to the service worker/backend and is not committed as a fixture.
- `sourceReference` strips query strings/fragments before persistence.
- Perekrestok price values are normalized as integer minor units (`priceMinor`), matching observed API evidence such as `8999` for 89.99 RUB.
- Phase A supports one Perekrestok observation path only; it does not claim the retailer `AVAILABLE_BROWSER_BRIDGE` until a real first-party page has produced sanitized live evidence.

---

## File Structure

Create a focused workspace rather than adding retailer-specific code to `apps/web`:

```text
apps/retailer-bridge/
├── package.json
├── tsconfig.json
├── vite.config.ts
├── vitest.config.ts
├── playwright.config.ts
├── scripts/
│   └── build.mjs
├── static/
│   ├── manifest.json
│   ├── manifest.e2e.json
│   └── service-worker.js
├── src/
│   ├── content.ts
│   ├── model/
│   │   └── browser-observation.ts
│   ├── collector/
│   │   └── browser-observation-collector.ts
│   └── adapters/
│       ├── retailer-browser-adapter.ts
│       └── perekrestok-browser-adapter.ts
├── test/
│   ├── fixtures/
│   │   ├── perekrestok-product-state.html
│   │   ├── perekrestok-missing-context.html
│   │   └── perekrestok-malformed-state.html
│   ├── manifest-security.test.ts
│   ├── browser-observation-collector.test.ts
│   └── perekrestok-browser-adapter.test.ts
└── e2e/
    ├── fixture-server.mjs
    └── perekrestok-extension.spec.ts
```

Repository integration files:

```text
pnpm-workspace.yaml
pnpm-lock.yaml
scripts/verify.sh
.github/workflows/retailer-bridge-ci.yml
docs/DEVELOPMENT.md
docs/PROJECT_STATE.md
CHANGELOG.md
```

---

### Task 1: Add the minimal Manifest V3 workspace and permission contract

**Files:**
- Modify: `pnpm-workspace.yaml`
- Create: `apps/retailer-bridge/package.json`
- Create: `apps/retailer-bridge/tsconfig.json`
- Create: `apps/retailer-bridge/vitest.config.ts`
- Create: `apps/retailer-bridge/vite.config.ts`
- Create: `apps/retailer-bridge/scripts/build.mjs`
- Create: `apps/retailer-bridge/static/manifest.json`
- Create: `apps/retailer-bridge/static/manifest.e2e.json`
- Create: `apps/retailer-bridge/static/service-worker.js`
- Test: `apps/retailer-bridge/test/manifest-security.test.ts`
- Modify: `pnpm-lock.yaml`

**Interfaces:**
- Produces package name: `@zakup-gotov/retailer-bridge`.
- Produces extension output directories: `apps/retailer-bridge/dist` and `apps/retailer-bridge/dist-e2e`.
- Production manifest may request only `storage` permission and Perekrestok content-script matching.

- [ ] **Step 1: Write the failing manifest security test**

```ts
import { readFileSync } from "node:fs";
import { resolve } from "node:path";
import { describe, expect, it } from "vitest";

const manifestPath = resolve(import.meta.dirname, "../static/manifest.json");
const manifest = JSON.parse(readFileSync(manifestPath, "utf8"));

describe("retailer bridge manifest", () => {
  it("uses Manifest V3 with the minimum production permissions", () => {
    expect(manifest.manifest_version).toBe(3);
    expect(manifest.permissions).toEqual(["storage"]);
    expect(manifest.host_permissions ?? []).toEqual([]);
    expect(manifest.content_scripts).toEqual([
      expect.objectContaining({
        matches: ["https://www.perekrestok.ru/*"],
        js: ["content.js"],
        run_at: "document_idle",
        world: "ISOLATED",
      }),
    ]);
    expect(JSON.stringify(manifest)).not.toMatch(
      /cookies|webRequest|debugger|proxy|declarativeNetRequest/i,
    );
  });
});
```

- [ ] **Step 2: Run the focused test and confirm RED**

Run:

```bash
pnpm --dir apps/retailer-bridge test -- manifest-security.test.ts
```

Expected: failure because the workspace/manifest does not exist yet.

- [ ] **Step 3: Create the workspace and build contract**

`apps/retailer-bridge/package.json`:

```json
{
  "name": "@zakup-gotov/retailer-bridge",
  "version": "0.1.0",
  "private": true,
  "type": "module",
  "scripts": {
    "build": "node scripts/build.mjs",
    "build:e2e": "node scripts/build.mjs --e2e",
    "typecheck": "tsc --noEmit",
    "test": "vitest run",
    "test:e2e": "playwright test"
  },
  "devDependencies": {
    "@playwright/test": "1.62.1",
    "@types/node": "20.19.43",
    "jsdom": "30.0.1",
    "typescript": "5.9.3",
    "vite": "8.2.1",
    "vitest": "4.1.10"
  }
}
```

`pnpm-workspace.yaml` must include:

```yaml
packages:
  - apps/web
  - apps/retailer-bridge
  - packages/*
```

`static/manifest.json`:

```json
{
  "manifest_version": 3,
  "name": "Zakup Gotov Retailer Bridge",
  "version": "0.1.0",
  "description": "Reads sanitized grocery offer observations from supported first-party retailer pages.",
  "permissions": ["storage"],
  "background": {
    "service_worker": "service-worker.js"
  },
  "content_scripts": [
    {
      "matches": ["https://www.perekrestok.ru/*"],
      "js": ["content.js"],
      "run_at": "document_idle",
      "world": "ISOLATED"
    }
  ]
}
```

`static/manifest.e2e.json` is identical except `matches` also includes `http://127.0.0.1:4174/*`.

`static/service-worker.js` must only store already-sanitized observations:

```js
chrome.runtime.onMessage.addListener((message) => {
  if (message?.type !== "ZG_STORE_OBSERVATIONS" || !Array.isArray(message.observations)) {
    return;
  }
  return chrome.storage.local.set({
    "zg.latestObservations": message.observations,
  });
});
```

`vite.config.ts` builds one bundled IIFE content script:

```ts
import { resolve } from "node:path";
import { defineConfig } from "vite";

export default defineConfig({
  build: {
    lib: {
      entry: resolve(import.meta.dirname, "src/content.ts"),
      name: "ZakupGotovRetailerBridge",
      formats: ["iife"],
      fileName: () => "content.js",
    },
    target: "es2022",
    sourcemap: true,
    minify: false,
  },
});
```

`scripts/build.mjs` invokes Vite and copies only the selected static manifest plus service worker:

```js
import { cp, mkdir, rm } from "node:fs/promises";
import { resolve } from "node:path";
import { build } from "vite";

const e2e = process.argv.includes("--e2e");
const root = resolve(import.meta.dirname, "..");
const outDir = resolve(root, e2e ? "dist-e2e" : "dist");

await rm(outDir, { recursive: true, force: true });
await build({
  root,
  configFile: resolve(root, "vite.config.ts"),
  build: { outDir, emptyOutDir: true },
});
await mkdir(outDir, { recursive: true });
await cp(resolve(root, e2e ? "static/manifest.e2e.json" : "static/manifest.json"), resolve(outDir, "manifest.json"));
await cp(resolve(root, "static/service-worker.js"), resolve(outDir, "service-worker.js"));
```

- [ ] **Step 4: Regenerate the lockfile and run GREEN**

Run:

```bash
pnpm install
pnpm --filter @zakup-gotov/retailer-bridge test -- manifest-security.test.ts
pnpm --filter @zakup-gotov/retailer-bridge typecheck
pnpm --filter @zakup-gotov/retailer-bridge build
```

Expected: manifest test PASS; TypeScript PASS; `dist/manifest.json`, `dist/content.js`, and `dist/service-worker.js` exist.

- [ ] **Step 5: Commit**

```bash
git add pnpm-workspace.yaml pnpm-lock.yaml apps/retailer-bridge
git commit -m "feat(bridge): add minimal Chromium extension workspace"
```

---

### Task 2: Define the normalized browser observation and fail-closed collector

**Files:**
- Create: `apps/retailer-bridge/src/model/browser-observation.ts`
- Create: `apps/retailer-bridge/src/adapters/retailer-browser-adapter.ts`
- Create: `apps/retailer-bridge/src/collector/browser-observation-collector.ts`
- Test: `apps/retailer-bridge/test/browser-observation-collector.test.ts`

**Interfaces:**
- Produces `BrowserObservation`.
- Produces `RetailerBrowserAdapter.collect(input): AdapterResult`.
- Produces `BrowserObservationCollector.collect(document, url, observedAt): Promise<CollectorResult>`.
- Collector output is reconstructed from an allow-list; arbitrary adapter fields are never forwarded.

- [ ] **Step 1: Write RED tests for provenance, sanitization and canonical source URLs**

```ts
import { JSDOM } from "jsdom";
import { describe, expect, it, vi } from "vitest";
import { BrowserObservationCollector } from "../src/collector/browser-observation-collector";

it("persists only allowed normalized observation fields", async () => {
  const dom = new JSDOM("<html></html>", { url: "https://www.perekrestok.ru/cat/1?token=secret#x" });
  const sink = vi.fn().mockResolvedValue(undefined);
  const adapter = {
    adapterId: "fixture",
    retailerId: "perekrestok",
    supports: () => true,
    collect: () => ({
      status: "ok",
      observations: [{
        schemaVersion: 1,
        retailerId: "perekrestok",
        sourceProviderId: "perekrestok-browser",
        sourceMode: "BROWSER_BRIDGE",
        fulfillmentContextId: "shop-1",
        sku: "3431579",
        productName: "Молоко",
        priceMinor: 8999,
        currencyCode: "RUB",
        availability: "AVAILABLE",
        observedAt: "2026-08-10T11:00:00Z",
        sourceReference: "https://www.perekrestok.ru/cat/1?token=secret#x",
        adapterVersion: "1",
        cookie: "SECRET_COOKIE"
      }]
    })
  } as never;

  const collector = new BrowserObservationCollector([adapter], sink);
  await collector.collect(dom.window.document, new URL(dom.window.location.href), "2026-08-10T11:00:00Z");

  const stored = sink.mock.calls[0][0];
  expect(stored[0]).not.toHaveProperty("cookie");
  expect(stored[0].sourceReference).toBe("https://www.perekrestok.ru/cat/1");
});
```

Add cases for no matching adapter, missing context, invalid price, invalid timestamp, and adapter returning zero observations.

- [ ] **Step 2: Run RED**

```bash
pnpm --filter @zakup-gotov/retailer-bridge test -- browser-observation-collector.test.ts
```

Expected: compile/test failure because the model and collector do not exist.

- [ ] **Step 3: Implement the minimal types and projection**

`browser-observation.ts`:

```ts
export type Availability = "AVAILABLE" | "UNAVAILABLE" | "UNKNOWN";

export type BrowserObservation = Readonly<{
  schemaVersion: 1;
  retailerId: string;
  sourceProviderId: string;
  sourceMode: "BROWSER_BRIDGE";
  fulfillmentContextId: string;
  sku: string;
  productName: string;
  priceMinor: number;
  currencyCode: "RUB";
  availability: Availability;
  observedAt: string;
  sourceReference: string;
  adapterVersion: string;
}>;
```

`retailer-browser-adapter.ts`:

```ts
import type { BrowserObservation } from "../model/browser-observation";

export type AdapterResult =
  | { status: "ok"; observations: BrowserObservation[] }
  | { status: "unsupported-page" | "missing-context" | "missing-product" | "malformed-state"; observations: [] };

export interface RetailerBrowserAdapter {
  readonly adapterId: string;
  readonly retailerId: string;
  supports(url: URL): boolean;
  collect(input: { document: Document; url: URL; observedAt: string }): AdapterResult;
}
```

Collector must construct a brand-new object for every observation, validate nonblank IDs/SKU/name/context, integer nonnegative `priceMinor`, valid ISO timestamp, and canonicalize `sourceReference` to `origin + pathname`.

- [ ] **Step 4: Run GREEN**

```bash
pnpm --filter @zakup-gotov/retailer-bridge test -- browser-observation-collector.test.ts
pnpm --filter @zakup-gotov/retailer-bridge typecheck
```

Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add apps/retailer-bridge/src apps/retailer-bridge/test/browser-observation-collector.test.ts
git commit -m "feat(bridge): add normalized browser observation boundary"
```

---

### Task 3: Implement the Perekrestok structured-state adapter

**Files:**
- Create: `apps/retailer-bridge/src/adapters/perekrestok-browser-adapter.ts`
- Create: `apps/retailer-bridge/test/fixtures/perekrestok-product-state.html`
- Create: `apps/retailer-bridge/test/fixtures/perekrestok-missing-context.html`
- Create: `apps/retailer-bridge/test/fixtures/perekrestok-malformed-state.html`
- Test: `apps/retailer-bridge/test/perekrestok-browser-adapter.test.ts`

**Interfaces:**
- Consumes `RetailerBrowserAdapter` and `BrowserObservation`.
- Produces `perekrestokBrowserAdapter` with `adapterId = "perekrestok-browser-v1"`.
- Extraction priority: parse JSON scripts only; do not eval arbitrary inline JavaScript.

Reference evidence to preserve in the sanitized fixture shape:

```json
{
  "shop": { "id": "shop-moscow-001" },
  "content": {
    "products": [
      {
        "masterData": { "plu": "3431579" },
        "title": "Молоко питьевое ультрапастеризованное 3.2%, 970мл",
        "priceTag": { "price": 8999 },
        "balanceState": "many"
      }
    ]
  }
}
```

The `masterData.plu`, `priceTag.price`, and `balanceState` shape is grounded in the current Open-Inflation Perekrestok snapshot; the fixture uses invented store/product labels where needed and contains no cookie/session/address data.

- [ ] **Step 1: Write RED tests for one product, price and availability**

```ts
it("extracts one store-scoped product from structured page state", () => {
  const result = adapter.collect({ document, url, observedAt: "2026-08-10T11:00:00Z" });
  expect(result).toEqual({
    status: "ok",
    observations: [expect.objectContaining({
      retailerId: "perekrestok",
      sourceProviderId: "perekrestok-browser",
      fulfillmentContextId: "shop-moscow-001",
      sku: "3431579",
      priceMinor: 8999,
      currencyCode: "RUB",
      availability: "AVAILABLE",
      adapterVersion: "1"
    })]
  });
});
```

Add tests proving:
- `balanceState: "none"` -> `UNAVAILABLE`;
- unknown balance values -> `UNKNOWN`;
- no unique shop context -> `missing-context`;
- malformed JSON is ignored and produces `malformed-state` only when no valid structured source remains;
- missing/negative/noninteger price -> `missing-product` rather than a fabricated observation;
- query/hash are not part of `sourceReference` after collector normalization.

- [ ] **Step 2: Run RED**

```bash
pnpm --filter @zakup-gotov/retailer-bridge test -- perekrestok-browser-adapter.test.ts
```

Expected: failure because the adapter does not exist.

- [ ] **Step 3: Implement recursive JSON extraction**

Implementation rules:

```ts
const PRODUCT_KEYS = ["masterData", "priceTag", "balanceState"] as const;
const CONTEXT_KEYS = ["selectedShopId", "shopId"] as const;
```

Algorithm:
1. accept only `https://www.perekrestok.ru/` URLs in production;
2. parse `script[type="application/json"]`, `script#__NEXT_DATA__`, and `script[type="application/ld+json"]` with `JSON.parse` only;
3. recursively visit objects/arrays;
4. recognize Perekrestok product objects only when `masterData.plu`, `title`, and integer `priceTag.price` are present;
5. find context candidates from `selectedShopId`, `shopId`, or an object named `shop` with scalar `id`;
6. require exactly one unique context ID;
7. map known balance states; preserve unknown as `UNKNOWN`;
8. never inspect `document.cookie`, `localStorage`, `sessionStorage`, or browser request headers.

- [ ] **Step 4: Run GREEN and full unit suite**

```bash
pnpm --filter @zakup-gotov/retailer-bridge test
pnpm --filter @zakup-gotov/retailer-bridge typecheck
```

Expected: all bridge unit/fixture tests PASS.

- [ ] **Step 5: Commit**

```bash
git add apps/retailer-bridge/src/adapters apps/retailer-bridge/test
git commit -m "feat(bridge): parse Perekrestok structured product state"
```

---

### Task 4: Wire the content script to the local extension storage boundary

**Files:**
- Create: `apps/retailer-bridge/src/content.ts`
- Test: extend `apps/retailer-bridge/test/browser-observation-collector.test.ts`

**Interfaces:**
- Content script invokes `BrowserObservationCollector` with `perekrestokBrowserAdapter`.
- Sink sends `{ type: "ZG_STORE_OBSERVATIONS", observations }` through `chrome.runtime.sendMessage`.
- Page-visible diagnostics contain status/count only, never observation payloads.

- [ ] **Step 1: Write RED tests around the message sink**

Define a small injectable `ObservationSink` in the collector and test that the Chrome sink receives only normalized observations. Add a regression payload containing `cookie`, `authorization`, `localStorage`, and `address` extra properties and assert none reach the sink.

- [ ] **Step 2: Run RED**

```bash
pnpm --filter @zakup-gotov/retailer-bridge test -- browser-observation-collector.test.ts
```

Expected: failure until the sink/wiring exists.

- [ ] **Step 3: Implement content startup**

`content.ts`:

```ts
import { BrowserObservationCollector } from "./collector/browser-observation-collector";
import { perekrestokBrowserAdapter } from "./adapters/perekrestok-browser-adapter";

const sink = async (observations: unknown[]) => {
  await chrome.runtime.sendMessage({ type: "ZG_STORE_OBSERVATIONS", observations });
};

const collector = new BrowserObservationCollector([perekrestokBrowserAdapter], sink);
const result = await collector.collect(document, new URL(location.href), new Date().toISOString());

document.documentElement.dataset.zgBridgeStatus = result.status;
document.documentElement.dataset.zgBridgeCount = String(result.observationCount);
```

Add a minimal local `chrome` ambient declaration covering `runtime.sendMessage`; do not add cookie/storage-reading APIs to that declaration.

- [ ] **Step 4: Run GREEN and build**

```bash
pnpm --filter @zakup-gotov/retailer-bridge test
pnpm --filter @zakup-gotov/retailer-bridge typecheck
pnpm --filter @zakup-gotov/retailer-bridge build
```

Expected: PASS and bundled `dist/content.js`.

- [ ] **Step 5: Commit**

```bash
git add apps/retailer-bridge/src apps/retailer-bridge/test
git commit -m "feat(bridge): collect sanitized Perekrestok observations"
```

---

### Task 5: Prove the extension in real Chromium against local fixtures

**Files:**
- Create: `apps/retailer-bridge/playwright.config.ts`
- Create: `apps/retailer-bridge/e2e/fixture-server.mjs`
- Create: `apps/retailer-bridge/e2e/perekrestok-extension.spec.ts`

**Interfaces:**
- Uses `dist-e2e` with the E2E-only localhost match.
- Uses Playwright bundled Chromium with a persistent context, because MV3 extension testing requires a persistent Chromium context.
- Reads the stored normalized observation through the extension service worker.

- [ ] **Step 1: Write the E2E test before enabling it in CI**

Core test:

```ts
test("stores a sanitized Perekrestok observation without page credentials", async () => {
  const page = await context.newPage();
  await page.goto("http://127.0.0.1:4174/perekrestok-product-state.html");
  await page.evaluate(() => {
    document.cookie = "session=SECRET_COOKIE";
    localStorage.setItem("auth", "SECRET_LOCAL_STORAGE");
  });
  await page.reload();

  await expect(page.locator("html")).toHaveAttribute("data-zg-bridge-status", "ok");
  await expect(page.locator("html")).toHaveAttribute("data-zg-bridge-count", "1");

  const worker = context.serviceWorkers()[0] ?? await context.waitForEvent("serviceworker");
  const stored = await worker.evaluate(async () => {
    return chrome.storage.local.get("zg.latestObservations");
  });

  const serialized = JSON.stringify(stored);
  expect(serialized).toContain("3431579");
  expect(serialized).toContain("8999");
  expect(serialized).not.toContain("SECRET_COOKIE");
  expect(serialized).not.toContain("SECRET_LOCAL_STORAGE");
});
```

Add an unsupported/missing-context fixture test proving no observation is persisted and the page diagnostic reports `missing-context`.

- [ ] **Step 2: Build E2E extension and run RED**

```bash
pnpm --filter @zakup-gotov/retailer-bridge build:e2e
pnpm --filter @zakup-gotov/retailer-bridge exec playwright install chromium
pnpm --filter @zakup-gotov/retailer-bridge test:e2e
```

Expected on first run: failure until Playwright fixture/bootstrap and the E2E manifest path are correctly wired.

- [ ] **Step 3: Implement the Playwright persistent-context fixture**

Follow the official Playwright extension pattern:

```ts
const context = await chromium.launchPersistentContext("", {
  channel: "chromium",
  args: [
    `--disable-extensions-except=${extensionPath}`,
    `--load-extension=${extensionPath}`,
  ],
});
```

The fixture server serves only committed sanitized HTML from `test/fixtures` on `127.0.0.1:4174`.

- [ ] **Step 4: Run GREEN repeatedly**

```bash
pnpm --filter @zakup-gotov/retailer-bridge test:e2e
pnpm --filter @zakup-gotov/retailer-bridge test:e2e
```

Expected: both runs PASS with no live retailer traffic.

- [ ] **Step 5: Commit**

```bash
git add apps/retailer-bridge/e2e apps/retailer-bridge/playwright.config.ts apps/retailer-bridge/static/manifest.e2e.json
git commit -m "test(bridge): verify MV3 Perekrestok collection in Chromium"
```

---

### Task 6: Add deterministic repository verification and CI

**Files:**
- Create: `.github/workflows/retailer-bridge-ci.yml`
- Modify: `scripts/verify.sh`
- Modify: `docs/DEVELOPMENT.md`

**Interfaces:**
- New workflow/check name: `Retailer Bridge CI`.
- Ordinary PR/main CI performs no live Perekrestok calls.

- [ ] **Step 1: Add the verification commands to `scripts/verify.sh`**

After frozen workspace install, add:

```bash
echo "==> Retailer bridge typecheck/tests/build"
pnpm --filter @zakup-gotov/retailer-bridge typecheck
pnpm --filter @zakup-gotov/retailer-bridge test
pnpm --filter @zakup-gotov/retailer-bridge build
```

Do not add extension E2E to `scripts/verify.sh`; it requires Playwright Chromium installation and belongs in its dedicated CI job.

- [ ] **Step 2: Create read-only bridge CI**

Workflow shape:

```yaml
name: Retailer Bridge CI
on:
  pull_request:
  push:
    branches: [main]
permissions:
  contents: read
concurrency:
  group: ${{ github.workflow }}-${{ github.event.pull_request.number || github.ref }}
  cancel-in-progress: true
jobs:
  verify:
    runs-on: ubuntu-latest
    timeout-minutes: 20
    steps:
      - uses: actions/checkout@3d3c42e5aac5ba805825da76410c181273ba90b1
        with:
          persist-credentials: false
      - uses: actions/setup-node@820762786026740c76f36085b0efc47a31fe5020
        with:
          node-version-file: .nvmrc
      - run: npm install --global pnpm@11.4.0
      - run: pnpm install --frozen-lockfile
      - run: pnpm --filter @zakup-gotov/retailer-bridge typecheck
      - run: pnpm --filter @zakup-gotov/retailer-bridge test
      - run: pnpm --filter @zakup-gotov/retailer-bridge build:e2e
      - run: pnpm --filter @zakup-gotov/retailer-bridge exec playwright install --with-deps chromium
      - run: pnpm --filter @zakup-gotov/retailer-bridge test:e2e
```

- [ ] **Step 3: Document local commands**

Add to `docs/DEVELOPMENT.md`:

```bash
pnpm --filter @zakup-gotov/retailer-bridge typecheck
pnpm --filter @zakup-gotov/retailer-bridge test
pnpm --filter @zakup-gotov/retailer-bridge build
pnpm --filter @zakup-gotov/retailer-bridge build:e2e
pnpm --filter @zakup-gotov/retailer-bridge exec playwright install chromium
pnpm --filter @zakup-gotov/retailer-bridge test:e2e
```

Also document loading `apps/retailer-bridge/dist` through `chrome://extensions` -> Developer mode -> Load unpacked for the opt-in live Phase A check.

- [ ] **Step 4: Run the complete deterministic gate**

```bash
pnpm install --frozen-lockfile
pnpm --filter @zakup-gotov/retailer-bridge typecheck
pnpm --filter @zakup-gotov/retailer-bridge test
pnpm --filter @zakup-gotov/retailer-bridge build:e2e
pnpm --filter @zakup-gotov/retailer-bridge test:e2e
./scripts/verify.sh
```

Expected: all commands PASS; no network access to Perekrestok is required.

- [ ] **Step 5: Commit**

```bash
git add .github/workflows/retailer-bridge-ci.yml scripts/verify.sh docs/DEVELOPMENT.md
git commit -m "ci(bridge): verify retailer extension deterministically"
```

---

### Task 7: Record Phase A status and execute the opt-in first-party live gate

**Files:**
- Modify: `docs/PROJECT_STATE.md`
- Modify: `CHANGELOG.md`
- Create after successful local capture: `docs/integrations/perekrestok-browser-bridge-phase-a.md`

**Interfaces:**
- No live check in GitHub Actions.
- Live evidence records booleans/IDs needed for reproducibility, not session values, raw HTML, or exact address.

- [ ] **Step 1: Synchronize deterministic implementation state before live testing**

`PROJECT_STATE.md` must say exactly:

- extension workspace implemented;
- deterministic fixture/unit/E2E status;
- Perekrestok browser path remains `LIVE_PENDING` until tested in a real first-party session;
- direct HTTP remains `store-403` evidence but no longer determines retailer product scope.

`CHANGELOG.md` records the new Manifest V3 browser-bridge architecture and deterministic Perekrestok adapter tests.

- [ ] **Step 2: Run all repository PR gates and review the diff**

Required successful workflows on the exact head:

- API CI;
- Contract CI;
- Web CI + Web E2E;
- Retailer Bridge CI;
- CodeQL;
- Dependency Review;
- Release Bundle CI;
- Release Contract CI;
- Container Security CI.

Review specifically for:

```text
no cookies permission
no webRequest/debugger/proxy permission
no document.cookie reads
no localStorage/sessionStorage reads
no raw HTML persistence
no precise address fixture
no live retailer request in deterministic CI
```

- [ ] **Step 3: Merge the implementation PR only after the full gate passes**

Use squash merge with exact expected head SHA.

- [ ] **Step 4: Perform the first-party manual live gate**

On the user's Chromium/Chrome profile:

```text
1. Build: pnpm --filter @zakup-gotov/retailer-bridge build
2. Open chrome://extensions.
3. Enable Developer mode.
4. Load unpacked: apps/retailer-bridge/dist.
5. Open the official Perekrestok site in the user's normal first-party session.
6. Manually choose the intended store/location and complete any login/CAPTCHA required by the site.
7. Open a product/search page with a visible current price.
8. Confirm the page gets data-zg-bridge-status="ok" and count >= 1.
9. Inspect the extension service worker storage and record only the normalized observation fields.
```

The live result passes only if the observation contains retailer ID, explicit fulfillment context, SKU, current price/currency, availability/UNKNOWN, observedAt, canonical sourceReference, and adapterVersion, with no cookie/token/storage/address material.

- [ ] **Step 5: Capture a sanitized real fixture and rerun offline tests**

Create `docs/integrations/perekrestok-browser-bridge-phase-a.md` with:

```text
Live date/time
Page type
Adapter version
Store-context present: true/false
SKU present: true/false
Price present: true/false
Availability semantic: AVAILABLE/UNAVAILABLE/UNKNOWN
Credential export observed: false
Raw HTML persisted: false
Result: PASS/FAIL
```

If live structured state differs from the synthetic fixture, capture only the minimal product/context JSON subtree needed for a sanitized regression fixture, write a failing regression test first, then adapt the parser and rerun the complete bridge suite.

- [ ] **Step 6: Commit the evidence update**

```bash
git add docs/integrations/perekrestok-browser-bridge-phase-a.md docs/PROJECT_STATE.md CHANGELOG.md apps/retailer-bridge/test/fixtures apps/retailer-bridge/test
git commit -m "docs(integrations): record Perekrestok browser bridge Phase A"
```

---

## Phase A Exit Criteria

Phase A is complete only when all of the following are true:

1. production Manifest V3 requests only `storage` permission;
2. content script runs in `ISOLATED` world on Perekrestok pages;
3. one explicit first-party store/fulfillment context is observed;
4. at least one stable Perekrestok PLU/SKU is observed;
5. current price is normalized as integer minor units + `RUB`;
6. availability is explicit or `UNKNOWN`, never inferred as available merely from catalog presence;
7. source URL is canonicalized without query/hash;
8. no cookie/token/localStorage/sessionStorage/address/raw HTML reaches extension storage or fixtures;
9. unit/fixture tests pass;
10. Playwright persistent-Chromium MV3 E2E passes twice consecutively;
11. complete repository CI/security gates pass on the exact merged head;
12. one real first-party Perekrestok browser session produces sanitized live evidence and a replayable sanitized fixture.

## Explicit Non-Goals for This Plan

- no automated CAPTCHA solving;
- no fingerprint/device spoofing;
- no token/cookie export or replay;
- no proxy/IP rotation;
- no server-side Perekrestok scraping retry after the existing `store-403` result;
- no backend ingestion endpoint yet;
- no basket comparison yet;
- no 20-item corpus yet;
- no Pyaterochka adapter yet.

Those last four product tasks begin only after this browser-bridge transport boundary is proven.

## Follow-up Plans After PASS

1. Perekrestok Browser Bridge 20-item corpus + real sanitized fixtures.
2. Pyaterochka Browser Bridge using the same observation/collector contract.
3. Backend universal provenance evolution (`retailerId`, `sourceProviderId`, `sourceMode`) and ingestion contract.
4. RetailerRegistry orchestration and coverage-state persistence.
5. Additional mandatory networks using the same adapter onboarding contract.
