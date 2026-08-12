# Roadmap

Updated: 2026-08-12

The roadmap is evidence-driven. Milestones may change when retailer integration feasibility or product usage contradicts current assumptions.

## Product connectivity invariant

Zakup Gotov targets **universal connectivity for the retailer registry**, not a permanently curated subset of easy integrations. Every retailer/banner added to the target registry remains mandatory coverage work until at least one reproducible acquisition path is available.

Durable design: [`superpowers/specs/2026-08-10-universal-retailer-connectivity-design.md`](superpowers/specs/2026-08-10-universal-retailer-connectivity-design.md).

## M0 — Product & Integration Discovery — COMPLETE

Decision: **GO to M1** — [`superpowers/specs/2026-08-12-m0-to-m1-go-decision.md`](superpowers/specs/2026-08-12-m0-to-m1-go-decision.md).

Exit evidence remains:
- Perekrestok `AVAILABLE_BROWSER_BRIDGE` v2;
- Pyaterochka `AVAILABLE_BROWSER_BRIDGE` v1;
- Magnit `AVAILABLE_PUBLIC_WEB` for explicit `shopCode` contexts;
- browser bridge + public web acquisition modes proven;
- deterministic sanitized verification and retailer-neutral architecture proven.

M0 completion is technical feasibility, not production access clearance. #69, #70 and #54 remain explicit constraints/hardening work.

## M1 — Shopping Core — CURRENT

Goal: compare a manually entered grocery list across connected retailers while preserving explicit coverage, location/context, provenance, freshness, package evidence and uncertainty.

### Entry rules

- fixture/evidence-first core;
- unavailable/blocked retailers remain visible;
- exact addresses redacted by default;
- retailer/source-provider/acquisition-mode/fulfillment context remain distinct;
- `UNKNOWN` availability remains first-class;
- observation time is not provider-update time;
- freshness evidence basis remains explicit and structurally valid;
- package quantity is explicit evidence, never inferred from product names;
- production activation respects usage-rights state;
- ordinary M1 tests make no live retailer requests.

### Implementation sequence

1. **Retailer registry + coverage-state model — COMPLETE (#72)**
2. **Shopping-list aggregate + canonical quantities/units — COMPLETE (#73)**
3. **Provider/path orchestration over deterministic fixtures — COMPLETE (#74)**
4. **Location / fulfillment-context boundary — COMPLETE (#75)**
5. **Price and availability snapshots — COMPLETE (#76)**
6. **Deterministic product matching — COMPLETE (#77)**
   - observed product labels preserved through snapshots;
   - matching-only deterministic normalization;
   - retailer/context-scoped exact-before-normalized matching;
   - explicit matched/ambiguous/unmatched states;
   - no semantic tie-break by price/availability/freshness/SKU and no fuzzy/AI baseline.
7. **Complete single-store basket comparison — COMPLETE (#78)**
   - explicit package-quantity evidence keyed by snapshot identity;
   - no package-size parsing from product names;
   - canonical unit-safe whole-package selection;
   - explicit item outcomes and `COMPLETE`, `UNCERTAIN`, `INCOMPLETE` basket states;
   - unknown availability cannot become a confirmed complete basket;
   - incomplete baskets expose no aggregate total;
   - mixed selected currencies fail closed.
8. **Failure / coverage / freshness product + API + UX boundary — COMPLETE (#79)**
   - product-facing retailer comparison/readiness model;
   - all eight canonical retailers remain visible in stable registry order;
   - technical coverage and production-access readiness map independently;
   - explicit `READY`, `UNCERTAIN`, `INCOMPLETE`, `UNAVAILABLE` states and finite product-safe reasons;
   - public records reject impossible status/coverage/access/total/freshness combinations;
   - provider-path failures translate without provider ID/acquisition/source leakage;
   - conservative freshness aggregation without invented stale thresholds;
   - `GET /api/v1/retailers` REST/OpenAPI/generated-client contract;
   - responsive M1 readiness surface with bounded server-side timeout and fail-closed unavailable behavior.
9. **Critical product journey — IMPLEMENTED IN #80; FINAL SHIPPING GATE IN PROGRESS**
   - `POST /api/v1/comparison-previews` accepts locality-only context and a manual shopping list;
   - request construction reuses canonical shopping quantities and product location;
   - comparison orchestration reuses provider snapshots, matching, basket and comparison/read-model semantics instead of duplicating them;
   - all eight canonical retailers remain visible with `READY`, `UNCERTAIN`, `INCOMPLETE` or `UNAVAILABLE` state;
   - item-level unmatched/ambiguous/package-unknown/unit-mismatch outcomes are product-safe and preserve the same basket evidence;
   - no SKU, provider ID, acquisition mode, source reference or fulfillment-context ID crosses the public API/web boundary;
   - production evidence is strict no-op/fail-closed in this slice, so production never falls back to deterministic fixture prices;
   - deterministic evidence and mock API exist only for test/acceptance composition;
   - OpenAPI/generated TypeScript client and the primary Next.js comparison form/results are synchronized;
   - request timeout is bounded and API failure renders one accessible unavailable state with no fabricated retailer results;
   - desktop/mobile Playwright proves the full deterministic journey without hidden live retailer dependencies;
   - architecture guards keep upstream production domains independent of `preview` and prevent production preview code from depending on fixture/test-support namespaces.

### Current shipping gate for #80

Before #80 can be called complete:

- final exact-head API, Contract, Web + responsive E2E, Retailer Bridge, Dependency Review, CodeQL, Container Security, Release Bundle and Release Contract checks must all be green;
- final read-only change review must have no unresolved P0/P1/P2 findings;
- a docs-only shipping marker must record the evidence;
- the branch-protection gate must be green again on that marker SHA;
- squash merge must use the exact reviewed head and post-merge `main` must be verified.

The Web E2E assertion defect discovered during hardening has already been corrected and the exact `683f29a` candidate passed all workflow groups before the final architecture/docs candidate was prepared.

### Important remaining basket-data limitation

The core can consume trusted package-quantity evidence, but accepted retailer adapters do not yet expose a universal structured package-quantity field. Add provider/source extraction only where supported evidence proves semantics. Do not parse presentation text heuristically.

### M1 exit criteria

- critical journey covered by automated integration and browser E2E tests;
- incomplete/ambiguous/uncertain outcomes transparent;
- one-store quote totals deterministic and explainable;
- unavailable retailer coverage explicit;
- no test/user path requires hidden live retailer access;
- product location/privacy boundary preserved;
- provider provenance, fulfillment context and freshness survive into comparison internally without leaking implementation identifiers publicly;
- freshness evidence basis is visible without invented stale/fresh policy;
- incomplete baskets cannot masquerade as complete winners;
- production retailer activation remains separately gated by access, location/context and source-evidence constraints.

### Next M1 engineering focus after #80 ships

1. **Trusted structured package-quantity extraction** where a specific accepted source proves the field semantics. Missing evidence stays unknown.
2. **Browser bridge lifecycle hardening (#54)** for persistent sessions, same-document store changes and SPA navigation.
3. **Magnit location → `shopCode` resolution (#69)** without leaking precise address into default product telemetry or contracts.
4. **Magnit production usage-rights decision (#70)** before recurring production polling.
5. **Kuper supported aggregator investigation (#36)**.
6. Continue mandatory Chizhik, Ozon Fresh, Samokat, Lenta, VkusVill and additional retailer onboarding.
7. Prove a successful real `v0.1.0-rc.3` release event.

## M2 — Recipes

Goal: make recipes a first-class source of shopping requirements.

Scope: built-in/user recipes, servings, normalized ingredient quantities, instructions, recipe → shopping-requirement conversion, editing/duplication and import experiments after the core model is stable.

Do not start M2 merely because the fixture-driven M1 critical journey is merged. M1 evidence limitations and production activation constraints must remain explicit and accepted rather than hidden.

## M3 — Weekly Planning

Goal: generate one coherent shopping-requirement set from several meals.

Scope: weekly planner, safe duplicate merging/unit conversion, pantry/exclusion controls and shopping-list review before comparison.

## M4 — Basket Optimization

Goal: optimize real checkout cost rather than naive SKU sums.

Scope: richer package-size/substitute optimization, fees, minimum orders, single-store convenience, future multi-store lowest-total-cost mode and confidence/freshness penalties.

## M5 — Productization

Goal: reliable repeat-use product with privacy-aware accounts/preferences, analytics abstraction, feature flags, provider health monitoring and production provider activation only after access constraints are resolved.

## M6 — Native Mobile

Goal: native Android/iOS clients via Expo/React Native/TypeScript using generated API clients and shared product vocabulary/design tokens.

## Guiding rule

Do not add infrastructure or data semantics because they are convenient. Add them only when evidence makes the behavior correct, explainable and worth the operational cost.
