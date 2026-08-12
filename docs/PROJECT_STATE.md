# Project State

Updated: 2026-08-12

## Project

**Zakup Gotov** is a recipe-to-cart grocery comparison product. The target experience is to turn a recipe, meal plan, or grocery list into a location-aware comparison of complete baskets using current retailer price and availability observations.

Repository: `True-Ruslan/zakup-gotov`  
Visibility: Public  
Current phase: **M1 — Shopping Core**  
M0 status: **technical discovery COMPLETE**  
M0→M1 decision: **GO** — [`superpowers/specs/2026-08-12-m0-to-m1-go-decision.md`](superpowers/specs/2026-08-12-m0-to-m1-go-decision.md)  
Current focus: **implement the retailer registry/coverage-state model and shopping-list core over deterministic provider observations without enabling unresolved production acquisition paths**

## Product connectivity invariant

Universal Retailer Connectivity remains a permanent product rule beyond M0:

> Every retailer/banner in the target registry remains mandatory coverage work until at least one reproducible accepted acquisition path exists. A failed transport changes the acquisition mode under investigation; it does not remove the retailer from product scope.

Accepted acquisition-mode families are:

1. supported/partner API;
2. aggregator-backed observations with explicit provider/retailer provenance;
3. stable public web/API surfaces;
4. user-assisted first-party browser bridge.

## M0 exit status

All technical M0 exit gates are satisfied:

| Gate | Status | Evidence |
|---|---|---|
| Pyaterochka accepted path | **PASS** | `AVAILABLE_BROWSER_BRIDGE`, adapter v1 |
| Perekrestok accepted path | **PASS** | `AVAILABLE_BROWSER_BRIDGE`, adapter v2 |
| Independent non-X5 accepted technical path | **PASS** | Magnit `AVAILABLE_PUBLIC_WEB` for explicit public `shopCode` contexts |
| Two distinct acquisition modes | **PASS** | Browser bridge + ordinary public web |
| Deterministic sanitized verification | **PASS** | Bridge fixtures/E2E + Magnit Phase B fixtures/regressions |
| Retailer-neutral provider boundary | **PASS** | `ObservedOffer`, provider feasibility harness, retailer registry/adapter boundaries |

M0 completion is a **technical feasibility decision**, not a blanket production-data-access approval.

## Accepted retailer paths

### Pyaterochka

Status: **`AVAILABLE_BROWSER_BRIDGE`**  
Adapter: `pyaterochka-browser`, v1

Real first-party browser gate from merged `main` on 2026-08-11:

- bridge status `ok`;
- 12 normalized observations;
- exactly one fulfillment context;
- exact `pyaterochka` / `pyaterochka-browser` provenance;
- adapter version `1`;
- zero normalized validation failures.

Evidence:

- [`integrations/pyaterochka-browser-bridge-phase-a.md`](integrations/pyaterochka-browser-bridge-phase-a.md);
- [`integrations/pyaterochka-browser-bridge-live-2026-08-11.md`](integrations/pyaterochka-browser-bridge-live-2026-08-11.md).

The direct anonymous server path remains unsuitable (`store-403`).

### Perekrestok

Status: **`AVAILABLE_BROWSER_BRIDGE`**  
Adapter: v2

Repeated real first-party browser gate on 2026-08-11:

- 90 normalized observations;
- exactly one fulfillment context;
- adapter version `2`;
- zero acceptance-validation failures;
- canonical source references without query/hash.

Evidence:

- [`integrations/perekrestok-browser-bridge-phase-a.md`](integrations/perekrestok-browser-bridge-phase-a.md);
- [`integrations/perekrestok-browser-bridge-live-2026-08-11.md`](integrations/perekrestok-browser-bridge-live-2026-08-11.md).

Issue #54 remains non-blocking lifecycle hardening for post-success same-document store changes / SPA navigation. The accepted path assumes intended store selection followed by page reload.

### Magnit

Status: **`AVAILABLE_PUBLIC_WEB` for explicit-store-context M0 feasibility**

Phase A ordinary public HTTP passed on merged `main` for the same SKU under two explicit public `shopCode` contexts.

Phase B final merged-main run `31544035409` on SHA `3bfadbf3ee569a561a6fc5222df9daebb21a5291` proved:

- 20/20 HTTP 2xx in the first context;
- 20/20 HTTP 2xx in the second context;
- 20/20 expected-SKU/current-price usable observations in each context;
- stable identity 20/20;
- zero failed requirements;
- price-bound promo status on all 40 final observations;
- explicit availability on 6/40 observations and `UNKNOWN` where stock was not proven;
- no invented regular/old price when a second supported price was absent.

Final evidence:

- [`integrations/magnit-phase-a.md`](integrations/magnit-phase-a.md);
- [`integrations/magnit-public-page-live-2026-08-12.md`](integrations/magnit-public-page-live-2026-08-12.md);
- [`integrations/magnit-phase-b.md`](integrations/magnit-phase-b.md);
- [`integrations/magnit-public-page-phase-b-live-2026-08-12.md`](integrations/magnit-public-page-phase-b-live-2026-08-12.md).

Production constraints remain explicit:

- **#69** — automatic location/address → public `shopCode` resolution is not proven;
- **#70** — recurring production catalog acquisition usage rights are `UNRESOLVED`.

Therefore M1 must not enable default recurring Magnit production polling until #70 reaches an authoritative `ACCEPTABLE` decision. Explicit/manual store context is the proven feasibility mode until #69 is resolved.

## Provider foundation

Verified M0B infrastructure includes:

- normalized `ObservedOffer` trust boundary;
- provider-scoped `LocationContext`;
- normalized `ProductQuery`;
- `RetailerProvider` port;
- structural fixture/live provider separation;
- explicit live-probe entry points;
- retailer-neutral browser adapter registry;
- first-class `apps/retailer-bridge` pnpm workspace package;
- frozen-install, unit, type, build and persistent-Chromium bridge gates;
- ordinary CI remains free of live retailer network dependencies.

## M1 entry rules

M1 Shopping Core is approved to start under these constraints:

1. shopping/basket logic must run deterministically over fixture providers;
2. retailer coverage state is explicit and unavailable paths are never silently omitted;
3. retailer, source-provider and fulfillment-context provenance remain distinct;
4. `UNKNOWN` availability is preserved rather than guessed from product presence;
5. observation time is not misrepresented as provider-side freshness when no provider timestamp exists;
6. production activation respects recorded usage-rights state;
7. universal retailer connectivity continues for every registry entry after M0.

Preferred first implementation sequence:

1. canonical retailer registry + coverage-state model;
2. shopping-list aggregate + canonical quantity/unit primitives;
3. provider/path orchestration over deterministic fixtures;
4. location/fulfillment-context input boundary;
5. price/availability snapshots with provenance/freshness;
6. deterministic product-matching baseline;
7. complete single-store basket comparison;
8. partial-provider failure and unavailable-coverage UX;
9. critical-journey browser E2E.

## Platform baseline

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
- Retailer Bridge CI with frozen install + unit/type/build/persistent-Chromium E2E;
- public Actuator limited to health/liveness/readiness/info.

## Open work

M1 may proceed while these remain explicit parallel items:

- #54 — browser bridge persistent-session lifecycle hardening;
- #69 — Magnit location → public `shopCode` resolution;
- #70 — Magnit production usage-rights decision;
- #36 — Kuper supported aggregator access;
- Chizhik, Ozon Fresh, Samokat, Lenta, VkusVill and other mandatory retailer registry onboarding;
- successful real `v0.1.0-rc.3` release-pipeline proof.

## Release-engineering state

`v0.1.0-rc.1` proved the real release trigger until an executable-mode defect; PR #28 fixed it.

`v0.1.0-rc.2` passed release verification and correctly failed closed on pgJDBC `42.7.11` / `CVE-2026-54291`; PR #29 moved to pgJDBC `42.7.12`, hardened the web runtime and added image security CI.

A successful real **`v0.1.0-rc.3` GitHub Release published event remains outstanding** to prove final GHCR promotion, SBOM/attestation, SemVer tags and final digest smoke evidence.
