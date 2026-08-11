# Roadmap

Updated: 2026-08-12

The roadmap is evidence-driven. Milestones may change when retailer integration feasibility or product usage contradicts current assumptions.

## Product connectivity invariant

Zakup Gotov targets **universal connectivity for the retailer registry**, not a permanently curated subset of easy integrations.

Every retailer/banner added to the target registry is mandatory coverage work until at least one reproducible acquisition path is available. A failed direct API does not remove that retailer from scope; it moves integration work to another accepted path such as a supported aggregator, public web surface, or user-assisted first-party browser bridge.

Durable design: [`superpowers/specs/2026-08-10-universal-retailer-connectivity-design.md`](superpowers/specs/2026-08-10-universal-retailer-connectivity-design.md).

## M0 — Product & Integration Discovery — COMPLETE

Goal: prove that the core product promise and universal retailer-connectivity architecture are technically viable before substantial shopping-core development.

Decision: **GO to M1** — [`superpowers/specs/2026-08-12-m0-to-m1-go-decision.md`](superpowers/specs/2026-08-12-m0-to-m1-go-decision.md).

Final exit status:

- **Perekrestok retailer-path criterion: satisfied** through `AVAILABLE_BROWSER_BRIDGE`, adapter v2;
- **Pyaterochka retailer-path criterion: satisfied** through `AVAILABLE_BROWSER_BRIDGE`, adapter v1;
- **mandatory X5 per-banner criterion: satisfied**;
- **retailer-bridge workspace maintenance gate: satisfied**;
- **independent non-X5 retailer criterion: satisfied** through Magnit `AVAILABLE_PUBLIC_WEB` for explicit public `shopCode` contexts;
- **second distinct acquisition-mode criterion: satisfied** through ordinary public web in addition to the browser bridge;
- **fixed-corpus/deterministic fixture criterion: satisfied** through Magnit 20×2 Phase B plus bridge fixture/E2E suites;
- **known limitations recorded rather than hidden: satisfied**.

Magnit final Phase B evidence: [`integrations/magnit-public-page-phase-b-live-2026-08-12.md`](integrations/magnit-public-page-phase-b-live-2026-08-12.md).

M0 completion is a technical feasibility decision. It does not silently clear unresolved production constraints:

- #69 — automatic Magnit location/address → public `shopCode` resolution remains open;
- #70 — Magnit recurring production catalog usage rights remain `UNRESOLVED`;
- #54 — browser bridge post-success SPA/store-change lifecycle hardening remains open.

Universal retailer connectivity remains mandatory after M0; Chizhik, Ozon Fresh, Samokat, Lenta, VkusVill, Kuper and other registry entries are still coverage work.

## M1 — Shopping Core — CURRENT

Goal: compare a manually entered grocery list across connected retailers while preserving explicit coverage state, fulfillment context, provenance, freshness and uncertainty.

### Entry rules

- fixture-first provider orchestration; live retailer access is never required for ordinary M1 tests;
- unavailable/blocked registry entries remain visible rather than silently disappearing;
- retailer identity, source-provider identity and fulfillment context remain distinct;
- `UNKNOWN` availability remains first-class;
- observation time is not misrepresented as a provider update timestamp;
- production activation respects recorded usage-rights status;
- Magnit recurring automated production polling remains disabled while #70 is unresolved;
- Magnit automatic location discovery is not claimed while #69 is unresolved.

### Implementation sequence

1. **Retailer registry + coverage-state model**
   - canonical retailer/banner identity;
   - supported acquisition paths per retailer;
   - coverage states such as available, unavailable, blocked, unresolved and degraded;
   - provider/path provenance kept outside shopping-list domain semantics.
2. **Shopping-list aggregate + canonical quantities/units**
   - item CRUD;
   - quantity/unit primitives;
   - deterministic normalization rules;
   - validation that rejects ambiguous/invalid quantities rather than guessing.
3. **Provider/path orchestration over deterministic fixtures**
   - select an eligible provider/path for a retailer/context;
   - partial-provider failure remains explicit;
   - live adapters stay outside ordinary test execution.
4. **Location / fulfillment-context boundary**
   - product-level location input;
   - provider-scoped context resolution/selection;
   - no provider-specific identifiers leaking into shopping/basket domain logic.
5. **Price and availability snapshots**
   - observation time;
   - currency/price minor units;
   - explicit availability including `UNKNOWN`;
   - source and fulfillment-context provenance;
   - freshness limitation surfaced to callers/UI.
6. **Deterministic product-matching baseline**
   - exact/normalized matching first;
   - explicit ambiguous/unmatched state;
   - AI matching remains optional later, never required for baseline correctness.
7. **Complete single-store basket comparison**
   - package/quantity selection baseline;
   - deterministic explainable ranking;
   - incomplete basket is not presented as a complete cheapest basket.
8. **Failure/coverage/freshness UX**
   - unavailable retailer coverage;
   - partial provider failures;
   - stale/unknown freshness;
   - ambiguous/unmatched shopping items.
9. **Critical-journey browser E2E**
   - manually enter list;
   - choose location/context where supported;
   - compare available retailers;
   - inspect missing coverage/freshness/provenance honestly.

### Scope

- shopping list CRUD;
- canonical units/quantities;
- address/location input;
- retailer registry and coverage-state visibility;
- retailer discovery;
- provider/path orchestration;
- deterministic product matching baseline;
- package/quantity selection baseline;
- price and availability snapshots;
- complete-basket comparison;
- partial-provider failure UX;
- data freshness UX;
- provider provenance UX for aggregator, public-web and browser-assisted observations.

### Exit criteria

- critical journey is covered by automated integration and browser E2E tests;
- incomplete/ambiguous matches are transparent;
- one-store ranking is deterministic and explainable;
- unavailable target-retailer coverage is explicit rather than silently omitted;
- no test or user-facing path requires hidden live retailer access;
- provider provenance, fulfillment context and freshness limitations survive through basket comparison.

## M2 — Recipes

Goal: make recipes a first-class source of shopping requirements.

Scope:

- built-in and user-created recipes;
- servings;
- normalized ingredient quantities;
- instructions/content model;
- recipe → shopping requirement conversion;
- recipe editing and duplication;
- import experiments only after core model is stable.

## M3 — Weekly Planning

Goal: generate one coherent shopping requirement set from several meals.

Scope:

- weekly meal planner;
- merge duplicate ingredients;
- unit conversion where safe;
- pantry/exclusion controls;
- shopping-list review before comparison.

## M4 — Basket Optimization

Goal: optimize for real checkout cost rather than naive SKU sums.

Scope:

- package-size optimization;
- substitutes and user preferences;
- delivery/service fees where available;
- minimum order constraints;
- single-store convenience mode;
- multi-store lowest-total-cost mode;
- confidence/freshness penalties.

## M5 — Productization

Goal: make the product reliable and useful for repeat users while enabling fast product experiments.

Scope:

- accounts/authentication;
- saved addresses with privacy controls;
- saved lists/recipes/preferences;
- product analytics abstraction;
- feature flags/experiments;
- stronger provider and retailer-path health monitoring;
- performance and accessibility budgets;
- public SEO/content surfaces where justified;
- production provider activation only after access/usage constraints are resolved.

## M6 — Native Mobile

Goal: ship native Android and iOS clients without redesigning the core platform.

Target stack:

- Expo;
- React Native;
- TypeScript;
- generated OpenAPI client;
- shared analytics vocabulary and design tokens.

Mobile-specific work may include barcode/camera flows, push notifications, deep links, native sharing and location UX only when validated by product needs.

## Parallel connectivity and engineering work

These items continue without blocking the first M1 domain slices:

- Kuper supported aggregator investigation (#36);
- browser bridge persistent-session lifecycle hardening (#54);
- Magnit location → public `shopCode` resolution (#69);
- Magnit production usage-rights decision (#70);
- Chizhik, Ozon Fresh, Samokat, Lenta, VkusVill and additional mandatory retailer onboarding;
- additional X5 supported/partner paths where available;
- successful real `v0.1.0-rc.3` release proof.

## Later candidates — not commitments

- broader retailer/affiliate partnerships beyond mandatory registry integration work;
- direct cart creation/checkout handoff;
- loyalty integration;
- price history and alerts;
- AI-assisted recipe import;
- AI-assisted product matching as a ranked optional stage;
- household collaboration;
- nutrition/dietary planning;
- retailer-facing or B2B APIs.

## Guiding rule

Do not add infrastructure because it is fashionable. Add a technology only when a measured product, reliability, scaling or team constraint makes its benefit exceed its operational cost.
