# Roadmap

Updated: 2026-08-15

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

Goal: combine several meals into one coherent weekly plan, derive one canonical shopping projection, compare that projection across retailers and then add explicit pantry/exclusion semantics without contaminating accepted planner, Recipe, Shopping or provider/runtime concerns.

### M3.1 — WeeklyPlan domain + deterministic shopping composition — COMPLETE / ACCEPTED

Acceptance: [`m3-1-weekly-plan-acceptance-2026-08-14.md`](m3-1-weekly-plan-acceptance-2026-08-14.md)  
Accepted squash merge: `13e09c63959b050d431cc913597fc868aa408718`.

Accepted result:

- immutable WeeklyPlan + ordered WeeklyMealOccurrence model;
- required Monday-through-Sunday metadata without fixed meal-slot taxonomy;
- repeated Recipe use through distinct occurrence IDs and explicit caller order;
- per-occurrence target servings;
- deterministic WeeklyPlan-scoped ShoppingList and planner provenance;
- accepted M2.5 remains authoritative for scaling/canonicalization/exact merge/order/ShoppingItem identity;
- identity/provenance drift fails closed;
- no persistence, API, UI, pantry, provider/retailer or comparison scope.

Acceptance proof: final reviewed head `ec1af08cbaf373f79c54858e9654451cebc4f009` passed **9/9 PR workflow groups**, clean review/no threads; merge `13e09c63959b050d431cc913597fc868aa408718` passed **8/8 post-merge workflows**.

### M3.2 — Stateless WeeklyPlan shopping preview API — COMPLETE / ACCEPTED

Acceptance: [`m3-2-weekly-plan-shopping-preview-acceptance-2026-08-14.md`](m3-2-weekly-plan-shopping-preview-acceptance-2026-08-14.md)  
Accepted squash merge: `9682ad1230910fc268ca3cddd8601a3fad7b100e`.

Accepted boundary: `POST /api/v1/weekly-plan-shopping-previews`.

Accepted result:

- stateless `1..35` ordered weekly occurrences with server-owned identities;
- nested Recipe validation/normalization reused from accepted M2.2;
- shopping composition delegated to accepted M3.1/M2.5;
- self-contained occurrence + Recipe + ingredient provenance;
- sanitized problem details and generated OpenAPI/TypeScript contract;
- no persistence, comparison, UI, pantry, calendar or AI/fuzzy scope.

Acceptance proof: final reviewed head `250aedb85b675036ffcb20e96a67db1afc03167a` passed **9/9 PR workflow groups**, clean review/no threads; merge `9682ad1230910fc268ca3cddd8601a3fad7b100e` passed **8/8 post-merge workflows**.

### M3.3 — WeeklyPlan → Comparison composition — COMPLETE / ACCEPTED

Acceptance: [`m3-3-weekly-plan-comparison-preview-acceptance-2026-08-14.md`](m3-3-weekly-plan-comparison-preview-acceptance-2026-08-14.md)  
Accepted squash merge: `89b9ef2ca95d07a7e4c964fdef38a9af1c5c3a43`.

Accepted boundary: `POST /api/v1/weekly-plan-comparison-previews`.

Accepted result:

- one stateless request combines provider-neutral locality with accepted M3.2 WeeklyPlan input;
- M3.2 remains authoritative for planner/Recipe shopping projection and provenance;
- generated ShoppingItem identity/order/requirement/canonical quantity are preserved into comparison;
- ComparisonPreview remains authoritative for locality, retailer readiness, production-access gating, runtime evidence, matching, package/basket semantics and truthful result states;
- cross-boundary drift fails closed;
- generated client exposes the composed contract without persistence/provider coupling.

Acceptance proof: final reviewed head `396445c333ea369bed6d428b33f38f37765eff20` passed **9/9 PR workflow groups**, clean review/no threads; merge `89b9ef2ca95d07a7e4c964fdef38a9af1c5c3a43` passed **8/8 post-merge workflows**.

### M3.4 — Responsive Weekly Planning UI — COMPLETE / ACCEPTED

Authoritative design: [`superpowers/specs/2026-08-15-m3-4-responsive-weekly-planning-ui-design.md`](superpowers/specs/2026-08-15-m3-4-responsive-weekly-planning-ui-design.md)  
Implementation plan: [`superpowers/plans/2026-08-15-m3-4-responsive-weekly-planning-ui.md`](superpowers/plans/2026-08-15-m3-4-responsive-weekly-planning-ui.md)  
Shipping evidence: [`superpowers/plans/2026-08-15-m3-4-responsive-weekly-planning-ui-shipping.md`](superpowers/plans/2026-08-15-m3-4-responsive-weekly-planning-ui-shipping.md)  
Acceptance: [`m3-4-responsive-weekly-planning-ui-acceptance-2026-08-15.md`](m3-4-responsive-weekly-planning-ui-acceptance-2026-08-15.md)  
Accepted squash merge: `1201030aed45075c676f796920b6268cdcf8e036`.

Accepted product flow:

`ordered weekly meal occurrences + locality → POST /api/v1/weekly-plan-comparison-previews → canonical weekly shopping requirements → truthful retailer comparison`

Accepted result:

- Weekly Planning becomes the primary homepage journey while Recipe and manual-list comparison remain secondary paths;
- add/remove/reorder `1..35` meal occurrences while explicit caller order stays independent from day metadata;
- choose Monday..Sunday and target servings per occurrence without fixed meal-slot taxonomy;
- edit explicit nested Recipe ingredients using generated request vocabulary;
- server identities remain server-owned; local browser row keys are React-only;
- generated M3.3 client contract is the only planner-comparison transport and frontend DTO/domain duplication is prohibited;
- canonical weekly shopping renders before retailer comparison in accepted server order;
- existing truthful comparison projection is reused without browser-side serving/merge/matching/package/total/winner recomputation;
- fail-closed transport covers missing configuration, timeout, network and unexpected-service behavior;
- deterministic desktop/mobile/accessibility Playwright covers weekly critical flow, reorder semantics, 390px no-overflow, keyboard focus, unavailable state and Recipe/manual regressions;
- ordinary browser acceptance makes no live retailer request.

Acceptance proof:

- final reviewed head `12973650f274f76ec54865be41963843afcb4558`: **9/9 PR workflow groups SUCCESS**;
- read-only review **Looks good**, no P0/P1/P2/P3 or nitpicks, no review threads;
- squash merge `1201030aed45075c676f796920b6268cdcf8e036`;
- issue #118 closed `completed`;
- **8/8 post-merge normal push workflows SUCCESS**.

### M3.5 — Pantry / exclusions semantics — IN PROGRESS

Goal: subtract explicitly known-at-home requirements from the accepted weekly shopping projection without hidden ingredient loss, identity drift or unexplained quantity mutation, then expose that accepted semantics through stateless WeeklyPlan/comparison boundaries and responsive controls.

#### M3.5.1 — Pure Pantry subtraction semantics — COMPLETE / ACCEPTED

Authoritative design: [`superpowers/specs/2026-08-15-m3-5-1-pantry-subtraction-semantics-design.md`](superpowers/specs/2026-08-15-m3-5-1-pantry-subtraction-semantics-design.md)  
Implementation plan: [`superpowers/plans/2026-08-15-m3-5-1-pantry-subtraction-semantics.md`](superpowers/plans/2026-08-15-m3-5-1-pantry-subtraction-semantics.md)  
Shipping evidence: [`superpowers/plans/2026-08-15-m3-5-1-pantry-subtraction-semantics-shipping.md`](superpowers/plans/2026-08-15-m3-5-1-pantry-subtraction-semantics-shipping.md)  
Acceptance: [`m3-5-1-pantry-subtraction-semantics-acceptance-2026-08-15.md`](m3-5-1-pantry-subtraction-semantics-acceptance-2026-08-15.md)  
Accepted squash merge: `bcc644bb243a63941e7629755f1b3196d94332c2`.

Accepted semantics:

- a pure provider-neutral `pantry` package adjusts an already canonical ShoppingList and depends only on accepted Shopping types;
- Pantry matching reuses exact `(ShoppingRequirement, canonical QuantityUnit)` semantics and existing kg→g / l→ml canonicalization;
- duplicate Pantry rows aggregate by that exact key and stock is consumed at most once in source ShoppingList order;
- every row consumes `min(required, available)`; no zero/negative Shopping quantity is emitted;
- unmatched rows remain unchanged, partial rows preserve Shopping identity/order and reduce only quantity, fully covered rows disappear from the remaining ShoppingList;
- all source rows retain immutable ordered `UNCHANGED / PARTIALLY_COVERED / FULLY_COVERED` audit evidence;
- no fuzzy/synonym/AI/case-folding equivalence, endpoint, UI, persistence, retailer/provider behavior or accepted M3.1–M3.4 production mutation was introduced;
- explicit omit-all exclusions remain a separate semantic decision rather than zero/negative Pantry stock.

Acceptance proof:

- final reviewed PR #122 head `b48a88e4ded457f81245223b75477be16ccf3051`: **9/9 PR workflow groups SUCCESS**;
- read-only review **Looks good**, no P0/P1/P2/P3 or nitpicks, no unresolved review threads;
- squash merge `bcc644bb243a63941e7629755f1b3196d94332c2`;
- issue #121 closed `completed`;
- exactly **8/8 post-merge normal push workflows SUCCESS, 0 failures**;
- Web CI/Web E2E and CodeQL Java + JavaScript/TypeScript succeeded on the accepted merge.

#### M3.5.2 — Stateless Pantry-aware WeeklyPlan shopping composition/API — NEXT

Goal: expose the accepted M3.5.1 semantics above the accepted M3.2 WeeklyPlan shopping projection without changing the existing M3.2 or M3.3 contracts silently.

Required design/implementation boundary:

1. introduce a **new stateless application/API composition**, rather than adding hidden behavior to `POST /api/v1/weekly-plan-shopping-previews`;
2. accept the existing ordered WeeklyPlan request vocabulary plus explicit request-scoped Pantry rows using accepted Shopping requirement/quantity vocabulary;
3. construct the canonical WeeklyPlan shopping projection through accepted M3.2/M3.1 semantics, then apply accepted `PantryShoppingListAdjuster` exactly once;
4. return inspectable original weekly Shopping requirements, Pantry adjustment evidence and remaining Shopping requirements so full coverage never becomes hidden ingredient loss;
5. preserve generated source ShoppingItem IDs/provenance across the adjustment bridge and fail closed on identity/order/requirement/quantity drift;
6. synchronize sanitized validation problems, OpenAPI 3.1 and generated TypeScript client without provider/database coupling;
7. keep persistence/history, retailer comparison and browser UI out of M3.5.2;
8. protect accepted M3.2/M3.3 endpoints with regression/architecture tests proving their behavior is unchanged.

Exit gate:

- authoritative design before production code;
- deterministic RED→GREEN application, provenance, HTTP/OpenAPI/generated-client and architecture coverage;
- no live retailer requests;
- exact-head **9/9 PR workflows + clean review**;
- squash merge + **8/8 post-merge workflows**.

Planned later slices:

- **M3.5.3 Pantry-aware WeeklyPlan → Comparison composition** — feed only the accepted remaining Shopping projection to ComparisonPreview while preserving original/pantry/remaining audit evidence;
- **M3.5.4 Responsive Pantry controls** — request-scoped Pantry editing plus inspectable before/after weekly shopping output before retailer comparison;
- explicit omit-all exclusion rules only after a separate semantic design proves they are needed and cannot be confused with Pantry stock.

Persistence/saved-plan history remains deferred until repeat-use evidence demonstrates product value and does not belong in the stateless Pantry composition slices by default.

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