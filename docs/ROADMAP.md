# Roadmap

Updated: 2026-08-13

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

### Accepted sequence

1. Retailer registry + independent technical/access states — #72
2. Shopping-list aggregate + canonical quantities — #73
3. Provider/path orchestration — #74
4. Location / fulfillment context — #75
5. Price / availability snapshots — #76
6. Deterministic product matching — #77
7. Single-store basket quote — #78
8. Failure / coverage / freshness product boundary — #79
9. Stateless critical comparison journey — #80
10. Structured package-evidence plumbing — #81
11. Magnit exact structured package semantics — #82
12. Magnit fixed-corpus instrumentation — #83
13. Magnit SKU-bound JSON-LD package evidence — #85
14. Magnit deterministic bbox → `shopCode` boundary — #86
15. Magnit merged-main location-resolution live proof — #87 / #69
16. Magnit production-access decision (`BLOCKED`) — #89 / #70
17. Pre-acquisition production-access enforcement — #91 / #90

### Exit properties

M1 now proves:

- all eight canonical retailers remain explicit and ordered;
- complete / uncertain / incomplete / unavailable states are distinct;
- unmatched, ambiguous, package-unknown and unit-mismatch paths fail safely;
- `UNKNOWN` availability stays uncertain;
- package evidence remains structured and source-bound through provider → snapshot → basket;
- mass / volume / count are not interchangeable;
- incomplete baskets cannot expose misleading complete totals or become hidden winners;
- precise addresses and provider/store implementation IDs remain outside product-facing comparison output;
- technical connectivity and production access remain independent;
- production access scopes acquisition **before** runtime evidence loading;
- blocked/pending/discovery retailers cannot enter evidence-source request scope;
- empty production-ready scope prevents source invocation entirely;
- evidence outside requested retailer scope is a contract violation;
- production preview remains no-op/live-free under the current registry;
- ordinary CI remains retailer-network-free.

### M1 GO decision

**GO to M2 Recipes for deterministic product/core development.**

This does not claim production retailer completeness. Retailer connectivity/access/release work continues in parallel and remains mandatory.

## M2 — Recipes — CURRENT

Goal: make recipes a first-class deterministic source of shopping requirements without weakening accepted Shopping Core invariants.

### M2.1 — Recipe domain and Recipe → ShoppingList — NEXT

First vertical slice:

`Recipe → explicit ingredients → canonical quantities → ShoppingList`

#### Domain scope

- stable `RecipeId`;
- recipe title;
- base servings;
- stable ingredient identity inside a recipe;
- ingredient display/requirement text;
- explicit quantity + unit;
- deterministic serving scaling;
- reuse Shopping Core quantity canonicalization;
- safe deterministic merging only when ingredient requirements are explicitly equivalent and units are compatible;
- provenance from generated shopping requirement back to recipe + ingredient;
- recipe → ShoppingList conversion.

#### Required behavior

- scaling from base servings to requested servings uses deterministic decimal arithmetic;
- incompatible units are never implicitly converted;
- zero/negative servings and quantities fail at the domain boundary;
- blank titles/ingredient text fail at construction;
- recipe ingredient ordering is stable;
- generated ShoppingList item identity/provenance is deterministic for the conversion request;
- merging cannot use fuzzy matching, category inference or AI;
- the converter depends on accepted shopping-domain primitives rather than duplicating quantity semantics.

#### Delivery sequence

1. design/spec with exact identity, scaling, merge and provenance rules;
2. TDD domain aggregate + ingredient model;
3. TDD serving scaler and Recipe → ShoppingList converter;
4. architecture guards;
5. after domain acceptance: API/OpenAPI/generated-client boundary;
6. responsive UI: create/edit recipe → choose servings → generate shopping list → comparison journey.

#### Initial non-goals

- AI recipe parsing;
- arbitrary recipe-web import;
- fuzzy ingredient equivalence;
- nutritional optimization;
- pantry prediction;
- image recognition;
- recommendation/ranking of recipes.

### M2 exit direction

After the first vertical slice is accepted, extend toward reusable recipe persistence/API UX and then recipe aggregation needed by M3 Weekly Planning. Do not introduce fuzzy/AI ingestion until deterministic recipe semantics are stable and tested.

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
