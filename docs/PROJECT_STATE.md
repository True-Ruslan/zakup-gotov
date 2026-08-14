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
M2.2 status: **Stateless Recipe application/API boundary COMPLETE / ACCEPTED (#97 / #96)**  
Current focus: **design M2.3 composed Recipe → Comparison flow while preserving Recipe provenance and accepted comparison/production-access invariants**

## Permanent connectivity rule

Universal Retailer Connectivity remains mandatory:

> Every retailer/banner in the target registry remains coverage work until at least one reproducible accepted acquisition path exists. A failed transport changes the acquisition mode under investigation; it does not remove the retailer from product scope.

Technical feasibility and production-access readiness are separate states. Advancing deterministic product/core milestones does not imply every retailer is production-ready.

## M0 — COMPLETE

| Gate | Status | Evidence |
|---|---|---|
| Pyaterochka path | **PASS** | `AVAILABLE_BROWSER_BRIDGE`, adapter v1 |
| Perekrestok path | **PASS** | `AVAILABLE_BROWSER_BRIDGE`, adapter v2 |
| Independent non-X5 path | **PASS** | Magnit `AVAILABLE_PUBLIC_WEB` |
| Two acquisition modes | **PASS** | browser bridge + public web |
| Deterministic verification | **PASS** | sanitized fixtures/E2E + finite guarded probes |
| Retailer-neutral boundary | **PASS** | provider harness + canonical retailer registry |

M0 completion proves technical feasibility, not blanket permission for recurring production acquisition.

## M1 — Shopping Core — COMPLETE / ACCEPTED

Final acceptance: [`m1-shopping-core-acceptance-2026-08-13.md`](m1-shopping-core-acceptance-2026-08-13.md).

Accepted final hardening baseline: `779d0b219a13e0bf82263a1e655fb732553ed5fe`.

Accepted M1 sequence: #72 retailer registry, #73 shopping list/quantities, #74 provider orchestration, #75 location/fulfillment, #76 snapshots, #77 matching, #78 single-store quote, #79 coverage/freshness boundary, #80 stateless critical journey, #81 structured package evidence, #82/#83/#85 Magnit package semantics/corpus/JSON-LD, #86/#87/#69 Magnit location resolution, #89/#70 Magnit production-access decision, and #91/#90 pre-acquisition production-access enforcement/final acceptance.

M1 guarantees that all eight retailers remain explicit, uncertainty/ambiguity/incomplete states fail safely, incomplete baskets do not expose misleading totals, technical coverage is independent from production access, and runtime acquisition is scoped by production-ready retailer IDs before an evidence source can run.

## Magnit status at M1 exit

### Technical package evidence

Accepted 20-product × 2-shop JSON-LD replay:

- HTTP 2xx: 40/40;
- usable observations: 40/40;
- stable identity: 20/20;
- `FOUND=36`;
- `MISSING=0`;
- `AMBIGUOUS_DIMENSIONS=4`;
- conflicts: 0;
- invalid: 0.

Milk SKU `1000013732` and kefir SKU `1000330180` remain deliberately ambiguous in both shop contexts because both weight and volume are present. Structured egg mass remains mass and cannot satisfy `PIECE` requirements.

### Technical location/store context

Accepted first-party contract: `POST /webgate/v1/stores-facade/search`.

Accepted rules: validated bbox → candidate set; 0 → `NO_STORES`; exactly 1 → `RESOLVED`; >1 → `AMBIGUOUS`; conflicting duplicate identity → `CONFLICTING_STORE_EVIDENCE`; explicit choice → `MANUAL`; no implicit first/nearest heuristic.

Merged-main run `31642543544` on SHA `6ff8372c9e9e61b4c48c43d0d0c159fb65ffe7a1` proved public `shopCode=992301` across two direct stateless requests with identical candidate sets and no session/auth/redirect dependence.

Text/locality/address → coordinates remains intentionally unproven; no hidden geocoder is introduced.

### Production-access decision — #70

Decision memo: [`integrations/magnit-production-access-decision-2026-08-13.md`](integrations/magnit-production-access-decision-2026-08-13.md).

Current product state:

- technical coverage: **`AVAILABLE_PUBLIC_WEB`**;
- production access: **`BLOCKED`**;
- comparison status: **`UNAVAILABLE`**;
- public reason: **`PRODUCTION_ACCESS_BLOCKED`**.

`BLOCKED` is a Zakup Gotov operating policy because an affirmative right to operate the intended recurring production catalog-acquisition/reuse model has not been established. It is not a claim that Magnit expressly prohibits every automated HTTP request and is not a legal adjudication. No production Spring/HTTP Magnit acquisition is activated.

## M2 — Recipes — CURRENT

### M2.1 — Recipe domain and Recipe → ShoppingList — COMPLETE / ACCEPTED (#94 / #93)

Approved design: [`superpowers/specs/2026-08-13-m2-1-recipe-domain-design.md`](superpowers/specs/2026-08-13-m2-1-recipe-domain-design.md).  
Implementation plan: [`superpowers/plans/2026-08-13-m2-1-recipe-domain.md`](superpowers/plans/2026-08-13-m2-1-recipe-domain.md).  
Shipping/acceptance evidence: [`superpowers/plans/2026-08-13-m2-1-recipe-domain-shipping.md`](superpowers/plans/2026-08-13-m2-1-recipe-domain-shipping.md).

Accepted squash merge: `423eb14f7c565bbe264257a92df89a6b42d0d158` (`feat(m2): add deterministic recipe domain (#94)`).

Post-merge acceptance proof on exact `main=423eb14f7c565bbe264257a92df89a6b42d0d158`:

- 8 push-triggered workflow runs total;
- 8/8 completed with `conclusion=success`;
- failures: 0;
- issue #93 closed `completed` after this proof.

Accepted behavior:

- separate top-level `recipe` domain with `RecipeId`, `RecipeIngredientId`, normalized `RecipeTitle`, positive-integer `RecipeServings`, immutable ordered ingredients and duplicate-ID rejection;
- each ingredient reuses existing `ShoppingRequirement` and `Quantity`; Recipe introduces no duplicate unit/canonicalization model;
- pure `RecipeShoppingListConverter` with no Spring, persistence, network, clock or retailer dependency;
- serving scaling sums each compatible merge group before applying `targetServings / baseServings`;
- terminating decimal division remains exact; non-terminating ratios use deterministic `MathContext.DECIMAL128`; no `double`/`float` path exists;
- merge key is exact normalized `ShoppingRequirement` + canonical `QuantityUnit`;
- case differences, synonyms and physical-dimension mismatches do not merge;
- output group order follows first ingredient occurrence;
- generated `ShoppingItemId` is deterministic and list-scoped from `ShoppingListId + requirement text + canonical unit`, independent of amount/target servings;
- kg/g and l/ml input representations converge through existing `Quantity` canonicalization;
- provenance is separate deep-immutable `ShoppingItemId → ordered List<RecipeIngredientRef(RecipeId, RecipeIngredientId)>`;
- artificial generated-ID collisions across different merge keys fail closed;
- Shopping Core production types remain recipe-agnostic and unchanged.

Verification evidence before merge:

- all planned RED → GREEN cycles completed;
- full API `verify` PASS, including Spring Modulith architecture verification;
- reviewed implementation head `734ed53712b4327039eabfb358548828aa1a1dbe` passed 9/9 PR workflow groups;
- code+docs head `250d00f10b1c51fee0826356dfb95f8e7b853c50` passed 9/9;
- final shipping marker head `512be04a2a0147d9787465481388e6847a20d69d` passed 9/9;
- independent review: **Looks good**, no P0/P1/P2; review threads empty.

M2.1 intentionally did **not** add REST/OpenAPI/generated-client contracts, persistence, recipe UI, AI/NLP import, fuzzy ingredient equivalence, nutrition optimization, pantry prediction, fractional servings, or multi-recipe aggregation.

### M2.2 — Stateless Recipe shopping preview application/API boundary — COMPLETE / ACCEPTED (#97 / #96)

Authoritative design: [`superpowers/specs/2026-08-13-m2-2-recipe-shopping-preview-design-v2.md`](superpowers/specs/2026-08-13-m2-2-recipe-shopping-preview-design-v2.md).  
Execution plan: [`superpowers/plans/2026-08-13-m2-2-recipe-shopping-preview-v2.md`](superpowers/plans/2026-08-13-m2-2-recipe-shopping-preview-v2.md).  
Shipping/acceptance evidence: [`superpowers/plans/2026-08-14-m2-2-recipe-shopping-preview-shipping.md`](superpowers/plans/2026-08-14-m2-2-recipe-shopping-preview-shipping.md).

Accepted boundary:

`POST /api/v1/recipe-shopping-previews`

`HTTP recipe request → request validation + server-owned transient IDs → Recipe domain → RecipeShoppingListConverter → self-contained ShoppingList projection`

Accepted behavior:

- stateless request/response; no Recipe persistence or CRUD;
- server-owned Recipe, ingredient and ShoppingList UUIDs; clients cannot supply internal identities;
- normalized title/requirements, positive integer base/target servings and 1..100 explicit ingredients;
- strict recipe-scoped JSON integer handling for servings; fractional serving counts fail as unreadable input instead of being silently coerced, while decimal ingredient quantities remain valid;
- input quantity units reuse `PIECE / GRAM / KILOGRAM / MILLILITER / LITER`; output uses existing canonical `PIECE / GRAM / MILLILITER` semantics;
- all scaling, exact-safe merge grouping, ordering and deterministic ShoppingItem identity remain delegated to accepted M2.1 `RecipeShoppingListConverter`;
- response contains normalized/canonical base-recipe ingredients plus generated shopping items;
- every shopping item exposes ordered `sourceIngredientIds` resolving to ingredients in the same response;
- missing, orphan, cross-recipe or mismatched-list provenance fails closed as an internal invariant failure;
- request validation returns ordered `INVALID_RECIPE_SHOPPING_PREVIEW` problem details; malformed/unknown-field/unknown-unit/non-integer binding failures return one sanitized `$request` error;
- controller remains thin; controller-scoped advice handles only known semantic/unreadable request failures; internal invariant failures are not mislabeled as 400;
- OpenAPI 3.1 is source of truth for the endpoint and schemas;
- generated TypeScript client exports `RECIPE_SHOPPING_PREVIEWS_PATH` and generated request/response types;
- architecture guards preserve `recipepreview → recipe → shopping` / `recipepreview → shopping` direction and forbid provider/retailer/matching/basket/comparison/database dependencies;
- no retailer network request, address/location data, persistence, recipe UI, comparison orchestration or fuzzy/AI matching is introduced.

Acceptance evidence:

- granular TDD recorded clean behavioral/contract RED checkpoints before their corresponding GREEN implementations; syntax/compile mistakes were corrected and rerun rather than counted as behavioral RED;
- full API verification continued to exercise Spring Boot context, Spring Modulith and the existing PostgreSQL 18/Testcontainers/Flyway integration baseline; no artificial M2.2 persistence was introduced for test-count optics;
- a real fractional-servings RED exposed Jackson's default `1.5 → Integer` coercion, fixed locally through `StrictIntegerDeserializer` without changing global API binding behavior;
- OpenAPI/generated-client RED and generated-schema freshness RED were observed before contract synchronization;
- final exact PR head `318a48c569d0d001a4c27b5792e1681f7884e518` passed all 9 normal PR workflow groups, including full API CI, Contract CI, Web CI + responsive Playwright E2E, Retailer Bridge CI + Chromium E2E, CodeQL Java + JS/TS, Dependency Review, Container Security, Release Contract and Release Bundle;
- final independent review: **Looks good**, no P0/P1/P2/P3; review threads empty;
- accepted squash merge: `8f0c1d8d31cfc1673656780a7989512d38788aff` (`feat(m2): add stateless recipe shopping preview API (#97)`);
- post-merge proof on exact `main=8f0c1d8d31cfc1673656780a7989512d38788aff`: 8 push-triggered workflows total, 8/8 `success`, 0 failures;
- issue #96 closed automatically with `state_reason=completed`.

### M2.3 — Composed Recipe → Comparison flow — CURRENT NEXT DESIGN TARGET

Target deterministic path:

`Recipe input → recipe-shopping preview → generated shopping requirements → comparison preview`

Required design constraints:

- compose accepted Recipe and Comparison semantics without duplicating either domain;
- preserve Recipe/ingredient provenance through the composed result instead of losing identity at an HTTP handoff;
- keep server-owned transient identity and provider/fulfillment internals on the correct side of the boundary;
- preserve existing production-access gating before evidence acquisition;
- preserve complete/uncertain/incomplete/unavailable comparison states and no-total-on-incomplete behavior;
- keep ordinary CI retailer-network-free;
- define application/contract tests RED-first before introducing Recipe UI.

Only after the composed flow is accepted should the first real responsive Recipe UI be implemented with frontend component TDD and desktop/mobile Playwright RED-first. Persistence, saved recipes and fuzzy/AI ingestion remain separate product decisions.

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

## Platform baseline

Backend: Java 25, Spring Boot 4.1, Spring MVC + Virtual Threads, Spring Modulith, PostgreSQL 18/Testcontainers, Flyway, jOOQ, pgJDBC `42.7.12`.

Contracts/web: OpenAPI 3.1, generated TypeScript client, Next.js 16.3.0, React 19.2.8, TypeScript 5.9.3, Node 24.18.1, Vitest/Testing Library and Playwright.

Operations/security: non-root API/web images, PostgreSQL→API→web release topology, CodeQL Java + JS/TS, Dependency Review, Release Bundle/Contract CI, fail-closed HIGH/CRITICAL container scans and Retailer Bridge CI.

## Release-engineering state

`v0.1.0-rc.1` proved the release trigger until an executable-mode defect; PR #28 fixed it. `v0.1.0-rc.2` proved fail-closed image security on pgJDBC `42.7.11` / `CVE-2026-54291`; PR #29 upgraded pgJDBC and hardened images.

A successful real **`v0.1.0-rc.3` GitHub Release published event remains outstanding**.
