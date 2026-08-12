# Project State

Updated: 2026-08-12

## Project

**Zakup Gotov** is a recipe-to-cart grocery comparison product. The target experience is to turn a recipe, meal plan, or grocery list into a location-aware comparison of complete baskets using current retailer price and availability observations.

Repository: `True-Ruslan/zakup-gotov`  
Visibility: Public  
Current phase: **M1 — Shopping Core**  
M0 status: **technical discovery COMPLETE**  
M0→M1 decision: **GO** — [`superpowers/specs/2026-08-12-m0-to-m1-go-decision.md`](superpowers/specs/2026-08-12-m0-to-m1-go-decision.md)  
Current focus: **provider/path orchestration over deterministic fixtures with explicit retailer/source-provider/acquisition-mode provenance**

## Product connectivity invariant

Universal Retailer Connectivity remains a permanent product rule beyond M0:

> Every retailer/banner in the target registry remains mandatory coverage work until at least one reproducible accepted acquisition path exists. A failed transport changes the acquisition mode under investigation; it does not remove the retailer from product scope.

Accepted acquisition-mode families are supported/partner API, aggregator-backed observations, stable public web/API surfaces and user-assisted first-party browser bridge.

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

PR #73 establishes deterministic shopping requirements without provider dependencies:

- `Quantity` accepts positive decimal amounts only;
- kilograms normalize to grams;
- liters normalize to milliliters;
- piece quantities remain piece-based;
- equivalent decimal representations normalize to stable value equality;
- package/container units are intentionally not part of the requirement model because package selection belongs to matching/basket optimization;
- `ShoppingRequirement` performs whitespace-only normalization and preserves user wording/case for later matching;
- `ShoppingList` owns stable UUID list/item identity and insertion order;
- add/replace/remove semantics reject duplicate or unknown item IDs instead of guessing;
- exposed item views are immutable;
- no automatic duplicate-name merging is performed in M1; recipe/weekly-plan merging remains later scope.

Both M1 slices were implemented through explicit RED→GREEN cycles and verified by full Maven `verify` before final repository gating.

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

Verified connectivity infrastructure includes:

- current `ObservedOffer` trust boundary;
- provider-scoped `LocationContext`;
- normalized `ProductQuery`;
- `RetailerProvider` port;
- structural fixture/live provider separation;
- explicit live-probe entry points;
- retailer-neutral browser adapter registry;
- first-class `apps/retailer-bridge` workspace and persistent-Chromium gate;
- ordinary CI remains free of live retailer network dependencies.

The next M1 domain evolution must update offer provenance to distinguish at least `retailerId`, `sourceProviderId` and acquisition/source mode before multiple accepted paths are orchestrated together.

## M1 entry rules

1. shopping/basket logic runs deterministically over fixtures;
2. retailer coverage remains explicit and unavailable paths are never silently omitted;
3. retailer, source-provider and fulfillment-context provenance remain distinct;
4. `UNKNOWN` availability is preserved;
5. observation time is not misrepresented as provider-side freshness;
6. production activation respects recorded usage-rights state;
7. universal retailer connectivity continues for every registry entry.

## Immediate next work

1. **Provider/path orchestration over deterministic fixtures — NEXT**
   - evolve normalized offer provenance to explicit retailer/source-provider/source-mode fields;
   - define ordered/capability-aware path selection without blind retailer-specific branches;
   - preserve partial/path failure explicitly;
   - keep live adapters outside ordinary CI.
2. Location / fulfillment-context product boundary.
3. Price/availability snapshots with provenance and freshness semantics.
4. Deterministic matching baseline.
5. Complete single-store basket comparison.
6. Coverage/failure/freshness UX and critical browser E2E.

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
