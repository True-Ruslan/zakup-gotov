# Perekrestok Browser Bridge Phase A

Updated: 2026-08-10
Status: `DETERMINISTIC_READY_LIVE_PENDING`
Tracking: issue #47 / PR #49

## Purpose

Prove the user-assisted first-party browser transport for a mandatory retailer after the direct anonymous/ordinary-cookie Perekrestok API path returned `store-403`.

This phase does **not** claim that Perekrestok is connected in production. It proves the extension boundary, deterministic parsing and privacy behavior. A real first-party browser session is still required before the path can advance from `LIVE_PENDING`.

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

## Perekrestok structured-state adapter

The current adapter reads only JSON already present in the first-party page document. It parses, without `eval`:

- `script[type="application/json"]`;
- `script#__NEXT_DATA__`;
- `script[type="application/ld+json"]`.

Recursive extraction recognizes the Perekrestok evidence shape already observed in independent endpoint snapshots:

- `masterData.plu` — stable product identity;
- `priceTag.price` — integer minor-unit price;
- `balanceState` — availability evidence;
- `selectedShopId`, `shopId`, or `shop.id` — fulfillment/store context candidates.

Exactly one store/context ID is required before observations are emitted. Unknown balance states become `UNKNOWN`; availability is never invented from catalog presence alone.

Committed fixtures are synthetic/sanitized and contain no real cookie, token, session value, user address or raw production page dump.

## Deterministic TDD evidence

The implementation was developed through explicit RED -> GREEN cycles.

Key behavioral gates include:

1. Manifest security contract RED on missing `storage`/content-script configuration, then GREEN.
2. Browser observation trust-boundary RED on missing collector, then GREEN with allow-list projection and invalid-observation rejection.
3. Perekrestok structured-state parser RED on missing adapter, then GREEN for PLU, price, availability, unique context and malformed-state behavior.
4. Chrome observation sink RED, then GREEN with a single normalized storage-message contract.
5. Chromium extension E2E RED proving stale observations remained after `missing-context`, then GREEN after fail-closed replacement with `[]`.

At the deterministic GREEN point:

- bridge unit/fixture suite: **15 tests PASS**;
- bridge TypeScript check: PASS;
- extension production build: PASS;
- persistent-Chromium MV3 E2E: PASS;
- the same Retailer Bridge CI job, including Chromium E2E, passed a second consecutive run on the same source head.

The E2E test uses the real production Perekrestok match pattern but intercepts the test page before any retailer network request and fulfills it from committed sanitized fixtures. Therefore ordinary CI has zero live Perekrestok dependency.

The E2E profile intentionally contains sentinel first-party data (`SECRET_COOKIE` and `SECRET_LOCAL_STORAGE`) and proves those values are absent from extension storage.

## Live Phase A gate

A real first-party browser check is still required and must be performed in the user's normal Chromium/Chrome profile:

1. build the extension with `pnpm --dir apps/retailer-bridge build`;
2. open `chrome://extensions`;
3. enable Developer mode;
4. Load unpacked: `apps/retailer-bridge/dist`;
5. open the official Perekrestok site;
6. manually select the intended store/location and manually complete any login/CAPTCHA required by the retailer;
7. open a product/search/catalog page showing current retailer data;
8. verify `data-zg-bridge-status="ok"` and observation count greater than zero;
9. inspect `zg.latestObservations` in the extension service worker storage;
10. record only the sanitized result fields below.

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

If real page state differs from the current synthetic fixture, only the minimum required structured subtree may be sanitized into a regression fixture. A failing fixture test must be added before adapting the parser.

## Decision

Current decision: **`BROWSER_BRIDGE_LIVE_PENDING`**.

Perekrestok must not be marked `AVAILABLE_BROWSER_BRIDGE` until the real first-party gate passes and the resulting sanitized fixture replays deterministically.
