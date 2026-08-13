# M2.2 Recipe Shopping Preview Implementation Plan v2

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Expose accepted M2.1 Recipe → ShoppingList conversion via stateless `POST /api/v1/recipe-shopping-previews` with server-owned transient identities, self-contained provenance, fail-closed validation, OpenAPI 3.1 and generated TypeScript coverage.

**Architecture:** `io.github.trueruslan.zakupgotov.recipepreview` is an application/HTTP adapter depending inward on `recipe` and `shopping`. `RecipeShoppingPreviewRequestFactory` owns validation/normalization/domain construction, `RecipeShoppingPreviewService` delegates all scale/merge/provenance semantics to existing `RecipeShoppingListConverter`, and a thin Spring controller exposes the result. M2.2 adds no persistence, retailer traffic, fuzzy matching or UI.

**Tech Stack:** Java 25, Spring Boot 4.1, Spring MVC, Jackson 3, JUnit 5, AssertJ, Spring MVC tests, ArchUnit/Spring Modulith verification, Maven, OpenAPI 3.1, openapi-typescript 7.13, TypeScript 5.9, Vitest 4.1, existing PostgreSQL 18 Testcontainers baseline, existing Next.js/Playwright regression.

## Global Constraints

- Authoritative design: `docs/superpowers/specs/2026-08-13-m2-2-recipe-shopping-preview-design-v2.md`.
- Endpoint: `POST /api/v1/recipe-shopping-previews`; success `200 OK`.
- Input units: `PIECE`, `GRAM`, `KILOGRAM`, `MILLILITER`, `LITER`; output uses canonical `PIECE`, `GRAM`, `MILLILITER`.
- Client supplies no Recipe/ingredient/ShoppingList/ShoppingItem UUIDs.
- Allowed: `recipepreview → recipe → shopping` and `recipepreview → shopping`.
- Forbidden: shopping→recipe/recipepreview, recipe→recipepreview, and recipepreview production dependencies on provider/retailer/matching/basket/comparison/database.
- Validation: normalized title 1..240, ingredients 1..100, normalized requirement 1..240, positive integer base/target servings, positive decimal amount.
- Only known request-validation/binding failures become public 400. Internal invariant failures remain server failures.
- OpenAPI remains source of truth; `schema.d.ts` is generated, never manually edited.
- Every behavior-changing production step starts from an executed failing RED test.
- Existing `./mvnw verify` must still run PostgreSQL/Testcontainers and Modulith checks.
- Existing Web/Playwright regression remains mandatory; no fake Recipe UI is added in M2.2.

## Exact file map

Create under `apps/api/src/main/java/io/github/trueruslan/zakupgotov/recipepreview/`:

- `RecipeShoppingPreviewRequest.java`
- `RecipeShoppingPreviewIngredientRequest.java`
- `RecipeShoppingPreviewQuantityRequest.java`
- `RecipeShoppingPreviewValidationError.java`
- `InvalidRecipeShoppingPreviewRequestException.java`
- `InvalidRecipeShoppingPreviewProblem.java`
- `RecipeShoppingPreviewIdGenerator.java`
- `UuidRecipeShoppingPreviewIdGenerator.java`
- `RecipeShoppingPreviewInput.java`
- `RecipeShoppingPreviewRequestFactory.java`
- `RecipeShoppingPreviewRecipeIngredient.java`
- `RecipeShoppingPreviewRecipe.java`
- `RecipeShoppingPreviewShoppingItem.java`
- `RecipeShoppingPreviewShoppingList.java`
- `RecipeShoppingPreview.java`
- `RecipeShoppingPreviewService.java`
- `RecipeShoppingPreviewConfiguration.java`
- `RecipeShoppingPreviewController.java`
- `RecipeShoppingPreviewExceptionHandler.java`

Create tests under matching test package:

- `RecipeShoppingPreviewArchitectureTest.java`
- `RecipeShoppingPreviewRequestFactoryTest.java`
- `RecipeShoppingPreviewServiceTest.java`
- `RecipeShoppingPreviewControllerTest.java`

Modify:

- `openapi/zakup-gotov.yaml`
- `packages/api-client/src/index.ts`
- `packages/api-client/src/index.test.ts`
- generated `packages/api-client/src/schema.d.ts`
- after implementation proof only: `docs/PROJECT_STATE.md`, `docs/ROADMAP.md`, `CHANGELOG.md`.

No migration/repository/database production file is created.

---

### Task 1: Architecture gate and request/domain construction

**Produces:**

```java
public interface RecipeShoppingPreviewIdGenerator {
    RecipeId nextRecipeId();
    RecipeIngredientId nextIngredientId();
    ShoppingListId nextShoppingListId();
}

public record RecipeShoppingPreviewInput(
        Recipe recipe,
        RecipeServings targetServings,
        ShoppingListId shoppingListId) {}

public final class RecipeShoppingPreviewRequestFactory {
    public RecipeShoppingPreviewRequestFactory(RecipeShoppingPreviewIdGenerator idGenerator);
    public RecipeShoppingPreviewInput create(RecipeShoppingPreviewRequest request);
}
```

- [ ] **Step 1 — Architecture RED before production package exists.** Create `RecipeShoppingPreviewArchitectureTest.java` first. Include rules that classes in `..shopping..` may not depend on `..recipe..`/`..recipepreview..`, classes in `..recipe..` may not depend on `..recipepreview..`, and `..recipepreview..` may not depend on provider/retailer/matching/basket/comparison/database. Add a required-package assertion that `recipepreview` contains application classes so the initial test fails because the package does not yet exist.

Run:

```bash
cd apps/api
./mvnw -Dtest=RecipeShoppingPreviewArchitectureTest test
```

Expected RED: assertion failure that `recipepreview` production package/application boundary is absent. Do not weaken dependency rules.

- [ ] **Step 2 — Request-factory RED.** Create request DTO and factory tests before their production types. Test normalized title/requirements, fixed IDs, `0.3 KILOGRAM→300 GRAM`, input order, and deterministic validation error ordering. Add separate tests for null request; blank/241-char title; base/target 0 and negative; null/empty/101 ingredients; null ingredient; blank/241-char requirement; null quantity; null/zero/negative amount; null unit.

Representative assertion:

```java
assertThat(input.recipe().title().text()).isEqualTo("Курица с овощами");
assertThat(input.recipe().ingredients()).extracting(i -> i.id().value())
        .containsExactly(INGREDIENT_1, INGREDIENT_2);
assertThat(input.recipe().ingredients().getFirst().quantity())
        .isEqualTo(new Quantity(new BigDecimal("300"), QuantityUnit.GRAM));
```

Run:

```bash
./mvnw -Dtest=RecipeShoppingPreviewRequestFactoryTest test
```

Expected RED: compilation/test failure for missing request/factory/ID types.

- [ ] **Step 3 — Minimal GREEN implementation.** Implement transport records, immutable validation error/exception, ID interface + UUID adapter, input record and factory. Explicit validation collects traversable errors before constructing domain objects. Normalize by `strip()` plus collapsing `\\s+` to one space. Allocate IDs exactly: Recipe ID → one ingredient ID per request row in order → ShoppingList ID. Construct only existing `RecipeTitle`, `RecipeServings`, `ShoppingRequirement`, `Quantity`, `RecipeIngredient`, `Recipe`.

Production UUID adapter:

```java
public final class UuidRecipeShoppingPreviewIdGenerator implements RecipeShoppingPreviewIdGenerator {
    public RecipeId nextRecipeId() { return new RecipeId(UUID.randomUUID()); }
    public RecipeIngredientId nextIngredientId() { return new RecipeIngredientId(UUID.randomUUID()); }
    public ShoppingListId nextShoppingListId() { return new ShoppingListId(UUID.randomUUID()); }
}
```

- [ ] **Step 4 — Run GREEN + architecture.**

```bash
./mvnw -Dtest='RecipeShoppingPreviewArchitectureTest,RecipeShoppingPreviewRequestFactoryTest' test
./mvnw -Dtest='io.github.trueruslan.zakupgotov.recipe.*Test,io.github.trueruslan.zakupgotov.shopping.*Test' test
```

Expected: PASS. Architecture package-presence RED is now closed without changing dependency rules.

- [ ] **Step 5 — Commit.**

```bash
git add apps/api/src/main/java/io/github/trueruslan/zakupgotov/recipepreview \
        apps/api/src/test/java/io/github/trueruslan/zakupgotov/recipepreview
git commit -m "feat(recipe): validate recipe shopping preview input"
```

---

### Task 2: Service projection and self-contained provenance

**Produces:**

```java
public final class RecipeShoppingPreviewService {
    public RecipeShoppingPreviewService(
            RecipeShoppingPreviewRequestFactory requestFactory,
            RecipeShoppingListConverter converter);
    public RecipeShoppingPreview create(RecipeShoppingPreviewRequest request);
}
```

Response records expose recipe ID/title/base/target servings, ordered canonical source ingredients, shopping-list ID, ordered shopping items and ordered `sourceIngredientIds`.

- [ ] **Step 1 — Service RED.** Create `RecipeShoppingPreviewServiceTest` using queued fixed IDs. One test must contain: same `"Молоко"` in LITER and MILLILITER merging; `"молоко"` not merging due case; KILOGRAM canonicalization; fractional PIECE retention; exact scaling; stable first-group order. A second test must exercise non-terminating `1/3` scaling and assert the existing DECIMAL128 result. Assert every provenance ID resolves to exactly one returned recipe ingredient ID and every shopping item has non-empty provenance.

Run:

```bash
./mvnw -Dtest=RecipeShoppingPreviewServiceTest test
```

Expected RED: missing response/service types.

- [ ] **Step 2 — Minimal service/projection GREEN.** Implement service only as factory → existing converter → projection:

```java
public RecipeShoppingPreview create(RecipeShoppingPreviewRequest request) {
    var input = requestFactory.create(request);
    var conversion = converter.convert(input.recipe(), input.targetServings(), input.shoppingListId());
    return project(input, conversion);
}
```

Never recompute scaling or merge keys. During projection, build the set of returned source ingredient IDs. For every `RecipeIngredientRef`, require matching recipe ID and membership in this set. Require exactly one non-empty provenance list for every generated shopping item. Missing/orphan/cross-recipe provenance throws `IllegalStateException`, not request validation.

All nested response lists use `List.copyOf` in canonical constructors.

- [ ] **Step 3 — GREEN + M2.1 regression.**

```bash
./mvnw -Dtest=RecipeShoppingPreviewServiceTest test
./mvnw -Dtest='io.github.trueruslan.zakupgotov.recipe.*Test' test
```

Expected: PASS.

- [ ] **Step 4 — Commit.**

```bash
git add apps/api/src/main/java/io/github/trueruslan/zakupgotov/recipepreview \
        apps/api/src/test/java/io/github/trueruslan/zakupgotov/recipepreview/RecipeShoppingPreviewServiceTest.java
git commit -m "feat(recipe): project stateless shopping preview"
```

---

### Task 3: Spring HTTP boundary and sanitized error contract

**Produces:** thin controller, controller-scoped advice, dedicated problem type, explicit configuration.

Exact Spring wiring in `RecipeShoppingPreviewConfiguration`:

```java
@Configuration(proxyBeanMethods = false)
public class RecipeShoppingPreviewConfiguration {
    @Bean RecipeShoppingPreviewIdGenerator recipeShoppingPreviewIdGenerator() {
        return new UuidRecipeShoppingPreviewIdGenerator();
    }

    @Bean RecipeShoppingListConverter recipeShoppingListConverter() {
        return new RecipeShoppingListConverter();
    }

    @Bean RecipeShoppingPreviewRequestFactory recipeShoppingPreviewRequestFactory(
            RecipeShoppingPreviewIdGenerator ids) {
        return new RecipeShoppingPreviewRequestFactory(ids);
    }

    @Bean RecipeShoppingPreviewService recipeShoppingPreviewService(
            RecipeShoppingPreviewRequestFactory factory,
            RecipeShoppingListConverter converter) {
        return new RecipeShoppingPreviewService(factory, converter);
    }
}
```

- [ ] **Step 1 — Controller RED.** Create `RecipeShoppingPreviewControllerTest` following existing MockMvc/controller patterns. Cover exact successful JSON/200; multiple semantic errors and order; malformed JSON; unknown root/ingredient/quantity fields; unknown unit; non-integer servings; and an injected internal service failure not converted to the public 400 code.

Public problem constants are exact:

```java
TYPE = "https://zakup-gotov.dev/problems/invalid-recipe-shopping-preview";
TITLE = "Invalid recipe shopping preview request";
CODE = "INVALID_RECIPE_SHOPPING_PREVIEW";
```

Malformed body must yield one `$request` / `malformed JSON request` error and must not contain `tools.jackson`, `java.lang`, stack trace text or raw exception details.

Run:

```bash
./mvnw -Dtest=RecipeShoppingPreviewControllerTest test
```

Expected RED: missing controller/advice/problem/configuration.

- [ ] **Step 2 — Implement GREEN boundary.** Controller only delegates. Advice is:

```java
@RestControllerAdvice(assignableTypes = RecipeShoppingPreviewController.class)
```

and handles only `InvalidRecipeShoppingPreviewRequestException` plus `HttpMessageNotReadableException`; no catch-all. Unknown fields must be rejected. First prove current Jackson configuration behavior through the RED test; if it accepts unknown fields, add the narrowest Jackson configuration needed and immediately re-run existing controller tests to ensure no unintended contract break.

- [ ] **Step 3 — GREEN + existing endpoint regression.**

```bash
./mvnw -Dtest=RecipeShoppingPreviewControllerTest test
./mvnw -Dtest='ComparisonPreviewControllerTest,RetailerControllerTest,SystemControllerTest' test
```

Expected: PASS.

- [ ] **Step 4 — Commit.**

```bash
git add apps/api/src/main/java/io/github/trueruslan/zakupgotov/recipepreview \
        apps/api/src/test/java/io/github/trueruslan/zakupgotov/recipepreview/RecipeShoppingPreviewControllerTest.java
git commit -m "feat(recipe): expose recipe shopping preview endpoint"
```

---

### Task 4: OpenAPI and generated TypeScript contract TDD

- [ ] **Step 1 — TypeScript RED before OpenAPI edit.** Add `RECIPE_SHOPPING_PREVIEWS_PATH` import/use and typed access to `paths["/api/v1/recipe-shopping-previews"]["post"]` plus `components["schemas"]["RecipeShoppingPreviewResponse"]` in `packages/api-client/src/index.test.ts`.

Run:

```bash
pnpm --filter @zakup-gotov/api-client typecheck
```

Expected RED: constant/path/schema missing.

- [ ] **Step 2 — Add OpenAPI path/schemas and client constant.** Add operation ID `createRecipeShoppingPreview`; 200 response and dedicated 400 problem. Create schemas `RecipeShoppingPreviewRequest`, `RecipeShoppingPreviewIngredientInput`, `RecipeShoppingPreviewResponse`, `RecipeShoppingPreviewRecipe`, `RecipeShoppingPreviewRecipeIngredient`, `RecipeShoppingPreviewShoppingList`, `RecipeShoppingPreviewShoppingItem`, `InvalidRecipeShoppingPreviewProblem`, `RecipeShoppingPreviewValidationError`. Every object uses `additionalProperties: false`; IDs are UUID strings; servings minimum 1; ingredients 1..100; title/requirement max 240; `sourceIngredientIds` minimum one. Reuse `QuantityInputUnit` for request and `CanonicalQuantity` for all response quantities.

Export:

```ts
export const RECIPE_SHOPPING_PREVIEWS_PATH = "/api/v1/recipe-shopping-previews" as const;
```

- [ ] **Step 3 — Regenerate and GREEN.**

```bash
pnpm --filter @zakup-gotov/api-client generate
pnpm --filter @zakup-gotov/api-client check:generated
pnpm --filter @zakup-gotov/api-client typecheck
pnpm --filter @zakup-gotov/api-client test
pnpm --filter @zakup-gotov/api-client build
```

Expected: all PASS; no generated diff remains after regeneration.

- [ ] **Step 4 — Commit.**

```bash
git add openapi/zakup-gotov.yaml packages/api-client/src/index.ts \
        packages/api-client/src/index.test.ts packages/api-client/src/schema.d.ts
git commit -m "feat(contract): add recipe shopping preview API"
```

---

### Task 5: Full verification, documentation, review and shipping

- [ ] **Step 1 — Focused backend and full Maven gate.**

```bash
cd apps/api
./mvnw -Dtest='io.github.trueruslan.zakupgotov.recipepreview.*Test,io.github.trueruslan.zakupgotov.recipe.*Test,io.github.trueruslan.zakupgotov.shopping.*Test' test
./mvnw verify
```

Expected: PASS, including Spring Modulith/application architecture and existing PostgreSQL/Testcontainers baseline. Do not add M2.2-specific DB code.

- [ ] **Step 2 — Contract and Web regression.** Read exact scripts from `apps/web/package.json`, then run all repository-supported lint/typecheck/unit/build/e2e commands plus API-client freshness/typecheck/test/build. Playwright must run against the unchanged Web UI to prove no regression; no new Recipe Playwright is claimed.

- [ ] **Step 3 — Conservative docs.** Update `PROJECT_STATE`, `ROADMAP`, `CHANGELOG` only with evidence that exists. Before merge mark #96 implemented/tested/review status precisely and explicitly not accepted. Do not claim merged/post-merge green early.

- [ ] **Step 4 — Open PR.** PR body includes `Closes #96`, authoritative v2 spec, this v2 plan, each RED result, focused GREEN results, full Maven/Testcontainers/Modulith result, generated-client checks, Web/Playwright regression, and explicit no-persistence/no-retailer-traffic scope.

- [ ] **Step 5 — Exact-head CI.** Require API CI, Contract CI, Web CI, Retailer Bridge CI, CodeQL Java + JS/TS, Dependency Review, Container Security, Release Contract CI and Release Bundle CI on the current PR head. Any code/config fix invalidates affected prior evidence and requires rerun.

- [ ] **Step 6 — Independent review.** Review complete diff against design v2. Block on unresolved P0/P1/P2. Fix findings via new RED where behavioral, re-run affected tests and exact-head CI.

- [ ] **Step 7 — Merge and post-merge proof.** Squash-merge only the reviewed green head. Fetch resulting main SHA and require all normal push workflows green. Only then mark #96 and M2.2 `COMPLETE / ACCEPTED`.

- [ ] **Step 8 — Next slice.** Proceed to deterministic composed flow `Recipe input → recipe-shopping preview → generated shopping requirements → comparison preview`; then implement real responsive Recipe UI using frontend component TDD and desktop/mobile Playwright RED-first.

## Self-review result

- Spec coverage: endpoint, validation, identity ownership, canonical quantities, self-contained provenance, fail-closed internal errors, dependency direction, OpenAPI generation, backend/frontend TDD, Testcontainers policy, Playwright policy and shipping evidence each map to an explicit task.
- Placeholder scan: no TBD/TODO/"similar to" steps; Spring wiring and production file set are explicit.
- Type consistency: request factory returns `RecipeShoppingPreviewInput`; service consumes that input and returns `RecipeShoppingPreview`; OpenAPI/client names mirror public response naming; provenance remains `sourceIngredientIds` end-to-end.
- Architecture RED is now guaranteed before production package creation through the required-package assertion; dependency rules themselves are never weakened to manufacture a failure.

This v2 supersedes `2026-08-13-m2-2-recipe-shopping-preview.md` for execution.