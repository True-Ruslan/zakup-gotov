# M2.3 Recipe → Comparison Preview Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a stateless Recipe → Comparison application/API composition that preserves Recipe provenance and generated shopping-item identity while delegating all recipe conversion and comparison semantics to accepted boundaries.

**Architecture:** Introduce `recipecomparisonpreview` as an orchestration package depending primarily on `recipepreview` and `preview`. Because those accepted DTOs expose canonical Shopping values in their public signatures, the composer may additionally depend only on `shopping.Quantity` and `shopping.QuantityUnit`; ArchUnit must reject every other direct Shopping dependency and all Recipe/downstream internals. The composer invokes Recipe shopping preview once, maps its canonical shopping items to comparison request items without semantic changes, invokes comparison preview once, verifies cross-boundary identity/order/value invariants, and returns both accepted projections.

**Tech Stack:** Java 25, Spring Boot 4.1/Spring MVC, JUnit 5/AssertJ/MockMvc/ArchUnit, OpenAPI 3.1, generated TypeScript client, pnpm/Vitest, existing GitHub Actions matrix.

## Global Constraints

- Baseline: `main=bcfa16e1497f72cc36aa379e0effb75b0c2f3532`.
- Endpoint: `POST /api/v1/recipe-comparison-previews`, operation `createRecipeComparisonPreview`, `200 OK`.
- Primary direct dependencies: `recipepreview` and `preview`; finite canonical Shopping value bridge: only `Quantity` and `QuantityUnit`.
- No direct Recipe-domain/provider/retailer/matching/basket/comparison/database/persistence dependency and no other Shopping type dependency.
- Preserve generated ShoppingItem UUID, order, requirement and canonical quantity exactly into comparison.
- Do not duplicate Recipe validation/scaling/merge/provenance or comparison/matching/basket/access-gate semantics.
- Ordinary CI makes no live retailer calls.
- OpenAPI is source of truth; generated `schema.d.ts` must be proven current by pinned regeneration plus clean diff.
- No UI, persistence, fuzzy/AI matching, retailer activation or database migration in M2.3.

---

### Task 1: Lock the composed service contract with RED tests

**Files:**
- Create: `apps/api/src/test/java/io/github/trueruslan/zakupgotov/recipecomparisonpreview/RecipeComparisonPreviewServiceTest.java`
- Create: `apps/api/src/test/java/io/github/trueruslan/zakupgotov/recipecomparisonpreview/RecipeComparisonPreviewArchitectureTest.java`

**Interfaces:**
- Consumes: `RecipeShoppingPreviewService#create(RecipeShoppingPreviewRequest)` and `ComparisonPreviewService#create(ComparisonPreviewRequest)`.
- Produces: desired `RecipeComparisonPreviewService#create(RecipeComparisonPreviewRequest)` behavior.

- [x] **Step 1: Write service RED tests** proving a generated recipe shopping item is forwarded with the same UUID, requirement, canonical quantity and order, and both accepted projections are returned.
- [x] **Step 2: Execute RED through API CI**; exact RED head `e8b58bbd01615eec5cdbdb8d7347d90746e488b3` failed with `cannot find symbol` for the absent M2.3 production types.
- [x] **Step 3: Write architecture guard** forbidding Recipe/downstream internals and every Shopping dependency except `Quantity`/`QuantityUnit`.
- [x] **Step 4: Verify architecture/package behavior in full Maven `verify`** on later GREEN heads.
- [x] **Step 5: Commit RED checkpoint** before production code.

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

- [x] **Step 1: Implement wrapper records** with null-safe immutable response semantics and no business-rule duplication.
- [x] **Step 2: Implement mapping** from each `RecipeShoppingPreviewShoppingItem` to `ComparisonPreviewItemRequest` using its existing UUID, requirement and quantity amount/unit.
- [x] **Step 3: Delegate comparison** with request locality and generated items.
- [x] **Step 4: Add post-composition invariant validation** for cardinality, item ID/order, requirement and canonical quantity equality; impossible drift throws `IllegalStateException`.
- [x] **Step 5: Wire Spring configuration** from accepted application services.
- [x] **Step 6: Verify GREEN through full API CI**; exact head `1503305e65f8520f9ae81c0c273d9e798be86e52` passed API CI including Maven `verify`.
- [x] **Step 7: Add explicit fail-closed invariant regression assertions** for cardinality, identity/order, requirement and quantity drift before shipping.

### Task 3: Add HTTP/controller and binding contract RED→GREEN

**Files:**
- Create: `apps/api/src/test/java/io/github/trueruslan/zakupgotov/recipecomparisonpreview/RecipeComparisonPreviewControllerTest.java`
- Create: `apps/api/src/main/java/io/github/trueruslan/zakupgotov/recipecomparisonpreview/RecipeComparisonPreviewController.java`
- Create: `apps/api/src/main/java/io/github/trueruslan/zakupgotov/recipecomparisonpreview/RecipeComparisonPreviewExceptionHandler.java`
- Create: `apps/api/src/main/java/io/github/trueruslan/zakupgotov/recipecomparisonpreview/InvalidRecipeComparisonPreviewProblem.java`

**Interfaces:**
- `@PostMapping("/api/v1/recipe-comparison-previews")` delegates only to the service.
- Wrapper unreadable/unknown JSON maps to one sanitized `$request` problem with code `INVALID_RECIPE_COMPARISON_PREVIEW`.
- Nested Recipe semantic failures remain `INVALID_RECIPE_SHOPPING_PREVIEW`.
- Locality/comparison semantic failures remain `INVALID_COMPARISON_PREVIEW`.
- Internal invariant failures are not caught as 400.

- [x] **Step 1: Write controller RED** for successful nested response and exact endpoint.
- [x] **Step 2: Write malformed/unknown-wrapper RED** asserting `400 application/problem+json`, stable problem code and sanitized error.
- [x] **Step 3: Execute controller RED through API CI**; exact RED head `5c188fff00207627368f19a8c9c029b11242d5da` failed with `cannot find symbol` for the missing controller/advice.
- [x] **Step 4: Implement thin controller and controller-scoped advice** for nested semantic problems and unreadable wrapper bodies only.
- [x] **Step 5: Verify focused behavior through full API CI**; exact GREEN head `8bfb7590b28228b397ec49b6759d110d89c54ebd` passed.
- [x] **Step 6: Re-run architecture tests** as part of full Maven verify.
- [x] **Step 7: Commit HTTP slice**.

### Task 4: Add OpenAPI/generated-client contract RED→GREEN

**Files:**
- Modify: `openapi/zakup-gotov.yaml`
- Modify: `packages/api-client/src/index.ts`
- Modify: `packages/api-client/src/index.test.ts`
- Regenerate/verify: `packages/api-client/src/schema.d.ts`

**Interfaces:**
- Path: `/api/v1/recipe-comparison-previews`.
- Operation: `createRecipeComparisonPreview`.
- Request wrapper references existing Recipe shopping request schema.
- Response wrapper references existing Recipe shopping preview and Comparison preview schemas.
- 400 response documents wrapper, Recipe and Comparison problem variants.
- Export `RECIPE_COMPARISON_PREVIEWS_PATH`.

- [x] **Step 1: Add TypeScript RED assertions** for path constant and generated operation/request/response types before editing OpenAPI.
- [x] **Step 2: Execute contract RED through Contract CI**; exact head `790c7ba4122f71da1978bf4d33c0a15c8ab2daff` passed generated-schema freshness but failed TypeScript typecheck because M2.3 types/path were absent.
- [x] **Step 3: Add OpenAPI path and wrapper schemas** with `additionalProperties: false`, explicit required fields and references to accepted schemas.
- [x] **Step 4: Synchronize generated schema as derived output** from OpenAPI; because local network/tool installation is unavailable in the execution environment, pinned Contract CI regeneration is the authoritative verification.
- [x] **Step 5: Add path constant** to `index.ts`.
- [x] **Step 6: Contract CI on exact head `ffdc6201885f74d11871d2ad262b85e1a37d55b3` passed generated-schema freshness, typecheck, Vitest and build, proving byte-equivalent pinned regeneration.
- [x] **Step 7: Commit contract/client slice**.

### Task 5: Full regression, review and shipping gate

**Files:**
- Modify only if required by proven regression: M2.3 files from Tasks 1–4.
- Create later shipping evidence: `docs/superpowers/plans/2026-08-14-m2-3-recipe-comparison-preview-shipping.md`.
- Update after acceptance only: `docs/PROJECT_STATE.md`, `docs/ROADMAP.md`, `CHANGELOG.md`.

- [ ] **Step 1: Full backend exact-head API CI** must pass Maven `verify`, including Modulith and existing PostgreSQL/Testcontainers baseline.
- [ ] **Step 2: Contract/client exact-head CI** must pass generated-schema freshness, client typecheck/tests/build.
- [ ] **Step 3: Web regression** unit/build/Playwright must remain green with no new UI claim.
- [ ] **Step 4: Inspect all normal exact-head PR workflow groups**: API CI, Web CI/E2E, Contract CI, Release Contract, Release Bundle, Retailer Bridge, Dependency Review, Container Security, CodeQL Java + JS/TS.
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
- The canonical Shopping value bridge is finite and architecture-tested rather than implied.
- Scope remains one independently shippable deterministic application/API slice; UI and persistence remain separate.
