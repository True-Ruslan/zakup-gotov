# Project State

Updated: 2026-08-14

## Project

**Zakup Gotov** is a recipe-to-cart grocery comparison product. The target experience is to turn recipes, weekly meal plans or a manual grocery list into a location-aware comparison of complete retailer baskets while preserving price/availability evidence, package semantics, provenance, freshness and uncertainty.

Repository: `True-Ruslan/zakup-gotov`  
Visibility: Public  
Current phase: **M3 — Weekly Planning**

Milestone status:

- M0 Product & Integration Discovery — **COMPLETE**;
- M1 Shopping Core — **COMPLETE / ACCEPTED**;
- M2 Recipes — **COMPLETE / ACCEPTED**;
- M2.1 Recipe domain + Recipe → ShoppingList — **COMPLETE / ACCEPTED** (#94 / #93);
- M2.2 Recipe application/API boundary — **COMPLETE / ACCEPTED** (#97 / #96);
- M2.3 Recipe → Comparison composition — **COMPLETE / ACCEPTED** (#101 / #100);
- M2.4 Responsive Recipe UI — **COMPLETE / ACCEPTED** (#104 / #103);
- M2.5 Deterministic multi-recipe aggregation — **COMPLETE / ACCEPTED** (#107 / #106);
- M3.1 WeeklyPlan domain + deterministic shopping composition — **COMPLETE / ACCEPTED** (#110 / #109);
- M3.2 Stateless WeeklyPlan shopping-preview application/API boundary — **COMPLETE / ACCEPTED** (#113 / #112).

Current focus: **design M3.3 WeeklyPlan → Comparison composition**.

## Permanent connectivity rule

Universal Retailer Connectivity remains mandatory:

> Every retailer/banner in the target registry remains coverage work until at least one reproducible accepted acquisition path exists. A failed transport changes the acquisition mode under investigation; it does not remove the retailer from product scope.

Technical feasibility, production-access readiness and deterministic product/core maturity remain separate dimensions.

## Accepted milestone baselines

### M0 — Product & Integration Discovery — COMPLETE

Accepted evidence established Perekrestok/Pyaterochka browser-bridge acquisition, Magnit public-web technical feasibility, at least two acquisition modes, deterministic sanitized fixtures/E2E and provider-neutral retailer architecture.

M0 proves technical feasibility only. It does not establish blanket permission for recurring production acquisition.

### M1 — Shopping Core — COMPLETE / ACCEPTED

Acceptance: [`m1-shopping-core-acceptance-2026-08-13.md`](m1-shopping-core-acceptance-2026-08-13.md).  
Accepted final hardening baseline: `779d0b219a13e0bf82263a1e655fb732553ed5fe`.

Accepted core includes canonical retailer registry/readiness, shopping requirements and canonical quantities, location/fulfillment provenance, immutable offer/freshness evidence, deterministic matching, whole-package basket calculation, truthful complete/uncertain/incomplete/unavailable comparison states, pre-acquisition production-access gating, stateless comparison preview API and responsive manual-list journey.

### M2 — Recipes — COMPLETE / ACCEPTED

M2 established deterministic Recipe semantics from domain through responsive Recipe-first product flow.

Accepted baselines:

- M2.1 Recipe domain/conversion — merge `423eb14f7c565bbe264257a92df89a6b42d0d158`;
- M2.2 `POST /api/v1/recipe-shopping-previews` — acceptance [`m2-2-recipe-shopping-preview-acceptance-2026-08-14.md`](m2-2-recipe-shopping-preview-acceptance-2026-08-14.md), merge `8f0c1d8d31cfc1673656780a7989512d38788aff`;
- M2.3 `POST /api/v1/recipe-comparison-previews` — acceptance [`m2-3-recipe-comparison-preview-acceptance-2026-08-14.md`](m2-3-recipe-comparison-preview-acceptance-2026-08-14.md), merge `15a086d135f40277c655b39549c3e7a04c2e914e`;
- M2.4 responsive Recipe-first UI — acceptance [`m2-4-responsive-recipe-ui-acceptance-2026-08-14.md`](m2-4-responsive-recipe-ui-acceptance-2026-08-14.md), merge `aba20c9cee263a683c0d4383ad840d7415851861`;
- M2.5 deterministic multi-recipe aggregation — acceptance [`m2-5-multi-recipe-aggregation-acceptance-2026-08-14.md`](m2-5-multi-recipe-aggregation-acceptance-2026-08-14.md), merge `0854fc5bf76ad2976986537d6b4f5f3b8ebd18f0`.

M2 guarantees deterministic Recipe scaling/canonicalization, exact normalized requirement + canonical unit implicit merging, list-scoped deterministic ShoppingItem identity and explicit provenance outside neutral Shopping Core, with no implicit fuzzy/AI equivalence.

### M3.1 — WeeklyPlan domain + deterministic shopping composition — COMPLETE / ACCEPTED

Authoritative design: [`superpowers/specs/2026-08-14-m3-1-weekly-plan-domain-design.md`](superpowers/specs/2026-08-14-m3-1-weekly-plan-domain-design.md)  
Acceptance: [`m3-1-weekly-plan-acceptance-2026-08-14.md`](m3-1-weekly-plan-acceptance-2026-08-14.md)  
Accepted squash merge: `13e09c63959b050d431cc913597fc868aa408718`.

Accepted flow:

`ordered WeeklyPlan meal occurrences + target servings → accepted M2.5 aggregation → one canonical weekly ShoppingList + WeeklyMealOccurrence/RecipeIngredient provenance`

Accepted behavior includes immutable ordered WeeklyPlan occurrences, Monday-through-Sunday metadata without fixed meal slots, repeated Recipe use through distinct planner occurrence IDs, deterministic plan-scoped list/internal aggregation identities, delegation to accepted M2.5 scaling/merge/order/ShoppingItem identity, self-contained planner lineage and fail-closed identity/provenance drift.

### M3.2 — Stateless WeeklyPlan shopping preview — COMPLETE / ACCEPTED

Authoritative design: [`superpowers/specs/2026-08-14-m3-2-weekly-plan-shopping-preview-design.md`](superpowers/specs/2026-08-14-m3-2-weekly-plan-shopping-preview-design.md)  
Implementation plan: [`superpowers/plans/2026-08-14-m3-2-weekly-plan-shopping-preview.md`](superpowers/plans/2026-08-14-m3-2-weekly-plan-shopping-preview.md)  
Shipping evidence: [`superpowers/plans/2026-08-14-m3-2-weekly-plan-shopping-preview-shipping.md`](superpowers/plans/2026-08-14-m3-2-weekly-plan-shopping-preview-shipping.md)  
Acceptance: [`m3-2-weekly-plan-shopping-preview-acceptance-2026-08-14.md`](m3-2-weekly-plan-shopping-preview-acceptance-2026-08-14.md)  
Accepted squash merge: `9682ad1230910fc268ca3cddd8601a3fad7b100e`.

Accepted boundary:

`POST /api/v1/weekly-plan-shopping-previews`

Accepted flow:

`ordered weekly occurrences → server-owned transient identities → accepted WeeklyPlan → accepted M3.1 shopping composition → self-contained weekly ShoppingList + occurrence/Recipe ingredient provenance`

Accepted behavior:

- stateless request owns planner content, not server identities;
- ordered occurrence count is bounded to `1..35`;
- day metadata is `MONDAY..SUNDAY` only; no fixed meal-slot taxonomy;
- nested Recipe validation/normalization and serving semantics delegate to accepted M2.2 request construction;
- WeeklyPlan/occurrence IDs are generated by M3.2; Recipe/ingredient IDs use accepted M2.2 construction;
- accepted M3.1 remains authoritative for weekly ShoppingList identity, scaling, canonicalization, exact merge, quantity sum, output order and ShoppingItem identity;
- public lineage is exactly occurrence + Recipe + Recipe ingredient identity and resolves entirely inside the same response;
- internal RecipeAggregationEntryId never leaks;
- semantic/unreadable JSON failures use sanitized `INVALID_WEEKLY_PLAN_SHOPPING_PREVIEW` problems;
- OpenAPI 3.1 and generated TypeScript client are synchronized;
- architecture guards prohibit comparison/provider/retailer/database coupling and reverse dependencies into accepted lower layers;
- no persistence/history, locality/comparison, UI, pantry, nutrition, calendar/time-zone, fuzzy/AI or retailer-network behavior was introduced.

Final reviewed PR head `250aedb85b675036ffcb20e96a67db1afc03167a`:

- **9/9 normal PR workflow groups SUCCESS**;
- final read-only review: **Looks good**, no P0/P1/P2/P3;
- review threads empty.

Post-merge proof on `main=9682ad1230910fc268ca3cddd8601a3fad7b100e`:

- issue #112 closed `completed`;
- exactly 8 normal push workflows;
- **8/8 SUCCESS, 0 failures**.

## Next deterministic target — M3.3 WeeklyPlan → Comparison composition

M3.2 now supplies a truthful stateless weekly shopping projection. The next slice should compose it with the already accepted comparison application boundary without changing planner, Recipe, shopping or retailer semantics.

Recommended flow:

`explicit WeeklyPlan + locality → accepted M3.2 weekly shopping preview → canonical generated shopping requirements → accepted ComparisonPreview`

M3.3 design should preserve:

1. server-owned WeeklyPlan/occurrence/Recipe/ingredient identity from M3.2;
2. exact Weekly ShoppingItem identity/order/requirement/canonical quantity across the composition boundary;
3. self-contained planner/Recipe provenance from M3.2;
4. existing comparison production-access gating before runtime evidence acquisition;
5. no direct provider/retailer/matching/basket dependencies from the new composition adapter;
6. locality only at the composition boundary rather than inside WeeklyPlan domain;
7. sanitized wrapper failure semantics while preserving accepted nested planner/comparison errors where appropriate;
8. ordinary CI with no live retailer traffic.

Responsive Weekly Planning UI is **M3.4 after M3.3 acceptance**. Persistence/saved-plan history and pantry/exclusion subtraction remain separate evidence-driven slices.

## Magnit production state

Decision: [`integrations/magnit-production-access-decision-2026-08-13.md`](integrations/magnit-production-access-decision-2026-08-13.md).

Current product state: technical coverage **`AVAILABLE_PUBLIC_WEB`**, production access **`BLOCKED`**, comparison **`UNAVAILABLE`**, reason **`PRODUCTION_ACCESS_BLOCKED`**. This is Zakup Gotov operating policy pending affirmative permission/licensed access, not a legal adjudication.

## Parallel mandatory work

Continue without blocking deterministic M3 work unless new evidence invalidates accepted core assumptions:

- **#54** browser-bridge persistent-session/store-change/SPA lifecycle hardening;
- **#36** Kuper supported aggregator investigation;
- Chizhik, Ozon Fresh, Samokat, Lenta and VkusVill onboarding/hardening;
- retailer-specific structured package semantics only where source evidence proves them;
- retailer-specific production-access decisions before activation;
- successful real **`v0.1.0-rc.3` GitHub Release** proving final image promotion, SBOM/attestation and digest smoke evidence.

## Permanent core invariants

1. Shopping/basket/comparison behavior is deterministic over supplied evidence.
2. Every canonical retailer remains visible; unavailable retailers are never silently omitted.
3. Technical connectivity and production-access readiness are independent.
4. Precise addresses are sensitive and redacted by default; provider/acquisition/fulfillment identifiers remain internal.
5. `UNKNOWN` availability is never coerced and observation time is not misrepresented as provider freshness.
6. Matching ambiguity never becomes a hidden winner.
7. Package quantity is explicit structured evidence; mass, volume and count are not interchangeable.
8. Incomplete baskets never expose misleading complete-basket totals.
9. Production-access policy scopes acquisition before source invocation; out-of-scope evidence is a contract violation.
10. Ordinary CI/browser acceptance makes no live retailer requests and production preview never falls back to deterministic fixtures.
11. Universal retailer connectivity remains mandatory; public technical accessibility alone is never production authorization.
12. Recipe semantics reuse Shopping Core requirement/quantity normalization.
13. Recipe and planner provenance remain outside neutral Shopping Core types.
14. Recipe and cross-Recipe automatic merge remain exact requirement + canonical-unit semantics; no fuzzy/AI equivalence is implicit.
15. Multi-recipe aggregation distinguishes Recipe identity from occurrence identity and supports repeated Recipe use without ambiguous lineage.
16. ShoppingItem identity remains list + normalized requirement + canonical unit scoped and independent of amount/target servings.
17. WeeklyPlan day metadata never changes Recipe/Shopping merge or quantity semantics.
18. WeeklyPlan occurrence identity is distinct from Recipe identity; repeated Recipe meals are valid under distinct occurrence IDs.
19. WeeklyPlan composition delegates accepted M2.5 semantics and never duplicates serving/canonicalization/merge logic.
20. Planner provenance exposes WeeklyMealOccurrenceId + RecipeIngredientRef and hides internal aggregation IDs.
21. Planner composition fails closed on identity/provenance drift.
22. WeeklyPlan preview requests own no server identities; M3.2 generates transient IDs at the application boundary.
23. Every public M3.2 shopping-item source resolves to one returned occurrence, Recipe and Recipe ingredient.
24. M3.2 delegates Recipe validation/scaling semantics to accepted M2.2/M3.1 boundaries rather than creating a parallel planner algorithm.
25. M3.2 remains locality/retailer-independent; comparison is an explicit composition concern.

## Platform baseline

Backend: Java 25, Spring Boot 4.1, Spring MVC + Virtual Threads, Spring Modulith, PostgreSQL 18/Testcontainers, Flyway, jOOQ, pgJDBC `42.7.12`.

Contracts/web: OpenAPI 3.1, generated TypeScript client, Next.js 16.3.0, React 19.2.8, TypeScript 5.9.3, Node 24.18.1, Vitest/Testing Library and Playwright.

Operations/security: non-root API/web images, PostgreSQL→API→web release topology, CodeQL Java + JS/TS, Dependency Review, Release Bundle/Contract CI, fail-closed HIGH/CRITICAL container scans and Retailer Bridge CI.

## Release-engineering state

`v0.1.0-rc.1` proved the release trigger until an executable-mode defect; PR #28 fixed it. `v0.1.0-rc.2` proved fail-closed image security on pgJDBC `42.7.11` / `CVE-2026-54291`; PR #29 upgraded pgJDBC and hardened images.

A successful real **`v0.1.0-rc.3` GitHub Release published event remains outstanding**.
