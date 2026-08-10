# Project State

Updated: 2026-08-10

## Project

**Zakup Gotov** is a recipe-to-cart grocery comparison product. The target experience is to turn a recipe, meal plan, or grocery list into a location-aware comparison of complete baskets using current retailer price and availability data.

Repository: `True-Ruslan/zakup-gotov`  
Visibility: Public  
Current phase: **M0 — Product & Integration Discovery**  
Current execution stage: **M0A closure + M0B Universal Retailer Connectivity**  
Current focus: **repeat the real first-party Perekrestok browser gate with adapter v2, keep Pyaterochka/Perekrestok mandatory, prove an independent non-X5 path, and keep the outstanding `v0.1.0-rc.3` release proof explicit**

## Product connectivity invariant

PR #48 was squash-merged to `main` as `f72c2f9b5f6c631b8dc0ed135d60e69ec55d7d90` after the complete repository gate. It records the approved Universal Retailer Connectivity design and the first browser-bridge implementation plan.

**Every retailer/banner added to the target retailer registry remains mandatory coverage work until at least one reproducible acquisition path is available.** A failed direct API does not remove a retailer from product scope.

Supported acquisition modes are:

1. direct supported/partner API;
2. aggregator-backed observations with explicit retailer/provider provenance;
3. stable public web/API surfaces;
4. user-assisted first-party browser bridge.

Durable design: [`superpowers/specs/2026-08-10-universal-retailer-connectivity-design.md`](superpowers/specs/2026-08-10-universal-retailer-connectivity-design.md).  
Executable browser plan: [`superpowers/plans/2026-08-10-perekrestok-browser-bridge-phase-a.md`](superpowers/plans/2026-08-10-perekrestok-browser-bridge-phase-a.md).

The initial priority registry includes Pyaterochka, Perekrestok, Chizhik, Magnit-family grocery surfaces, Lenta, VkusVill, Ozon Fresh, Samokat and relevant aggregator/provider surfaces such as Kuper.

## Current product status

The platform foundation is executable and automatically verified. The core multi-retailer basket-comparison user flow is **not implemented yet**. The web surface intentionally does not fake retailer prices or availability.

**No retailer/provider is supported yet.** M0B is proving acquisition transports and normalized evidence before M1.

## M0B verified foundation

PR #35 established the normalized `ObservedOffer` trust boundary.

PR #37 established the reusable provider feasibility harness and was squash-merged to `main` as `e318c8ee92ab5f62dd593f4fd214735eb8c59750`. It provides provider capabilities, provider-scoped `LocationContext`, normalized `ProductQuery`, structural fixture/live provider separation, offline fixture verification and explicit live-probe entry points.

The fixture/live boundary was strengthened through a second RED→GREEN cycle after review found that a provider-supplied execution-mode flag could be misdeclared.

## Retailer evidence

### Pyaterochka / 5ka

Final direct live evidence on `main` SHA `73d9f18d714bd1eafc165e7f5941405a0ce10b5b`:

**`Provider Live Probe / Pyaterochka / store-403` → failure**

Interpretation:

- direct anonymous server-side path: `DIRECT_ANONYMOUS_HTTP_UNSUITABLE`;
- Pyaterochka product coverage: still mandatory;
- alternative paths: supported X5/partner access, aggregator-backed coverage, or browser bridge.

### Perekrestok direct path

PR #44 was squash-merged to `main` as `2d827479830c9ce4946f10bf80c145efb8ec6bf3` after full CI/security verification.

Its ordinary first-party-cookie HTTP path returned:

**`Provider Live Probe / Perekrestok / store-403` → failure**

No browser-derived `Auth` was exported or replayed.

Interpretation:

- direct anonymous/ordinary-cookie server-side path: `DIRECT_ANONYMOUS_HTTP_UNSUITABLE`;
- Perekrestok product coverage: still mandatory;
- selected fallback: user-assisted first-party browser bridge.

### Perekrestok Browser Bridge Phase A — v1 live FAIL, v2 merged, real retest pending

PR #49 was squash-merged to `main` as `333ad5d6ffbdcce3622a587b09004690afbe8e60`. It established the Chromium Manifest V3 bridge, normalized observation boundary, sanitized extension-local storage, fail-closed stale-data clearing, deterministic fixtures and persistent-Chromium E2E.

The first real first-party browser gate on 2026-08-10 against an official Perekrestok catalog/category page returned:

- bridge content script executed successfully;
- `data-zg-bridge-status = missing-context`;
- observation count `0`;
- result: **FAIL for adapter v1**.

Sanitized diagnostics proved that the current frontend differs from the original structured-state fixture:

- 2 structured JSON scripts parsed successfully but contained no v1 `masterData` + `priceTag` products and no usable store context;
- `cart-store` / `orderStore` local-storage shapes were cart/order state rather than selected fulfillment context;
- the live page rendered 101 stable `.product-card` elements with title and visible-price classes;
- a same-origin first-party resource pathname shaped as `/api/customer/1.4.1.0/shop/<numeric-id>` exposed the selected shop context without needing response bodies, request headers, cookies, tokens or storage values.

PR #53 was squash-merged to `main` as `218c96def777622ab66f1f8663f0466e35a9d804`. Its exact final head `ac29d2d2d6fe50d3c999c96e26d5bbfe0f6ff7ca` passed all repository workflow groups, including API CI, Contract CI, Web CI + Web E2E, Retailer Bridge CI + persistent-Chromium E2E, CodeQL, Dependency Review, Release Bundle CI, Release Contract CI and both Container Security scans.

Adapter v2 behavior now on `main`:

- preserves the original embedded structured-state parser as a compatible path;
- falls back to semantic `.product-card` DOM when structured-state products are absent;
- derives product identity from the numeric product-link suffix;
- normalizes visible `.price-new` RUB text to integer minor units;
- emits DOM availability as `UNKNOWN` rather than inventing stock semantics;
- accepts fulfillment context only from same-origin `/api/customer/<version>/shop/<numeric-id>` resource path evidence;
- strips resource query/hash and passes only canonical same-origin `origin + pathname` values;
- uses `PerformanceObserver` to recollect when asynchronous first-party resource evidence arrives;
- uses `MutationObserver` to recollect when product DOM arrives after store context;
- serializes overlapping collection attempts and disconnects both observers after the first `ok`;
- keeps the production manifest permission surface at `storage` only;
- records adapter provenance as version `2`.

PR #53 was developed through four explicit behavioral RED→GREEN gates:

1. current live-shape regression: `missing-context` before the DOM/resource parser existed;
2. asynchronous shop-resource regression: `missing-context` before resource-driven recollection existed;
3. adapter provenance regression: v2 required before changing production metadata;
4. delayed product-DOM regression: `missing-product` before DOM-driven recollection existed.

The final E2E additionally seeds a resource-query sentinel and proves it does not reach extension storage.

Current decision remains **`BROWSER_BRIDGE_LIVE_PENDING`**. Deterministic v2 success is not treated as live retailer success. The updated extension must now pass the same real first-party catalog gate before Perekrestok can advance to `AVAILABLE_BROWSER_BRIDGE`.

Detailed Phase A procedure: [`integrations/perekrestok-browser-bridge-phase-a.md`](integrations/perekrestok-browser-bridge-phase-a.md).  
First live-gate evidence: [`integrations/perekrestok-browser-bridge-live-2026-08-10.md`](integrations/perekrestok-browser-bridge-live-2026-08-10.md).

Follow-up issue #54 tracks the non-blocking persistent-session limitation: after the first successful snapshot, same-document store changes or SPA navigation require explicit lifecycle handling before the bridge is treated as a long-lived session transport.

### Magnit

PR #46 remains open on `spike/m0b-magnit`. It targets public SSR product pages for one fixed SKU under two explicit `shopCode` contexts without cookies/auth/API keys. No support claim is made until that PR/evidence path is resolved.

### Kuper

Issue #36 remains active and explicitly asks whether Kuper `Client apps API` can expose Pyaterochka/Perekrestok banner, store, catalog, price, promotions, availability and freshness with usable provenance and fixture rights.

Aggregator observations must remain modeled as aggregator-sourced observations rather than direct retailer API observations.

## Verified platform baseline

### Backend

- Java 25;
- Spring Boot 4.1;
- Spring MVC + Virtual Threads;
- Spring Modulith verification;
- PostgreSQL 18 / Testcontainers PostgreSQL 18.4;
- Flyway;
- jOOQ;
- pgJDBC `42.7.12`.

### Contracts/web

- OpenAPI 3.1;
- generated `@zakup-gotov/api-client`;
- generated-schema drift/type gates;
- Next.js 16.3.0;
- React 19.2.8;
- TypeScript 5.9.3;
- Node 24.18.1;
- distroless Node 24 Debian 13 non-root runtime;
- Vitest/Testing Library;
- Playwright production-browser tests.

### Operations/security

- separate non-root API/web production images;
- PostgreSQL 18.4 → API → web release topology;
- CodeQL Java + JS/TS;
- Dependency Review;
- Release Bundle/Contract CI;
- Container Security CI with HIGH/CRITICAL fail-closed scans;
- `Retailer Bridge CI` for deterministic extension unit/type/build/Chromium E2E verification;
- public Actuator limited to health/liveness/readiness/info.

## Release-engineering state

`v0.1.0-rc.1` proved the real release trigger until an executable-mode defect; PR #28 fixed it.

`v0.1.0-rc.2` passed release verification, built/pushed staging indexes, then correctly failed closed on pgJDBC `42.7.11` / `CVE-2026-54291`; PR #29 moved to pgJDBC `42.7.12`, hardened the web runtime and added image security CI.

A successful real **`v0.1.0-rc.3` GitHub Release published event remains outstanding** to prove final GHCR promotion, SBOM/attestation, SemVer tags and final digest smoke evidence.

## Immediate next work

1. Rebuild/reload adapter v2 in the normal first-party browser profile and repeat the real Perekrestok catalog gate from [`integrations/perekrestok-browser-bridge-phase-a.md`](integrations/perekrestok-browser-bridge-phase-a.md).
2. If v2 live PASSes, record sanitized evidence, advance Perekrestok to `AVAILABLE_BROWSER_BRIDGE`, and run the fixed corpus plan.
3. If v2 still fails, add only the minimum sanitized regression evidence and a RED test before any further adapter change.
4. Reuse the proven browser transport contract for Pyaterochka after Perekrestok live proof.
5. Complete issue #50 before the bridge gains its own external dependencies or multiple substantial retailer adapters: make `apps/retailer-bridge` a first-class pnpm workspace importer and remove its temporary tooling coupling to `apps/web`.
6. Resolve issue #54 before treating the browser bridge as a persistent-session transport across same-document store changes / SPA navigation.
7. Continue Kuper/X5 supported-access work and resolve the independent Magnit path in parallel.
8. Continue Chizhik, Ozon Fresh, Samokat, Lenta, VkusVill and additional chains through the same registry/adapter process.
9. Publish `v0.1.0-rc.3` through the real GitHub Release event when a release-capable path is available.

## Definition of M0 success

M0 is complete only when:

- Pyaterochka has at least one reproducible accepted path;
- Perekrestok has at least one reproducible accepted path;
- at least one independent non-X5 retailer has a reproducible accepted path;
- at least two acquisition modes are proven end to end;
- deterministic sanitized fixtures/tests preserve retailer/provider/store provenance;
- the registry/adapter architecture can add another chain without retailer-specific changes to shopping/basket domain logic.

Universal retailer connectivity remains the product invariant beyond M0: an unavailable registry entry is an explicit coverage blocker, not an omitted retailer.
