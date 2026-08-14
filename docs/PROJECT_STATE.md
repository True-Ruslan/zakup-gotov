# Project State

Updated: 2026-08-14

## Project

**Zakup Gotov** is a recipe-to-cart grocery comparison product. The target experience is to turn recipes, meal plans or a manual grocery list into a location-aware comparison of complete retailer baskets while preserving price/availability evidence, package semantics, provenance, freshness and uncertainty.

Repository: `True-Ruslan/zakup-gotov`  
Visibility: Public  
Current phase: **M3 — Weekly Planning**  
M0 status: **technical discovery COMPLETE**  
M1 status: **Shopping Core COMPLETE / ACCEPTED**  
M2 status: **Recipes COMPLETE / ACCEPTED**  
M2.1 status: **Recipe domain + Recipe → ShoppingList COMPLETE / ACCEPTED (#94 / #93)**  
M2.2 status: **Recipe application/API boundary COMPLETE / ACCEPTED (#97 / #96)**  
M2.3 status: **Recipe → Comparison composition COMPLETE / ACCEPTED (#101 / #100)**  
M2.4 status: **Responsive Recipe UI COMPLETE / ACCEPTED (#104 / #103)**  
M2.5 status: **Deterministic multi-recipe aggregation COMPLETE / ACCEPTED (#107 / #106)**  
Current focus: **design the first M3 Weekly Planning domain/application slice over accepted M2.5 aggregation semantics**

## Permanent connectivity rule

Universal Retailer Connectivity remains mandatory:

> Every retailer/banner in the target registry remains coverage work until at least one reproducible accepted acquisition path exists. A failed transport changes the acquisition mode under investigation; it does not remove the retailer from product scope.

Technical feasibility, production-access readiness and deterministic product/core maturity remain separate dimensions.

## Accepted milestone baselines

### M0 — Product & Integration Discovery — COMPLETE

Accepted evidence established:

- Perekrestok and Pyaterochka browser-bridge acquisition paths;
- Magnit public-web acquisition path;
- at least two acquisition modes;
- deterministic sanitized fixtures/E2E and finite guarded probes;
- provider-neutral retailer architecture.

M0 proves technical feasibility only. It does not establish blanket permission for recurring production acquisition.

### M1 — Shopping Core — COMPLETE / ACCEPTED

Acceptance: [`m1-shopping-core-acceptance-2026-08-13.md`](m1-shopping-core-acceptance-2026-08-13.md).  
Accepted final hardening baseline: `779d0b219a13e0bf82263a1e655fb732553ed5fe`.

Accepted capabilities:

- canonical eight-retailer registry with independent technical-coverage and production-access states;
- shopping requirements, canonical quantities and shopping-list identity;
- provider/path orchestration and provider-neutral location/fulfillment context;
- immutable offer snapshots with explicit observation/provider freshness and `UNKNOWN` availability;
- deterministic exact/normalized matching with explicit matched/ambiguous/unmatched states;
- whole-package single-store basket calculation with complete/uncertain/incomplete states;
- truthful retailer comparison/readiness projection;
- structured package evidence and Magnit package/location technical research;
- pre-acquisition production-access gate that scopes retailer evidence before a source can run;
- stateless `POST /api/v1/comparison-previews` and responsive manual-list web journey.

M1 guarantees that unavailable retailers stay visible, ambiguity remains explicit, incomplete baskets do not expose misleading complete totals, and production preview evidence never falls back to deterministic fixtures.

## Magnit production state

Decision: [`integrations/magnit-production-access-decision-2026-08-13.md`](integrations/magnit-production-access-decision-2026-08-13.md).

Current product state:

- technical coverage: **`AVAILABLE_PUBLIC_WEB`**;
- production access: **`BLOCKED`**;
- comparison status: **`UNAVAILABLE`**;
- public reason: **`PRODUCTION_ACCESS_BLOCKED`**.

`BLOCKED` is a Zakup Gotov operating policy because an affirmative right to operate the intended recurring production catalog-acquisition/reuse model has not been established. It is not a legal adjudication. No production Spring/HTTP Magnit acquisition is activated.

## M2 — Recipes — COMPLETE / ACCEPTED

M2 established deterministic Recipe semantics, exposed them through stateless application/API composition, shipped a responsive Recipe-first user journey and completed the multi-recipe aggregation foundation required by M3.

### M2.1 — Recipe domain + Recipe → ShoppingList — COMPLETE / ACCEPTED

Design: [`superpowers/specs/2026-08-13-m2-1-recipe-domain-design.md`](superpowers/specs/2026-08-13-m2-1-recipe-domain-design.md)  
Shipping evidence: [`superpowers/plans/2026-08-13-m2-1-recipe-domain-shipping.md`](superpowers/plans/2026-08-13-m2-1-recipe-domain-shipping.md)  
Accepted squash merge: `423eb14f7c565bbe264257a92df89a6b42d0d158`.

Accepted behavior:

- immutable Recipe aggregate with stable Recipe/ingredient identities;
- normalized title, positive integer servings and ordered explicit ingredients;
- deterministic Recipe → ShoppingList conversion;
- exact-safe serving scaling and canonical unit handling;
- exact merge only by normalized requirement + canonical unit;
- deterministic list-scoped ShoppingItem identity;
- ordered per-ingredient provenance kept outside Shopping Core;
- no fuzzy/synonym/AI equivalence.

Post-merge acceptance: 8/8 normal push workflows SUCCESS.

### M2.2 — Stateless Recipe application/API boundary — COMPLETE / ACCEPTED

Authoritative design: [`superpowers/specs/2026-08-13-m2-2-recipe-shopping-preview-design-v2.md`](superpowers/specs/2026-08-13-m2-2-recipe-shopping-preview-design-v2.md)  
Acceptance: [`m2-2-recipe-shopping-preview-acceptance-2026-08-14.md`](m2-2-recipe-shopping-preview-acceptance-2026-08-14.md)  
Accepted squash merge: `8f0c1d8d31cfc1673656780a7989512d38788aff`.

Accepted boundary: `POST /api/v1/recipe-shopping-previews`.

Accepted behavior includes server-owned transient identities, strict serving validation, accepted M2.1 scaling/merge/identity semantics, self-contained ingredient provenance, sanitized request problems and generated OpenAPI/TypeScript contracts without persistence or retailer traffic.

Post-merge acceptance: 8/8 normal push workflows SUCCESS.

### M2.3 — Recipe → Comparison composition — COMPLETE / ACCEPTED

Authoritative design: [`superpowers/specs/2026-08-14-m2-3-recipe-comparison-preview-design.md`](superpowers/specs/2026-08-14-m2-3-recipe-comparison-preview-design.md)  
Acceptance: [`m2-3-recipe-comparison-preview-acceptance-2026-08-14.md`](m2-3-recipe-comparison-preview-acceptance-2026-08-14.md)  
Accepted squash merge: `15a086d135f40277c655b39549c3e7a04c2e914e`.

Accepted boundary: `POST /api/v1/recipe-comparison-previews`.

Accepted journey:

`Recipe input + locality → accepted RecipeShoppingPreview → canonical generated Shopping items → accepted ComparisonPreview`

Generated ShoppingItem identity/order/requirement/canonical quantity remain stable across the composition boundary, Recipe provenance remains self-contained and production-access gating remains authoritative before runtime evidence acquisition.

Post-merge acceptance: 8/8 normal push workflows SUCCESS.

### M2.4 — Responsive Recipe UI — COMPLETE / ACCEPTED

Authoritative design: [`superpowers/specs/2026-08-14-m2-4-responsive-recipe-ui-design.md`](superpowers/specs/2026-08-14-m2-4-responsive-recipe-ui-design.md)  
Implementation plan: [`superpowers/plans/2026-08-14-m2-4-responsive-recipe-ui.md`](superpowers/plans/2026-08-14-m2-4-responsive-recipe-ui.md)  
Shipping evidence: [`superpowers/plans/2026-08-14-m2-4-responsive-recipe-ui-shipping.md`](superpowers/plans/2026-08-14-m2-4-responsive-recipe-ui-shipping.md)  
Acceptance: [`m2-4-responsive-recipe-ui-acceptance-2026-08-14.md`](m2-4-responsive-recipe-ui-acceptance-2026-08-14.md)  
Accepted squash merge: `aba20c9cee263a683c0d4383ad840d7415851861`.

Accepted primary web journey:

`Recipe title/servings + ingredient editing + locality → composed Recipe comparison endpoint → canonical generated shopping requirements → truthful retailer comparison`

The browser remains a generated-contract client, internal IDs stay hidden, failure is fail-closed, manual list comparison remains available and desktop/mobile Playwright covers scaling, generated-list output, unavailable state, keyboard focus and horizontal-overflow safety.

Post-merge acceptance: 8/8 normal push workflows SUCCESS.

### M2.5 — Deterministic multi-recipe aggregation — COMPLETE / ACCEPTED

Authoritative design: [`superpowers/specs/2026-08-14-m2-5-multi-recipe-aggregation-design.md`](superpowers/specs/2026-08-14-m2-5-multi-recipe-aggregation-design.md)  
Implementation plan: [`superpowers/plans/2026-08-14-m2-5-multi-recipe-aggregation.md`](superpowers/plans/2026-08-14-m2-5-multi-recipe-aggregation.md)  
Shipping evidence: [`superpowers/plans/2026-08-14-m2-5-multi-recipe-aggregation-shipping.md`](superpowers/plans/2026-08-14-m2-5-multi-recipe-aggregation-shipping.md)  
Acceptance: [`m2-5-multi-recipe-aggregation-acceptance-2026-08-14.md`](m2-5-multi-recipe-aggregation-acceptance-2026-08-14.md)  
Accepted squash merge: `0854fc5bf76ad2976986537d6b4f5f3b8ebd18f0`.

Accepted flow:

`ordered Recipe occurrences + target servings → accepted per-recipe conversion → one canonical aggregate ShoppingList + occurrence-aware provenance`

Accepted behavior:

- occurrence identity is separate from Recipe identity so the same Recipe may appear multiple times;
- accepted `RecipeShoppingListConverter` remains authoritative for serving scaling and source-unit canonicalization;
- automatic cross-Recipe merge remains exact normalized requirement + canonical unit only;
- canonical amounts are summed exactly and first compatible occurrence controls ordering;
- aggregate ShoppingItem IDs reuse accepted M2.1 list+requirement+unit derivation and stay independent of amount/serving changes;
- provenance preserves aggregation occurrence + accepted RecipeIngredientRef lineage and is deeply immutable;
- empty/duplicate occurrence sets, missing provenance and generated-ID collisions fail closed;
- no API, web, persistence, planner, provider or retailer behavior was added.

Compatibility proof locked accepted M2.1 ShoppingItem UUID `3d737f10-a263-39b3-b90a-fe7868c035b9` before shared-helper extraction and preserved it afterwards.

Final reviewed PR head `a6e1095696ebfd67fafe7675a37b125ae02b3170`: 9/9 normal PR workflow groups SUCCESS; review `REVIEWED_READY / Looks good`; no unresolved P0/P1/P2; review threads empty.

Post-merge proof on exact `main=0854fc5bf76ad2976986537d6b4f5f3b8ebd18f0`:

- issue #106 closed `completed`;
- exactly 8 normal push workflow runs;
- **8/8 SUCCESS; 0 failures**;
- API, Contract, Web/E2E, CodeQL Java + JavaScript/TypeScript, Container Security, Retailer Bridge, Release Contract and Release Bundle all passed.

## M3 — Weekly Planning — CURRENT / DESIGN NEXT

M2 has supplied the deterministic aggregation primitive. M3 may now add planner-specific semantics without changing accepted Recipe/Shopping merge behavior implicitly.

Recommended first design slice:

`WeeklyPlan identity + ordered meal/day occurrences + per-occurrence target servings → accepted M2.5 aggregation → reviewable weekly ShoppingList projection`

Design questions to resolve before implementation:

1. What is the smallest WeeklyPlan aggregate: ordered meals only, or explicit day/slot ownership from the start?
2. Should M3 reuse `RecipeAggregationEntryId` as an internal bridge while exposing planner-specific occurrence identity publicly?
3. How are day/meal labels represented without contaminating Recipe or Shopping Core?
4. Which planner invariants are deterministic domain rules versus UI-only organization?
5. Should the first M3 slice remain stateless, with persistence deferred until saved weekly plans demonstrate value? Default direction: yes.
6. Pantry/exclusions should be a separate explicit semantics layer after the base weekly-plan composition is accepted; they must not silently alter M2.5 provenance.
7. What minimal API contract is needed before planner UI work, and how does it preserve complete occurrence provenance?

Persistence, accounts, pantry prediction, fuzzy/AI recipe interpretation and optimization remain separate evidence-driven decisions.

## Parallel mandatory work

Continue without blocking deterministic M3 work unless new evidence invalidates accepted core assumptions:

- **#54** browser-bridge persistent-session/store-change/SPA lifecycle hardening;
- **#36** Kuper supported aggregator access investigation;
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
6. `UNKNOWN` availability is never coerced.
7. Observation time is not misrepresented as provider freshness.
8. Matching ambiguity never becomes a hidden winner.
9. Package quantity is explicit structured evidence and is never guessed from presentation text.
10. Mass, volume and count are not interchangeable.
11. Incomplete baskets never expose misleading complete-basket totals.
12. Production-access policy scopes acquisition before source invocation.
13. Evidence outside requested retailer scope is a contract violation.
14. Ordinary CI/browser acceptance makes no live retailer requests.
15. Production preview evidence does not fall back to deterministic fixtures.
16. Universal retailer connectivity remains mandatory.
17. Public technical accessibility is never treated as production authorization by itself.
18. Recipe semantics reuse Shopping Core quantity/requirement normalization instead of duplicating it.
19. Recipe provenance remains outside Shopping Core types.
20. Recipe exact-safe merging never introduces fuzzy/AI equivalence implicitly.
21. Recipe application requests own no server identities; transient IDs are generated at the application boundary.
22. Public Recipe provenance is self-contained and every source ingredient ID resolves inside the same response.
23. Fractional ingredient quantities remain valid; serving counts remain positive JSON integers.
24. Recipe → Comparison composition preserves generated ShoppingItem identity, order, requirement and canonical quantity across the application boundary.
25. Recipe → Comparison composition delegates accepted Recipe and Comparison semantics and never bypasses production-access gating.
26. Browser Recipe UI is a generated-contract client; Recipe scaling/merge/provenance/comparison semantics remain server-owned.
27. Browser/E2E fixtures can prove UX behavior but never establish production retailer readiness.
28. Multi-recipe aggregation distinguishes Recipe identity from occurrence identity.
29. Repeating one Recipe under distinct aggregation occurrence IDs is valid and lineage remains unambiguous.
30. Cross-Recipe automatic merge uses the same exact requirement + canonical-unit semantics as single-Recipe conversion.
31. Aggregate ShoppingItem identity remains list+requirement+canonical-unit scoped and independent of amount/target servings.
32. Multi-recipe provenance remains outside neutral Shopping Core types and is deeply immutable.
33. Planner-specific semantics in M3 must compose accepted M2 aggregation rather than silently changing Recipe/Shopping merge rules.

## Platform baseline

Backend: Java 25, Spring Boot 4.1, Spring MVC + Virtual Threads, Spring Modulith, PostgreSQL 18/Testcontainers, Flyway, jOOQ, pgJDBC `42.7.12`.

Contracts/web: OpenAPI 3.1, generated TypeScript client, Next.js 16.3.0, React 19.2.8, TypeScript 5.9.3, Node 24.18.1, Vitest/Testing Library and Playwright.

Operations/security: non-root API/web images, PostgreSQL→API→web release topology, CodeQL Java + JS/TS, Dependency Review, Release Bundle/Contract CI, fail-closed HIGH/CRITICAL container scans and Retailer Bridge CI.

## Release-engineering state

`v0.1.0-rc.1` proved the release trigger until an executable-mode defect; PR #28 fixed it. `v0.1.0-rc.2` proved fail-closed image security on pgJDBC `42.7.11` / `CVE-2026-54291`; PR #29 upgraded pgJDBC and hardened images.

A successful real **`v0.1.0-rc.3` GitHub Release published event remains outstanding**.
