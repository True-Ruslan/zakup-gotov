# Project State

Updated: 2026-08-12

## Project

**Zakup Gotov** is a recipe-to-cart grocery comparison product. The target experience is to turn a recipe, meal plan, or grocery list into a location-aware comparison of complete baskets using current retailer price and availability observations.

Repository: `True-Ruslan/zakup-gotov`  
Visibility: Public  
Current phase: **M1 — Shopping Core**  
M0 status: **technical discovery COMPLETE**  
M0→M1 decision: **GO** — [`superpowers/specs/2026-08-12-m0-to-m1-go-decision.md`](superpowers/specs/2026-08-12-m0-to-m1-go-decision.md)  
Current focus: **location / fulfillment-context product boundary over the completed provenance-aware provider/path orchestration layer**

## Product connectivity invariant

Universal Retailer Connectivity remains a permanent product rule beyond M0:

> Every retailer/banner in the target registry remains mandatory coverage work until at least one reproducible accepted acquisition path exists. A failed transport changes the acquisition mode under investigation; it does not remove the retailer from product scope.

Accepted acquisition-mode families are supported/direct API, aggregator-backed observations, stable public web/API surfaces and user-assisted first-party browser bridge.

## M0 exit status

All technical M0 exit gates remain satisfied:

| Gate | Status | Evidence |
|---|---|---|
| Pyaterochka accepted path | **PASS** | `AVAILABLE_BROWSER_BRIDGE`, adapter v1 |
| Perekrestok accepted path | **PASS** | `AVAILABLE_BROWSER_BRIDGE`, adapter v2 |
| Independent non-X5 accepted technical path | **PASS** | Magnit `AVAILABLE_PUBLIC_WEB` for explicit public `shopCode` contexts |
| Two distinct acquisition modes | **PASS** | Browser bridge + ordinary public web |
| Deterministic sanitized verification | **PASS** | Bridge fixtures/E2E + Magnit Phase B fixtures/regressions |
| Retailer-neutral connectivity boundary | **PASS** | provider harness, normalized observations and canonical retailer registry |

M0 completion is a **technical feasibility decision**, not blanket production-data-access approval.

## M1 delivered foundations

### Slice 1 — canonical retailer registry — COMPLETE

PR #72 established the first M1 domain boundary:

- canonical retailer/banner IDs for Pyaterochka, Perekrestok, Chizhik, Magnit, Lenta, VkusVill, Ozon Fresh and Samokat;
- explicit `RetailerCoverageState` vocabulary;
- independent `ProductionAccessStatus` so technical feasibility cannot silently become production activation;
- immutable registry entries and stable product order;
- fail-fast invariant requiring every canonical retailer ID to have a registry entry;
- Kuper intentionally remains acquisition-provider/aggregator provenance rather than retailer identity.

Initial accepted technical states are preserved:

- Pyaterochka — `AVAILABLE_BROWSER_BRIDGE`;
- Perekrestok — `AVAILABLE_BROWSER_BRIDGE`;
- Magnit — `AVAILABLE_PUBLIC_WEB`, production access `UNRESOLVED`;
- Chizhik, Lenta, VkusVill, Ozon Fresh and Samokat — `DISCOVERY`.

### Slice 2 — shopping-list core + canonical quantities — COMPLETE

PR #73 established deterministic shopping requirements without provider dependencies:

- positive decimal `Quantity` values only;
- kilograms normalize to grams;
- liters normalize to milliliters;
- piece quantities remain piece-based;
- equivalent decimal representations normalize to stable value equality;
- package/container selection remains outside the requirement model and belongs to matching/basket optimization;
- `ShoppingRequirement` performs whitespace-only normalization and preserves user wording/case;
- `ShoppingList` owns stable UUID list/item identity and insertion order;
- add/replace/remove reject duplicate or unknown item IDs instead of guessing;
- exposed item views are immutable;
- automatic duplicate-name merging is intentionally deferred.

### Slice 3 — provenance-aware provider/path orchestration — COMPLETE

PR #74 establishes the first deterministic M1 acquisition-routing boundary:

- normalized `ObservedOffer` now preserves `retailerId`, `sourceProviderId` and `sourceMode` independently from fulfillment context, SKU, price, availability, observation time and source reference;
- `AcquisitionMode` distinguishes `DIRECT_API`, `AGGREGATOR`, `PUBLIC_WEB` and `BROWSER_BRIDGE` without overloading `ProviderAccessType`;
- `RetailerProvider` declares its retailer identity, source-provider identity and acquisition mode explicitly;
- `LocationContext` is source-provider scoped rather than ambiguously provider-labelled;
- fixture-only `ProviderPathOrchestrator` selects deterministic priority `DIRECT_API → AGGREGATOR → PUBLIC_WEB → BROWSER_BRIDGE`, then source-provider ID as a stable tie-breaker;
- providers for other retailers are ignored rather than mixed into the requested retailer result;
- missing required capabilities and missing provider-scoped context are explicit non-invoking attempt states;
- only the explicit `ProviderPathUnavailableException` triggers fallback to another path;
- a successful empty search remains a successful selected path and does not silently mix results from a lower-priority source;
- unexpected runtime defects propagate instead of being hidden by fallback;
- unsuccessful routing returns an explicit outcome with attempted-path evidence rather than pretending the retailer is absent;
- the common trust boundary rejects observations whose retailer, source provider, acquisition mode or fulfillment context do not match the provider/request contract.

All three provider/orchestration behaviors were developed through separate RED→GREEN cycles and verified by full Maven `verify` before final repository gating. Ordinary CI still performs no live retailer traffic.

## Accepted retailer paths

### Pyaterochka

Status: **`AVAILABLE_BROWSER_BRIDGE`**, adapter `pyaterochka-browser` v1. The real first-party browser gate on 2026-08-11 produced 12 normalized observations, one fulfillment context and zero normalized validation failures. The direct anonymous server path remains unsuitable (`store-403`).

Evidence:

- [`integrations/pyaterochka-browser-bridge-phase-a.md`](integrations/pyaterochka-browser-bridge-phase-a.md);
- [`integrations/pyaterochka-browser-bridge-live-2026-08-11.md`](integrations/pyaterochka-browser-bridge-live-2026-08-11.md).

### Perekrestok

Status: **`AVAILABLE_BROWSER_BRIDGE`**, adapter v2. Repeated real first-party browser evidence produced 90 normalized observations, one fulfillment context and zero acceptance-validation failures.

Issue #54 remains non-blocking lifecycle hardening for same-document store changes / SPA navigation; accepted page-snapshot operation assumes intended store selection followed by reload.

Evidence:

- [`integrations/perekrestok-browser-bridge-phase-a.md`](integrations/perekrestok-browser-bridge-phase-a.md);
- [`integrations/perekrestok-browser-bridge-live-2026-08-11.md`](integrations/perekrestok-browser-bridge-live-2026-08-11.md).

### Magnit

Status: **`AVAILABLE_PUBLIC_WEB` for explicit-store-context technical feasibility**.

Final Phase B merged-main evidence proved 20/20 HTTP and usable SKU/current-price observations in each of two explicit public `shopCode` contexts, stable identity 20/20, zero failed requirements, explicit availability where supported and `UNKNOWN` otherwise, plus fail-closed promo/regular-price semantics.

Production constraints remain explicit:

- **#69** — automatic location/address → public `shopCode` resolution is not proven;
- **#70** — recurring production catalog acquisition usage rights are `UNRESOLVED`.

M1 must not enable default recurring Magnit production polling until #70 reaches an authoritative `ACCEPTABLE` decision.

Evidence:

- [`integrations/magnit-phase-b.md`](integrations/magnit-phase-b.md);
- [`integrations/magnit-public-page-phase-b-live-2026-08-12.md`](integrations/magnit-public-page-phase-b-live-2026-08-12.md).

## Provider foundation

Verified connectivity infrastructure now includes:

- provenance-complete `ObservedOffer` trust boundary;
- source-provider-scoped `LocationContext`;
- normalized `ProductQuery`;
- metadata-explicit `RetailerProvider` port;
- structural fixture/live provider separation;
- deterministic fixture-only path orchestration and explicit attempt outcomes;
- explicit live-probe entry points kept outside ordinary CI;
- retailer-neutral browser adapter registry;
- first-class `apps/retailer-bridge` workspace and persistent-Chromium gate.

## M1 entry rules

1. shopping/basket logic runs deterministically over fixtures;
2. retailer coverage remains explicit and unavailable paths are never silently omitted;
3. retailer, source-provider, acquisition mode and fulfillment-context provenance remain distinct;
4. `UNKNOWN` availability is preserved;
5. observation time is not misrepresented as provider-side freshness;
6. production activation respects recorded usage-rights state;
7. universal retailer connectivity continues for every registry entry.

## Immediate next work

1. **Location / fulfillment-context product boundary — NEXT**
   - define provider-neutral user/product location input separately from provider-specific fulfillment contexts;
   - prevent `shopCode`, X5 store IDs and other provider identifiers from leaking into shopping/basket domain objects;
   - support explicit/manual contexts where automatic resolution is not proven;
   - keep precise user addresses out of fixtures/logs by default.
2. Price/availability snapshots with provenance and freshness semantics.
3. Deterministic product-matching baseline.
4. Complete single-store basket comparison.
5. Coverage/failure/freshness UX and critical browser E2E.

## Parallel open work

- #36 — Kuper supported aggregator access;
- #54 — browser bridge persistent-session lifecycle hardening;
- #69 — Magnit location → public `shopCode` resolution;
- #70 — Magnit production usage-rights decision;
- Chizhik, Ozon Fresh, Samokat, Lenta, VkusVill and other mandatory retailer onboarding;
- successful real `v0.1.0-rc.3` release-pipeline proof.

## Platform baseline

Backend: Java 25, Spring Boot 4.1, Spring MVC + Virtual Threads, Spring Modulith, PostgreSQL 18/Testcontainers 18.4, Flyway, jOOQ, pgJDBC `42.7.12`.

Contracts/web: OpenAPI 3.1, generated TypeScript client, Next.js 16.3.0, React 19.2.8, TypeScript 5.9.3, Node 24.18.1, Vitest/Testing Library and Playwright.

Operations/security: non-root API/web images, PostgreSQL→API→web release topology, CodeQL Java + JS/TS, Dependency Review, Release Bundle/Contract CI, fail-closed HIGH/CRITICAL container scans and Retailer Bridge CI.

## Release-engineering state

`v0.1.0-rc.1` proved the real release trigger until an executable-mode defect; PR #28 fixed it.

`v0.1.0-rc.2` passed release verification and correctly failed closed on pgJDBC `42.7.11` / `CVE-2026-54291`; PR #29 moved to pgJDBC `42.7.12`, hardened the web runtime and added image security CI.

A successful real **`v0.1.0-rc.3` GitHub Release published event remains outstanding** to prove final GHCR promotion, SBOM/attestation, SemVer tags and final digest smoke evidence.
