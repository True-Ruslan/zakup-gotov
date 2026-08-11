# Project State

Updated: 2026-08-11

## Project

**Zakup Gotov** is a recipe-to-cart grocery comparison product. The target experience is to turn a recipe, meal plan, or grocery list into a location-aware comparison of complete baskets using current retailer price and availability data.

Repository: `True-Ruslan/zakup-gotov`  
Visibility: Public  
Current phase: **M0 — Product & Integration Discovery**  
Current execution stage: **M0A closure + M0B Universal Retailer Connectivity**  
Current focus: **finish Pyaterochka Browser Bridge Phase A through the real first-party browser gate, then resolve bridge workspace debt before any third substantial browser adapter, prove an independent non-X5 path, and keep the outstanding `v0.1.0-rc.3` release proof explicit**

## Product connectivity invariant

PR #48 was squash-merged to `main` as `f72c2f9b5f6c631b8dc0ed135d60e69ec55d7d90` and records the approved Universal Retailer Connectivity design.

**Every retailer/banner added to the target retailer registry remains mandatory coverage work until at least one reproducible accepted acquisition path exists.** A failed direct API changes the acquisition mode under investigation; it does not remove the retailer from product scope.

Supported acquisition modes are:

1. direct supported/partner API;
2. aggregator-backed observations with explicit retailer/provider provenance;
3. stable public web/API surfaces;
4. user-assisted first-party browser bridge.

The initial priority registry includes Pyaterochka, Perekrestok, Chizhik, Magnit-family grocery surfaces, Lenta, VkusVill, Ozon Fresh, Samokat and relevant aggregator/provider surfaces such as Kuper.

## Current product status

The platform foundation is executable and automatically verified. The core multi-retailer basket-comparison user flow is **not implemented yet**. The web surface intentionally does not fake retailer prices or availability.

**Perekrestok has one accepted acquisition path: `AVAILABLE_BROWSER_BRIDGE` for page-snapshot acquisition.**

**Pyaterochka Browser Bridge Phase A is deterministic-ready but remains `BROWSER_BRIDGE_LIVE_PENDING`.** PR #58 introduces the second browser adapter with distinct provenance, deterministic fixtures, exact official service-resource allow-listing, and persistent-Chromium async-context/DOM evidence. A real first-party browser PASS is still required before availability can be claimed.

Other target retailers remain in discovery/unimplemented states until their own evidence gates pass.

## M0B verified foundation

PR #35 established the normalized `ObservedOffer` trust boundary.

PR #37 was squash-merged to `main` as `e318c8ee92ab5f62dd593f4fd214735eb8c59750` and established the reusable provider feasibility harness: provider capabilities, provider-scoped `LocationContext`, normalized `ProductQuery`, structural fixture/live provider separation, offline fixture verification and explicit live-probe entry points.

## Retailer evidence

### Pyaterochka / 5ka direct path

Final direct live evidence on `main` SHA `73d9f18d714bd1eafc165e7f5941405a0ce10b5b`:

**`Provider Live Probe / Pyaterochka / store-403` → failure**

Interpretation:

- direct anonymous server-side path: `DIRECT_ANONYMOUS_HTTP_UNSUITABLE`;
- Pyaterochka product coverage: still mandatory;
- selected technical fallback: user-assisted first-party browser bridge;
- supported X5/partner access and aggregator-backed coverage remain valid parallel tracks.

### Pyaterochka Browser Bridge Phase A — deterministic ready, live pending

Issue #57 / PR #58 reuse the retailer-neutral browser transport already proven for Perekrestok without sharing retailer provenance.

Deterministic implementation:

- production MV3 routing includes `https://5ka.ru/*` and `https://www.5ka.ru/*` while keeping `storage` as the only extension permission;
- `pyaterochkaBrowserAdapter` uses `retailerId=pyaterochka`, `sourceProviderId=pyaterochka-browser`, `adapterVersion=1`;
- product identity is derived only from official `/product/<slug>--<numeric-id>/` links;
- product-local visible RUB price is normalized to integer `priceMinor`;
- catalog DOM emits `availability=UNKNOWN` unless a supported stock semantic is actually present;
- fulfillment context is accepted only from canonical `https://5d.5ka.ru/api/catalog/v2/stores/<store-id>/...` resource pathname metadata;
- runtime rejects other 5d paths, lookalike origins and cross-retailer use;
- query strings/fragments are removed before resource evidence reaches adapters;
- a retailer-neutral adapter registry now contains Perekrestok and Pyaterochka;
- shared `PerformanceObserver` + `MutationObserver` behavior handles late store context and late product DOM;
- fail-closed stale clearing remains in place;
- no cookies, tokens, request headers, response bodies, arbitrary storage values or raw production HTML are captured.

TDD proof is recorded in [`integrations/pyaterochka-browser-bridge-phase-a.md`](integrations/pyaterochka-browser-bridge-phase-a.md).

Key executable GREEN head before final docs synchronization: `73b73f1049c9e3b5f6c5000644ead07e1b0754b4`:

- 23 unit tests PASS;
- TypeScript PASS;
- production bridge build PASS;
- all 3 persistent-Chromium E2E scenarios PASS;
- Pyaterochka async cross-origin store-context + delayed-DOM scenario PASS;
- resource-query and browser-cookie sentinels do not reach extension storage.

The implementation was built through explicit RED→GREEN gates for manifest routing, adapter creation, retailer-neutral registry wiring, async cross-origin context, and the resource observation security policy.

Current decision: **`BROWSER_BRIDGE_LIVE_PENDING`**. Deterministic success is not a retailer support claim. After PR #58 is merged, the rebuilt extension must pass the documented real first-party browser gate before Pyaterochka may advance to `AVAILABLE_BROWSER_BRIDGE`.

### Perekrestok direct path

PR #44 was squash-merged to `main` as `2d827479830c9ce4946f10bf80c145efb8ec6bf3`.

Its ordinary first-party-cookie HTTP path returned:

**`Provider Live Probe / Perekrestok / store-403` → failure**

No browser-derived `Auth` was exported or replayed. The direct anonymous/ordinary-cookie server-side path remains unsuitable.

### Perekrestok Browser Bridge Phase A — LIVE PASS

PR #49 was squash-merged to `main` as `333ad5d6ffbdcce3622a587b09004690afbe8e60`. It established the Chromium Manifest V3 bridge, normalized observation boundary, sanitized extension-local storage, fail-closed stale-data clearing, deterministic fixtures and persistent-Chromium E2E.

The first real browser gate on 2026-08-10 exposed an adapter-v1 mismatch:

- bridge content script executed;
- `data-zg-bridge-status = missing-context`;
- observation count `0`;
- result: **FAIL for adapter v1**.

Sanitized diagnostics proved the current site uses stable `.product-card` DOM and a same-origin `/api/customer/.../shop/<numeric-id>` resource pathname rather than the original embedded product/store state.

PR #53 was squash-merged to `main` as `218c96def777622ab66f1f8663f0466e35a9d804`. Its exact final head `ac29d2d2d6fe50d3c999c96e26d5bbfe0f6ff7ca` passed the complete repository workflow/security gate.

Adapter v2 on `main`:

- preserves the legacy structured-state path;
- falls back to semantic `.product-card` DOM when required;
- derives SKU from the numeric product-link suffix;
- normalizes visible RUB price to integer minor units;
- uses `UNKNOWN` availability when catalog DOM does not prove stock;
- accepts store context only from same-origin `/api/customer/<version>/shop/<numeric-id>` resource paths;
- strips resource query/hash before adapter use;
- handles late resource and late DOM evidence through `PerformanceObserver` + `MutationObserver`;
- serializes overlapping collections and disconnects observers after first success;
- keeps production permissions at `storage` only;
- records adapter provenance as version `2`.

#### Repeated real-browser gate — 2026-08-11

The rebuilt v2 extension was exercised in the user's normal Yandex Browser profile on the official Perekrestok catalog after normal first-party store selection and full page reload.

Observed live result:

- `data-zg-bridge-status = ok`;
- `data-zg-bridge-count = 90`;
- adapter versions: exactly `2`;
- fulfillment contexts: exactly one (`656`);
- normalized validation failures: `0`;
- sample observations contained nonblank SKU, integer non-negative `priceMinor`, `RUB`, `UNKNOWN` availability and canonical source references without query/hash.

Result: **PASS**.

Current Perekrestok decision: **`AVAILABLE_BROWSER_BRIDGE`** for page-snapshot acquisition through the user-assisted first-party browser transport.

Evidence:

- initial live failure/root cause: [`integrations/perekrestok-browser-bridge-live-2026-08-10.md`](integrations/perekrestok-browser-bridge-live-2026-08-10.md);
- final live PASS: [`integrations/perekrestok-browser-bridge-live-2026-08-11.md`](integrations/perekrestok-browser-bridge-live-2026-08-11.md);
- Phase A decision/procedure: [`integrations/perekrestok-browser-bridge-phase-a.md`](integrations/perekrestok-browser-bridge-phase-a.md).

Issue #54 remains a non-blocking lifecycle hardening item: after the first successful snapshot, same-document store changes or SPA navigation are not yet automatically refreshed. The accepted current path therefore assumes intended store selection followed by page reload.

### Magnit

PR #46 remains an independent non-X5 path candidate using public SSR product pages under explicit `shopCode` contexts without cookies/auth/API keys. No support claim is made until that evidence path is resolved.

### Kuper

Issue #36 remains active for supported aggregator-backed access. Any Kuper observation must preserve aggregator provider provenance separately from the underlying retailer/banner identity.

## Bridge maintenance threshold

Issue #50 was intentionally created when Perekrestok Phase A reused TypeScript/Vitest/Playwright tooling from `apps/web` rather than making `apps/retailer-bridge` a first-class pnpm workspace importer.

PR #58 introduces the second substantial browser adapter, so that maintenance threshold is now reached. To keep the maintenance refactor behavior-neutral as issue #50 requires, it remains separate from PR #58 but becomes a **hard precondition before any third substantial browser adapter or bridge-owned dependency**.

Issue #50 must:

- add `apps/retailer-bridge` to the root pnpm workspace;
- give the bridge explicit pinned devDependencies;
- regenerate the lockfile normally;
- remove `../web` tooling/node_modules coupling;
- preserve frozen-install/supply-chain checks and all bridge security/E2E gates.

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
- `Retailer Bridge CI` with unit/type/build/persistent-Chromium E2E verification;
- public Actuator limited to health/liveness/readiness/info.

## Release-engineering state

`v0.1.0-rc.1` proved the real release trigger until an executable-mode defect; PR #28 fixed it.

`v0.1.0-rc.2` passed release verification, built/pushed staging indexes, then correctly failed closed on pgJDBC `42.7.11` / `CVE-2026-54291`; PR #29 moved to pgJDBC `42.7.12`, hardened the web runtime and added image security CI.

A successful real **`v0.1.0-rc.3` GitHub Release published event remains outstanding** to prove final GHCR promotion, SBOM/attestation, SemVer tags and final digest smoke evidence.

## Immediate next work

1. Finish PR #58 through exact-final-head repository CI/security verification and read-only review, then merge the deterministic Pyaterochka Browser Bridge implementation.
2. Run the real first-party Pyaterochka browser gate from merged `main`; on PASS, commit sanitized live evidence and advance Pyaterochka to `AVAILABLE_BROWSER_BRIDGE`. On mismatch, add the minimum sanitized fixture and a failing regression test before any adapter change.
3. Resolve issue #50 in a separate behavior-neutral maintenance PR before a third substantial browser adapter or bridge-owned dependency.
4. Resolve at least one independent non-X5 retailer path, with Magnit currently the nearest existing candidate.
5. Prove a second accepted acquisition mode so M0 does not depend only on browser-assisted acquisition.
6. Run Perekrestok fixed-corpus/second-context validation as hardening and product-quality evidence; it is no longer the Phase A connectivity blocker.
7. Resolve issue #54 before treating the bridge as a persistent-session transport across post-success store changes / SPA navigation.
8. Continue Kuper/X5 supported-access work in parallel.
9. Continue Chizhik, Ozon Fresh, Samokat, Lenta, VkusVill and additional chains through the same registry/adapter process after the issue #50 maintenance gate.
10. Publish `v0.1.0-rc.3` through the real GitHub Release event when a release-capable path is available.

## Definition of M0 success

M0 is complete only when:

- Pyaterochka has at least one reproducible accepted path — **deterministic browser implementation exists; real-browser acceptance remains outstanding**;
- Perekrestok has at least one reproducible accepted path — **satisfied via `AVAILABLE_BROWSER_BRIDGE`**;
- at least one independent non-X5 retailer has a reproducible accepted path;
- at least two acquisition modes are proven end to end;
- deterministic sanitized fixtures/tests preserve retailer/provider/store provenance;
- the registry/adapter architecture can add another chain without retailer-specific changes to shopping/basket domain logic.

Universal retailer connectivity remains the product invariant beyond M0: an unavailable registry entry is an explicit coverage blocker, not an omitted retailer.
