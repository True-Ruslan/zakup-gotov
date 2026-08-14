# M2.3 Recipe → Comparison Preview Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a stateless Recipe → Comparison application/API composition that preserves Recipe provenance and generated shopping-item identity while delegating all recipe conversion and comparison semantics to accepted boundaries.

**Architecture:** Introduce `recipecomparisonpreview` as an orchestration package depending only on `recipepreview` and `preview`. It invokes Recipe shopping preview once, maps its canonical shopping items to comparison request items without semantic changes, invokes comparison preview once, verifies cross-boundary identity/order/value invariants, and returns both accepted projections.

**Tech Stack:** Java 25, Spring Boot 4.1/Spring MVC, JUnit 5/AssertJ/MockMvc/ArchUnit, OpenAPI 3.1, generated TypeScript client, pnpm/Vitest, existing GitHub Actions matrix.

## Global Constraints

- Baseline: `main=bcfa16e1497f72cc36aa379e0effb75b0c2f3532`.
- Endpoint: `POST /api/v1/recipe-comparison-previews`, operation `createRecipeComparisonPreview`, `200 OK`.
- Direct dependencies: only `recipepreview` and `preview`.
- No direct provider/retailer/matching/basket/comparison/database/persistence dependency.
- Preserve generated ShoppingItem UUID, order, requirement and canonical quantity exactly into comparison.
- Do not duplicate Recipe validation/scaling/merge/provenance or comparison/matching/basket/access-gate semantics.
- Ordinary CI makes no live retailer calls.
- OpenAPI is source of truth; generated `schema.d.ts` is never hand-edited.
- No UI, persistence, fuzzy/AI matching, retailer activation or database migration in M2.3.

---

### Task 1: Lock the composed service contract with RED tests

**Files:**
- Create: `apps/api/src/test/java/io/github/trueruslan/zakupgotov/recipecomparisonpreview/RecipeComparisonPreviewServiceTest.java`
- Create: `apps/api/src/test/java/io/github/trueruslan/zakupgotov/recipecomparisonpreview/RecipeComparisonPreviewArchitectureTest.java`

**Interfaces:**
- Consumes: `RecipeShoppingPreviewService#create(RecipeShoppingPreviewRequest)` and `ComparisonPreviewService#create(ComparisonPreviewRequest)`.
- Produces: desired `RecipeComparisonPreviewService#create(RecipeComparisonPreviewRequest)` behavior.

- [ ] **Step 1: Write service RED tests** proving a generated recipe shopping item is forwarded with the same UUID, requirement, canonical quantity and order, and both accepted projections are returned.
- [ ] **Step 2: Run focused tests** with `cd apps/api && ./mvnw -Dtest=RecipeComparisonPreviewServiceTest test`; expected failure is missing M2.3 production types.
- [ ] **Step 3: Write architecture RED** forbidding direct dependencies outside `recipepreview` and `preview` plus Java/Spring/Jackson standard dependencies.
- [ ] **Step 4: Run architecture test** and confirm failure is caused by absent M2.3 package/types, not test syntax.
- [ ] **Step 5: Commit RED checkpoint** before production code.

### Task 2: Implement minimal application composition GREEN

**Files:**
- Create: `apps/api/src/main/java/io/github/trueruslan/zakupgotov/recipecomparisonpreview/RecipeComparisonPreviewRequest.java`
- Create: `apps/api/src/main/java/io/github/trueruslan/zakupgotov/recipecomparisonpreview/RecipeComparisonPreview.java`
- Create: `apps/api/src/main/java/io/github/trueruslan/zakupgotov/recipecomparisonpreview/RecipeComparisonPreviewService.java`
- Create: `apps/api/src/main/java/io/github/trueruslan/zakupgotov/recipecomparisonpreview/RecipeComparisonPreviewConfiguration.java`

**Interfaces:**
- `RecipeComparisonPreviewRequest(String locality, RecipeShoppingPreviewRequest recipe)`.
- `RecipeComparisonPreview(RecipeShoppingPreview recipeShoppingPreview, ComparisonPreview comparisonPreview)`.
- `RecipeComparisonPreviewService(RecipeShoppingPreviewService, ComparisonPreviewService)`.
- `RecipeComparisonPreview create(RecipeComparisonPreviewRequest request)`.

- [ ] **Step 1: Implement wrapper records** with null-safe immutable semantics and no business-rule duplication.
- [ ] **Step 2: Implement mapping** from each `RecipeShoppingPreviewShoppingItem` to `ComparisonPreviewItemRequest` using its existing UUID, requirement and quantity amount/unit.
- [ ] **Step 3: Delegate comparison** with request locality and generated items.
- [ ] **Step 4: Add post-composition invariant validation** for cardinality, item ID, order, requirement and canonical quantity equality; throw `IllegalStateException` on impossible drift.
- [ ] **Step 5: Wire Spring configuration** from accepted application services.
- [ ] **Step 6: Run focused tests**; expected PASS.
- [ ] **Step 7: Commit GREEN application slice**.

### Task 3: Add HTTP/controller and binding contract RED→GREEN

**Files:**
- Create: `apps/api/src/test/java/io/github/trueruslan/zakupgotov/recipecomparisonpreview/RecipeComparisonPreviewControllerTest.java`
- Create: `apps/api/src/main/java/io/github/trueruslan/zakupgotov/recipecomparisonpreview/RecipeComparisonPreviewController.java`
- Create: `apps/api/src/main/java/io/github/trueruslan/zakupgotov/recipecomparisonpreview/RecipeComparisonPreviewExceptionHandler.java`
- Create: `apps/api/src/main/java/io/github/trueruslan/zakupgotov/recipecomparisonpreview/InvalidRecipeComparisonPreviewProblem.java`

**Interfaces:**
- `@PostMapping("/api/v1/recipe-comparison-previews")` delegates only to the service.
- Wrapper unreadable/unknown JSON maps to one sanitized `$request` problem with code `INVALID_RECIPE_COMPARISON_PREVIEW`.
- Nested semantic service failures are not converted to generic wrapper failures.
- Internal invariant failures are not caught as 400.

- [ ] **Step 1: Write controller RED** for successful nested response and exact endpoint.
- [ ] **Step 2: Write malformed/unknown-wrapper RED** asserting `400 application/problem+json`, stable problem code and no Java/Jackson internals.
- [ ] **Step 3: Run controller tests** and verify intended RED.
- [ ] **Step 4: Implement thin controller and controller-scoped advice** catching only unreadable wrapper-body binding failures.
- [ ] **Step 5: Run focused controller tests**; expected PASS.
- [ ] **Step 6: Re-run architecture tests**; expected PASS.
- [ ] **Step 7: Commit HTTP slice**.

### Task 4: Add OpenAPI/generated-client contract RED→GREEN

**Files:**
- Modify: `openapi/zakup-gotov.yaml`
- Modify: `packages/api-client/src/index.ts`
- Modify: `packages/api-client/src/index.test.ts`
- Regenerate: `packages/api-client/src/schema.d.ts`

**Interfaces:**
- Path: `/api/v1/recipe-comparison-previews`.
- Operation: `createRecipeComparisonPreview`.
- Request wrapper references existing Recipe shopping request schema.
- Response wrapper references existing Recipe shopping preview and Comparison preview schemas.
- Export `RECIPE_COMPARISON_PREVIEWS_PATH`.

- [ ] **Step 1: Add TypeScript RED assertions** for path constant and generated operation/request/response types before editing OpenAPI.
- [ ] **Step 2: Run API-client test/typecheck** and confirm failure is due to missing M2.3 path/types.
- [ ] **Step 3: Add OpenAPI path and wrapper schemas** with `additionalProperties: false`, explicit required fields and references to accepted schemas.
- [ ] **Step 4: Regenerate schema** using repository generator (`pnpm --filter @zakup-gotov/api-client generate`).
- [ ] **Step 5: Add path constant** to `index.ts`.
- [ ] **Step 6: Run generated-schema freshness, typecheck, Vitest and build**; all expected PASS.
- [ ] **Step 7: Commit contract/client slice**.

### Task 5: Full regression, review and shipping gate

**Files:**
- Modify only if required by proven regression: M2.3 files from Tasks 1–4.
- Create later shipping evidence: `docs/superpowers/plans/2026-08-14-m2-3-recipe-comparison-preview-shipping.md`.
- Update after acceptance only: `docs/PROJECT_STATE.md`, `docs/ROADMAP.md`, `CHANGELOG.md`.

- [ ] **Step 1: Run full backend** `cd apps/api && ./mvnw verify`; must include Modulith and existing PostgreSQL/Testcontainers baseline.
- [ ] **Step 2: Run repository contract/client verification** including generated-schema freshness, client typecheck/tests/build.
- [ ] **Step 3: Run Web regression** unit/build/Playwright with no new UI claims.
- [ ] **Step 4: Push exact implementation head and inspect all normal PR workflow groups**: API CI, Web CI/E2E, Contract CI, Release Contract, Release Bundle, Retailer Bridge, Dependency Review, Container Security, CodeQL Java + JS/TS.
- [ ] **Step 5: Fix only evidence-backed failures with RED→GREEN bug tests when behavior changes.**
- [ ] **Step 6: Perform independent read-only PR review**; block merge on unresolved P0/P1/P2.
- [ ] **Step 7: Write shipping evidence** with exact reviewed SHA and check results; keep docs honest about implemented/tested/reviewed vs accepted.
- [ ] **Step 8: Mark draft PR ready only after exact-head green and review-clean.**
- [ ] **Step 9: Squash merge with expected head SHA.**
- [ ] **Step 10: Verify all normal post-merge `main` workflows on the merge SHA.**
- [ ] **Step 11: Close #100 as completed and update PROJECT_STATE/ROADMAP/CHANGELOG only after post-merge proof.**

## Self-review

- Spec coverage: endpoint, delegation, identity/provenance preservation, locality, fail-closed invariants, architecture, OpenAPI/client and shipping gates are each assigned to a task.
- No placeholder/TODO steps remain.
- Type consistency: wrapper/service/endpoint/path names are identical across Tasks 1–4.
- Scope remains one independently shippable deterministic application/API slice; UI and persistence remain separate.
