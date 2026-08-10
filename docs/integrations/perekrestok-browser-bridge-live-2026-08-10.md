# Perekrestok Browser Bridge — Live Evidence 2026-08-10

Status: `V1_LIVE_FAIL_V2_RETEST_PENDING`
Tracking: issue #52 / umbrella #47 / PR #53

## Scope

This record captures the first real first-party Perekrestok browser gate and the deterministic follow-up adaptation. It contains only sanitized structural evidence. No cookie values, authorization material, request headers, response bodies, browser-storage values, exact address, or raw production HTML are recorded.

## First real browser gate

Page type: category/catalog page on the official `www.perekrestok.ru` origin.

Initial bridge result:

- bridge content script executed: **yes**;
- result: **FAIL**;
- status: `missing-context`;
- observation count: `0`.

The failure was an adapter mismatch rather than an extension-load failure.

## Sanitized structural evidence

The current live page differs materially from the original v1 fixture assumptions:

- two structured JSON scripts parsed successfully;
- those scripts exposed no v1 `masterData` + `priceTag` product objects;
- they exposed no `selectedShopId`, `shopId`, `shop.id`, or other usable store/location key;
- `cart-store` and `orderStore` local-storage entries were inspected by **shape only** and proved to be cart/order state rather than the selected fulfillment context;
- the live catalog rendered 101 stable `.product-card` elements during the observation;
- the same cards exposed stable title/price classes including `.product-card__title-link`, `.product-card__price`, and `.price-new`;
- product links ended in a numeric product identity suitable for deterministic SKU extraction;
- the browser resource timeline contained a same-origin first-party path shaped as `/api/customer/1.4.1.0/shop/<numeric-id>`;
- no response body is needed to obtain that fulfillment-context identifier.

## Root cause

Adapter v1 depended on embedded structured JSON that the current frontend no longer uses for the required catalog/store evidence.

The current viable first-party evidence path is:

1. product identity/title/current visible price from semantic catalog DOM;
2. fulfillment context from the same-origin first-party `/shop/<numeric-id>` resource pathname already present in the browser performance timeline;
3. availability remains `UNKNOWN` when the DOM does not expose an explicit stock semantic.

## TDD adaptation evidence

PR #53 introduced the adaptation through separate RED -> GREEN cycles.

### RED 1 — current live shape

A sanitized DOM fixture plus first-party resource-path regression test was committed before production changes.

On head `c7f197e422980f418d89f47677b0f5664ffd34a3`:

- 15 pre-existing bridge tests passed;
- the single new live-shape test failed;
- expected `ok`, received `missing-context`.

### GREEN 1 — adapter parser

The adapter was extended to:

- preserve the existing structured-state path;
- recognize only same-origin `/api/customer/<version>/shop/<numeric-id>` resource paths as runtime store-context evidence;
- parse `.product-card` DOM only when structured-state products are absent;
- derive SKU from the numeric product-link suffix;
- normalize visible RUB price text to integer minor units;
- use `UNKNOWN` availability rather than infer stock from catalog presence;
- deduplicate DOM products by SKU.

The focused adapter suite then passed.

### RED 2 — asynchronous runtime context

A persistent-Chromium extension E2E was added where the shop resource resolves asynchronously after page parsing.

On head `825000ee6561262fed9fa955ed44ecc06136cbed`:

- 16 unit tests passed;
- typecheck passed;
- production build passed;
- the original extension E2E passed;
- the new live-shape E2E failed with `missing-context`.

This proved that a one-shot `document_idle` collection could run before the asynchronous shop resource entered the performance timeline.

### GREEN 2 — event-driven recollection

The content script now:

- observes browser `resource` performance entries;
- retains only same-origin canonical `origin + pathname` evidence;
- strips query strings and fragments before the adapter sees resource evidence;
- recollects when new first-party resource evidence appears;
- serializes overlapping collection attempts;
- disconnects the resource observer after the first successful collection;
- keeps fail-closed stale-observation clearing before success.

On head `deaa7357a0f15645bcc89a3b7726161e4c8be477`, Retailer Bridge CI passed all unit/type/build checks and both persistent-Chromium E2E scenarios.

### RED/GREEN 3 — provenance version

The updated acquisition logic is explicitly versioned as Perekrestok browser adapter v2 so observations cannot be confused with the original structured-state-only implementation.

A test-first provenance change failed against v1 and then passed after the adapter was advanced to v2. On head `94cdfdebf762e3d3c5fda64f34287636160e4a75`, Retailer Bridge CI passed completely.

## Security/privacy boundary

The v2 adaptation does **not** add extension permissions and does not read or export:

- cookies;
- authorization headers or tokens;
- response bodies;
- arbitrary local/session-storage values;
- CAPTCHA artifacts;
- precise user addresses;
- cross-origin resource URLs.

Only same-origin canonical resource pathnames and normalized product observations participate in the new path. The production manifest remains `storage`-only.

## Current decision

The first real browser gate is recorded as **FAIL for adapter v1**.

PR #53 provides a deterministic v2 fix, but Perekrestok remains **`BROWSER_BRIDGE_LIVE_PENDING`** until the rebuilt v2 extension is loaded in the user's normal browser and the same real page produces sanitized store/SKU/price observations.

A successful deterministic CI run is not substituted for that real-browser retest.
