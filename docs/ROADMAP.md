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

Goal: combine several meals into one coherent weekly plan, derive one canonical shopping projection and compare that projection across retailers without contaminating planner semantics with provider/runtime concerns.

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

### M3.2 — Stateless WeeklyPlan shopping preview API — COMPLETE / ACCEPTED

Authoritative design: [`superpowers/specs/2026-08-14-m3-2-weekly-plan-shopping-preview-design.md`](superpowers/specs/2026-08-14-m3-2-weekly-plan-shopping-preview-design.md)  
Implementation plan: [`superpowers/plans/2026-08-14-m3-2-weekly-plan-shopping-preview.md`](superpowers/plans/2026-08-14-m3-2-weekly-plan-shopping-preview.md)  
Shipping evidence: [`superpowers/plans/2026-08-14-m3-2-weekly-plan-shopping-preview-shipping.md`](superpowers/plans/2026-08-14-m3-2-weekly-plan-shopping-preview-shipping.md)  
Acceptance: [`m3-2-weekly-plan-shopping-preview-acceptance-2026-08-14.md`](m3-2-weekly-plan-shopping-preview-acceptance-2026-08-14.md)  
Accepted squash merge: `9682ad1230910fc268ca3cddd8601a3fad7b100e`.

Accepted boundary:

`POST /api/v1/weekly-plan-shopping-previews`

Accepted result:

- stateless `1..35` ordered weekly occurrences;
- no client-supplied planner/Recipe/ingredient identities;
- server-owned transient WeeklyPlan/occurrence identities and accepted M2.2 Recipe/ingredient construction;
- nested Recipe validation/normalization delegated to M2.2;
- shopping composition delegated to accepted M3.1/M2.5;
- self-contained public source tuples `occurrenceId + recipeId + recipeIngredientId`;
- internal aggregation IDs hidden;
- sanitized `INVALID_WEEKLY_PLAN_SHOPPING_PREVIEW` failures;
- OpenAPI/generated TypeScript contract synchronized;
- architecture guard excludes comparison/provider/retailer/database coupling;
- no persistence, comparison, UI, pantry, calendar or AI/fuzzy scope.

Acceptance proof:

- final reviewed head `250aedb85b675036ffcb20e96a67db1afc03167a`: **9/9 PR workflow groups SUCCESS**;
- read-only review **Looks good**, no P0/P1/P2/P3, no review threads;
- squash merge `9682ad1230910fc268ca3cddd8601a3fad7b100e`;
- issue #112 closed `completed`;
- **8/8 post-merge normal push workflows SUCCESS**.

### M3.3 — WeeklyPlan → Comparison composition — COMPLETE / ACCEPTED

Authoritative design: [`superpowers/specs/2026-08-14-m3-3-weekly-plan-comparison-preview-design.md`](superpowers/specs/2026-08-14-m3-3-weekly-plan-comparison-preview-design.md)  
Implementation plan: [`superpowers/plans/2026-08-14-m3-3-weekly-plan-comparison-preview.md`](superpowers/plans/2026-08-14-m3-3-weekly-plan-comparison-preview.md)  
Shipping evidence: [`superpowers/plans/2026-08-14-m3-3-weekly-plan-comparison-preview-shipping.md`](superpowers/plans/2026-08-14-m3-3-weekly-plan-comparison-preview-shipping.md)  
Acceptance: [`m3-3-weekly-plan-comparison-preview-acceptance-2026-08-14.md`](m3-3-weekly-plan-comparison-preview-acceptance-2026-08-14.md)  
Accepted squash merge: `89b9ef2ca95d07a7e4c964fdef38a9af1c5c3a43`.

Accepted boundary:

`POST /api/v1/weekly-plan-comparison-previews`

Accepted result:

- one stateless request combines provider-neutral locality with the accepted M3.2 WeeklyPlan input and owns no server identities;
- accepted M3.2 remains the only planner/Recipe shopping-preview authority and its self-contained provenance is returned unchanged;
- generated weekly ShoppingItem UUID, order, normalized requirement and canonical quantity are preserved exactly into comparison;
- accepted ComparisonPreview remains the only authority for locality validation, retailer visibility/readiness, production-access gating, runtime evidence, matching, basket/package semantics and truthful comparison projection;
- fail-closed verification rejects cardinality, identity/order, requirement or quantity drift across the composition boundary;
- whole-wrapper binding failures use sanitized `INVALID_WEEKLY_PLAN_COMPARISON_PREVIEW`; successfully bound M3.2 and comparison semantic failures preserve their accepted contracts;
- OpenAPI/generated client exposes `createWeeklyPlanComparisonPreview` and `WEEKLY_PLAN_COMPARISON_PREVIEWS_PATH`;
- ArchUnit prevents direct planner-domain/provider/retailer/matching/basket/comparison-domain/database coupling and protects accepted boundary direction;
- no persistence, UI, pantry, retailer activation or new acquisition behavior.

Acceptance proof:

- final reviewed head `396445c333ea369bed6d428b33f38f37765eff20`: **9/9 PR workflow groups SUCCESS**;
- read-only review **Looks good**, no P0/P1/P2/P3, no review threads;
- squash merge `89b9ef2ca95d07a7e4c964fdef38a9af1c5c3a43`;
- issue #115 closed `completed`;
- **8/8 post-merge normal push workflows SUCCESS**.

### M3.4 — Responsive Weekly Planning UI — NEXT

Goal: provide a Recipe/meal-by-day editor over the accepted composed WeeklyPlan→Comparison contract and expose canonical weekly shopping plus truthful retailer comparison.

Expected scope:

- consume generated `POST /api/v1/weekly-plan-comparison-previews` contract as the primary planner product boundary;
- add/remove/reorder weekly meal occurrences;
- choose day and target servings per occurrence without introducing a fixed meal-slot taxonomy;
- edit explicit Recipe ingredients;
- show canonical weekly shopping requirements before comparison;
- render accepted complete/uncertain/incomplete/unavailable retailer states without browser-side business recomputation;
- preserve manual-list and Recipe comparison journeys;
- use generated API/client types rather than duplicated frontend DTOs;
- desktop/mobile accessibility and Playwright RED-first coverage;
- deterministic E2E fixtures only; no live retailer traffic in ordinary browser acceptance.

Exit gate:

- authoritative UI/interaction design approved;
- browser/service TDD RED→GREEN;
- desktop/mobile accessibility, overflow, loading/error and critical-journey regression coverage green;
- no browser duplication of WeeklyPlan/Recipe/shopping/comparison semantics;
- exact-head 9/9 PR workflows + clean review;
- squash merge + 8/8 post-merge acceptance proof.

### M3.5 — Pantry / exclusions semantics — AFTER BASE PLANNER FLOW

Pantry/subtraction must be an explicit semantics layer with its own design and provenance rules. It must never silently mutate accepted M2.5/M3.1/M3.2/M3.3 behavior.

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
