# Perekrestok Browser Bridge Phase A

Updated: 2026-08-11
Status: `AVAILABLE_BROWSER_BRIDGE`
Tracking: issue #47 / issue #52 / PR #49 / PR #53

## Purpose

Prove a user-assisted first-party browser transport for mandatory Perekrestok coverage after the direct anonymous/ordinary-cookie server-side path returned `store-403`.

Phase A is now complete for **page-snapshot acquisition**: adapter v2 passed the repeated real first-party browser gate on 2026-08-11. This does not claim a supported direct retailer API or a persistent long-lived browser-session transport.

## Implemented transport

`apps/retailer-bridge` is a Chromium Manifest V3 extension package.

Production boundary:

- `storage` is the only extension permission;
- no `host_permissions`;
- content script matches only `https://www.perekrestok.ru/*`;
- no `cookies`, `webRequest`, `debugger`, proxy-control or declarative-network-request permissions;
- login, location/store selection and any CAPTCHA remain manual first-party user actions;
- no cookie/token/auth export or replay;
- no response-body or request-header capture.

The browser collector persists only allow-listed normalized observations: retailer/provider provenance, `BROWSER_BRIDGE` source mode, fulfillment context, SKU/product name, integer minor-unit price, `RUB`, explicit/`UNKNOWN` availability, timestamp, canonical source URL without query/hash, and adapter version.

## Adapter v2 acquisition path

Adapter v2 preserves the original embedded structured-state parser for compatible pages and adds the current catalog path proven from live evidence.

When structured-state products are absent, v2 reads:

- `.product-card` as the product boundary;
- `.product-card__title-link` / `.product-card__title` for product identity/name;
- the numeric product-link suffix as SKU candidate;
- `.product-card__price .price-new` / `.price-new` for the visible current RUB price;
- same-origin `/api/customer/<version>/shop/<numeric-id>` resource pathname for fulfillment context.

Catalog presence alone does not prove stock, so DOM-derived observations use `UNKNOWN` availability unless an explicit supported stock semantic is later proven.

Only same-origin canonical `origin + pathname` resource evidence crosses the runtime boundary; query strings and fragments are removed before adapter use.

## Asynchronous SPA handling

TDD against the sanitized live shape proved two independent races:

1. the shop-context resource can arrive after `document_idle`;
2. product cards can render after shop context is already available.

The content runtime therefore uses:

- `PerformanceObserver` for new first-party resource evidence;
- `MutationObserver` for later DOM evidence;
- one serialized collection path;
- fail-closed stale-observation clearing before success;
- observer shutdown after the first successful snapshot.

No arbitrary sleep, polling interval or unbounded retry loop is used.

## First real browser gate — 2026-08-10

Adapter v1 on an official Perekrestok category/catalog page returned:

- content script executed: yes;
- status: `missing-context`;
- observation count: `0`;
- result: **FAIL**.

Sanitized diagnostics established that the current frontend no longer exposed the required v1 embedded product/store JSON, while the live page did expose stable `.product-card` DOM and same-origin `/api/customer/.../shop/<numeric-id>` resource-path evidence.

Historical evidence: [`perekrestok-browser-bridge-live-2026-08-10.md`](perekrestok-browser-bridge-live-2026-08-10.md).

## Deterministic TDD adaptation

PR #53 delivered adapter v2 through four behavioral RED -> GREEN gates:

1. current live DOM/resource shape reproduced `missing-context` before the new parser;
2. asynchronous shop-resource timing reproduced `missing-context` before resource-driven recollection;
3. adapter provenance was required as version `2` before production metadata changed;
4. delayed product DOM reproduced `missing-product` before DOM-driven recollection.

The final persistent-Chromium E2E also seeds a resource-query sentinel and proves it does not reach extension storage. The exact final PR #53 head passed the complete repository CI/security gate before merge.

## Repeated real-browser v2 gate — 2026-08-11

The rebuilt v2 extension was loaded/reloaded in the user's normal Yandex Browser profile. After normal first-party store selection and a full page reload, the official Perekrestok category page produced:

- `data-zg-bridge-status = ok`;
- `data-zg-bridge-count = 90`;
- adapter versions: exactly `2`;
- fulfillment contexts: exactly one (`656`);
- normalized-validation failures: `0`.

The sanitized validation required every observation to contain a nonblank context/SKU, integer `priceMinor >= 0`, `RUB`, valid availability (`AVAILABLE`, `UNAVAILABLE`, `UNKNOWN`), adapter version `2`, and canonical `sourceReference` without query/hash.

Result: **PASS**.

Final live evidence: [`perekrestok-browser-bridge-live-2026-08-11.md`](perekrestok-browser-bridge-live-2026-08-11.md).

## Security/privacy evidence

The live acceptance record contains no cookies, tokens, authorization headers, response bodies, arbitrary browser-storage values, exact street address, CAPTCHA artifacts, or raw production HTML.

The production manifest remains `storage`-only and the normalized collector allow-list remains the persistence boundary.

## Decision

Current Perekrestok state: **`AVAILABLE_BROWSER_BRIDGE`** for page-snapshot acquisition through the user-assisted first-party browser transport.

This is one reproducible accepted Perekrestok path for the M0 connectivity invariant.

Known limitation: issue #54 tracks post-success same-document store changes / SPA navigation. Until that lifecycle is implemented and verified, callers should treat the accepted path as a page snapshot obtained after the intended store is selected and the page is reloaded, not as a continuously self-refreshing session transport.

Next connectivity work:

1. reuse the proven browser transport contract for Pyaterochka;
2. run the fixed Perekrestok corpus/second-context validation as hardening, not as a blocker to the Phase A path acceptance;
3. prove at least one independent non-X5 retailer path;
4. continue supported/aggregator access research in parallel.
