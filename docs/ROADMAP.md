# Roadmap

Updated: 2026-08-14

The roadmap is evidence-driven. Milestones change when integration evidence, product behavior or production constraints contradict an earlier assumption.

## Product connectivity invariant

Zakup Gotov targets **universal connectivity for the retailer registry**, not a permanently curated subset of easy integrations. Every retailer/banner remains mandatory coverage work until at least one reproducible acquisition path exists.

Durable design: [`superpowers/specs/2026-08-10-universal-retailer-connectivity-design.md`](superpowers/specs/2026-08-10-universal-retailer-connectivity-design.md).

Deterministic product/core progress does not imply that every retailer is production-ready. Technical coverage, production access and product-domain maturity remain separate dimensions.

## M0 — Product & Integration Discovery — COMPLETE

Decision: **GO to M1** — [`superpowers/specs/2026-08-12-m0-to-m1-go-decision.md`](superpowers/specs/2026-08-12-m0-to-m1-go-decision.md).

Accepted evidence: Perekrestok/Pyaterochka browser-bridge paths, Magnit public-web technical feasibility, two acquisition modes, deterministic sanitized verification and retailer-neutral architecture.

## M1 — Shopping Core — COMPLETE / ACCEPTED

Acceptance: [`m1-shopping-core-acceptance-2026-08-13.md`](m1-shopping-core-acceptance-2026-08-13.md).  
Accepted final hardening baseline: `779d0b219a13e0bf82263a1e655fb732553ed5fe`.

Accepted guarantees include canonical retailer visibility/readiness, deterministic Shopping quantities/identity, provider/location provenance, freshness/availability evidence, exact/normalized product matching, whole-package basket semantics, truthful incomplete/uncertain states, pre-acquisition production-access gating and stateless comparison preview + responsive manual-list flow.

## M2 — Recipes — COMPLETE / ACCEPTED

Goal achieved: recipes are a deterministic first-class source of shopping requirements, including single-Recipe conversion, stateless Recipe APIs, Recipe→Comparison composition, responsive Recipe-first UI and occurrence-aware multi-Recipe aggregation.

Accepted slices:

- **M2.1 Recipe domain + Recipe → ShoppingList** — merge `423eb14f7c565bbe264257a92df89a6b42d0d158`;
- **M2.2 stateless Recipe shopping preview API** — acceptance [`m2-2-recipe-shopping-preview-acceptance-2026-08-14.md`](m2-2-recipe-shopping-preview-acceptance-2026-08-14.md), merge `8f0c1d8d31cfc1673656780a7989512d38788aff`;
- **M2.3 composed Recipe → Comparison flow** — acceptance [`m2-3-recipe-comparison-preview-acceptance-2026-08-14.md`](m2-3-recipe-comparison-preview-acceptance-2026-08-14.md), merge `15a086d135f40277c655b39549c3e7a04c2e914e`;
- **M2.4 responsive Recipe UI** — acceptance [`m2-4-responsive-recipe-ui-acceptance-2026-08-14.md`](m2-4-responsive-recipe-ui-acceptance-2026-08-14.md), merge `aba20c9cee263a683c0d4383ad840d7415851861`;
- **M2.5 deterministic multi-Recipe aggregation** — acceptance [`m2-5-multi-recipe-aggregation-acceptance-2026-08-14.md`](m2-5-multi-recipe-aggregation-acceptance-2026-08-14.md), merge `0854fc5bf76ad2976986537d6b4f5f3b8ebd18f0`.

M2 permanent direction: exact normalized requirement + canonical unit remains the only implicit Recipe merge rule; no fuzzy/synonym/AI equivalence is introduced silently.

## M3 — Weekly Planning — CURRENT

Goal: combine several meals into one coherent weekly plan and review its resulting shopping requirements before retailer comparison.

### M3.1 — WeeklyPlan domain + deterministic shopping composition — COMPLETE / ACCEPTED

Authoritative design: [`superpowers/specs/2026-08-14-m3-1-weekly-plan-domain-design.md`](superpowers/specs/2026-08-14-m3-1-weekly-plan-domain-design.md)  
Implementation plan: [`superpowers/plans/2026-08-14-m3-1-weekly-plan-domain.md`](superpowers/plans/2026-08-14-m3-1-weekly-plan-domain.md)  
Shipping evidence: [`superpowers/plans/2026-08-14-m3-1-weekly-plan-domain-shipping.md`](superpowers/plans/2026-08-14-m3-1-weekly-plan-domain-shipping.md)  
Acceptance: [`m3-1-weekly-plan-acceptance-2026-08-14.md`](m3-1-weekly-plan-acceptance-2026-08-14.md)  
Accepted squash merge: `13e09c63959b050d431cc913597fc868aa408718`.

Accepted result:

- immutable WeeklyPlan + ordered WeeklyMealOccurrence model;
- required Monday-through-Sunday metadata without fixed meal-slot taxonomy;
- repeated use of one Recipe through distinct planner occurrence IDs;
- explicit caller occurrence order rather than implicit day sorting;
- per-occurrence target servings through accepted RecipeServings;
- deterministic WeeklyPlan-scoped ShoppingList and internal aggregation-entry identity;
- composition delegates accepted M2.5 scaling/canonicalization/exact merge/order/final ShoppingItem identity;
- planner provenance projects to WeeklyMealOccurrenceId + RecipeIngredientRef;
- internal RecipeAggregationEntryId remains hidden;
- identity/provenance drift fails closed;
- architecture guards keep weeklyplan dependent inward only on recipe/shopping;
- no persistence, API, UI, pantry, retailer/provider or comparison scope.

Acceptance proof:

- final reviewed head `ec1af08cbaf373f79c54858e9654451cebc4f009`: **9/9 PR workflow groups SUCCESS**;
- read-only review **Looks good**, no P0/P1/P2/P3, no review threads;
- squash merge `13e09c63959b050d431cc913597fc868aa408718`;
- issue #109 closed `completed`;
- **8/8 post-merge normal push workflows SUCCESS**.

### M3.2 — Stateless WeeklyPlan application/API boundary — NEXT

Goal: expose accepted M3.1 planner semantics through one self-contained stateless contract without prematurely introducing persistence or UI.

Recommended target flow:

`explicit weekly-plan request → server-owned transient planner/Recipe/ingredient identities → accepted M3.1 composition → self-contained weekly ShoppingList + planner/Recipe provenance`

Design defaults to validate:

- request owns planner content, not server IDs;
- server generates transient WeeklyPlanId, WeeklyMealOccurrenceIds, RecipeIds, RecipeIngredientIds and weekly ShoppingList identity through accepted boundaries;
- ordered occurrences include required day, Recipe title/base servings, target servings and explicit ingredients;
- bounded occurrence/ingredient counts keep stateless execution predictable;
- M3.1 remains authoritative for planner composition and M2.5 remains authoritative for Recipe aggregation;
- response is self-contained: every ShoppingItem lineage resolves to one returned weekly occurrence + one returned Recipe ingredient;
- internal RecipeAggregationEntryId and provider/retailer identifiers never appear publicly;
- semantic/unreadable request errors use a sanitized planner-specific Problem Detail contract;
- OpenAPI 3.1 remains source of truth and generated TypeScript client is regenerated/verified;
- controller/application adapter remains independent from provider, retailer, matching, basket, comparison and database;
- ordinary CI makes no live retailer request.

Explicit M3.2 non-goals:

- persistence/saved plans/history;
- pantry/exclusion subtraction;
- retailer comparison orchestration;
- responsive planner UI;
- calendar dates/week numbers/time zones;
- breakfast/lunch/dinner slot taxonomy;
- nutrition/macros;
- fuzzy/AI ingredient semantics.

Exit gate:

- authoritative design approved;
- contract/application TDD RED→GREEN;
- OpenAPI/generated-client synchronization green;
- architecture/full API regression green;
- exact-head 9/9 PR workflows + clean review;
- squash merge + 8/8 post-merge acceptance proof.

### M3.3 — Responsive Weekly Planning UI — AFTER M3.2

Goal: provide a Recipe/meal-by-day editor over the accepted M3.2 generated contract, show canonical weekly shopping requirements and preserve manual/Recipe comparison journeys.

Expected scope:

- add/remove/reorder weekly meal occurrences;
- choose day and target servings per occurrence;
- edit explicit Recipe ingredients;
- show weekly canonical shopping projection and validation/error states;
- desktop/mobile accessibility and Playwright RED-first coverage;
- no business-semantic duplication in browser code.

### M3.4 — Pantry / exclusions semantics — AFTER BASE PLANNER FLOW

Pantry/subtraction must be an explicit semantics layer with its own design and provenance rules. It must never silently mutate accepted M2.5/M3.1 aggregation behavior.

Persistence remains deferred until saved-plan reuse/history demonstrates product value.

## Parallel connectivity / operational work

Continue without blocking deterministic M3 work unless evidence invalidates accepted core assumptions:

- **#54** browser-bridge persistent-session/store-change/SPA lifecycle hardening;
- **#36** Kuper supported aggregator investigation;
- Chizhik, Ozon Fresh, Samokat, Lenta and VkusVill onboarding/hardening;
- structured package semantics only where source evidence proves them;
- retailer-specific production-access/right-to-operate decisions before activation;
- successful real **`v0.1.0-rc.3`** release event with final image promotion, SBOM/attestation and digest smoke evidence.

## M4 — Basket Optimization

Goal: optimize real checkout cost rather than naive SKU sums.

Scope: richer package/substitute optimization, fees, minimum orders, single-store convenience, future multi-store lowest-total-cost mode and confidence/freshness penalties.

## M5 — Productization

Goal: reliable repeat use with privacy-aware accounts/preferences, analytics abstraction, feature flags, provider health monitoring and production provider activation only after access constraints are resolved.

## M6 — Native Mobile

Goal: Android/iOS clients using shared API vocabulary/generated contracts after web/core stability.

## Guiding rule

Do not add infrastructure or semantics because they are convenient. Add them only when evidence makes the behavior correct, explainable and worth the operational cost.
