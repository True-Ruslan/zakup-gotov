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
- production access scopes acquisition before runtime evidence loading;
- evidence outside requested retailer scope is a contract violation;
- ordinary CI remains retailer-network-free.

## M2 — Recipes — COMPLETE / ACCEPTED

Goal achieved: recipes are a first-class deterministic source of shopping requirements from one Recipe through responsive product UI and multi-Recipe aggregation.

### M2.1 — Recipe domain and Recipe → ShoppingList — COMPLETE / ACCEPTED

Design: [`superpowers/specs/2026-08-13-m2-1-recipe-domain-design.md`](superpowers/specs/2026-08-13-m2-1-recipe-domain-design.md)  
Shipping evidence: [`superpowers/plans/2026-08-13-m2-1-recipe-domain-shipping.md`](superpowers/plans/2026-08-13-m2-1-recipe-domain-shipping.md)  
Accepted merge: `423eb14f7c565bbe264257a92df89a6b42d0d158`.

Result: immutable Recipe semantics, exact-safe serving scaling, canonical Shopping quantities, deterministic exact merge/order/ShoppingItem identities and ordered provenance outside Shopping Core.

### M2.2 — Stateless Recipe application/API boundary — COMPLETE / ACCEPTED

Design: [`superpowers/specs/2026-08-13-m2-2-recipe-shopping-preview-design-v2.md`](superpowers/specs/2026-08-13-m2-2-recipe-shopping-preview-design-v2.md)  
Acceptance: [`m2-2-recipe-shopping-preview-acceptance-2026-08-14.md`](m2-2-recipe-shopping-preview-acceptance-2026-08-14.md)  
Accepted merge: `8f0c1d8d31cfc1673656780a7989512d38788aff`.

Boundary: `POST /api/v1/recipe-shopping-previews`.

Result: stateless server-owned transient identities, strict serving validation, self-contained Recipe provenance, OpenAPI/generated TypeScript contract and no persistence/retailer traffic.

### M2.3 — Composed Recipe → Comparison flow — COMPLETE / ACCEPTED

Design: [`superpowers/specs/2026-08-14-m2-3-recipe-comparison-preview-design.md`](superpowers/specs/2026-08-14-m2-3-recipe-comparison-preview-design.md)  
Acceptance: [`m2-3-recipe-comparison-preview-acceptance-2026-08-14.md`](m2-3-recipe-comparison-preview-acceptance-2026-08-14.md)  
Accepted merge: `15a086d135f40277c655b39549c3e7a04c2e914e`.

Boundary: `POST /api/v1/recipe-comparison-previews`.

Result: one stateless request composes accepted Recipe conversion with accepted comparison semantics while preserving ShoppingItem identity/order/requirement/canonical quantity and production-access gating.

### M2.4 — Responsive Recipe UI — COMPLETE / ACCEPTED

Design: [`superpowers/specs/2026-08-14-m2-4-responsive-recipe-ui-design.md`](superpowers/specs/2026-08-14-m2-4-responsive-recipe-ui-design.md)  
Shipping evidence: [`superpowers/plans/2026-08-14-m2-4-responsive-recipe-ui-shipping.md`](superpowers/plans/2026-08-14-m2-4-responsive-recipe-ui-shipping.md)  
Acceptance: [`m2-4-responsive-recipe-ui-acceptance-2026-08-14.md`](m2-4-responsive-recipe-ui-acceptance-2026-08-14.md)  
Accepted merge: `aba20c9cee263a683c0d4383ad840d7415851861`.

Result: Recipe-first responsive homepage flow over generated contracts, canonical generated shopping output, truthful retailer comparison, fail-closed errors, desktop/mobile accessibility regression and preserved manual-list comparison.

### M2.5 — Deterministic multi-recipe aggregation — COMPLETE / ACCEPTED

Design: [`superpowers/specs/2026-08-14-m2-5-multi-recipe-aggregation-design.md`](superpowers/specs/2026-08-14-m2-5-multi-recipe-aggregation-design.md)  
Implementation plan: [`superpowers/plans/2026-08-14-m2-5-multi-recipe-aggregation.md`](superpowers/plans/2026-08-14-m2-5-multi-recipe-aggregation.md)  
Shipping evidence: [`superpowers/plans/2026-08-14-m2-5-multi-recipe-aggregation-shipping.md`](superpowers/plans/2026-08-14-m2-5-multi-recipe-aggregation-shipping.md)  
Acceptance: [`m2-5-multi-recipe-aggregation-acceptance-2026-08-14.md`](m2-5-multi-recipe-aggregation-acceptance-2026-08-14.md)  
Accepted merge: `0854fc5bf76ad2976986537d6b4f5f3b8ebd18f0`.

Accepted flow:

`ordered Recipe occurrences + target servings → accepted per-recipe conversion → one canonical aggregate ShoppingList + occurrence-aware provenance`

Accepted guarantees:

- Recipe occurrence identity is distinct from Recipe identity;
- the same Recipe may be included repeatedly under distinct occurrence IDs;
- serving scaling and source-unit canonicalization remain delegated to accepted M2.1 conversion;
- cross-Recipe merge remains exact normalized requirement + canonical unit only;
- aggregate amounts are exact and order is first-compatible-occurrence deterministic;
- aggregate ShoppingItem ID remains aggregate-list + requirement + canonical-unit scoped and independent of amount/target servings;
- occurrence-aware provenance is ordered, complete and deeply immutable;
- empty/duplicate occurrence sets and identity collisions fail closed;
- accepted single-Recipe UUID semantics remain byte-for-byte stable after shared-helper extraction;
- no API/UI/persistence/planner/provider/retailer scope was introduced.

Acceptance proof:

- reviewed exact head `a6e1095696ebfd67fafe7675a37b125ae02b3170`: 9/9 normal PR workflow groups SUCCESS;
- review `REVIEWED_READY / Looks good`; no unresolved P0/P1/P2; review threads empty;
- squash merge `main=0854fc5bf76ad2976986537d6b4f5f3b8ebd18f0`;
- #106 closed `completed`;
- **8/8 post-merge normal push workflows SUCCESS, 0 failures**.

### M2 exit decision

M2 has the complete deterministic foundation required by weekly planning:

- one Recipe → canonical ShoppingList;
- stateless Recipe application boundary;
- Recipe → retailer comparison composition;
- responsive Recipe-first UI;
- multiple Recipe occurrences → one canonical aggregate ShoppingList with unambiguous lineage.

Decision: **advance to M3 Weekly Planning**.

## M3 — Weekly Planning — CURRENT / DESIGN NEXT

Goal: combine several meal occurrences into one coherent, reviewable weekly shopping requirement set while keeping planner semantics separate from accepted Recipe/Shopping aggregation rules.

### Recommended M3.1 first slice

Start with a **pure WeeklyPlan domain/application composition**, not persistence or UI.

Target model direction:

`WeeklyPlan identity + ordered meal occurrences + optional day/slot ownership + per-occurrence target servings → accepted M2.5 aggregation → reviewable weekly ShoppingList projection`

Key design questions:

1. Should the first WeeklyPlan aggregate model explicit days/meal slots immediately, or start with an ordered meal-occurrence list and add calendar placement separately?
2. Planner occurrence identity should be distinct at the WeeklyPlan boundary while mapping deterministically to accepted `RecipeAggregationEntryId` internally.
3. How are user-facing meal labels/day labels represented without contaminating Recipe or Shopping Core?
4. Which constraints are true domain invariants: non-empty plan, max occurrences, duplicate planner occurrence IDs, optional repeated Recipe entries, ordering?
5. Should the first slice remain stateless? **Default: yes** until saved weekly plans/history demonstrate persistence value.
6. Pantry/exclusions must be a later explicit semantics layer because removing/subtracting requirements changes lineage and should not be hidden inside M2.5 aggregation.
7. After pure WeeklyPlan semantics are accepted, define the smallest stateless API/OpenAPI contract, then responsive planner UI with RED-first desktop/mobile Playwright.

Suggested M3 sequence:

- **M3.1** WeeklyPlan domain + deterministic composition over M2.5;
- **M3.2** stateless WeeklyPlan application/API boundary with complete occurrence provenance;
- **M3.3** responsive weekly planner/review UI;
- **M3.4** pantry/exclusion semantics as a dedicated deterministic layer;
- persistence only when repeat-use evidence justifies saved weekly plans.

M3 must not silently change M2.5 exact merge, identity or provenance rules.

## Parallel connectivity / operational work

Continue without blocking deterministic M3 work unless evidence invalidates accepted core assumptions:

- **#54** browser-bridge persistent-session/store-change/SPA lifecycle hardening;
- **#36** Kuper supported aggregator investigation;
- Chizhik, Ozon Fresh, Samokat, Lenta and VkusVill onboarding/hardening;
- structured package semantics for additional providers only where source evidence proves them;
- retailer-specific production-access/right-to-operate decisions before activation;
- successful real **`v0.1.0-rc.3`** release event with final image promotion, SBOM/attestation and digest smoke evidence.

## M4 — Basket Optimization

Goal: optimize real checkout cost rather than naive SKU sums.

Scope: richer package/substitute optimization, fees, minimum orders, single-store convenience, future multi-store lowest-total-cost mode and confidence/freshness penalties.

## M5 — Productization

Goal: reliable repeat use with privacy-aware accounts/preferences, analytics abstraction, feature flags, provider health monitoring and production provider activation only after access constraints are resolved.

## M6 — Native Mobile

Goal: Android/iOS clients using the shared API vocabulary and generated client contracts after the web/core product is stable.

## Guiding rule

Do not add infrastructure or semantics because they are convenient. Add them only when evidence makes the behavior correct, explainable and worth the operational cost.
