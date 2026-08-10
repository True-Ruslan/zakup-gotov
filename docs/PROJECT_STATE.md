# Project State

Updated: 2026-08-10

## Project

**Zakup Gotov** is a recipe-to-cart grocery comparison product. The target experience is to turn a recipe, meal plan, or grocery list into a location-aware comparison of complete baskets using current retailer price and availability data.

Repository: `True-Ruslan/zakup-gotov`  
Visibility: Public  
Current phase: **M0 — Product & Integration Discovery**  
Current execution stage: **M0A closure + M0B Universal Retailer Connectivity**  
Current focus: **run the first real first-party Perekrestok browser gate, keep Pyaterochka/Perekrestok mandatory, prove an independent non-X5 path, and keep the outstanding `v0.1.0-rc.3` release proof explicit**

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

### Perekrestok Browser Bridge Phase A — deterministic implementation merged, live pending

PR #49 was squash-merged to `main` as `333ad5d6ffbdcce3622a587b09004690afbe8e60` after the complete repository CI/security gate.

Implemented and verified:

- Chromium Manifest V3 extension package under `apps/retailer-bridge`;
- production extension permission contract: `storage` only, no `cookies`, `webRequest`, `debugger`, proxy-control or broad host permissions;
- isolated Perekrestok content script on `https://www.perekrestok.ru/*`;
- `BrowserObservation` allow-list trust boundary with explicit retailer/provider provenance;
- canonical source URLs with query/hash removed;
- fail-closed validation for missing context, malformed state, invalid price and invalid observation metadata;
- Perekrestok structured-state parser using JSON already delivered to the page, without `eval`;
- extraction of `masterData.plu`, integer `priceTag.price`, `balanceState` and a unique store/context ID;
- `AVAILABLE` / `UNAVAILABLE` / `UNKNOWN` mapping without inventing availability;
- local extension storage only for sanitized normalized observations;
- stale observations replaced with `[]` whenever the current page fails closed;
- deterministic synthetic/sanitized fixtures containing no production cookie, token, user address or raw page dump;
- dedicated read-only `Retailer Bridge CI` with zero live Perekrestok dependency.

TDD/runtime evidence:

- manifest permission RED was validated after rejecting two earlier harness failures that did not reach the assertion;
- collector RED failed only because `BrowserObservationCollector` did not exist;
- Perekrestok adapter RED failed only because the adapter did not exist;
- Chrome sink RED failed only because the sink did not exist;
- real Chromium MV3 E2E RED proved stale prior observations remained after `missing-context`;
- focused stale-reset unit RED then failed only because `createChromeObservationClearer` did not exist;
- bridge suite: **15 unit/fixture tests PASS**;
- bridge TypeScript: PASS;
- production extension build: PASS;
- persistent-Chromium MV3 E2E: PASS;
- the complete Retailer Bridge CI job, including Chromium E2E, passed twice consecutively on the same source behavior before final merge verification;
- final PR head `f79178956cedb8586fc6a1c04490a8c53a715960` passed API CI, Contract CI, Web CI + Web E2E, Retailer Bridge CI, CodeQL, Dependency Review, Release Bundle CI, Release Contract CI and Container Security CI.

The Chromium E2E uses the real production Perekrestok URL match but intercepts the page before network access and fulfills it from committed sanitized fixtures. It seeds sentinel cookie/localStorage secrets and verifies that neither reaches extension storage.

Current decision: **`BROWSER_BRIDGE_LIVE_PENDING`**. A real first-party browser session must still prove that current Perekrestok page state yields store context, SKU and price before the retailer can be marked `AVAILABLE_BROWSER_BRIDGE`.

Detailed evidence: [`integrations/perekrestok-browser-bridge-phase-a.md`](integrations/perekrestok-browser-bridge-phase-a.md).

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

1. Perform the opt-in real first-party Perekrestok browser gate from [`integrations/perekrestok-browser-bridge-phase-a.md`](integrations/perekrestok-browser-bridge-phase-a.md).
2. If real page structure differs, add only a sanitized minimal fixture and a failing regression test before modifying the adapter.
3. After Perekrestok live PASS, run the fixed corpus plan and reuse the browser transport contract for Pyaterochka.
4. Complete issue #50 before the bridge gains its own external dependencies or multiple substantial retailer adapters: make `apps/retailer-bridge` a first-class pnpm workspace importer and remove its temporary tooling coupling to `apps/web`.
5. Continue Kuper/X5 supported-access work and resolve the independent Magnit path in parallel.
6. Continue Chizhik, Ozon Fresh, Samokat, Lenta, VkusVill and additional chains through the same registry/adapter process.
7. Publish `v0.1.0-rc.3` through the real GitHub Release event when a release-capable path is available.

## Definition of M0 success

M0 is complete only when:

- Pyaterochka has at least one reproducible accepted path;
- Perekrestok has at least one reproducible accepted path;
- at least one independent non-X5 retailer has a reproducible accepted path;
- at least two acquisition modes are proven end to end;
- deterministic sanitized fixtures/tests preserve retailer/provider/store provenance;
- the registry/adapter architecture can add another chain without retailer-specific changes to shopping/basket domain logic.

Universal retailer connectivity remains the product invariant beyond M0: an unavailable registry entry is an explicit coverage blocker, not an omitted retailer.
