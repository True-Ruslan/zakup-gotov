# Roadmap

Updated: 2026-08-14

The roadmap is evidence-driven. Milestones change when integration evidence, product behavior or production constraints contradict an earlier assumption.

## Product connectivity invariant

Zakup Gotov targets **universal connectivity for the retailer registry**, not a permanently curated subset of easy integrations. Every retailer/banner remains mandatory coverage work until at least one reproducible acquisition path exists.

Durable design: [`superpowers/specs/2026-08-10-universal-retailer-connectivity-design.md`](superpowers/specs/2026-08-10-universal-retailer-connectivity-design.md).

Advancing deterministic product/core milestones does not imply that every retailer is production-ready. Technical coverage, production access and product-domain maturity remain separate dimensions.

## M0 — Product & Integration Discovery — COMPLETE

Decision: **GO to M1** — [`superpowers/specs/2026-08-12-m0-to-m1-go-decision.md`](superpowers/specs/2026-08-12-m0-to-m1-go-decision.md).

Exit evidence: accepted Perekrestok/Pyaterochka browser-bridge paths, Magnit public-web path, two acquisition modes, deterministic sanitized verification and retailer-neutral architecture.

## M1 — Shopping Core — COMPLETE / ACCEPTED

Acceptance decision: [`m1-shopping-core-acceptance-2026-08-13.md`](m1-shopping-core-acceptance-2026-08-13.md).

Accepted final hardening baseline: `779d0b219a13e0bf82263a1e655fb732553ed5fe`.

Accepted sequence: #72 retailer registry, #73 shopping list/quantities, #74 provider/path orchestration, #75 location/fulfillment, #76 snapshots, #77 matching, #78 basket quote, #79 failure/coverage/freshness boundary, #80 critical journey, #81 package plumbing, #82/#83/#85 Magnit package evidence, #86/#87/#69 Magnit location resolution, #89/#70 production-access decision, and #91/#90 pre-acquisition production-access enforcement/final acceptance.

### M1 exit guarantees

- all eight canonical retailers remain explicit;
- complete / uncertain / incomplete / unavailable states are distinct;
- unmatched, ambiguous, package-unknown and unit-mismatch paths fail safely;
- `UNKNOWN` availability stays uncertain;
- incomplete baskets cannot expose misleading complete totals or hidden winners;
- technical connectivity and production access remain independent;
- production access scopes acquisition **before** runtime evidence loading;
- blocked/pending/discovery retailers cannot enter evidence-source request scope;
- evidence outside requested retailer scope is a contract violation;
- ordinary CI remains retailer-network-free.

**GO to M2 Recipes for deterministic product/core development.** This does not claim production retailer completeness.

## M2 — Recipes — CURRENT

Goal: make recipes a first-class deterministic source of shopping requirements without weakening accepted Shopping Core invariants.

### M2.1 — Recipe domain and Recipe → ShoppingList — COMPLETE / ACCEPTED (#94 / #93)

Approved design: [`superpowers/specs/2026-08-13-m2-1-recipe-domain-design.md`](superpowers/specs/2026-08-13-m2-1-recipe-domain-design.md).  
Implementation plan: [`superpowers/plans/2026-08-13-m2-1-recipe-domain.md`](superpowers/plans/2026-08-13-m2-1-recipe-domain.md).  
Shipping/acceptance evidence: [`superpowers/plans/2026-08-13-m2-1-recipe-domain-shipping.md`](superpowers/plans/2026-08-13-m2-1-recipe-domain-shipping.md).

Accepted squash merge: `423eb14f7c565bbe264257a92df89a6b42d0d158`.

Post-merge proof on exact `main`:

- 8 push workflow runs total;
- 8/8 completed `success`;
- 0 failures;
- #93 closed `completed`.

#### Accepted behavior

- immutable `Recipe` aggregate with stable recipe/ingredient UUID identities;
- normalized non-blank title and positive integer servings;
- ingredients reuse existing `ShoppingRequirement` + `Quantity` semantics;
- pure Recipe → ShoppingList conversion without Spring/network/database/clock dependencies;
- group amounts summed before serving scaling;
- exact terminating decimal division and deterministic `MathContext.DECIMAL128` fallback for non-terminating ratios;
- exact-safe merge only by normalized requirement + canonical unit;
- no case folding, synonym matching, fuzzy matching, category inference or AI equivalence;
- deterministic output order by first merge-group occurrence;
- deterministic list-scoped `ShoppingItemId` independent of quantity/target servings;
- deep-immutable ordered `RecipeId + RecipeIngredientId` provenance outside Shopping Core;
- generated-ID collision across different merge keys fails closed;
- Shopping Core production types remain recipe-agnostic.

#### Verification

- all planned M2.1 RED→GREEN cycles complete;
- full API `verify` PASS including Spring Modulith architecture verification;
- reviewed implementation head `734ed53712b4327039eabfb358548828aa1a1dbe`: 9/9 PR groups success;
- code+docs head `250d00f10b1c51fee0826356dfb95f8e7b853c50`: 9/9 success;
- final shipping head `512be04a2a0147d9787465481388e6847a20d69d`: 9/9 success;
- independent review: **Looks good**, no P0/P1/P2;
- merged `main=423eb14f7c565bbe264257a92df89a6b42d0d158`: 8/8 push workflows success.

#### Explicit non-goals preserved

- REST/OpenAPI/generated-client contract;
- persistence;
- recipe UI;
- AI/NLP or arbitrary web import;
- fuzzy/case-insensitive ingredient equivalence;
- nutritional optimization;
- pantry prediction;
- fractional servings;
- multi-recipe aggregation.

### M2.2 — Stateless Recipe application/API boundary — COMPLETE / ACCEPTED (#97 / #96)

Authoritative design: [`superpowers/specs/2026-08-13-m2-2-recipe-shopping-preview-design-v2.md`](superpowers/specs/2026-08-13-m2-2-recipe-shopping-preview-design-v2.md).  
Execution plan: [`superpowers/plans/2026-08-13-m2-2-recipe-shopping-preview-v2.md`](superpowers/plans/2026-08-13-m2-2-recipe-shopping-preview-v2.md).  
Shipping evidence: [`superpowers/plans/2026-08-14-m2-2-recipe-shopping-preview-shipping.md`](superpowers/plans/2026-08-14-m2-2-recipe-shopping-preview-shipping.md).  
Acceptance decision: [`m2-2-recipe-shopping-preview-acceptance-2026-08-14.md`](m2-2-recipe-shopping-preview-acceptance-2026-08-14.md).

Accepted squash merge: `8f0c1d8d31cfc1673656780a7989512d38788aff`.

Accepted direction:

`POST /api/v1/recipe-shopping-previews`

`Recipe request → application validation/server-owned transient IDs → Recipe domain → RecipeShoppingListConverter → canonical ShoppingList projection`

#### Accepted scope

- stateless lifecycle; no saved Recipe persistence/CRUD;
- server-generated Recipe, ingredient and ShoppingList identities;
- request: normalized title, positive integer base/target servings, 1..100 explicit ingredient requirements/quantities;
- input quantities reuse Shopping Core units; output uses canonical Shopping Core quantities;
- strict JSON integer binding for servings; fractional ingredient quantities remain allowed;
- conversion semantics remain exclusively owned by accepted M2.1 `RecipeShoppingListConverter`;
- self-contained response provenance through ordered `sourceIngredientIds` resolving within the returned recipe;
- fail-closed projection invariants for mismatched list identity and missing/orphan/cross-recipe provenance;
- sanitized validation/unreadable-body 400 problem contract with internal failures left as server failures;
- OpenAPI 3.1 contract and generated TypeScript schema/client path;
- application architecture guards preventing provider/retailer/matching/basket/comparison/database coupling;
- no retailer traffic, location lookup, persistence, Recipe→Comparison orchestration, Recipe UI or fuzzy/AI matching.

#### Acceptance proof

- code checkpoint `b451dacbec41e3d7bd75ce4580f76fb6f86d5cae`: 13/13 individual checks across all 9 normal PR workflow groups;
- final reviewed head `318a48c569d0d001a4c27b5792e1681f7884e518`: all 9 normal PR workflow groups success;
- independent review: **Looks good**; no unresolved P0/P1/P2/P3; review threads empty;
- squash merge to `main=8f0c1d8d31cfc1673656780a7989512d38788aff`;
- issue #96 closed `completed`;
- exact merged main SHA: 8/8 normal push workflows success, 0 failures.

### M2.3 — Composed Recipe → Comparison flow — NEXT: DESIGN

Target deterministic path:

`Recipe input → recipe-shopping preview → generated shopping requirements → comparison preview`

Design goals:

- compose the two accepted stateless boundaries without duplicating recipe or comparison semantics;
- decide whether composition is an application service/internal orchestration seam or requires a new public endpoint;
- preserve self-contained recipe provenance while keeping retailer/provider internals out of the Recipe API;
- preserve existing production-access gating and fail-closed comparison states;
- define identity/provenance/error behavior across the composed boundary;
- add contract/application tests before introducing UI;
- keep retailer traffic absent from ordinary CI.

After the composed flow is accepted, implement the first real responsive Recipe UI using frontend component TDD and desktop/mobile Playwright RED-first.

Do **not** add persistence, saved recipes or fuzzy/AI ingestion merely because M2.2 makes them convenient. Those remain separate product decisions.

### M2 exit direction

After the composed comparison flow is accepted, extend toward the minimal usable Recipe UI and then deterministic multi-recipe aggregation needed by M3 Weekly Planning. Do not introduce fuzzy/AI ingestion until deterministic recipe semantics remain stable through these application boundaries.

## Parallel connectivity / operational work

Continue without blocking deterministic M2 work unless evidence invalidates accepted core assumptions:

- **#54** browser-bridge persistent-session/store-change/SPA lifecycle hardening;
- **#36** Kuper supported aggregator investigation;
- Chizhik, Ozon Fresh, Samokat, Lenta and VkusVill onboarding/hardening;
- structured package semantics for additional providers only where source evidence proves them;
- retailer-specific production-access/right-to-operate decisions before activation;
- successful real **`v0.1.0-rc.3`** release event with final image promotion, SBOM/attestation and digest smoke evidence.

## M3 — Weekly Planning

Goal: combine several meals into one coherent shopping-requirement set.

Scope: weekly planner, deterministic duplicate merging/unit conversion, pantry/exclusion controls and shopping-list review before comparison.

## M4 — Basket Optimization

Goal: optimize real checkout cost rather than naive SKU sums.

Scope: richer package/substitute optimization, fees, minimum orders, single-store convenience, future multi-store lowest-total-cost mode and confidence/freshness penalties.

## M5 — Productization

Goal: reliable repeat use with privacy-aware accounts/preferences, analytics abstraction, feature flags, provider health monitoring and production provider activation only after access constraints are resolved.

## M6 — Native Mobile

Goal: Android/iOS clients using the shared API vocabulary and generated client contracts after the web/core product is stable.

## Guiding rule

Do not add infrastructure or semantics because they are convenient. Add them only when evidence makes the behavior correct, explainable and worth the operational cost.
