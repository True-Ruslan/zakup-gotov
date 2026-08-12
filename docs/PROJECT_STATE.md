# Project State

Updated: 2026-08-12

## Project

**Zakup Gotov** is a recipe-to-cart grocery comparison product. The target experience is to turn a recipe, meal plan, or grocery list into a location-aware comparison of complete baskets using current retailer price and availability observations.

Repository: `True-Ruslan/zakup-gotov`  
Visibility: Public  
Current phase: **M1 — Shopping Core**  
M0 status: **technical discovery COMPLETE**  
M0→M1 decision: **GO** — [`superpowers/specs/2026-08-12-m0-to-m1-go-decision.md`](superpowers/specs/2026-08-12-m0-to-m1-go-decision.md)  
Current focus: **failure / coverage / freshness product/API/UX boundary before critical browser E2E**

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

### Slice 1 — retailer registry / coverage state — COMPLETE (#72)

Canonical retailer/banner identities, explicit technical coverage state, independent production-access status, registry completeness and Kuper/provider identity separation.

### Slice 2 — shopping list / canonical quantities — COMPLETE (#73)

Stable UUID list/item identity, explicit mutation semantics, positive quantities, `kg → g`, `l → ml`, pieces preserved, package selection deferred.

### Slice 3 — provider/path orchestration — COMPLETE (#74)

Explicit retailer/source-provider/acquisition-mode/fulfillment provenance, deterministic path priority, explicit attempt outcomes, expected-failure-only fallback and fail-closed validation.

### Slice 4 — location / fulfillment context — COMPLETE (#75)

Provider-neutral product location, redacted sensitive addresses, typed provider-scoped fulfillment bindings and no precise-address routing leakage.

### Slice 5 — price / availability snapshots — COMPLETE (#76)

Immutable `OfferSnapshot` derived only from validated `ObservedOffer`, explicit freshness evidence, observation time separated from optional provider-side update time, and `UNKNOWN` availability preserved.

### Slice 6 — deterministic product matching — COMPLETE (#77)

Observed product labels preserved through snapshots; matching-only NFKC/case/`ё→е`/separator normalization; exact-before-normalized matching; explicit `MATCHED` / `AMBIGUOUS` / `UNMATCHED`; retailer+fulfillment scope isolation; no price/availability/freshness/SKU semantic tie-break and no fuzzy/AI baseline.

Design: [`superpowers/specs/2026-08-12-m1-deterministic-matching-design.md`](superpowers/specs/2026-08-12-m1-deterministic-matching-design.md).

### Slice 7 — single-store basket quote — COMPLETE (#78 implementation; shipping gate pending)

The first deterministic one-retailer/one-context basket core is implemented:

- package quantity is explicit trusted evidence keyed by `OfferSnapshotId`; it is never inferred from `productName`;
- missing package evidence is `PACKAGE_QUANTITY_UNKNOWN`, not an assumed one-piece package;
- duplicate package evidence fails closed and binding snapshots are immutable;
- whole-package math uses canonical `Quantity` and exact decimal arithmetic: `ceil(required / package)`;
- package count is a positive `BigInteger`; provided quantity and line total are structurally validated;
- per-item outcomes are explicit: `FULFILLED`, `AVAILABILITY_UNKNOWN`, `UNMATCHED`, `AMBIGUOUS`, `UNAVAILABLE`, `PACKAGE_QUANTITY_UNKNOWN`, `QUANTITY_UNIT_MISMATCH`;
- explicit `UNAVAILABLE` wins before package math;
- `UNKNOWN` availability can produce a priced selection but only an **UNCERTAIN** quote;
- basket states are `COMPLETE`, `UNCERTAIN`, `INCOMPLETE`;
- `COMPLETE` requires all items fulfilled;
- `UNCERTAIN` requires all items selected and at least one unknown-availability line;
- `INCOMPLETE` has **no basket total**, preventing partial totals from masquerading as complete-basket prices;
- mixed selected currencies fail closed;
- shopping item order is preserved and result collections are immutable;
- ArchUnit prevents production provider/shopping/matching/retailer code from depending back on basket.

Design: [`superpowers/specs/2026-08-12-m1-single-store-basket-design.md`](superpowers/specs/2026-08-12-m1-single-store-basket-design.md).  
Implementation evidence: [`superpowers/plans/2026-08-12-m1-single-store-basket.md`](superpowers/plans/2026-08-12-m1-single-store-basket.md).

Important limitation: accepted retailer adapters do **not yet provide structured package-quantity evidence** through the normalized provider boundary. The basket core is therefore fixture/evidence-ready, but this PR does not claim a production end-to-end basket flow or parse package size from presentation text.

## Accepted retailer paths

### Pyaterochka

Status: **`AVAILABLE_BROWSER_BRIDGE`**, adapter `pyaterochka-browser` v1. Real first-party gate: 12 normalized observations, one fulfillment context, zero normalized validation failures. Anonymous direct server path remains unsuitable (`store-403`).

Evidence: [`integrations/pyaterochka-browser-bridge-phase-a.md`](integrations/pyaterochka-browser-bridge-phase-a.md), [`integrations/pyaterochka-browser-bridge-live-2026-08-11.md`](integrations/pyaterochka-browser-bridge-live-2026-08-11.md).

### Perekrestok

Status: **`AVAILABLE_BROWSER_BRIDGE`**, adapter v2. Repeated first-party evidence: 90 normalized observations, one fulfillment context, zero acceptance-validation failures. #54 remains lifecycle hardening for same-document store changes / SPA navigation.

### Magnit

Status: **`AVAILABLE_PUBLIC_WEB` for explicit-store-context technical feasibility**. Phase B proved 20/20 HTTP and usable SKU/current-price observations in two explicit `shopCode` contexts, stable identity 20/20 and zero failed requirements; availability stays `UNKNOWN` where stock semantics are not proven.

Constraints:
- **#69** — automatic location/address → public `shopCode` resolution not proven;
- **#70** — recurring production catalog acquisition usage rights `UNRESOLVED`.

M1 must not enable default recurring Magnit production polling until #70 reaches authoritative `ACCEPTABLE`.

## M1 invariants

1. Core shopping/basket logic is deterministic over fixtures/evidence.
2. Retailer coverage is explicit; unavailable retailers are not silently omitted.
3. Precise addresses remain sensitive and redacted by default.
4. Provider-specific fulfillment identifiers remain provider-scoped.
5. Retailer, source provider, acquisition mode and fulfillment context remain distinct.
6. `UNKNOWN` availability is never coerced to available/unavailable.
7. Observation time is never misrepresented as provider freshness.
8. Snapshots derive only from validated provider observations.
9. Matching ambiguity never becomes a hidden winner.
10. Package quantity is explicit evidence; missing evidence is not guessed.
11. Incomplete baskets never expose a misleading complete-basket total.
12. Production activation respects usage-rights state.
13. Universal retailer connectivity remains mandatory for every registry entry.

## Immediate next work

1. **Failure / coverage / freshness product/API/UX boundary — NEXT**
   - define a product-facing comparison/read model that preserves retailer coverage state, provider attempt/failure evidence, match/basket status and freshness provenance;
   - expose complete/uncertain/incomplete semantics without leaking provider implementation details;
   - represent unavailable/discovery retailers rather than omitting them;
   - expose observation-only vs provider-timestamp freshness honestly;
   - keep production-access restrictions visible to orchestration, not bypassed by UI/API;
   - remain fixture-first while the public contract stabilizes.
2. Connect structured package-quantity evidence only where a supported retailer/source proves it; do not parse names heuristically.
3. Critical-journey browser E2E over the product contract/UI.

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

`v0.1.0-rc.1` proved the release trigger until an executable-mode defect; PR #28 fixed it. `v0.1.0-rc.2` proved fail-closed image security on pgJDBC `42.7.11` / `CVE-2026-54291`; PR #29 upgraded pgJDBC and hardened images.

A successful real **`v0.1.0-rc.3` GitHub Release published event remains outstanding** to prove final GHCR promotion, SBOM/attestation, SemVer tags and final digest smoke evidence.
