# Roadmap

Updated: 2026-08-12

The roadmap is evidence-driven. Milestones may change when retailer integration feasibility or product usage contradicts current assumptions.

## Product connectivity invariant

Zakup Gotov targets **universal connectivity for the retailer registry**, not a permanently curated subset of easy integrations.

Every retailer/banner added to the target registry remains mandatory coverage work until at least one reproducible acquisition path is available. A failed direct path moves integration work to another accepted mode rather than removing the retailer from scope.

Durable design: [`superpowers/specs/2026-08-10-universal-retailer-connectivity-design.md`](superpowers/specs/2026-08-10-universal-retailer-connectivity-design.md).

## M0 — Product & Integration Discovery — COMPLETE

Decision: **GO to M1** — [`superpowers/specs/2026-08-12-m0-to-m1-go-decision.md`](superpowers/specs/2026-08-12-m0-to-m1-go-decision.md).

Final technical exit status:

- Perekrestok: `AVAILABLE_BROWSER_BRIDGE`, adapter v2;
- Pyaterochka: `AVAILABLE_BROWSER_BRIDGE`, adapter v1;
- independent non-X5 path: Magnit `AVAILABLE_PUBLIC_WEB` for explicit `shopCode` contexts;
- two acquisition modes proven: browser bridge + ordinary public web;
- deterministic sanitized fixtures/tests proven;
- retailer-neutral connectivity architecture proven;
- known limitations recorded rather than hidden.

Magnit final evidence: [`integrations/magnit-public-page-phase-b-live-2026-08-12.md`](integrations/magnit-public-page-phase-b-live-2026-08-12.md).

M0 completion is technical feasibility, not production access clearance. Issues #69, #70 and #54 remain explicit constraints/hardening work.

## M1 — Shopping Core — CURRENT

Goal: compare a manually entered grocery list across connected retailers while preserving explicit coverage state, fulfillment context, provenance, freshness and uncertainty.

### Entry rules

- fixture-first provider orchestration;
- unavailable/blocked registry entries remain visible;
- retailer identity, source-provider identity and fulfillment context remain distinct;
- `UNKNOWN` availability remains first-class;
- observation time is not misrepresented as provider update time;
- production activation respects usage-rights state;
- no ordinary M1 test depends on live retailer access.

### Implementation sequence

1. **Retailer registry + coverage-state model — COMPLETE (PR #72)**
   - canonical retailer/banner identities;
   - explicit technical coverage state;
   - separate production-access readiness;
   - Kuper/provider identity kept distinct from retailer identity;
   - registry completeness fails fast when a canonical retailer is omitted.
2. **Shopping-list aggregate + canonical quantities/units — COMPLETE (PR #73)**
   - UUID list/item identity;
   - stable item order and explicit add/replace/remove semantics;
   - whitespace-only requirement normalization;
   - mass canonicalized to grams;
   - volume canonicalized to milliliters;
   - piece quantities preserved;
   - non-positive quantities rejected;
   - package/container selection deliberately deferred to matching/basket optimization.
3. **Provider/path orchestration over deterministic fixtures — NEXT**
   - evolve `ObservedOffer` provenance to explicit retailer/source-provider/source-mode fields;
   - ordered/capability-aware path selection;
   - no retailer-specific branches in shopping domain;
   - partial/path failure stays explicit;
   - live adapters remain outside ordinary CI.
4. **Location / fulfillment-context boundary**
   - product-level location input;
   - provider-scoped context resolution/selection;
   - no provider-specific identifiers leaking into shopping/basket logic.
5. **Price and availability snapshots**
   - observation time;
   - currency/price representation;
   - explicit availability including `UNKNOWN`;
   - source and fulfillment-context provenance;
   - freshness limitations surfaced to callers/UI.
6. **Deterministic product-matching baseline**
   - exact/normalized matching first;
   - explicit ambiguous/unmatched state;
   - AI matching optional later, never required for baseline correctness.
7. **Complete single-store basket comparison**
   - package/quantity selection baseline;
   - deterministic explainable ranking;
   - incomplete baskets never presented as complete cheapest baskets.
8. **Failure/coverage/freshness UX**
   - unavailable retailer coverage;
   - partial provider failures;
   - stale/unknown freshness;
   - ambiguous/unmatched items.
9. **Critical-journey browser E2E**
   - enter list;
   - choose location/context where supported;
   - compare available retailers;
   - inspect missing coverage/freshness/provenance honestly.

### Scope

- shopping list CRUD;
- canonical units/quantities;
- address/location input;
- retailer registry and coverage-state visibility;
- provider/path orchestration;
- deterministic product matching baseline;
- package/quantity selection baseline;
- price and availability snapshots;
- complete-basket comparison;
- partial-provider failure UX;
- data freshness UX;
- provenance UX for aggregator, public-web and browser-assisted observations.

### Exit criteria

- critical journey covered by automated integration and browser E2E tests;
- incomplete/ambiguous matches transparent;
- one-store ranking deterministic and explainable;
- unavailable retailer coverage explicit;
- no test or user path requires hidden live retailer access;
- provider provenance, fulfillment context and freshness survive through basket comparison.

## M2 — Recipes

Goal: make recipes a first-class source of shopping requirements.

Scope: built-in/user recipes, servings, normalized ingredient quantities, instructions, recipe → shopping-requirement conversion, editing/duplication and import experiments only after the core model is stable.

## M3 — Weekly Planning

Goal: generate one coherent shopping-requirement set from several meals.

Scope: weekly planner, safe duplicate merging/unit conversion, pantry/exclusion controls and shopping-list review before comparison.

## M4 — Basket Optimization

Goal: optimize real checkout cost rather than naive SKU sums.

Scope: package-size optimization, substitutes/preferences, fees, minimum orders, single-store convenience, future multi-store lowest-total-cost mode and confidence/freshness penalties.

## M5 — Productization

Goal: make the product reliable and useful for repeat users while enabling fast experiments.

Scope: accounts/authentication, privacy-aware saved addresses/lists/preferences, analytics abstraction, feature flags, stronger provider health monitoring, performance/accessibility budgets and production provider activation only after access constraints are resolved.

## M6 — Native Mobile

Goal: ship native Android and iOS clients without redesigning the core platform.

Target stack: Expo, React Native, TypeScript, generated OpenAPI client, shared analytics vocabulary and design tokens.

## Parallel connectivity and engineering work

- Kuper supported aggregator investigation (#36);
- browser bridge persistent-session lifecycle hardening (#54);
- Magnit location → public `shopCode` resolution (#69);
- Magnit production usage-rights decision (#70);
- Chizhik, Ozon Fresh, Samokat, Lenta, VkusVill and additional mandatory retailer onboarding;
- additional X5 supported/partner paths where available;
- successful real `v0.1.0-rc.3` release proof.

## Later candidates — not commitments

Broader retailer/affiliate partnerships, direct cart handoff, loyalty, price history/alerts, AI-assisted recipe import/matching, household collaboration, nutrition planning and retailer-facing/B2B APIs remain later candidates.

## Guiding rule

Do not add infrastructure because it is fashionable. Add a technology only when a measured product, reliability, scaling or team constraint makes its benefit exceed its operational cost.
