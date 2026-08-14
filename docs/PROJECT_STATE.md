# Project State

Updated: 2026-08-15

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
- M3.2 Stateless WeeklyPlan shopping-preview application/API boundary — **COMPLETE / ACCEPTED** (#113 / #112);
- M3.3 WeeklyPlan → Comparison composition — **COMPLETE / ACCEPTED** (#116 / #115);
- M3.4 Responsive Weekly Planning UI — **COMPLETE / ACCEPTED** (#119 / #118).

Current focus: **M3.5 — Pantry / exclusions semantics**.

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

M2 guarantees deterministic serving scaling/canonicalization, exact normalized requirement + canonical-unit merge only, deterministic list-scoped ShoppingItem identity and explicit Recipe/multi-Recipe provenance outside neutral Shopping Core. Fuzzy/synonym/AI equivalence is never introduced implicitly.

### M3.1 — WeeklyPlan domain + deterministic shopping composition — COMPLETE / ACCEPTED

Acceptance: [`m3-1-weekly-plan-acceptance-2026-08-14.md`](m3-1-weekly-plan-acceptance-2026-08-14.md)  
Accepted squash merge: `13e09c63959b050d431cc913597fc868aa408718`.

Accepted result:

- immutable WeeklyPlan identity + ordered non-empty meal occurrences;
- Monday-through-Sunday metadata without fixed meal-slot taxonomy;
- multiple occurrences per day and repeated Recipe use through distinct occurrence IDs;
- explicit caller order, never implicit day sorting;
- target servings delegated through accepted Recipe semantics;
- deterministic WeeklyPlan-scoped ShoppingList and internal aggregation-entry identity;
- accepted M2.5 remains authoritative for scaling, canonicalization, exact merge, final ordering and ShoppingItem identity;
- planner provenance projects to WeeklyMealOccurrenceId + RecipeIngredientRef while internal aggregation IDs remain hidden;
- identity/provenance drift and generated-ID collisions fail closed.

Acceptance proof: reviewed exact head `ec1af08cbaf373f79c54858e9654451cebc4f009` had **9/9 PR workflow groups SUCCESS**, clean read-only review and no threads; merge `13e09c63959b050d431cc913597fc868aa408718` had **8/8 post-merge workflows SUCCESS**.

### M3.2 — Stateless WeeklyPlan shopping preview — COMPLETE / ACCEPTED

Acceptance: [`m3-2-weekly-plan-shopping-preview-acceptance-2026-08-14.md`](m3-2-weekly-plan-shopping-preview-acceptance-2026-08-14.md)  
Accepted squash merge: `9682ad1230910fc268ca3cddd8601a3fad7b100e`.

Accepted boundary: `POST /api/v1/weekly-plan-shopping-previews`.

Accepted result:

- stateless `1..35` ordered weekly occurrences;
- no client-supplied planner/Recipe/ingredient identities;
- server-owned transient IDs and accepted M2.2 nested Recipe construction;
- shopping composition delegated to accepted M3.1/M2.5;
- self-contained public `occurrenceId + recipeId + recipeIngredientId` provenance;
- sanitized problem details and synchronized OpenAPI/generated TypeScript contract;
- no persistence/history, locality/comparison, UI, pantry, calendar/time-zone, AI/fuzzy or retailer-network behavior.

Acceptance proof: reviewed exact head `250aedb85b675036ffcb20e96a67db1afc03167a` had **9/9 PR workflow groups SUCCESS**, clean read-only review and no threads; merge `9682ad1230910fc268ca3cddd8601a3fad7b100e` had **8/8 post-merge workflows SUCCESS**.

### M3.3 — WeeklyPlan → Comparison composition — COMPLETE / ACCEPTED

Acceptance: [`m3-3-weekly-plan-comparison-preview-acceptance-2026-08-14.md`](m3-3-weekly-plan-comparison-preview-acceptance-2026-08-14.md)  
Accepted squash merge: `89b9ef2ca95d07a7e4c964fdef38a9af1c5c3a43`.

Accepted boundary: `POST /api/v1/weekly-plan-comparison-previews`.

Accepted result:

- one stateless request combines provider-neutral locality with accepted M3.2 WeeklyPlan input;
- accepted M3.2 remains authoritative for planner/Recipe shopping projection and provenance;
- generated ShoppingItem UUID/order/requirement/canonical quantity cross into comparison unchanged;
- accepted ComparisonPreview remains authoritative for locality, retailer readiness, production-access gating, runtime evidence, matching, package/basket semantics and truthful result states;
- composition drift fails closed and wrapper binding failures are sanitized;
- OpenAPI/generated client exposes the composed boundary without provider/database coupling.

Acceptance proof: reviewed exact head `396445c333ea369bed6d428b33f38f37765eff20` had **9/9 PR workflow groups SUCCESS**, clean read-only review and no threads; merge `89b9ef2ca95d07a7e4c964fdef38a9af1c5c3a43` had **8/8 post-merge workflows SUCCESS**.

### M3.4 — Responsive Weekly Planning UI — COMPLETE / ACCEPTED

Authoritative design: [`superpowers/specs/2026-08-15-m3-4-responsive-weekly-planning-ui-design.md`](superpowers/specs/2026-08-15-m3-4-responsive-weekly-planning-ui-design.md)  
Implementation plan: [`superpowers/plans/2026-08-15-m3-4-responsive-weekly-planning-ui.md`](superpowers/plans/2026-08-15-m3-4-responsive-weekly-planning-ui.md)  
Shipping evidence: [`superpowers/plans/2026-08-15-m3-4-responsive-weekly-planning-ui-shipping.md`](superpowers/plans/2026-08-15-m3-4-responsive-weekly-planning-ui-shipping.md)  
Acceptance: [`m3-4-responsive-weekly-planning-ui-acceptance-2026-08-15.md`](m3-4-responsive-weekly-planning-ui-acceptance-2026-08-15.md)  
Accepted squash merge: `1201030aed45075c676f796920b6268cdcf8e036`.

Accepted product flow:

`edit ordered weekly meal occurrences + locality → accepted POST /api/v1/weekly-plan-comparison-previews → canonical weekly shopping requirements → truthful retailer comparison`

Accepted behavior:

- Weekly Planning is the primary homepage journey while Recipe and manual-list comparison remain explicit secondary paths;
- one-to-35 occurrences can be added, removed and explicitly reordered without auto-sorting by day;
- each occurrence edits day, target servings, Recipe title/base servings and explicit ingredients without meal-slot taxonomy;
- generated OpenAPI/client request/response/day/unit types are authoritative; no duplicate frontend DTO/domain algorithm exists;
- browser-local row keys are React-only and never become domain/server identities;
- accepted M3.3 is the only planner comparison transport; the browser never composes M3.2 + comparison itself;
- canonical weekly shopping items render in accepted server order before retailer comparison;
- the existing truthful comparison projection is reused without browser-side scaling, merge, matching, package arithmetic, totals or winner recomputation;
- generated identities and planner provenance remain hidden from ordinary user-facing output;
- missing configuration, timeout, network and unexpected service failures fail closed; generated product-safe 400 validation fields/messages remain visible;
- deterministic desktop/mobile Playwright covers the weekly critical flow, explicit reorder semantics, 390px no-overflow, keyboard focus, unavailable state and Recipe/manual regressions with no live retailer traffic.

Final reviewed PR #119 head `12973650f274f76ec54865be41963843afcb4558`:

- **9/9 normal PR workflow groups SUCCESS**;
- final read-only review: **Looks good**, no P0/P1/P2/P3 or nitpicks;
- review threads empty.

Post-merge proof on `main=1201030aed45075c676f796920b6268cdcf8e036`:

- issue #118 closed `completed`;
- exactly 8 normal push workflows;
- **8/8 SUCCESS, 0 failures**;
- Web CI/Web E2E and both CodeQL languages succeeded.

## Next deterministic target — M3.5 Pantry / exclusions semantics

The base planner flow is now accepted from domain through responsive browser composition. The next slice should define explicit pantry/exclusion subtraction semantics without mutating accepted Recipe/WeeklyPlan/Shopping/Comparison behavior.

M3.5 design must answer before implementation:

1. whether pantry state is request-scoped first or immediately persistent;
2. which canonical quantity/unit combinations are subtractable and how incompatible dimensions fail closed;
3. how partial subtraction, exact exhaustion and over-supplied pantry quantities are represented;
4. how provenance records both original weekly requirement and the explicit pantry/exclusion operation;
5. whether exclusions are quantity-bearing pantry evidence, explicit omit-all rules, or separate concepts;
6. how subtraction affects ShoppingItem identity/order while preserving accepted M2.5/M3.1 invariants;
7. how M3.2/M3.3 composed responses expose inspectable pre-/post-subtraction evidence;
8. how deterministic UI/API tests prove no silent mutation or hidden ingredient loss.

Persistence/saved-plan history remains deferred unless M3.5 evidence demonstrates that persistence is required for correct product value rather than convenience.

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
23. WeeklyPlan preview requests own no server identities; M3.2 generates transient IDs at the application boundary.
24. Every public M3.2 shopping-item source resolves to one returned occurrence, Recipe and Recipe ingredient.
25. M3.2 delegates Recipe validation/scaling semantics to accepted M2.2/M3.1 boundaries rather than creating a parallel planner algorithm.
26. M3.2 remains locality/retailer-independent; comparison is an explicit composition concern.
27. M3.3 is a stateless application composition adapter: planner projection remains owned by M3.2 and comparison behavior remains owned by accepted ComparisonPreview.
28. M3.3 preserves generated ShoppingItem identity/order/requirement/canonical quantity and returns M3.2 self-contained planner provenance unchanged.
29. M3.3 sanitizes whole-wrapper binding failures while preserving accepted planner/comparison semantic problem contracts after successful binding.
30. M3.4 browser code consumes the generated M3.3 composed contract rather than defining parallel planner/comparison DTOs or algorithms.
31. M3.4 preserves explicit occurrence order independently from day metadata and never introduces implicit meal-slot semantics.
32. M3.4 renders canonical weekly shopping before retailer comparison and does not recompute serving scale, merge, matching, package counts, totals or winners in the browser.
33. M3.4 keeps generated server identities/provenance out of editable/user-facing state while local React keys remain presentation-only.
34. M3.4 deterministic browser acceptance remains retailer-network-free and preserves Recipe/manual-list critical journeys.
35. Pantry/exclusion semantics must be an explicit future layer with inspectable provenance; subtraction may not silently mutate accepted M2.5/M3.1/M3.2/M3.3/M3.4 behavior.

## Platform baseline

Backend: Java 25, Spring Boot 4.1, Spring MVC + Virtual Threads, Spring Modulith, PostgreSQL 18/Testcontainers, Flyway, jOOQ, pgJDBC `42.7.12`.

Contracts/web: OpenAPI 3.1, generated TypeScript client, Next.js 16.3.0, React 19.2.8, TypeScript 5.9.3, Node 24.18.1, Vitest/Testing Library and Playwright.

Operations/security: non-root API/web images, PostgreSQL→API→web release topology, CodeQL Java + JS/TS, Dependency Review, Release Bundle/Contract CI, fail-closed HIGH/CRITICAL container scans and Retailer Bridge CI.

## Release-engineering state

`v0.1.0-rc.1` proved the release trigger until an executable-mode defect; PR #28 fixed it. `v0.1.0-rc.2` proved fail-closed image security on pgJDBC `42.7.11` / `CVE-2026-54291`; PR #29 upgraded pgJDBC and hardened images.

A successful real **`v0.1.0-rc.3` GitHub Release published event remains outstanding**.
