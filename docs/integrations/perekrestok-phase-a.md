# Perekrestok Phase A — plain HTTP feasibility

Updated: 2026-08-10
Status: `PHASE_A_IMPLEMENTATION_READY_LIVE_PENDING`
Tracking: issue #43 / PR #44

## Purpose

Determine whether Perekrestok can provide a store-specific consumer data path through ordinary first-party HTTP without browser-only authorization or anti-bot circumvention.

This is a feasibility probe, not a supported-provider implementation.

## Research evidence

The independent Open-Inflation `perekrestok_api` reference identifies consumer endpoints under:

`https://www.perekrestok.ru/api/customer/1.4.1.0`

Relevant calls include:

- nearby shops: `GET /shop?...&lat={lat}&lng={lng}`;
- pickup context: `PUT /delivery/mode/pickup/{shopId}`;
- product search: `GET /catalog/search/all?textQuery={query}&entityTypes[]=product`;
- product details / shop availability endpoints.

Its recorded API snapshots contain concrete shop IDs and catalog-availability flags. Search snapshots contain product `plu`, `priceTag.price`, and `balanceState`, so Phase A can test meaningful product/price evidence rather than only HTTP reachability.

However, the reference manager launches Camoufox, waits for a first-party `session` cookie, intercepts an `Auth` header from browser traffic, and then sends credentialed API requests. Zakup Gotov does **not** inherit the browser or intercepted-Auth behavior.

## Allowed plain-HTTP path

`PerekrestokPlainHttpProbe` uses JDK `HttpClient` with:

- a standard first-party `CookieManager` limited to the original server;
- one transparent request to `https://www.perekrestok.ru/` so ordinary `Set-Cookie` values may be accepted;
- `Accept: application/json`;
- transparent Zakup Gotov `User-Agent`;
- fixed connect/request timeouts;
- no retries.

Ordinary cookies issued directly to this client are allowed, but cookie names/values are not sent to evidence output except the boolean fact that a `session` cookie was observed.

The probe never explicitly sends or reconstructs:

- `Auth` / Authorization tokens captured from browser traffic;
- another user's cookies/session;
- CAPTCHA solutions;
- browser fingerprints;
- proxy/IP rotation;
- retry/evasion logic for `401`, `403`, or `429`.

## Phase A sequence

1. transparent main-site warmup to accept ordinary first-party cookies;
2. nearby-store lookup for a coarse Moscow coordinate;
3. extract one shop ID from a successful response;
4. attempt pickup-store selection using the same first-party cookie jar;
5. search `молоко`;
6. require product PLU and price evidence.

A non-2xx required gate stops the chain immediately.

## Sanitized evidence

The live probe emits only:

- warmup HTTP status;
- whether a `session` cookie name was observed;
- store-lookup HTTP status;
- whether a shop ID was found;
- pickup-selection HTTP status;
- search HTTP status;
- whether a PLU was found;
- whether price evidence was found.

No cookie value, shop ID, PLU, address, response body, or authorization material is emitted.

The GitHub workflow publishes a finite machine-readable status context:

`Provider Live Probe / Perekrestok / <outcome>`

where outcome is one of `pass`, `store-<status>`, `store-shape`, `selection-<status>`, `search-<status>`, `search-shape`, `price-missing`, `no-evidence`, or `failed`.

## Decision rule

Only `pass` allows Phase B fixture/corpus work.

A browser-only authorization requirement, guarded HTTP gate, or anti-bot dependency is recorded as evidence and is not bypassed. Response-shape failure may be investigated only with public/non-evasive evidence to distinguish API drift from access control.
