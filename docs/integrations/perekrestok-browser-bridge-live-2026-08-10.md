# Perekrestok Browser Bridge — Live Evidence 2026-08-10

Status: `V1_LIVE_FAIL_SUPERSEDED_BY_V2_LIVE_PASS`
Tracking: issue #52 / umbrella #47 / PR #53

## Scope

This record preserves the first real first-party Perekrestok browser gate that exposed the adapter-v1 mismatch and motivated adapter v2. It is historical evidence, not the current connectivity decision.

The successful repeated v2 gate is recorded separately in [`perekrestok-browser-bridge-live-2026-08-11.md`](perekrestok-browser-bridge-live-2026-08-11.md).

No cookie values, authorization material, request headers, response bodies, browser-storage values, exact address, or raw production HTML were recorded.

## First real browser gate

Page type: official `www.perekrestok.ru` category/catalog page.

Adapter-v1 result:

- bridge content script executed: **yes**;
- result: **FAIL**;
- status: `missing-context`;
- observation count: `0`.

This proved an adapter mismatch rather than an extension-load failure.

## Sanitized structural evidence

The live page differed materially from the original v1 fixture assumptions:

- two structured JSON scripts parsed successfully;
- those scripts exposed no v1 `masterData` + `priceTag` product objects;
- they exposed no usable `selectedShopId`, `shopId` or `shop.id` context;
- `cart-store` and `orderStore` storage entries were inspected by shape only and proved to be cart/order state rather than selected fulfillment context;
- the live catalog rendered 101 stable `.product-card` elements;
- cards exposed stable title/price classes including `.product-card__title-link`, `.product-card__price` and `.price-new`;
- product links ended in numeric product identity suitable for deterministic SKU extraction;
- the browser resource timeline contained a same-origin path shaped as `/api/customer/1.4.1.0/shop/<numeric-id>`;
- no response body was required to derive that context candidate.

## Root cause

Adapter v1 depended on embedded structured JSON that the current frontend no longer used for the required catalog/store evidence.

The viable first-party evidence path discovered from this gate was:

1. product identity/title/current visible price from semantic catalog DOM;
2. fulfillment context from the same-origin first-party `/shop/<numeric-id>` resource pathname;
3. availability `UNKNOWN` when the DOM exposes no explicit stock semantic.

The live frontend was also asynchronous in two independent dimensions: shop-context resources and product cards could arrive in either order after `document_idle`.

## TDD follow-up

PR #53 adapted the bridge through four behavioral RED -> GREEN gates:

1. current live DOM/resource shape reproduced `missing-context`;
2. late shop-resource timing reproduced `missing-context` until `PerformanceObserver` recollection was added;
3. adapter provenance was advanced explicitly to version `2`;
4. late product DOM reproduced `missing-product` until `MutationObserver` recollection was added.

The final persistent-Chromium E2E retained the original privacy/stale-data checks and additionally proved that a resource-query sentinel is stripped before extension persistence.

## Historical decision

**2026-08-10:** adapter v1 real-browser gate = FAIL (`missing-context`).

**Superseded on 2026-08-11:** adapter v2 passed the repeated real-browser gate with 90 normalized observations, one fulfillment context, adapter version `2`, and zero validation failures. Current Perekrestok state is `AVAILABLE_BROWSER_BRIDGE` for page-snapshot acquisition.

See [`perekrestok-browser-bridge-live-2026-08-11.md`](perekrestok-browser-bridge-live-2026-08-11.md) for the accepted live evidence.
