# M3.2 Stateless WeeklyPlan Shopping Preview Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Expose accepted M3.1 WeeklyPlan composition as a stateless, contract-first shopping-preview API with server-owned identities and self-contained occurrence-aware Recipe provenance.

**Architecture:** Add a new `weeklyplanpreview` application package. Planner-level request construction delegates nested Recipe semantics to the accepted `RecipeShoppingPreviewRequestFactory`, then builds `WeeklyPlan` and invokes `WeeklyPlanShoppingListComposer` exactly once. HTTP, problem details and OpenAPI remain thin projections around the application service; no persistence, comparison or retailer path is introduced.

**Tech Stack:** Java 25, Spring Boot 4.1/Spring MVC, Jackson 3, Spring Modulith, JUnit 5/AssertJ/ArchUnit, OpenAPI 3.1, generated TypeScript client, GitHub Actions.

## Global Constraints

- Endpoint is exactly `POST /api/v1/weekly-plan-shopping-previews`.
- Request contains ordered `1..35` occurrences and no UUID/server identity fields.
- Days are only accepted M3.1 `MONDAY..SUNDAY` values; no meal-slot enum.
- Base/target servings are positive JSON integers; ingredient quantities remain positive decimals.
- Nested Recipe validation/normalization/1..100 ingredient semantics are delegated to accepted M2.2 construction rather than copied.
- M3.1 remains authoritative for weekly ShoppingList identity, scaling, canonicalization, exact merge, quantity sum, output order and final ShoppingItem identity.
- Public provenance is occurrence ID + Recipe ID + RecipeIngredient ID and must resolve entirely inside the same response.
- `RecipeAggregationEntryId` never appears publicly.
- Semantic/unreadable requests use sanitized `INVALID_WEEKLY_PLAN_SHOPPING_PREVIEW` Problem Details.
- OpenAPI 3.1 is the source of truth; TypeScript contract remains generated.
- No persistence/history, locality/comparison, UI, pantry, nutrition, calendar/time-zone, fuzzy/AI or provider/retailer traffic.
- Ordinary CI makes no live retailer request.

---

### Task 1: Planner request construction and transient domain identities

**Files:**
- Create: `apps/api/src/main/java/io/github/trueruslan/zakupgotov/weeklyplanpreview/WeeklyPlanShoppingPreviewRequest.java`
- Create: `apps/api/src/main/java/io/github/trueruslan/zakupgotov/weeklyplanpreview/WeeklyPlanShoppingPreviewOccurrenceRequest.java`
- Create: `apps/api/src/main/java/io/github/trueruslan/zakupgotov/weeklyplanpreview/WeeklyPlanShoppingPreviewRecipeRequest.java`
- Create: `apps/api/src/main/java/io/github/trueruslan/zakupgotov/weeklyplanpreview/WeeklyPlanShoppingPreviewIdGenerator.java`
- Create: `apps/api/src/main/java/io/github/trueruslan/zakupgotov/weeklyplanpreview/RandomWeeklyPlanShoppingPreviewIdGenerator.java`
- Create: `apps/api/src/main/java/io/github/trueruslan/zakupgotov/weeklyplanpreview/WeeklyPlanShoppingPreviewInput.java`
- Create: `apps/api/src/main/java/io/github/trueruslan/zakupgotov/weeklyplanpreview/WeeklyPlanShoppingPreviewValidationError.java`
- Create: `apps/api/src/main/java/io/github/trueruslan/zakupgotov/weeklyplanpreview/InvalidWeeklyPlanShoppingPreviewRequestException.java`
- Create: `apps/api/src/main/java/io/github/trueruslan/zakupgotov/weeklyplanpreview/WeeklyPlanShoppingPreviewRequestFactory.java`
- Create: `apps/api/src/main/java/io/github/trueruslan/zakupgotov/weeklyplanpreview/WeeklyPlanStrictIntegerDeserializer.java`
- Test: `apps/api/src/test/java/io/github/trueruslan/zakupgotov/weeklyplanpreview/WeeklyPlanShoppingPreviewRequestFactoryTest.java`

**Interfaces:**
- Consumes: `RecipeShoppingPreviewRequestFactory.create(RecipeShoppingPreviewRequest)`, `WeeklyPlan`, `WeeklyMealOccurrence`, `WeeklyPlanDay`.
- Produces: `WeeklyPlanShoppingPreviewInput(WeeklyPlan weeklyPlan)` for Task 2.

- [ ] **Step 1: Write RED request-factory tests** proving: 1..35 ordered occurrences; empty/null/>35 rejection; null occurrence/day/recipe rejection; positive integer target servings; nested Recipe validation errors receive `occurrences[i].recipe.*` prefixes; generated WeeklyPlan/occurrence/Recipe/ingredient IDs are server-owned and unique; same Recipe payload in two occurrences still receives distinct occurrence/Recipe identities; caller order is preserved.
- [ ] **Step 2: Run `./mvnw --batch-mode --no-transfer-progress -Dtest=WeeklyPlanShoppingPreviewRequestFactoryTest test` from `apps/api`** and capture expected missing-type/test failures.
- [ ] **Step 3: Implement the minimal request types/factory.** Adapt each nested Recipe to `RecipeShoppingPreviewRequest(title, baseServings, occurrence.targetServings, ingredients)` and delegate to `RecipeShoppingPreviewRequestFactory`; translate nested validation errors instead of revalidating Recipe fields.
- [ ] **Step 4: Re-run targeted tests and then `./mvnw --batch-mode --no-transfer-progress verify`.** Both must pass.
- [ ] **Step 5: Commit the GREEN checkpoint.**

### Task 2: WeeklyPlan composition and self-contained projection

**Files:**
- Create: `apps/api/src/main/java/io/github/trueruslan/zakupgotov/weeklyplanpreview/WeeklyPlanShoppingPreview.java`
- Create focused response records under `weeklyplanpreview` for weekly plan, occurrence, recipe, ingredient, shopping list, shopping item and source projection.
- Create: `apps/api/src/main/java/io/github/trueruslan/zakupgotov/weeklyplanpreview/WeeklyPlanShoppingPreviewService.java`
- Test: `apps/api/src/test/java/io/github/trueruslan/zakupgotov/weeklyplanpreview/WeeklyPlanShoppingPreviewServiceTest.java`
- Test: `apps/api/src/test/java/io/github/trueruslan/zakupgotov/weeklyplanpreview/WeeklyPlanShoppingPreviewProjectionInvariantTest.java`

**Interfaces:**
- Consumes: Task 1 `WeeklyPlanShoppingPreviewRequestFactory`; accepted `WeeklyPlanShoppingListComposer.compose(WeeklyPlan)`.
- Produces: immutable `WeeklyPlanShoppingPreview` response model for Task 3.

- [ ] **Step 1: Write RED service/projection tests** proving one call to accepted M3.1 composition; canonical cross-occurrence merge and serving scaling; occurrence order preservation; public source tuples contain exact occurrence/Recipe/ingredient IDs; every source resolves inside the returned plan; internal aggregation IDs are absent; impossible missing/orphan/mismatched provenance fails closed; nested response lists are immutable.
- [ ] **Step 2: Run targeted tests and capture expected missing production types/failures.**
- [ ] **Step 3: Implement the minimal service and response projection.** Do not perform quantity math, merge-key logic or ShoppingItem-ID derivation in this package.
- [ ] **Step 4: Run targeted tests plus full Maven verify.**
- [ ] **Step 5: Commit the GREEN checkpoint.**

### Task 3: HTTP endpoint and sanitized problem details

**Files:**
- Create: `apps/api/src/main/java/io/github/trueruslan/zakupgotov/weeklyplanpreview/WeeklyPlanShoppingPreviewController.java`
- Create: `apps/api/src/main/java/io/github/trueruslan/zakupgotov/weeklyplanpreview/WeeklyPlanShoppingPreviewConfiguration.java`
- Create: `apps/api/src/main/java/io/github/trueruslan/zakupgotov/weeklyplanpreview/InvalidWeeklyPlanShoppingPreviewProblem.java`
- Create: `apps/api/src/main/java/io/github/trueruslan/zakupgotov/weeklyplanpreview/WeeklyPlanShoppingPreviewExceptionHandler.java`
- Test: `apps/api/src/test/java/io/github/trueruslan/zakupgotov/weeklyplanpreview/WeeklyPlanShoppingPreviewControllerTest.java`
- Test: `apps/api/src/test/java/io/github/trueruslan/zakupgotov/weeklyplanpreview/WeeklyPlanShoppingPreviewHttpFailureContractTest.java`

**Interfaces:**
- Consumes: Task 2 service.
- Produces: `POST /api/v1/weekly-plan-shopping-previews` JSON/Problem Details behavior.

- [ ] **Step 1: Write RED MockMvc tests** for 200 response shape/order/provenance and 400 problem behavior for empty occurrences, nested semantic errors, fractional servings, unknown fields, unknown day/unit and malformed JSON.
- [ ] **Step 2: Run targeted HTTP tests and capture expected missing controller/problem behavior.**
- [ ] **Step 3: Implement thin controller/configuration/advice/problem projection.** `HttpMessageNotReadableException` must map to one `$request: malformed JSON request` error; no raw parser message may escape.
- [ ] **Step 4: Run HTTP tests plus full Maven verify.**
- [ ] **Step 5: Commit the GREEN checkpoint.**

### Task 4: OpenAPI and generated TypeScript contract

**Files:**
- Modify: `packages/api-client/openapi/zakup-gotov.openapi.yaml`
- Modify generated TypeScript contract file(s) under `packages/api-client/src/generated/` according to the repository generator.
- Modify path-export file under `packages/api-client/src/` to export `WEEKLY_PLAN_SHOPPING_PREVIEWS_PATH` if it is not generated today.
- Test existing contract/client freshness checks; add a small explicit path/type assertion only if existing generated-contract tests do not cover the new endpoint.

**Interfaces:**
- Consumes: Task 3 public JSON shape.
- Produces: operation `createWeeklyPlanShoppingPreview` and generated TypeScript request/response/problem types.

- [ ] **Step 1: Add/adjust a contract test so the missing path/operation/types are RED before editing the OpenAPI source.**
- [ ] **Step 2: Run the repository contract/client check and capture the expected failure.**
- [ ] **Step 3: Add the OpenAPI 3.1 path/components matching Task 3 exactly, including `1..35`, integer servings, supported quantity units, `additionalProperties: false`, UUID response identities and `INVALID_WEEKLY_PLAN_SHOPPING_PREVIEW`.**
- [ ] **Step 4: Run the pinned generator and generated-client build/typecheck/freshness checks; verify a clean generated diff afterwards.**
- [ ] **Step 5: Commit the GREEN contract checkpoint.**

### Task 5: Architecture guard, shipping evidence and release gate

**Files:**
- Create: `apps/api/src/test/java/io/github/trueruslan/zakupgotov/weeklyplanpreview/WeeklyPlanShoppingPreviewArchitectureTest.java`
- Create after implementation: `docs/superpowers/plans/2026-08-14-m3-2-weekly-plan-shopping-preview-shipping.md`

**Interfaces:**
- Consumes all earlier tasks.
- Produces reviewable final PR head and acceptance evidence.

- [ ] **Step 1: Add architecture tests** requiring the production package to exist, forbidding provider/retailer/matching/basket/comparison/preview/database dependencies and forbidding accepted recipe/shopping/weeklyplan packages from depending back on `weeklyplanpreview`.
- [ ] **Step 2: Run full API Maven/Testcontainers/Modulith verify plus contract/client/web regression checks used by normal CI.**
- [ ] **Step 3: Write shipping evidence with every RED/GREEN SHA and current verification state.** Keep status `IMPLEMENTED / TESTED / SHIPPING — acceptance pending` until merge/post-merge proof exists.
- [ ] **Step 4: Open/update PR, require 9/9 exact-head workflow groups, perform read-only change review, resolve blockers, and re-run exact-head CI after any change.**
- [ ] **Step 5: Squash merge with expected-head protection, verify exactly 8 normal push workflows all succeed on the merged main SHA, then synchronize acceptance memo, PROJECT_STATE, ROADMAP and CHANGELOG in a docs-only acceptance PR.**

## Self-review

- Spec coverage: every approved request rule, identity rule, provenance invariant, error contract, architecture boundary, OpenAPI/client requirement and non-goal maps to a task above.
- Placeholder scan: no deferred implementation placeholders remain.
- Type consistency: Task 1 produces `WeeklyPlanShoppingPreviewInput`; Task 2 produces `WeeklyPlanShoppingPreview`; Task 3 serializes that exact model; Task 4 mirrors Task 3.
- Scope remains one independently testable subsystem: stateless WeeklyPlan shopping-preview boundary. Comparison and UI remain follow-on M3.3/M3.4 slices.