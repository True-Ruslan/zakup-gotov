# Project State

Updated: 2026-08-12

## Project

**Zakup Gotov** is a recipe-to-cart grocery comparison product. The target experience is to turn a recipe, meal plan, or grocery list into a location-aware comparison of complete baskets using current retailer price and availability observations.

Repository: `True-Ruslan/zakup-gotov`  
Visibility: Public  
Current phase: **M1 — Shopping Core**  
M0 status: **technical discovery COMPLETE**  
M0→M1 decision: **GO** — [`superpowers/specs/2026-08-12-m0-to-m1-go-decision.md`](superpowers/specs/2026-08-12-m0-to-m1-go-decision.md)  
Current focus: **ship the hardened product comparison/readiness boundary; then implement the critical shopping-list → location/context → comparison browser journey**

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

### Slice 7 — single-store basket quote — COMPLETE (#78)

The deterministic one-retailer/one-context basket core is merged to `main`:

- package quantity is explicit trusted evidence keyed by `OfferSnapshotId`; it is never inferred from `productName`;
- missing package evidence is `PACKAGE_QUANTITY_UNKNOWN`, not an assumed one-piece package;
- whole-package math uses canonical `Quantity` and exact decimal arithmetic: `ceil(required / package)`;
- item outcomes preserve fulfilled/unknown/unmatched/ambiguous/unavailable/package-unknown/unit-mismatch semantics;
- `UNKNOWN` availability can produce a priced selection but only an **UNCERTAIN** quote;
- basket states are `COMPLETE`, `UNCERTAIN`, `INCOMPLETE`;
- `INCOMPLETE` has **no basket total**;
- mixed selected currencies fail closed;
- ArchUnit protects upstream domain direction.

Design: [`superpowers/specs/2026-08-12-m1-single-store-basket-design.md`](superpowers/specs/2026-08-12-m1-single-store-basket-design.md).  
Implementation evidence: [`superpowers/plans/2026-08-12-m1-single-store-basket.md`](superpowers/plans/2026-08-12-m1-single-store-basket.md).

### Slice 8 — failure / coverage / freshness product boundary — IMPLEMENTED + REVIEW HARDENED (#79; shipping gate pending)

PR #79 provides the first stable product-facing comparison/readiness contract:

- all eight canonical retailers remain visible in registry order even when integration/access/data is unavailable;
- technical coverage and production-access readiness map independently;
- public comparison states are `READY`, `UNCERTAIN`, `INCOMPLETE`, `UNAVAILABLE` with finite product-safe reasons;
- provider IDs, acquisition modes, source references and precise addresses remain internal and do not cross the read-model/API boundary;
- coverage/access restrictions take precedence over runtime quote evidence;
- source failure maps to `SOURCE_UNAVAILABLE` without leaking provider error details;
- complete/uncertain/incomplete basket invariants survive the product boundary without hidden ranking or certainty upgrades;
- public records themselves reject impossible status/coverage/access/total/freshness combinations rather than trusting assembler-only correctness;
- status/reason vocabulary is fail-closed: `UNCERTAIN` is availability uncertainty, `INCOMPLETE` accepts only item-level causes, and `UNAVAILABLE` has one cause matching coverage/access precedence or runtime-data absence;
- aggregate freshness is conservative: oldest selected observation; provider timestamp only when every selected line has trusted provider-side evidence;
- `RetailerFreshness` structurally rejects provider timestamps that contradict its basis or occur after observation time;
- no universal `fresh/stale` threshold is invented;
- `GET /api/v1/retailers` exposes the canonical readiness contract;
- OpenAPI and generated TypeScript client include the retailer endpoint;
- the Next.js home surface is M1-aware and uses the typed server loader;
- the UI distinguishes observation-only evidence from a trusted provider-side update timestamp without inventing an age verdict;
- readiness fetches are bounded by a 3-second abort timeout; hanging upstream requests fail closed to the existing unavailable state;
- CI without an API displays an accessible service-unavailable state and never fabricates retailer cards/prices;
- responsive desktop/mobile Playwright verifies the failure state, focus visibility and no horizontal overflow;
- upstream retailer/provider/shopping/matching/basket/location packages cannot depend back on `comparison`.

Design: [`superpowers/specs/2026-08-12-m1-product-comparison-read-model-design.md`](superpowers/specs/2026-08-12-m1-product-comparison-read-model-design.md).  
Implementation/TDD evidence: [`superpowers/plans/2026-08-12-m1-product-comparison-read-model.md`](superpowers/plans/2026-08-12-m1-product-comparison-read-model.md).

Important limitations:

- the initial controller intentionally supplies no fabricated runtime provider/basket evidence; it reports current canonical registry/readiness truth;
- accepted retailer adapters still do **not** provide a universal structured package-quantity field through the normalized provider boundary;
- this slice therefore does not claim a production end-to-end basket comparison journey yet.

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

1. Core shopping/basket/comparison logic is deterministic over fixtures/evidence.
2. Retailer coverage is explicit; unavailable retailers are not silently omitted.
3. Technical connectivity and production-access readiness remain independent.
4. Precise addresses remain sensitive and redacted by default.
5. Provider-specific fulfillment identifiers remain provider-scoped.
6. Retailer, source provider, acquisition mode and fulfillment context remain distinct internally.
7. `UNKNOWN` availability is never coerced to available/unavailable.
8. Observation time is never misrepresented as provider freshness.
9. Freshness basis and provider-side timestamp presence must agree structurally.
10. No stale/fresh policy is invented without source-specific evidence.
11. Snapshots derive only from validated provider observations.
12. Matching ambiguity never becomes a hidden winner.
13. Package quantity is explicit evidence; missing evidence is not guessed.
14. Incomplete baskets never expose a misleading complete-basket total.
15. Product comparison statuses and reason codes cannot form impossible combinations.
16. Product/API responses never expose internal provider transport details as user-facing semantics.
17. Server-side readiness loading is time-bounded and fails closed on upstream hangs.
18. Production activation respects usage-rights state.
19. Universal retailer connectivity remains mandatory for every registry entry.

## Immediate next work

1. **Critical product journey — NEXT after #79 ships**
   - accept a manually entered shopping list through a stable product/API boundary;
   - choose provider-neutral location and fulfillment context where supported;
   - invoke comparison through the public contract over deterministic fixture/runtime evidence;
   - render `READY`, `UNCERTAIN`, `INCOMPLETE` and `UNAVAILABLE` retailer outcomes without hiding registry entries;
   - prove desktop/mobile browser behavior end-to-end;
   - preserve current privacy, provenance, freshness and production-access boundaries.
2. Connect structured package-quantity evidence only where a supported retailer/source proves it; do not parse names heuristically.
3. Continue universal retailer onboarding and open connectivity hardening in parallel.

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
