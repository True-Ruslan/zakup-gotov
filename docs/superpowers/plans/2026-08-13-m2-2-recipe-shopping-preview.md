# M2.2 Recipe Shopping Preview Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Expose the accepted M2.1 Recipe → ShoppingList conversion through a stateless, contract-first `POST /api/v1/recipe-shopping-previews` API with server-owned identities, self-contained provenance, exhaustive validation and generated TypeScript contract coverage.

**Architecture:** Add an application boundary in `io.github.trueruslan.zakupgotov.recipepreview` that validates transport DTOs, allocates transient IDs, constructs existing `recipe`/`shopping` domain objects and delegates all scaling/merge/provenance semantics to `RecipeShoppingListConverter`. The HTTP layer remains thin, OpenAPI is the public source of truth, generated TypeScript types are regenerated rather than hand-edited, and M2.2 introduces no persistence or retailer traffic.

**Tech Stack:** Java 25, Spring Boot 4.1, Spring MVC, Jackson 3, JUnit 5, AssertJ, Spring MVC tests, Spring Modulith verification, Maven, OpenAPI 3.1, openapi-typescript 7.13, TypeScript 5.9, Vitest 4.1, existing PostgreSQL 18 Testcontainers baseline, existing Next.js/Playwright regression.

## Global Constraints

- Authoritative design: `docs/superpowers/specs/2026-08-13-m2-2-recipe-shopping-preview-design-v2.md`.
- Endpoint: `POST /api/v1/recipe-shopping-previews`; success is `200 OK`.
- Client supplies no Recipe/RecipeIngredient/ShoppingList/ShoppingItem UUIDs.
- Input units: `PIECE`, `GRAM`, `KILOGRAM`, `MILLILITER`, `LITER`; response quantities are canonical `PIECE`, `GRAM`, `MILLILITER`.
- `recipepreview → recipe → shopping` and `recipepreview → shopping` are allowed; reverse dependencies are forbidden.
- No M2.2 production dependency on provider, retailer, matching, basket, comparison or database packages.
- No persistence, recipe CRUD, Recipe→Comparison orchestration, retailer traffic, fuzzy/synonym/AI matching or UI in this slice.
- Validation limits: normalized title 1..240, ingredients 1..100, normalized requirement 1..240, positive integer base/target servings, positive decimal quantity.
- Known request failures only map to `INVALID_RECIPE_SHOPPING_PREVIEW`; arbitrary internal exceptions must not be converted to 400.
- Every RED test must be executed and fail for the intended missing/incorrect behavior before production code is added.
- Existing Web/Playwright regression remains mandatory; do not fabricate Recipe UI solely to claim new Playwright coverage.
- Full `./mvnw verify` remains mandatory and must execute the existing PostgreSQL/Testcontainers integration baseline.

---

## File Structure

### New backend production files

- `apps/api/src/main/java/io/github/trueruslan/zakupgotov/recipepreview/RecipeShoppingPreviewRequest.java` — transport request record.
- `.../RecipeShoppingPreviewIngredientRequest.java` — ingredient request record.
- `.../RecipeShoppingPreviewQuantityRequest.java` — quantity request record using existing `QuantityUnit`.
- `.../RecipeShoppingPreviewValidationError.java` — public ordered field/message validation item.
- `.../InvalidRecipeShoppingPreviewRequestException.java` — known validation exception carrying immutable errors.
- `.../InvalidRecipeShoppingPreviewProblem.java` — dedicated RFC7807-like public problem body.
- `.../RecipeShoppingPreviewIdGenerator.java` — injectable ID source abstraction.
- `.../UuidRecipeShoppingPreviewIdGenerator.java` — production random UUID implementation.
- `.../RecipeShoppingPreviewRequestFactory.java` — validate, normalize and build Recipe + target servings + list ID without HTTP concerns.
- `.../RecipeShoppingPreviewInput.java` — validated domain input plus generated ingredient identity context.
- `.../RecipeShoppingPreviewRecipeIngredient.java` — public canonical source ingredient projection.
- `.../RecipeShoppingPreviewRecipe.java` — public recipe projection.
- `.../RecipeShoppingPreviewShoppingItem.java` — shopping item + ordered source ingredient IDs.
- `.../RecipeShoppingPreviewShoppingList.java` — shopping-list projection.
- `.../RecipeShoppingPreview.java` — top-level response.
- `.../RecipeShoppingPreviewService.java` — application orchestration and projection.
- `.../RecipeShoppingPreviewConfiguration.java` — explicit Spring beans for ID generator/converter/service if constructor discovery does not already cover them.
- `.../RecipeShoppingPreviewController.java` — thin POST controller.
- `.../RecipeShoppingPreviewExceptionHandler.java` — controller-scoped known error mapping and sanitized unreadable-body response.

### New backend tests

- `apps/api/src/test/java/io/github/trueruslan/zakupgotov/recipepreview/RecipeShoppingPreviewRequestFactoryTest.java`
- `.../RecipeShoppingPreviewServiceTest.java`
- `.../RecipeShoppingPreviewControllerTest.java`
- `.../RecipeShoppingPreviewArchitectureTest.java`

### Modified contract/client files

- `openapi/zakup-gotov.yaml` — new path and schemas; reuse quantity enums/schemas.
- `packages/api-client/src/index.ts` — export `RECIPE_SHOPPING_PREVIEWS_PATH`.
- `packages/api-client/src/index.test.ts` — typed contract RED/GREEN test.
- `packages/api-client/src/schema.d.ts` — generated only by `pnpm --filter @zakup-gotov/api-client generate`.

No DB migration/repository file is created in M2.2.

---

### Task 1: Request validation, normalization and server-owned identity allocation

**Files:**
- Create: backend request DTOs, validation types, ID generator types, `RecipeShoppingPreviewInput`, `RecipeShoppingPreviewRequestFactory`.
- Test: `apps/api/src/test/java/io/github/trueruslan/zakupgotov/recipepreview/RecipeShoppingPreviewRequestFactoryTest.java`.

**Interfaces:**
- Consumes existing `RecipeId`, `RecipeIngredientId`, `RecipeTitle`, `RecipeServings`, `RecipeIngredient`, `Recipe`, `ShoppingRequirement`, `Quantity`, `QuantityUnit`, `ShoppingListId`.
- Produces:

```java
interface RecipeShoppingPreviewIdGenerator {
    RecipeId nextRecipeId();
    RecipeIngredientId nextIngredientId();
    ShoppingListId nextShoppingListId();
}

record RecipeShoppingPreviewInput(
        Recipe recipe,
        RecipeServings targetServings,
        ShoppingListId shoppingListId) {}

final class RecipeShoppingPreviewRequestFactory {
    RecipeShoppingPreviewInput create(RecipeShoppingPreviewRequest request);
}
```

- [ ] **Step 1: Write the initial RED validation/mapping tests**

Create tests that instantiate a deterministic queued ID generator and assert:

```java
var input = factory.create(new RecipeShoppingPreviewRequest(
        "  Курица   с овощами  ",
        2,
        4,
        List.of(
                new RecipeShoppingPreviewIngredientRequest(
                        "  морковь  ", new RecipeShoppingPreviewQuantityRequest(new BigDecimal("0.3"), QuantityUnit.KILOGRAM)),
                new RecipeShoppingPreviewIngredientRequest(
                        "лук", new RecipeShoppingPreviewQuantityRequest(new BigDecimal("2"), QuantityUnit.PIECE))))));

assertThat(input.recipe().title().text()).isEqualTo("Курица с овощами");
assertThat(input.recipe().ingredients().get(0).quantity())
        .isEqualTo(new Quantity(new BigDecimal("300"), QuantityUnit.GRAM));
assertThat(input.recipe().id()).isEqualTo(new RecipeId(RECIPE_ID));
assertThat(input.recipe().ingredients()).extracting(ingredient -> ingredient.id().value())
        .containsExactly(INGREDIENT_1_ID, INGREDIENT_2_ID);
assertThat(input.shoppingListId()).isEqualTo(new ShoppingListId(LIST_ID));
```

Add dedicated cases for null request, blank/overlong title, base/target servings `0` and negative, null/empty/101 ingredients, null ingredient, blank/overlong requirement, null quantity, null/zero/negative amount, null unit. Assert multiple traversable errors in deterministic field order, e.g. `title`, `baseServings`, `ingredients[0].requirement`, `ingredients[0].quantity.amount`.

- [ ] **Step 2: Execute RED**

Run:

```bash
cd apps/api
./mvnw -Dtest=RecipeShoppingPreviewRequestFactoryTest test
```

Expected: compilation/test failure because the `recipepreview` request/factory/validation/ID types do not exist.

- [ ] **Step 3: Implement the minimal request types and explicit validator/factory**

Use transport records without Bean Validation magic so error ordering is fully controlled. Normalize string fields with the same behavior used by existing value objects:

```java
private static String normalize(String value) {
    return value.strip().replaceAll("\\s+", " ");
}
```

Validation must collect errors before constructing domain objects. After validation, allocate IDs in exactly this order: recipe ID, one ingredient ID per request ingredient in request order, then shopping-list ID. Construct existing domain objects and return `RecipeShoppingPreviewInput`.

`UuidRecipeShoppingPreviewIdGenerator` uses `UUID.randomUUID()` only in the production adapter; tests never depend on random values.

- [ ] **Step 4: Execute GREEN and regression**

Run:

```bash
cd apps/api
./mvnw -Dtest=RecipeShoppingPreviewRequestFactoryTest test
./mvnw -Dtest='io.github.trueruslan.zakupgotov.recipe.*Test,io.github.trueruslan.zakupgotov.shopping.*Test' test
```

Expected: all selected tests PASS.

- [ ] **Step 5: Commit**

```bash
git add apps/api/src/main/java/io/github/trueruslan/zakupgotov/recipepreview \
        apps/api/src/test/java/io/github/trueruslan/zakupgotov/recipepreview/RecipeShoppingPreviewRequestFactoryTest.java
git commit -m "feat(recipe): validate recipe shopping preview input"
```

---

### Task 2: Application service, self-contained provenance and canonical projection

**Files:**
- Create: response projection records and `RecipeShoppingPreviewService.java`.
- Test: `RecipeShoppingPreviewServiceTest.java`.

**Interfaces:**
- Consumes `RecipeShoppingPreviewRequestFactory.create(request)` and existing `RecipeShoppingListConverter.convert(recipe, targetServings, shoppingListId)`.
- Produces:

```java
final class RecipeShoppingPreviewService {
    RecipeShoppingPreview create(RecipeShoppingPreviewRequest request);
}

record RecipeShoppingPreview(
        RecipeShoppingPreviewRecipe recipe,
        RecipeShoppingPreviewShoppingList shoppingList) {}
```

`RecipeShoppingPreviewRecipe` includes generated recipe ID, normalized title, base/target servings and ordered canonical source ingredients. `RecipeShoppingPreviewShoppingItem` includes item ID, normalized requirement, canonical quantity and ordered `List<UUID> sourceIngredientIds`.

- [ ] **Step 1: Write RED service/projection tests**

Use fixed IDs and assert one comprehensive success case containing:
- two exact same `"Молоко"` volume ingredients expressed in `LITER` and `MILLILITER` that merge;
- one same text with different case (`"молоко"`) that does not merge;
- one `KILOGRAM` input canonicalized to grams;
- a fractional `PIECE` amount retained;
- target/base ratio that exercises exact scaling;
- a separate `1/3` case asserting DECIMAL128 behavior inherited from M2.1.

Assert source recipe ingredients remain in original order and every shopping item provenance ID belongs to the returned `recipe.ingredients` IDs. For the merged item:

```java
assertThat(merged.sourceIngredientIds())
        .containsExactly(INGREDIENT_1_ID, INGREDIENT_2_ID);
```

Assert no item has empty provenance and no provenance ID is orphaned.

- [ ] **Step 2: Execute RED**

```bash
cd apps/api
./mvnw -Dtest=RecipeShoppingPreviewServiceTest test
```

Expected: FAIL because service/projection types are absent.

- [ ] **Step 3: Implement the minimal service by delegation**

Pseudo-code must remain structurally equivalent to:

```java
public RecipeShoppingPreview create(RecipeShoppingPreviewRequest request) {
    var input = requestFactory.create(request);
    var conversion = converter.convert(input.recipe(), input.targetServings(), input.shoppingListId());
    return projection(input, conversion);
}
```

Projection must read shopping items from `conversion.shoppingList().items()` and provenance from `conversion.provenance()`. It must never reimplement scaling or merge logic. For each `RecipeIngredientRef`, require `ref.recipeId().equals(input.recipe().id())`; require each referenced ingredient ID exists in the response recipe ingredient set; require provenance exists and is non-empty for every output ShoppingItem. Violations are internal invariant failures (`IllegalStateException`), never request-validation exceptions.

Return immutable nested lists via record constructors using `List.copyOf` where records contain lists.

- [ ] **Step 4: Execute GREEN + M2.1 regression**

```bash
cd apps/api
./mvnw -Dtest=RecipeShoppingPreviewServiceTest test
./mvnw -Dtest='io.github.trueruslan.zakupgotov.recipe.*Test' test
```

Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add apps/api/src/main/java/io/github/trueruslan/zakupgotov/recipepreview \
        apps/api/src/test/java/io/github/trueruslan/zakupgotov/recipepreview/RecipeShoppingPreviewServiceTest.java
git commit -m "feat(recipe): project stateless shopping preview"
```

---

### Task 3: Spring HTTP boundary and sanitized problem contract

**Files:**
- Create: `RecipeShoppingPreviewController.java`, `RecipeShoppingPreviewExceptionHandler.java`, `InvalidRecipeShoppingPreviewProblem.java`, optional explicit configuration bean file.
- Test: `RecipeShoppingPreviewControllerTest.java`.

**Interfaces:**
- Controller:

```java
@RestController
@RequestMapping("/api/v1/recipe-shopping-previews")
final class RecipeShoppingPreviewController {
    @PostMapping
    RecipeShoppingPreview create(@RequestBody RecipeShoppingPreviewRequest request) {
        return service.create(request);
    }
}
```

- Public problem constants:

```java
TYPE = "https://zakup-gotov.dev/problems/invalid-recipe-shopping-preview";
TITLE = "Invalid recipe shopping preview request";
CODE = "INVALID_RECIPE_SHOPPING_PREVIEW";
```

- [ ] **Step 1: Write RED controller tests**

Mirror the existing `ComparisonPreviewControllerTest` style. Cover:
1. successful POST returns exact normalized/canonical JSON and `200`;
2. multiple semantic validation errors return ordered `errors` in `application/problem+json`;
3. malformed JSON returns one sanitized `$request` error;
4. unknown root field returns 400 sanitized body;
5. unknown ingredient field returns 400 sanitized body;
6. unknown quantity field returns 400 sanitized body;
7. unknown unit enum returns 400 sanitized body;
8. non-integer servings JSON returns 400 sanitized body;
9. an injected internal service failure is not transformed into `INVALID_RECIPE_SHOPPING_PREVIEW`.

The malformed binding assertion must check absence of implementation leakage:

```java
.andExpect(jsonPath("$.errors[0].message").value("malformed JSON request"))
.andExpect(content().string(not(containsString("tools.jackson"))))
.andExpect(content().string(not(containsString("java.lang"))));
```

- [ ] **Step 2: Execute RED**

```bash
cd apps/api
./mvnw -Dtest=RecipeShoppingPreviewControllerTest test
```

Expected: FAIL because controller/advice/problem types are absent.

- [ ] **Step 3: Implement controller-scoped advice and Spring wiring**

Follow existing comparison-preview conventions. Advice uses:

```java
@RestControllerAdvice(assignableTypes = RecipeShoppingPreviewController.class)
```

Handle only `InvalidRecipeShoppingPreviewRequestException` and `HttpMessageNotReadableException`. Do not add a catch-all handler. Unknown JSON properties must remain fail-closed; if repository Jackson defaults do not reject them, add the narrowest existing-supported configuration and prove existing endpoints remain green rather than silently accepting unknown fields.

- [ ] **Step 4: Execute GREEN + existing controller regression**

```bash
cd apps/api
./mvnw -Dtest=RecipeShoppingPreviewControllerTest test
./mvnw -Dtest='ComparisonPreviewControllerTest,RetailerControllerTest,SystemControllerTest' test
```

Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add apps/api/src/main/java/io/github/trueruslan/zakupgotov/recipepreview \
        apps/api/src/test/java/io/github/trueruslan/zakupgotov/recipepreview/RecipeShoppingPreviewControllerTest.java
git commit -m "feat(recipe): expose recipe shopping preview endpoint"
```

---

### Task 4: OpenAPI source of truth and generated TypeScript client TDD

**Files:**
- Modify: `packages/api-client/src/index.test.ts` first for RED.
- Modify: `openapi/zakup-gotov.yaml`.
- Modify: `packages/api-client/src/index.ts`.
- Generate: `packages/api-client/src/schema.d.ts`.

**Interfaces:**
- New path constant:

```ts
export const RECIPE_SHOPPING_PREVIEWS_PATH = "/api/v1/recipe-shopping-previews" as const;
```

- New OpenAPI operation: `createRecipeShoppingPreview`.
- Request/response schemas must represent self-contained provenance: every shopping item has `sourceIngredientIds`, and response recipe includes ingredient IDs.

- [ ] **Step 1: Write the TypeScript RED contract test before editing OpenAPI**

Add to `packages/api-client/src/index.test.ts`:

```ts
it("exposes the recipe shopping preview endpoint through the generated contract", () => {
  const path: keyof paths = RECIPE_SHOPPING_PREVIEWS_PATH;
  const client = createZakupGotovClient("https://api.example.test");
  type RecipePreviewPost = paths["/api/v1/recipe-shopping-previews"]["post"];
  type Response = components["schemas"]["RecipeShoppingPreviewResponse"];
  const operationExists: RecipePreviewPost | undefined = undefined;
  const responseExists: Response | undefined = undefined;

  expect(path).toBe("/api/v1/recipe-shopping-previews");
  expect(client.POST).toBeTypeOf("function");
  expect(operationExists).toBeUndefined();
  expect(responseExists).toBeUndefined();
});
```

- [ ] **Step 2: Execute RED**

```bash
pnpm --filter @zakup-gotov/api-client typecheck
```

Expected: FAIL because path constant and generated path/schema do not exist.

- [ ] **Step 3: Add exact OpenAPI schemas and export constant**

Add `POST /api/v1/recipe-shopping-previews` with 200 and 400 responses. Required schemas:
- `RecipeShoppingPreviewRequest`
- `RecipeShoppingPreviewIngredientInput`
- `RecipeShoppingPreviewResponse`
- `RecipeShoppingPreviewRecipe`
- `RecipeShoppingPreviewRecipeIngredient`
- `RecipeShoppingPreviewShoppingList`
- `RecipeShoppingPreviewShoppingItem`
- `InvalidRecipeShoppingPreviewProblem`
- `RecipeShoppingPreviewValidationError`

All object schemas use `additionalProperties: false`. Reuse `QuantityInputUnit` for request quantity unit and `CanonicalQuantity` for response quantities. Recipe response ingredient `quantity` is canonical, therefore also `CanonicalQuantity`.

For IDs use `type: string`, `format: uuid`. `sourceIngredientIds` has `minItems: 1`. Servings use integer `minimum: 1`. Input arrays use `minItems: 1`, `maxItems: 100`; title/requirements use explicit `minLength` and `maxLength: 240`.

- [ ] **Step 4: Regenerate schema and execute GREEN**

```bash
pnpm --filter @zakup-gotov/api-client generate
pnpm --filter @zakup-gotov/api-client check:generated
pnpm --filter @zakup-gotov/api-client typecheck
pnpm --filter @zakup-gotov/api-client test
pnpm --filter @zakup-gotov/api-client build
```

Expected: all PASS and `check:generated` reports no diff after generation.

- [ ] **Step 5: Commit**

```bash
git add openapi/zakup-gotov.yaml packages/api-client/src/index.ts \
        packages/api-client/src/index.test.ts packages/api-client/src/schema.d.ts
git commit -m "feat(contract): add recipe shopping preview API"
```

---

### Task 5: Architecture proof, full verification, docs/status and shipping

**Files:**
- Create: `apps/api/src/test/java/io/github/trueruslan/zakupgotov/recipepreview/RecipeShoppingPreviewArchitectureTest.java`.
- Modify only after all tests are green: `docs/PROJECT_STATE.md`, `docs/ROADMAP.md`, `CHANGELOG.md` to mark M2.2 implemented/tested but not accepted until post-merge proof.
- Create shipping evidence plan only if repository process requires it, following existing M2.1 patterns.

**Interfaces:**
- Architecture tests use ArchUnit/Spring Modulith patterns already present in the repository.

- [ ] **Step 1: Write RED architecture tests**

Assert:
- classes in `..shopping..` do not depend on `..recipe..` or `..recipepreview..`;
- classes in `..recipe..` do not depend on `..recipepreview..`;
- `..recipepreview..` production classes do not depend on `..provider..`, `..retailer..`, `..matching..`, `..basket..`, `..comparison..`, or `..database..`;
- controller/service/factory dependencies point inward as specified.

- [ ] **Step 2: Execute architecture RED/GREEN honestly**

Run the test immediately after writing it:

```bash
cd apps/api
./mvnw -Dtest=RecipeShoppingPreviewArchitectureTest test
```

If it already passes because the implementation naturally satisfies the invariant, record that as a test-first characterization rather than inventing a production defect. If it fails, fix only the dependency violation and rerun until PASS. Do not weaken the rule to obtain green.

- [ ] **Step 3: Run focused backend suite and complete Maven verification**

```bash
cd apps/api
./mvnw -Dtest='io.github.trueruslan.zakupgotov.recipepreview.*Test,io.github.trueruslan.zakupgotov.recipe.*Test,io.github.trueruslan.zakupgotov.shopping.*Test' test
./mvnw verify
```

Expected: PASS. Verify output must include existing PostgreSQL/Testcontainers integration and Spring Modulith/application architecture verification. No M2.2-specific DB code is added.

- [ ] **Step 4: Run contract/frontend/Playwright regression locally or through exact-head CI**

```bash
pnpm --filter @zakup-gotov/api-client check:generated
pnpm --filter @zakup-gotov/api-client typecheck
pnpm --filter @zakup-gotov/api-client test
pnpm --filter @zakup-gotov/api-client build
pnpm --filter @zakup-gotov/web lint
pnpm --filter @zakup-gotov/web typecheck
pnpm --filter @zakup-gotov/web test
pnpm --filter @zakup-gotov/web build
pnpm --filter @zakup-gotov/web test:e2e
```

Expected: all supported repository scripts PASS. If an exact script name differs, use the script from `apps/web/package.json` and record the exact command in PR evidence; do not skip the corresponding gate.

- [ ] **Step 5: Update project documentation conservatively**

State must distinguish `implemented`, `tested`, `reviewed`, `merged`, `accepted`. Before merge, describe #96 as implemented/tested on PR branch and explicitly not yet accepted. Do not claim post-merge green evidence before it exists.

- [ ] **Step 6: Commit verification/docs changes**

```bash
git add apps/api/src/test/java/io/github/trueruslan/zakupgotov/recipepreview/RecipeShoppingPreviewArchitectureTest.java \
        docs/PROJECT_STATE.md docs/ROADMAP.md CHANGELOG.md
git commit -m "test(m2): verify recipe shopping preview boundaries"
```

- [ ] **Step 7: Open PR and require exact-head CI**

PR body must include:
- issue `Closes #96`;
- authoritative v2 design and this plan;
- RED evidence per task;
- GREEN focused commands;
- full Maven/Testcontainers/Modulith evidence;
- generated-client evidence;
- Web/Playwright regression evidence;
- explicit note that M2.2 adds no persistence and no retailer network traffic.

Wait for all normal workflow groups on the exact current PR head: API CI, Contract CI, Web CI, Retailer Bridge CI, CodeQL Java + JS/TS, Dependency Review, Container Security, Release Contract CI and Release Bundle CI.

- [ ] **Step 8: Independent change review**

Review the complete diff against v2 spec with severity calibration. Block merge on any unresolved P0/P1/P2. Re-run affected focused tests and all invalidated CI gates after fixes.

- [ ] **Step 9: Merge exact reviewed head and verify post-merge main**

Use squash merge only after exact-head checks/review are green. Fetch resulting `main` SHA, require all normal push workflows green, then close/accept #96 and update state to **M2.2 COMPLETE / ACCEPTED** only with that evidence.

- [ ] **Step 10: Select next product slice**

After acceptance, next slice is the composed application path:

`Recipe input → recipe-shopping preview → generated shopping requirements → comparison preview`

Then implement the actual responsive Recipe UI with frontend component TDD and desktop/mobile Playwright RED-first. Do not skip directly to candidate AI matching before the deterministic vertical flow exists.
