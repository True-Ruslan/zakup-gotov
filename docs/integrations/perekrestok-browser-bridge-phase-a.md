# Perekrestok Browser Bridge Phase A

Updated: 2026-08-10
Status: `V2_DETERMINISTIC_READY_RETEST_PENDING`
Tracking: issue #47 / issue #52 / PR #49 / PR #53

## Purpose

Prove the user-assisted first-party browser transport for a mandatory retailer after the direct anonymous/ordinary-cookie Perekrestok API path returned `store-403`.

This phase does **not** claim that Perekrestok is connected in production. PR #49 proved the extension/privacy boundary. The first real browser gate then exposed a current-site adapter mismatch, and PR #53 provides the deterministic v2 adaptation. A repeated real first-party browser PASS is still required before the path can advance from `LIVE_PENDING`.

## Implemented transport

`apps/retailer-bridge` is a Chromium Manifest V3 extension package.

Production manifest properties:

- Manifest V3;
- `storage` is the only extension permission;
- no `host_permissions`;
- content script matches `https://www.perekrestok.ru/*`;
- content script runs at `document_idle` in the default isolated execution world;
- no `cookies`, `webRequest`, `debugger`, proxy-control or declarative-network-request permissions.

The user remains responsible for any first-party login, store selection or CAPTCHA interaction required by Perekrestok. The extension does not automate those access-control steps.

## Observation boundary

The browser collector persists only the allow-listed normalized fields:

- schema version;
- retailer ID;
- source provider ID;
- source mode `BROWSER_BRIDGE`;
- fulfillment/store context ID;
- SKU/PLU;
- product name;
- integer minor-unit price;
- `RUB` currency;
- `AVAILABLE`, `UNAVAILABLE`, or `UNKNOWN` availability;
- observation timestamp;
- canonical source URL without query/hash;
- adapter version.

Adapter output is treated as untrusted. The collector reconstructs a new normalized object before it reaches extension storage. Extra adapter fields such as cookies, authorization material or addresses are discarded.

On any fail-closed page state, the content script replaces `zg.latestObservations` with an empty array so a previous store/page cannot leave stale prices visible as current observations.

## Perekrestok adapter v2

Adapter v2 preserves the original structured-state path for pages where that evidence still exists and adds the current catalog path observed in the first real browser gate.

### Structured-state path

The adapter still parses, without `eval`:

- `script[type="application/json"]`;
- `script#__NEXT_DATA__`;
- `script[type="application/ld+json"]`.

The legacy evidence shape remains supported:

- `masterData.plu` — stable product identity;
- `priceTag.price` — integer minor-unit price;
- `balanceState` — availability evidence;
- `selectedShopId`, `shopId`, or `shop.id` — fulfillment/store context candidates.

### Current catalog DOM path

The 2026-08-10 live page no longer exposed the required product/store evidence in those structured scripts. It did expose stable semantic catalog DOM and a first-party shop resource pathname.

When structured-state products are absent, v2 can read:

- `.product-card` — product-card boundary;
- `.product-card__title-link` / `.product-card__title` — product name and product href;
- numeric product-link suffix — stable SKU candidate;
- `.product-card__price .price-new` / `.price-new` — visible current price;
- same-origin `/api/customer/<version>/shop/<numeric-id>` resource pathname — fulfillment context.

Visible RUB price text is normalized to integer minor units. DOM catalog presence alone does **not** prove availability, so this path emits `UNKNOWN` unless an explicit supported availability semantic is later proven.

Only same-origin canonical resource `origin + pathname` values are passed to the adapter. Query strings and fragments are removed before runtime resource evidence crosses the content-script boundary. No response body or request headers are read.

## Asynchronous SPA handling

The original Phase A implementation collected once at `document_idle`. TDD regressions against the sanitized live shape proved two independent timing races:

1. the first-party shop request can appear after the initial collection;
2. the shop context can already be known while `.product-card` elements are rendered later by the SPA.

The v2 content runtime therefore:

- seeds already-completed resource entries;
- observes new `resource` performance entries;
- retains only same-origin canonical resource paths;
- observes DOM child-list changes through `MutationObserver`;
- recollects when either new first-party resource evidence or later DOM evidence appears;
- serializes overlapping collection attempts;
- disconnects both observers after the first successful collection;
- keeps fail-closed stale-observation clearing before success.

No arbitrary sleep, polling interval, or unbounded retry loop is used.

## First real browser gate — 2026-08-10

The first live check was performed on an official Perekrestok category/catalog page.

Result for adapter v1:

- content script executed: yes;
- status: `missing-context`;
- observation count: `0`;
- result: **FAIL**.

Sanitized diagnostics established:

- 2 structured JSON scripts parsed successfully;
- 0 v1 `masterData` + `priceTag` product objects;
- no usable store context in those scripts;
- `cart-store` / `orderStore` storage entries were cart/order state, not fulfillment context;
- 101 stable `.product-card` elements were rendered;
- same-origin runtime resources included the current `/api/customer/.../shop/<numeric-id>` shape.

Detailed sanitized evidence: [`perekrestok-browser-bridge-live-2026-08-10.md`](perekrestok-browser-bridge-live-2026-08-10.md).

## Deterministic TDD evidence

### Phase A v1

PR #49 established:

1. manifest permission contract RED -> GREEN;
2. browser observation collector RED -> GREEN;
3. structured-state adapter RED -> GREEN;
4. Chrome observation sink RED -> GREEN;
5. Chromium stale-observation regression RED -> GREEN.

At its merge point:

- bridge unit/fixture suite: 15 tests PASS;
- bridge TypeScript: PASS;
- production extension build: PASS;
- persistent-Chromium MV3 E2E: PASS.

### Live adaptation v2

PR #53 adds four explicit test-first gates:

1. **Current live shape RED:** 15 old tests PASS, one new DOM/resource test FAIL with `missing-context`; then adapter parsing GREEN.
2. **Asynchronous resource RED:** 16 unit tests/type/build PASS and original E2E PASS, while the new live-shape E2E FAILed with `missing-context`; resource-driven recollection then made the tested path GREEN.
3. **Provenance RED:** tests required adapter version `2` before production metadata changed; the suite then returned GREEN after v2 provenance was applied.
4. **Delayed DOM RED:** 16 unit tests/type/build PASS and original E2E PASS, while the new delayed-card E2E FAILed with `missing-product`; DOM-driven recollection then made the full live-shape E2E GREEN.

The final live-shape E2E deliberately places `SECRET_RESOURCE_QUERY` in the first-party shop-resource query string and verifies that neither the sentinel nor `session=` reaches extension storage.

The live-shape fixtures are synthetic/sanitized. Ordinary CI intercepts the Perekrestok origin before external network access and therefore has zero live retailer dependency.

## Security/privacy boundary

The v2 adaptation does not expand production permissions.

It does not export or persist:

- cookies;
- authorization headers or tokens;
- CAPTCHA data;
- raw response bodies;
- arbitrary browser-storage values;
- exact street addresses;
- raw production HTML.

The original sentinel cookie/localStorage E2E remains active. Runtime resource evidence is restricted to same-origin canonical URL paths without query strings or fragments, and the new resource-query sentinel regression verifies that boundary through the production extension.

## Real-browser v2 retest

Use a fresh build from PR #53 (or `main` after merge):

```bash
pnpm install --frozen-lockfile
pnpm --dir apps/retailer-bridge test
pnpm --dir apps/retailer-bridge typecheck
pnpm --dir apps/retailer-bridge build
```

Then reload the unpacked extension in the normal browser profile.

For Chrome/Chromium use `chrome://extensions`. For Yandex Browser use its extension-management page (for example `browser://extensions` where supported). Enable developer mode and load/reload `apps/retailer-bridge/dist`.

On the official Perekrestok site:

1. manually select the intended store/location;
2. manually complete any login/CAPTCHA required by the retailer;
3. open a catalog/category page showing current product prices;
4. reload the page after the updated extension is active;
5. verify `document.documentElement.dataset.zgBridgeStatus`;
6. verify `document.documentElement.dataset.zgBridgeCount`;
7. if status is `ok`, inspect only sanitized `zg.latestObservations` in extension storage.

Acceptance requires:

- bridge status `ok`;
- observation count greater than zero;
- adapter version `2`;
- fulfillment context present and nonblank;
- SKU present and nonblank;
- integer `priceMinor >= 0`;
- currency `RUB`;
- availability explicit or `UNKNOWN`;
- source reference contains no query/hash;
- no cookie/auth/browser-storage value exported;
- no raw HTML persisted.

Live evidence record must contain only:

- observation date/time;
- page type;
- adapter version;
- store/context present: true/false;
- SKU present: true/false;
- price present: true/false;
- availability semantic;
- credential export observed: false;
- raw HTML persisted: false;
- result: PASS/FAIL.

## Decision

Current decision: **`BROWSER_BRIDGE_LIVE_PENDING`**.

Adapter v1 has a recorded real-browser FAIL. Adapter v2 is deterministic-ready, but Perekrestok must not be marked `AVAILABLE_BROWSER_BRIDGE` until the v2 real first-party retest passes.
