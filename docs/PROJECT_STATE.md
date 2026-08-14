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
- M3.1 WeeklyPlan domain + deterministic shopping composition — **COMPLETE / ACCEPTED** (#110 / #109).

Current focus: **design M3.2 stateless WeeklyPlan application/API boundary**.

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

M2 guarantees that Recipe scaling/canonicalization is deterministic, automatic merge remains exact normalized requirement + canonical unit only, ShoppingItem identity is deterministic/list-scoped, Recipe/multi-Recipe provenance remains outside neutral Shopping Core and no fuzzy/AI equivalence is introduced implicitly.

### M3.1 — WeeklyPlan domain + deterministic shopping composition — COMPLETE / ACCEPTED

Authoritative design: [`superpowers/specs/2026-08-14-m3-1-weekly-plan-domain-design.md`](superpowers/specs/2026-08-14-m3-1-weekly-plan-domain-design.md)  
Implementation plan: [`superpowers/plans/2026-08-14-m3-1-weekly-plan-domain.md`](superpowers/plans/2026-08-14-m3-1-weekly-plan-domain.md)  
Shipping evidence: [`superpowers/plans/2026-08-14-m3-1-weekly-plan-domain-shipping.md`](superpowers/plans/2026-08-14-m3-1-weekly-plan-domain-shipping.md)  
Acceptance: [`m3-1-weekly-plan-acceptance-2026-08-14.md`](m3-1-weekly-plan-acceptance-2026-08-14.md)  
Accepted squash merge: `13e09c63959b050d431cc913597fc868aa408718`.

Accepted flow:

`ordered WeeklyPlan meal occurrences + target servings → accepted M2.5 aggregation → one canonical weekly ShoppingList + WeeklyMealOccurrence/RecipeIngredient provenance`

Accepted behavior:

- immutable WeeklyPlan identity + ordered non-empty meal occurrences;
- required Monday-through-Sunday day metadata, with no fixed breakfast/lunch/dinner/snack slots;
- multiple occurrences may share a day;
- the same Recipe may appear multiple times under distinct WeeklyMealOccurrenceIds;
- caller occurrence order is explicit and never auto-sorted by day;
- weekly ShoppingList identity derives deterministically from WeeklyPlanId;
- internal aggregation-entry identity derives deterministically from WeeklyPlanId + WeeklyMealOccurrenceId;
- accepted M2.5 remains authoritative for scaling, canonicalization, cross-Recipe exact merge, final ordering and ShoppingItem identity;
- planner provenance projects to WeeklyMealOccurrenceId + exact RecipeIngredientRef and hides internal RecipeAggregationEntryId;
- key drift, empty lineage, unknown internal identity and generated-ID collisions fail closed;
- weeklyplan may depend only on accepted recipe/shopping project packages; reverse dependency is prohibited;
- no persistence, API/OpenAPI, web UI, pantry/exclusions, provider/retailer or comparison behavior was introduced.

Final reviewed PR head `ec1af08cbaf373f79c54858e9654451cebc4f009`:

- **9/9 normal PR workflow groups SUCCESS**;
- final read-only review: **Looks good**, no P0/P1/P2/P3;
- review threads empty.

Post-merge proof on `main=13e09c63959b050d431cc913597fc868aa408718`:

- issue #109 closed `completed`;
- exactly 8 normal push workflows;
- **8/8 SUCCESS, 0 failures**.

## Next deterministic target — M3.2 stateless WeeklyPlan application/API boundary

M3.1 accepted planner-domain semantics. The next slice should expose those semantics without introducing persistence or UI prematurely.

Recommended flow:

`explicit WeeklyPlan request → server-owned transient WeeklyPlan/occurrence/Recipe/ingredient identities → accepted M3.1 composition → self-contained weekly ShoppingList + planner/Recipe provenance`

M3.2 design should decide:

1. public request shape for ordered meal occurrences and days;
2. bounded recipe/ingredient/occurrence counts for predictable stateless execution;
3. server-owned transient identity generation and collision handling;
4. self-contained response shape that exposes planner occurrence/day/Recipe ingredient lineage without leaking internal aggregation IDs;
5. sanitized validation/problem vocabulary;
6. whether locality/comparison remains a later composition slice rather than part of the first WeeklyPlan API — default direction: keep M3.2 planner-only;
7. OpenAPI 3.1 + generated TypeScript contract requirements;
8. architecture guard preventing provider/retailer/comparison/database coupling.

Persistence, saved-plan history, pantry/exclusion subtraction, retailer comparison orchestration and Weekly Planning UI remain separate follow-on slices unless M3.2 design evidence requires otherwise.

## Magnit production state

Decision: [`integrations/magnit-production-access-decision-2026-08-13.md`](integrations/magnit-production-access-decision-2026-08-13.md).

Current product state:

- technical coverage: **`AVAILABLE_PUBLIC_WEB`**;
- production access: **`BLOCKED`**;
- comparison status: **`UNAVAILABLE`**;
- public reason: **`PRODUCTION_ACCESS_BLOCKED`**.

`BLOCKED` is a Zakup Gotov operating policy because affirmative right to operate the intended recurring production catalog-acquisition/reuse model has not been established. It is not a legal adjudication. No production Spring/HTTP Magnit acquisition is activated.

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
4. Precise addresses are sensitive and redacted by default.
5. Provider/acquisition/fulfillment identifiers remain internal.
6. `UNKNOWN` availability is never coerced and observation time is not misrepresented as provider freshness.
7. Matching ambiguity never becomes a hidden winner.
8. Package quantity is explicit structured evidence; mass, volume and count are not interchangeable.
9. Incomplete baskets never expose misleading complete-basket totals.
10. Production-access policy scopes acquisition before source invocation; out-of-scope evidence is a contract violation.
11. Ordinary CI/browser acceptance makes no live retailer requests and production preview never falls back to deterministic fixtures.
12. Universal retailer connectivity remains mandatory; public technical accessibility alone is never production authorization.
13. Recipe semantics reuse Shopping Core requirement/quantity normalization.
14. Recipe and planner provenance remain outside neutral Shopping Core types.
15. Recipe and cross-Recipe automatic merge remain exact requirement + canonical-unit semantics; no fuzzy/AI equivalence is implicit.
16. Multi-recipe aggregation distinguishes Recipe identity from occurrence identity and supports repeated Recipe use without ambiguous lineage.
17. ShoppingItem identity remains list + normalized requirement + canonical unit scoped and independent of amount/target servings.
18. WeeklyPlan day metadata never changes Recipe/Shopping merge or quantity semantics.
19. WeeklyPlan occurrence identity is distinct from Recipe identity; repeated Recipe meals are valid under distinct occurrence IDs.
20. WeeklyPlan composition delegates accepted M2.5 semantics and never duplicates serving/canonicalization/merge logic.
21. Planner provenance exposes WeeklyMealOccurrenceId + RecipeIngredientRef and hides internal aggregation IDs.
22. Planner composition fails closed on identity/provenance drift.

## Platform baseline

Backend: Java 25, Spring Boot 4.1, Spring MVC + Virtual Threads, Spring Modulith, PostgreSQL 18/Testcontainers, Flyway, jOOQ, pgJDBC `42.7.12`.

Contracts/web: OpenAPI 3.1, generated TypeScript client, Next.js 16.3.0, React 19.2.8, TypeScript 5.9.3, Node 24.18.1, Vitest/Testing Library and Playwright.

Operations/security: non-root API/web images, PostgreSQL→API→web release topology, CodeQL Java + JS/TS, Dependency Review, Release Bundle/Contract CI, fail-closed HIGH/CRITICAL container scans and Retailer Bridge CI.

## Release-engineering state

`v0.1.0-rc.1` proved the release trigger until an executable-mode defect; PR #28 fixed it. `v0.1.0-rc.2` proved fail-closed image security on pgJDBC `42.7.11` / `CVE-2026-54291`; PR #29 upgraded pgJDBC and hardened images.

A successful real **`v0.1.0-rc.3` GitHub Release published event remains outstanding**.
