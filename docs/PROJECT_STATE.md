# Project State

Updated: 2026-08-14

## Project

**Zakup Gotov** is a recipe-to-cart grocery comparison product. The target experience is to turn recipes, meal plans or a manual grocery list into a location-aware comparison of complete retailer baskets while preserving price/availability evidence, package semantics, provenance, freshness and uncertainty.

Repository: `True-Ruslan/zakup-gotov`  
Visibility: Public  
Current phase: **M2 — Recipes**  
M0 status: **technical discovery COMPLETE**  
M1 status: **Shopping Core COMPLETE / ACCEPTED**  
M1→M2 decision: **GO** — [`m1-shopping-core-acceptance-2026-08-13.md`](m1-shopping-core-acceptance-2026-08-13.md)  
M2.1 status: **Recipe domain + Recipe → ShoppingList COMPLETE / ACCEPTED (#94 / #93)**  
M2.2 status: **Recipe application/API boundary COMPLETE / ACCEPTED (#97 / #96)**  
M2.3 status: **Recipe → Comparison composition COMPLETE / ACCEPTED (#101 / #100)**  
M2.4 status: **Responsive Recipe UI COMPLETE / ACCEPTED (#104 / #103)**  
Current focus: **design M2.5 deterministic multi-recipe aggregation for M3 Weekly Planning**

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

## M2 — Recipes — CURRENT

### M2.1 — Recipe domain + Recipe → ShoppingList — COMPLETE / ACCEPTED

Design: [`superpowers/specs/2026-08-13-m2-1-recipe-domain-design.md`](superpowers/specs/2026-08-13-m2-1-recipe-domain-design.md)  
Shipping evidence: [`superpowers/plans/2026-08-13-m2-1-recipe-domain-shipping.md`](superpowers/plans/2026-08-13-m2-1-recipe-domain-shipping.md)  
Accepted squash merge: `423eb14f7c565bbe264257a92df89a6b42d0d158`.

Accepted behavior:

- immutable Recipe aggregate with stable Recipe/ingredient identities;
- normalized title, positive integer servings and ordered explicit ingredients;
- ingredients reuse Shopping Core requirement/quantity semantics;
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

Accepted boundary:

`POST /api/v1/recipe-shopping-previews`

Accepted behavior:

- stateless Recipe request/response;
- server-owned transient Recipe/ingredient/ShoppingList identities;
- strict positive-integer serving counts and explicit decimal ingredient quantities;
- accepted M2.1 converter remains authoritative for scaling/merge/order/ShoppingItem identity;
- self-contained source ingredient provenance in the response;
- sanitized request problem contract;
- OpenAPI 3.1 + generated TypeScript contract;
- no retailer traffic, persistence, Recipe UI or comparison orchestration.

Post-merge acceptance: 8/8 normal push workflows SUCCESS.

### M2.3 — Recipe → Comparison composition — COMPLETE / ACCEPTED

Authoritative design: [`superpowers/specs/2026-08-14-m2-3-recipe-comparison-preview-design.md`](superpowers/specs/2026-08-14-m2-3-recipe-comparison-preview-design.md)  
Acceptance: [`m2-3-recipe-comparison-preview-acceptance-2026-08-14.md`](m2-3-recipe-comparison-preview-acceptance-2026-08-14.md)  
Accepted squash merge: `15a086d135f40277c655b39549c3e7a04c2e914e`.

Accepted boundary:

`POST /api/v1/recipe-comparison-previews`

Accepted journey:

`Recipe input + locality → accepted RecipeShoppingPreview → canonical generated Shopping items → accepted ComparisonPreview`

Accepted behavior:

- one stateless use case composes the accepted Recipe and comparison application boundaries;
- generated ShoppingItem UUID/order/requirement/canonical quantity are preserved end-to-end;
- Recipe provenance remains self-contained;
- comparison production-access gating stays authoritative before runtime evidence acquisition;
- identity/order/requirement/quantity drift fails closed;
- OpenAPI/generated client remains synchronized;
- no retailer activation, persistence, exact-address flow, Recipe UI or fuzzy/AI semantics.

Post-merge acceptance: 8/8 normal push workflows SUCCESS.

### M2.4 — Responsive Recipe UI — COMPLETE / ACCEPTED

Authoritative design: [`superpowers/specs/2026-08-14-m2-4-responsive-recipe-ui-design.md`](superpowers/specs/2026-08-14-m2-4-responsive-recipe-ui-design.md)  
Implementation plan: [`superpowers/plans/2026-08-14-m2-4-responsive-recipe-ui.md`](superpowers/plans/2026-08-14-m2-4-responsive-recipe-ui.md)  
Shipping evidence: [`superpowers/plans/2026-08-14-m2-4-responsive-recipe-ui-shipping.md`](superpowers/plans/2026-08-14-m2-4-responsive-recipe-ui-shipping.md)  
Acceptance: [`m2-4-responsive-recipe-ui-acceptance-2026-08-14.md`](m2-4-responsive-recipe-ui-acceptance-2026-08-14.md)  
Accepted squash merge: `aba20c9cee263a683c0d4383ad840d7415851861`.

Accepted primary web journey:

`Recipe title/servings + ingredient editing + locality → composed Recipe comparison endpoint → canonical generated shopping requirements → truthful retailer comparison`

Accepted behavior:

- Recipe-first homepage flow with manual basket comparison preserved as a secondary path;
- title, base/target servings, locality and 1..100 editable ingredients;
- generated `QuantityInputUnit` vocabulary and generated M2.3 request/response types;
- client preflight only; Recipe scaling/canonicalization/merge/identity/provenance/comparison semantics remain server-owned;
- generated canonical shopping requirements render before retailer results;
- transient IDs/provenance remain hidden from user-facing output;
- existing READY / UNCERTAIN / INCOMPLETE / UNAVAILABLE comparison renderer is reused without invented winners;
- finite timeout and product-safe fail-closed validation/unavailable UI;
- deterministic hydration-safe local row identities;
- desktop/mobile Playwright coverage for serving scaling, generated list, error state, keyboard focus and no horizontal overflow;
- deterministic retailer evidence exists only in E2E test support; production web code contains no fixture retailer data.

Final reviewed PR head: `fb069d64b96f0d989951e67fd62b793277453024` — 9/9 normal PR workflow groups SUCCESS, read-only review `REVIEWED_READY / Looks good`, no unresolved P0/P1/P2, review threads empty.

Post-merge proof on exact `main=aba20c9cee263a683c0d4383ad840d7415851861`:

- issue #103 closed `completed`;
- exactly 8 normal push workflow runs;
- **8/8 SUCCESS; 0 failures**;
- API, Contract, Web/E2E, CodeQL Java + JavaScript/TypeScript, Container Security, Retailer Bridge, Release Contract and Release Bundle all passed.

## Next deterministic target — M2.5 multi-recipe aggregation

M2 single-recipe product flow is complete from domain semantics through responsive UI.

Next focus:

`several accepted Recipe conversions → one deterministic aggregated ShoppingList + per-recipe provenance`

M2.5 should provide the deterministic merge/provenance foundation required by **M3 Weekly Planning** while preserving accepted Shopping/Recipe invariants.

Design questions to resolve before implementation:

1. Aggregate from Recipe domain inputs or accepted RecipeShoppingListConversion results?
2. How are aggregate ShoppingList/ShoppingItem identities derived deterministically across multiple recipes?
3. How is provenance represented so each aggregate item resolves back to recipe + ingredient identities without polluting Shopping Core?
4. Should exact-safe merging remain requirement + canonical unit across recipes? Default direction: yes, with no fuzzy/synonym semantics.
5. How are duplicate Recipe IDs and duplicate ingredient identities rejected/fail-closed?
6. What ordering rule survives aggregation? Default direction: recipe input order, then first compatible requirement occurrence.
7. What stateless application/API boundary is the smallest useful bridge into M3 weekly planning?

Persistence, saved recipes, AI ingestion and fuzzy/semantic matching remain separate evidence-driven decisions, not prerequisites for M2.5.

## Parallel mandatory work

Continue without blocking deterministic M2 work unless new evidence invalidates accepted core assumptions:

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

## Platform baseline

Backend: Java 25, Spring Boot 4.1, Spring MVC + Virtual Threads, Spring Modulith, PostgreSQL 18/Testcontainers, Flyway, jOOQ, pgJDBC `42.7.12`.

Contracts/web: OpenAPI 3.1, generated TypeScript client, Next.js 16.3.0, React 19.2.8, TypeScript 5.9.3, Node 24.18.1, Vitest/Testing Library and Playwright.

Operations/security: non-root API/web images, PostgreSQL→API→web release topology, CodeQL Java + JS/TS, Dependency Review, Release Bundle/Contract CI, fail-closed HIGH/CRITICAL container scans and Retailer Bridge CI.

## Release-engineering state

`v0.1.0-rc.1` proved the release trigger until an executable-mode defect; PR #28 fixed it. `v0.1.0-rc.2` proved fail-closed image security on pgJDBC `42.7.11` / `CVE-2026-54291`; PR #29 upgraded pgJDBC and hardened images.

A successful real **`v0.1.0-rc.3` GitHub Release published event remains outstanding**.
