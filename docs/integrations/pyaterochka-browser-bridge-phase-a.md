# Pyaterochka Browser Bridge — Phase A

Status: `DETERMINISTIC_READY_LIVE_PENDING`
Tracking: issue #57 / umbrella #47 / PR #58

## Goal

Provide one reproducible Pyaterochka acquisition path through the existing user-assisted first-party browser bridge after the direct anonymous server-side 5ka path failed closed with `store-403`.

This Phase A proves deterministic parsing, provenance, privacy boundaries, async browser timing, and extension wiring. It does **not** advance Pyaterochka to `AVAILABLE_BROWSER_BRIDGE` until a real first-party browser gate passes.

## Browser contract

The user remains in control of the official retailer session:

1. open the official `https://5ka.ru` or `https://www.5ka.ru` surface in the normal browser profile;
2. manually select location/store and manually complete login/CAPTCHA when required;
3. let the bridge observe only rendered page evidence and allow-listed resource URL metadata already visible to that browser context;
4. normalize observations locally;
5. persist only the sanitized `BrowserObservation` projection in extension-local storage.

The bridge does not automate CAPTCHA, spoof browser/device identity, rotate proxies, replay credentials, export cookies/tokens/auth headers, or persist response bodies/raw production HTML.

## Deterministic implementation

### Manifest routing

The production MV3 content script now runs on:

- `https://5ka.ru/*`;
- `https://www.5ka.ru/*`;
- the previously supported Perekrestok origin.

The production permission surface remains exactly:

- `storage`.

No `host_permissions`, `cookies`, `webRequest`, `debugger`, proxy control, or `declarativeNetRequest` permission was added.

### Pyaterochka adapter v1

`pyaterochkaBrowserAdapter` has distinct provenance:

- `retailerId = pyaterochka`;
- `sourceProviderId = pyaterochka-browser`;
- `sourceMode = BROWSER_BRIDGE`;
- `adapterVersion = 1`.

The adapter:

- supports only HTTPS `5ka.ru` / `www.5ka.ru` pages;
- derives SKU only from official product links shaped as `/product/<slug>--<numeric-id>/`;
- derives product name from semantic link text/`aria-label` evidence;
- derives current visible RUB price from the product-local DOM container and emits integer `priceMinor`;
- emits `availability = UNKNOWN` unless a future supported rendered semantic proves stock state;
- requires exactly one fulfillment context before emitting observations.

### Store / fulfillment context

The store context is accepted only from canonical resource URL metadata on the exact official service origin:

`https://5d.5ka.ru`

and only when the pathname matches:

`/api/catalog/v2/stores/<store-id>/...`

The runtime resource policy:

- still accepts same-origin pathname evidence used by Perekrestok;
- additionally accepts only the Pyaterochka catalog-store path above when the active page is an official 5ka page;
- rejects other `5d.5ka.ru` paths;
- rejects lookalike origins;
- rejects the Pyaterochka service origin while browsing another retailer;
- strips query strings and fragments before the resource evidence reaches an adapter.

No request headers or response bodies are inspected.

### Async browser timing

The shared bridge runtime continues to use:

- `PerformanceObserver` for late resource evidence;
- `MutationObserver` for late rendered product DOM;
- serialized recollection when evidence arrives concurrently;
- fail-closed clearing of stale observations until one valid snapshot reaches `ok`.

The runtime still disconnects observers after the first successful page snapshot. Issue #54 separately tracks persistent-session refresh after success.

## TDD evidence

### RED 1 — official-page manifest routing

Head `b4fbbdc12ed157c9c2e89e1ab065bbe5db8b9393` changed only the manifest-security test.

Result:

- 15 pre-existing tests PASS;
- 1 new test FAIL;
- expected 5ka production matches were absent.

GREEN: `779730cc3da92a4335c02d51bbf6072aa57c8030` added only the minimum production manifest matches. Unit/type/build and existing Chromium E2E returned GREEN.

### RED 2 — dedicated retailer adapter

Head `122b39cf794f35a1d0ba3a4aac23928d613a2d53` contained sanitized Pyaterochka fixture/test evidence before the production adapter existed.

Result:

- all 16 pre-existing tests PASS;
- the new suite failed only because `pyaterochka-browser-adapter` did not exist.

GREEN: `1d57c3907356a53bd4d4a7519548f2523a658bda` added the minimum adapter. Pyaterochka adapter tests, existing Perekrestok tests, typecheck, build and Chromium E2E passed.

### RED 3 — retailer-neutral adapter registry

Head `43ba78ac0d2549125b81e6adffc3908b0d60a2f7` required a single registry containing both Perekrestok and Pyaterochka with unique adapter and retailer identities.

Result:

- 20 behavior tests PASS;
- the new registry suite failed only because the registry module did not exist.

GREEN: `e26b726d6928cf93d1c7abbc0cb83bd12a4d9cbb` added the registry and switched `content.ts` from a hard-coded Perekrestok adapter to the registry. The complete repository workflow set on that head was GREEN.

### RED 4 — async cross-origin store context

Head `447cf19e3806175165d9ca493bb90d747326c5f1` added a persistent-Chromium scenario with:

- an official `5ka.ru` page;
- delayed product DOM;
- delayed `https://5d.5ka.ru/api/catalog/v2/stores/ZG001/...` resource evidence;
- a query sentinel `SECRET_RESOURCE_QUERY`;
- a browser cookie sentinel `SECRET_PYATEROCHKA_COOKIE`.

Result:

- 21 unit tests PASS;
- typecheck PASS;
- build PASS;
- both pre-existing Perekrestok Chromium E2E scenarios PASS;
- new Pyaterochka Chromium E2E FAIL;
- expected `ok`, received `missing-context`.

This proved the remaining gap was runtime resource filtering rather than adapter parsing or extension routing.

### RED 4b — cross-origin resource security policy

Head `37923e2d318cf1eadc41e023fd05e25c8ddc5065` added a focused security contract before production policy code existed.

Result:

- 21 existing tests PASS;
- the new suite failed only because `resource-observation-policy` did not exist.

The contract requires:

- same-origin path canonicalization;
- exact Pyaterochka service-origin/path allow-listing;
- query/hash stripping;
- lookalike-origin rejection;
- cross-retailer rejection.

### GREEN 4 — allow-listed runtime resource evidence

Executable head `73b73f1049c9e3b5f6c5000644ead07e1b0754b4` introduced the minimal resource policy and routed observed resources through it.

Verified on Retailer Bridge CI:

- 23 unit tests PASS;
- TypeScript PASS;
- production build PASS;
- all 3 persistent-Chromium E2E scenarios PASS;
- Pyaterochka E2E stores the expected retailer/provider/context/SKU/price/version projection;
- neither the resource-query sentinel nor browser-cookie sentinel reaches extension storage.

Ordinary CI performs no live retailer request; all browser network interactions are intercepted deterministic fixtures.

## Security / privacy decision

Phase A preserves the existing browser-bridge trust boundary:

- user session remains in the first-party browser profile;
- no credential/session export or replay;
- no CAPTCHA bypass or anti-bot evasion;
- no proxy/IP rotation intended to defeat access controls;
- no broad cross-origin resource capture;
- no response-body interception;
- no arbitrary browser-storage reads;
- only canonical allow-listed resource `origin + pathname` metadata is passed to adapters;
- collector allow-list projection remains authoritative before storage;
- stale observations are cleared on failed collection;
- normal CI is live-retailer-free.

## Real-browser gate

After PR #58 is merged, build the current extension from `main`:

```bash
git checkout main
git pull
pnpm install --frozen-lockfile
pnpm --dir apps/retailer-bridge build
```

Then in a Chromium-compatible normal user browser profile:

1. load/reload unpacked `apps/retailer-bridge/dist`;
2. open the official Pyaterochka web catalog;
3. manually select the intended location/store and complete any first-party login/CAPTCHA manually if required;
4. fully reload the catalog page;
5. inspect only:
   - `document.documentElement.dataset.zgBridgeStatus`;
   - `document.documentElement.dataset.zgBridgeCount`.

Expected first gate:

- status `ok`;
- count greater than `0`.

If the first gate passes, inspect only the normalized extension-local observations and validate:

- `retailerId = pyaterochka`;
- `sourceProviderId = pyaterochka-browser`;
- `adapterVersion = 1`;
- exactly one nonblank fulfillment context for the page snapshot;
- nonblank SKU;
- integer `priceMinor >= 0`;
- `currencyCode = RUB`;
- availability in `AVAILABLE`, `UNAVAILABLE`, `UNKNOWN`;
- canonical `sourceReference` without query/hash;
- zero invalid observations under that predicate.

Do not export cookies, tokens, request headers, storage values, exact address, response bodies, or raw HTML for evidence.

## Decision rule

Current Phase A decision: **`BROWSER_BRIDGE_LIVE_PENDING`**.

Advance Pyaterochka to `AVAILABLE_BROWSER_BRIDGE` only after the real first-party browser gate above passes and sanitized evidence is committed.

If the live shape differs, record only the minimum sanitized structural evidence required, add a failing regression fixture/test first, then change the adapter through another RED → GREEN cycle.
