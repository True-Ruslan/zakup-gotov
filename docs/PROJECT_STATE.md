# Project State

Updated: 2026-08-12

## Project

**Zakup Gotov** is a recipe-to-cart grocery comparison product. The target experience is to turn a recipe, meal plan, or grocery list into a location-aware comparison of complete baskets using current retailer price and availability observations.

Repository: `True-Ruslan/zakup-gotov`  
Visibility: Public  
Current phase: **M1 — Shopping Core**  
M0 status: **technical discovery COMPLETE**  
M0→M1 decision: **GO** — [`superpowers/specs/2026-08-12-m0-to-m1-go-decision.md`](superpowers/specs/2026-08-12-m0-to-m1-go-decision.md)  
Current focus: **ship #81 structured package-evidence plumbing; then prove the first source-specific structured package field without heuristic parsing or broader browser permissions**

## Product connectivity invariant

Universal Retailer Connectivity remains permanent:

> Every retailer/banner in the target registry remains mandatory coverage work until at least one reproducible accepted acquisition path exists. A failed transport changes the acquisition mode under investigation; it does not remove the retailer from product scope.

Accepted acquisition-mode families are supported/direct API, aggregator-backed observations, stable public web/API surfaces and user-assisted first-party browser bridge.

## M0 exit status

| Gate | Status | Evidence |
|---|---|---|
| Pyaterochka accepted path | **PASS** | `AVAILABLE_BROWSER_BRIDGE`, adapter v1 |
| Perekrestok accepted path | **PASS** | `AVAILABLE_BROWSER_BRIDGE`, adapter v2 |
| Independent non-X5 path | **PASS** | Magnit `AVAILABLE_PUBLIC_WEB` for explicit public `shopCode` contexts |
| Two acquisition modes | **PASS** | Browser bridge + ordinary public web |
| Deterministic verification | **PASS** | sanitized fixtures/E2E + Magnit Phase B regressions |
| Retailer-neutral boundary | **PASS** | provider harness + canonical retailer registry |

M0 completion is **technical feasibility**, not blanket production-data-access approval.

## M1 delivered foundations

1. **Retailer registry / coverage state — COMPLETE (#72)**  
   Canonical retailer/banner identities, explicit technical coverage state, independent production-access status and registry completeness.

2. **Shopping list / canonical quantities — COMPLETE (#73)**  
   Stable UUID identity, explicit mutation semantics, positive quantities and canonical `kg → g`, `l → ml` conversion.

3. **Provider/path orchestration — COMPLETE (#74)**  
   Retailer/source-provider/acquisition-mode/fulfillment provenance, deterministic path priority and fail-closed validation.

4. **Location / fulfillment context — COMPLETE (#75)**  
   Provider-neutral location, sensitive-address redaction and typed provider-scoped fulfillment bindings.

5. **Price / availability snapshots — COMPLETE (#76)**  
   Immutable snapshots, explicit freshness evidence and first-class `UNKNOWN` availability.

6. **Deterministic product matching — COMPLETE (#77)**  
   Exact-before-normalized matching, explicit matched/ambiguous/unmatched outcomes, retailer/context isolation and no fuzzy/AI baseline.

7. **Single-store basket quote — COMPLETE (#78)**  
   Explicit package evidence, whole-package arithmetic, per-item resolution states, `COMPLETE` / `UNCERTAIN` / `INCOMPLETE` basket states, no total for incomplete baskets and fail-closed mixed-currency behavior.

8. **Failure / coverage / freshness product boundary — COMPLETE (#79)**  
   Merged to `main` at `3c4a60f`. All eight canonical retailers remain visible; technical coverage, production access, comparison status, product-safe reasons and freshness are separated. `GET /api/v1/retailers`, OpenAPI/generated client and responsive web readiness behavior are shipped in the repository. Provider IDs, acquisition modes, source references and precise addresses remain internal.

9. **Stateless critical comparison journey — COMPLETE / ACCEPTED (#80)**  
   #80 provides:
   - `POST /api/v1/comparison-previews` with locality-only public location input;
   - repeatable manual shopping-list items with client UUIDs and positive typed quantities;
   - orchestration through the existing shopping/location/provider/snapshot/matching/basket/comparison layers;
   - all eight canonical retailer results in stable order with `READY`, `UNCERTAIN`, `INCOMPLETE` or `UNAVAILABLE` states;
   - product-safe item-level unmatched/ambiguous/package-unknown/unit-mismatch details;
   - no public SKU, source-provider ID, acquisition mode, source reference or fulfillment-context ID;
   - a strict production `NoopComparisonRuntimeEvidenceSource`: production comparison does **not** fabricate fixture prices and makes no retailer calls in this slice;
   - deterministic test-only evidence for API/browser acceptance;
   - OpenAPI/generated TypeScript client synchronization;
   - responsive comparison form/results UI and bounded 3-second server action timeout;
   - desktop/mobile Playwright coverage with deterministic mock API and no live retailer dependency;
   - architecture guards preventing upstream production domains from depending back on `preview` and preventing production preview code from depending on fixture/test-support namespaces;
   - fail-closed JSON deserialization for unknown request fields, matching OpenAPI `additionalProperties: false` and preventing unsupported provider/store-shaped input from silently weakening the public contract.

   Acceptance evidence before the docs-only shipping marker:
   - exact candidate `16eff25` passed API CI, Contract CI, Web CI + responsive Web E2E, Retailer Bridge CI, Dependency Review, CodeQL Java + JS/TS, Container Security CI, Release Bundle CI and Release Contract CI;
   - final read-only change review reported **no P0/P1/P2 findings**;
   - the earlier Web E2E ambiguity was an assertion-scoping defect and was corrected without weakening product behavior.

   Design: [`superpowers/specs/2026-08-12-m1-stateless-comparison-preview-design.md`](superpowers/specs/2026-08-12-m1-stateless-comparison-preview-design.md)  
   Implementation plan: [`superpowers/plans/2026-08-12-m1-stateless-comparison-preview.md`](superpowers/plans/2026-08-12-m1-stateless-comparison-preview.md)

10. **Structured package-evidence plumbing — IMPLEMENTED / SHIPPING (#81)**  
    #81 adds a provider-neutral internal path for package quantities that are already proven by a source as structured evidence:
    - `ObservedOffer` can carry optional canonical `Quantity packageQuantity` evidence;
    - the legacy observation constructor remains source-compatible and produces `Optional.empty()`, so every existing retailer path keeps its previous package-unknown semantics;
    - `OfferSnapshot` preserves the optional package quantity unchanged through both freshness factories;
    - `PackageQuantitySet.fromSnapshots(...)` projects only explicitly present package evidence into the existing basket bindings;
    - product presentation text remains non-authoritative: regression tests prove names such as `Молоко 3,2%, 970мл` and `Вода 1,5л` do not create package evidence;
    - preview/runtime evidence derives package bindings from snapshots, eliminating a second independent source of truth;
    - compatibility paths that still accept an explicit `PackageQuantitySet` fail closed unless it exactly matches structured snapshot evidence;
    - deterministic acceptance fixtures now attach package evidence at the provider-observation boundary before snapshotting rather than injecting basket bindings later;
    - no public API/UI shape, browser permission, network path, production-access state or retailer polling behavior changes in this slice.

    **Important:** #81 does **not** claim that any accepted production retailer adapter already exposes a trustworthy structured package field. The plumbing is ready for such evidence; source-specific extraction remains separate work.

    Design: [`superpowers/specs/2026-08-12-m1-structured-package-evidence-design.md`](superpowers/specs/2026-08-12-m1-structured-package-evidence-design.md)  
    Implementation plan: [`superpowers/plans/2026-08-12-m1-structured-package-evidence.md`](superpowers/plans/2026-08-12-m1-structured-package-evidence.md)

## Important M1 limitations

- Production comparison evidence remains deliberately **no-op/fail-closed** in #80. The critical journey proves the product/API/core integration, not live production retailer acquisition.
- The core/provider/snapshot/runtime path can now carry trusted structured package quantity evidence end-to-end internally, but accepted retailer adapters still do **not** prove a supported structured package field. Missing evidence remains `PACKAGE_QUANTITY_UNKNOWN`; presentation text must not be parsed heuristically.
- Perekrestok and Pyaterochka accepted browser paths remain package-unknown until a separate evidence-backed extractor proves stable field semantics. Do not widen browser permissions or inspect arbitrary response bodies merely to obtain package size.
- Magnit location/address → public `shopCode` resolution is still unresolved (#69).
- Magnit recurring production acquisition usage rights remain unresolved (#70); default recurring polling must remain disabled until authoritative acceptance.
- Browser-bridge persistent-session/store-change lifecycle hardening remains open (#54).
- Kuper supported aggregator access remains open (#36).
- Chizhik, Ozon Fresh, Samokat, Lenta, VkusVill and additional mandatory retailer paths still require onboarding/hardening.
- A successful real `v0.1.0-rc.3` GitHub Release proof remains outstanding.

## Accepted retailer paths

### Pyaterochka

Status: **`AVAILABLE_BROWSER_BRIDGE`**, adapter `pyaterochka-browser` v1. Real first-party gate: 12 normalized observations, one fulfillment context, zero normalized validation failures. Anonymous direct server access remains unsuitable (`store-403`). No accepted structured package-quantity field is proven yet.

### Perekrestok

Status: **`AVAILABLE_BROWSER_BRIDGE`**, adapter v2. Repeated first-party evidence: 90 normalized observations, one fulfillment context and zero acceptance-validation failures. #54 remains lifecycle hardening for same-document store changes / SPA navigation. Current accepted evidence does not prove a dedicated structured package-quantity field.

### Magnit

Status: **`AVAILABLE_PUBLIC_WEB` for explicit-store-context technical feasibility**. Phase B proved stable usable observations in explicit `shopCode` contexts. Availability remains `UNKNOWN` where stock semantics are not proven.

Constraints:
- **#69** — automatic location/address → public `shopCode` resolution not proven;
- **#70** — recurring production catalog acquisition usage rights `UNRESOLVED`.

## M1 invariants

1. Core shopping/basket/comparison behavior is deterministic over supplied evidence.
2. Every canonical retailer stays visible; unavailable retailers are never silently omitted.
3. Technical connectivity and production-access readiness remain independent.
4. Precise addresses are sensitive and redacted by default; #80 accepts locality only.
5. Retailer, source provider, acquisition mode and fulfillment context remain distinct internally and do not leak into the public preview contract.
6. `UNKNOWN` availability is never coerced to available/unavailable.
7. Observation time is never misrepresented as provider freshness; freshness basis must remain structurally valid.
8. Matching ambiguity never becomes a hidden winner and no fuzzy/AI tie-break is introduced implicitly.
9. Package quantity is explicit structured evidence; missing evidence is never guessed from names, URLs or other presentation text.
10. Package evidence attached to runtime basket calculation must derive from and agree with immutable snapshot evidence.
11. Incomplete baskets never expose a misleading complete-basket total.
12. Production activation respects usage-rights state.
13. Ordinary CI and browser acceptance make no live retailer requests.
14. Production preview evidence fails closed rather than falling back to deterministic fixtures.
15. Public comparison requests remain stateless and persistence-free in M1.
16. Unknown public JSON request fields fail closed rather than being ignored.
17. Universal retailer connectivity remains mandatory for every registry entry.

## Immediate next work

1. **Finish shipping #81** with exact-head CI/security verification and independent review.
2. **Prove the first source-specific structured package field** and only then add a retailer/source extractor. Document field semantics, provenance, absence behavior and production-access constraints; do not parse product names or widen browser permissions without separate evidence.
3. Continue **#54** browser-bridge lifecycle hardening and **#69** Magnit location → `shopCode` resolution.
4. Resolve **#70** Magnit production usage rights before recurring polling and continue **#36** Kuper supported-access investigation.
5. Continue mandatory Chizhik, Ozon Fresh, Samokat, Lenta, VkusVill and additional retailer onboarding.
6. Prove a successful real `v0.1.0-rc.3` release event.
7. Move to **M2 Recipes** only after remaining M1 evidence/production constraints are explicitly accepted rather than hidden.

## Platform baseline

Backend: Java 25, Spring Boot 4.1, Spring MVC + Virtual Threads, Spring Modulith, PostgreSQL 18/Testcontainers, Flyway, jOOQ, pgJDBC `42.7.12`.

Contracts/web: OpenAPI 3.1, generated TypeScript client, Next.js 16.3.0, React 19.2.8, TypeScript 5.9.3, Node 24.18.1, Vitest/Testing Library and Playwright.

Operations/security: non-root API/web images, PostgreSQL→API→web release topology, CodeQL Java + JS/TS, Dependency Review, Release Bundle/Contract CI, fail-closed HIGH/CRITICAL container scans and Retailer Bridge CI.

## Release-engineering state

`v0.1.0-rc.1` proved the release trigger until an executable-mode defect; PR #28 fixed it. `v0.1.0-rc.2` proved fail-closed image security on pgJDBC `42.7.11` / `CVE-2026-54291`; PR #29 upgraded pgJDBC and hardened images.

A successful real **`v0.1.0-rc.3` GitHub Release published event remains outstanding** to prove final GHCR promotion, SBOM/attestation, SemVer tags and final digest smoke evidence.
