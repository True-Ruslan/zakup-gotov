# Roadmap

Updated: 2026-08-14

The roadmap is evidence-driven. Milestones change when integration evidence, product behavior or production constraints contradict an earlier assumption.

## Product connectivity invariant

Zakup Gotov targets **universal connectivity for the retailer registry**, not a permanently curated subset of easy integrations. Every retailer/banner remains mandatory coverage work until at least one reproducible acquisition path exists.

Durable design: [`superpowers/specs/2026-08-10-universal-retailer-connectivity-design.md`](superpowers/specs/2026-08-10-universal-retailer-connectivity-design.md).

Deterministic product/core progress does not imply that every retailer is production-ready. Technical coverage, production access and product-domain maturity remain separate dimensions.

## M0 — Product & Integration Discovery — COMPLETE

Decision: **GO to M1** — [`superpowers/specs/2026-08-12-m0-to-m1-go-decision.md`](superpowers/specs/2026-08-12-m0-to-m1-go-decision.md).

Exit evidence: accepted Perekrestok/Pyaterochka browser-bridge paths, Magnit public-web path, two acquisition modes, deterministic sanitized verification and retailer-neutral architecture.

## M1 — Shopping Core — COMPLETE / ACCEPTED

Acceptance: [`m1-shopping-core-acceptance-2026-08-13.md`](m1-shopping-core-acceptance-2026-08-13.md).  
Accepted final hardening baseline: `779d0b219a13e0bf82263a1e655fb732553ed5fe`.

Accepted guarantees:

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

Goal: make recipes a first-class deterministic source of shopping requirements and expose a usable Recipe-first product flow without weakening accepted Shopping Core invariants.

### M2.1 — Recipe domain and Recipe → ShoppingList — COMPLETE / ACCEPTED

Design: [`superpowers/specs/2026-08-13-m2-1-recipe-domain-design.md`](superpowers/specs/2026-08-13-m2-1-recipe-domain-design.md)  
Shipping evidence: [`superpowers/plans/2026-08-13-m2-1-recipe-domain-shipping.md`](superpowers/plans/2026-08-13-m2-1-recipe-domain-shipping.md)  
Accepted squash merge: `423eb14f7c565bbe264257a92df89a6b42d0d158`.

Accepted result: immutable Recipe semantics, exact-safe serving scaling, canonical Shopping quantities, deterministic exact merge/order/ShoppingItem identities and ordered provenance outside Shopping Core.

### M2.2 — Stateless Recipe application/API boundary — COMPLETE / ACCEPTED

Design: [`superpowers/specs/2026-08-13-m2-2-recipe-shopping-preview-design-v2.md`](superpowers/specs/2026-08-13-m2-2-recipe-shopping-preview-design-v2.md)  
Acceptance: [`m2-2-recipe-shopping-preview-acceptance-2026-08-14.md`](m2-2-recipe-shopping-preview-acceptance-2026-08-14.md)  
Accepted squash merge: `8f0c1d8d31cfc1673656780a7989512d38788aff`.

Accepted boundary:

`POST /api/v1/recipe-shopping-previews`

Accepted result: stateless server-owned transient identities, strict serving validation, self-contained Recipe provenance, OpenAPI/generated TypeScript contract and no persistence/retailer traffic.

### M2.3 — Composed Recipe → Comparison flow — COMPLETE / ACCEPTED

Design: [`superpowers/specs/2026-08-14-m2-3-recipe-comparison-preview-design.md`](superpowers/specs/2026-08-14-m2-3-recipe-comparison-preview-design.md)  
Acceptance: [`m2-3-recipe-comparison-preview-acceptance-2026-08-14.md`](m2-3-recipe-comparison-preview-acceptance-2026-08-14.md)  
Accepted squash merge: `15a086d135f40277c655b39549c3e7a04c2e914e`.

Accepted boundary:

`POST /api/v1/recipe-comparison-previews`

Accepted result: one stateless request composes accepted Recipe conversion with accepted comparison semantics while preserving ShoppingItem identity/order/requirement/canonical quantity and production-access gating.

### M2.4 — Responsive Recipe UI — COMPLETE / ACCEPTED

Design: [`superpowers/specs/2026-08-14-m2-4-responsive-recipe-ui-design.md`](superpowers/specs/2026-08-14-m2-4-responsive-recipe-ui-design.md)  
Implementation plan: [`superpowers/plans/2026-08-14-m2-4-responsive-recipe-ui.md`](superpowers/plans/2026-08-14-m2-4-responsive-recipe-ui.md)  
Shipping evidence: [`superpowers/plans/2026-08-14-m2-4-responsive-recipe-ui-shipping.md`](superpowers/plans/2026-08-14-m2-4-responsive-recipe-ui-shipping.md)  
Acceptance: [`m2-4-responsive-recipe-ui-acceptance-2026-08-14.md`](m2-4-responsive-recipe-ui-acceptance-2026-08-14.md)  
Accepted squash merge: `aba20c9cee263a683c0d4383ad840d7415851861`.

Accepted primary journey:

`Recipe title/servings + ingredient editing + locality → composed Recipe comparison endpoint → generated canonical shopping requirements → truthful retailer comparison`

Accepted result:

- Recipe-first responsive homepage experience;
- manual list comparison remains a secondary path;
- generated TypeScript contract is the only transport contract;
- browser owns form state/preflight only and does not duplicate Recipe/comparison semantics;
- canonical generated shopping requirements render before retailer results;
- service failure is fail-closed with no fabricated result;
- transient internal IDs are hidden;
- desktop/mobile Playwright covers scaling, generated list, unavailable state, keyboard focus and horizontal-overflow safety;
- deterministic retailer evidence remains E2E-only.

Acceptance proof:

- reviewed exact PR head `fb069d64b96f0d989951e67fd62b793277453024`: 9/9 normal PR workflow groups SUCCESS;
- read-only review `REVIEWED_READY / Looks good`; no unresolved P0/P1/P2; review threads empty;
- squash merge `main=aba20c9cee263a683c0d4383ad840d7415851861`;
- #103 closed `completed`;
- **8/8 post-merge normal push workflows SUCCESS, 0 failures**.

### M2.5 — Deterministic multi-recipe aggregation — NEXT

Goal: provide the deterministic merge/provenance layer required by M3 Weekly Planning without prematurely introducing persistence, AI semantics or planner UI.

Target flow:

`several accepted Recipe inputs/conversions → one aggregated canonical ShoppingList + per-recipe/per-ingredient provenance`

Recommended default direction:

- remain stateless first;
- aggregate accepted Recipe semantics rather than inventing a second Recipe model;
- preserve exact normalized requirement + canonical unit as the only automatic merge key;
- preserve deterministic input order and first compatible group occurrence;
- derive aggregate ShoppingItem identities deterministically from aggregate-list identity + requirement + canonical unit;
- retain complete lineage back to RecipeId + RecipeIngredientId outside neutral Shopping Core types;
- reject duplicate/colliding Recipe or ingredient identities fail-closed;
- do not add case folding, synonyms, fuzzy/semantic/AI equivalence;
- do not require database persistence merely to aggregate multiple recipes;
- define a stateless application/API boundary only after the pure aggregation semantics are accepted.

Required design questions:

1. Should aggregation consume Recipe domain objects directly or accepted `RecipeShoppingListConversion` outputs?
2. What aggregate identity inputs make ShoppingItem IDs stable across target-serving changes while remaining list-scoped?
3. What public provenance shape is useful for M3 without leaking internal maps?
4. How should the aggregator represent the same source Recipe included twice intentionally versus an accidental duplicate identity?
5. What maximum recipe/item bounds keep stateless API behavior predictable?
6. Does M2.5 end at pure domain/application aggregation, or include the smallest API contract needed for M3 planner work?

Exit gate:

- pure deterministic aggregation behavior accepted with explicit RED→GREEN evidence;
- provenance/order/identity invariants covered;
- full API architecture/verification green;
- any public contract generated from OpenAPI, not handwritten in web;
- no persistence/fuzzy/AI scope expansion;
- exact-head review/CI/merge/post-merge acceptance proof.

### M2 exit direction

After M2.5 acceptance, M2 has the complete deterministic foundation for M3:

- one Recipe → canonical ShoppingList;
- stateless Recipe application boundary;
- Recipe → retailer comparison composition;
- responsive Recipe-first UI;
- multiple recipes → one canonical aggregate ShoppingList with lineage.

Then advance to **M3 Weekly Planning** rather than extending M2 with planner-specific UI/state.

## Parallel connectivity / operational work

Continue without blocking deterministic M2 work unless evidence invalidates accepted core assumptions:

- **#54** browser-bridge persistent-session/store-change/SPA lifecycle hardening;
- **#36** Kuper supported aggregator investigation;
- Chizhik, Ozon Fresh, Samokat, Lenta and VkusVill onboarding/hardening;
- structured package semantics for additional providers only where source evidence proves them;
- retailer-specific production-access/right-to-operate decisions before activation;
- successful real **`v0.1.0-rc.3`** release event with final image promotion, SBOM/attestation and digest smoke evidence.

## M3 — Weekly Planning

Goal: combine several meals into one coherent shopping-requirement set and let the user review the resulting week before retailer comparison.

Scope after M2.5:

- weekly planner composition over accepted multi-recipe aggregation;
- per-meal serving choices;
- deterministic duplicate merging/unit conversion inherited from M2.5;
- pantry/exclusion controls as an explicit new semantics layer;
- shopping-list review before comparison;
- persistence only if weekly-plan reuse/history becomes a demonstrated product requirement.

## M4 — Basket Optimization

Goal: optimize real checkout cost rather than naive SKU sums.

Scope: richer package/substitute optimization, fees, minimum orders, single-store convenience, future multi-store lowest-total-cost mode and confidence/freshness penalties.

## M5 — Productization

Goal: reliable repeat use with privacy-aware accounts/preferences, analytics abstraction, feature flags, provider health monitoring and production provider activation only after access constraints are resolved.

## M6 — Native Mobile

Goal: Android/iOS clients using the shared API vocabulary and generated client contracts after the web/core product is stable.

## Guiding rule

Do not add infrastructure or semantics because they are convenient. Add them only when evidence makes the behavior correct, explainable and worth the operational cost.
