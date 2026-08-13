# M2.1 Recipe Domain Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build the first M2 Recipes domain slice: an immutable Recipe aggregate that deterministically scales and converts explicit ingredients into the accepted Shopping Core while returning complete recipe/ingredient provenance.

**Architecture:** Add a new top-level `recipe` module that depends one-way on the existing `shopping` module. Reuse `ShoppingRequirement`, `Quantity`, `QuantityUnit`, `ShoppingList`, `ShoppingListId`, `ShoppingItem`, and `ShoppingItemId`; do not modify Shopping Core types to understand recipes. A pure `RecipeShoppingListConverter` performs canonical-unit grouping, serving scaling, deterministic list-scoped item identity, and immutable provenance with no Spring/network/database/clock dependency.

**Tech Stack:** Java 25, JUnit 5, AssertJ, Spring Modulith 2.1 architecture verification, existing Maven wrapper at `apps/api/mvnw`.

## Global Constraints

- Baseline design: `docs/superpowers/specs/2026-08-13-m2-1-recipe-domain-design.md`.
- Issue: #93.
- Recipe may depend on Shopping Core; Shopping Core must not depend on Recipe.
- `Quantity` remains the only owner of kg→g and l→ml canonicalization.
- `ShoppingRequirement` remains the ingredient requirement text normalization boundary.
- Positive integer servings only in M2.1.
- Use `BigDecimal` only; never `double` or `float`.
- For non-terminating division, use `MathContext.DECIMAL128` deterministically.
- Merge only exact normalized `ShoppingRequirement` + equal canonical `QuantityUnit`.
- Case differences, synonyms, fuzzy equivalence, AI parsing, persistence, REST/OpenAPI and web UI are non-goals.
- Provenance belongs to conversion output, not `ShoppingItem`.
- Normal tests and production code make no retailer/network requests.

---

## File Structure

### New production files

- `apps/api/src/main/java/io/github/trueruslan/zakupgotov/recipe/RecipeId.java` — UUID recipe identity.
- `apps/api/src/main/java/io/github/trueruslan/zakupgotov/recipe/RecipeIngredientId.java` — UUID ingredient identity.
- `apps/api/src/main/java/io/github/trueruslan/zakupgotov/recipe/RecipeTitle.java` — title whitespace normalization/validation.
- `apps/api/src/main/java/io/github/trueruslan/zakupgotov/recipe/RecipeServings.java` — positive integer servings.
- `apps/api/src/main/java/io/github/trueruslan/zakupgotov/recipe/RecipeIngredient.java` — ingredient identity + existing shopping requirement + quantity.
- `apps/api/src/main/java/io/github/trueruslan/zakupgotov/recipe/Recipe.java` — immutable ordered aggregate and duplicate-ID validation.
- `apps/api/src/main/java/io/github/trueruslan/zakupgotov/recipe/RecipeIngredientRef.java` — self-contained `(RecipeId, RecipeIngredientId)` provenance value.
- `apps/api/src/main/java/io/github/trueruslan/zakupgotov/recipe/RecipeShoppingListConversion.java` — generated list + immutable provenance map.
- `apps/api/src/main/java/io/github/trueruslan/zakupgotov/recipe/RecipeShoppingItemIdDeriver.java` — package-private deterministic-ID seam, with production name-based UUID implementation.
- `apps/api/src/main/java/io/github/trueruslan/zakupgotov/recipe/RecipeShoppingListConverter.java` — pure grouping/scaling/conversion service.

### New tests

- `apps/api/src/test/java/io/github/trueruslan/zakupgotov/recipe/RecipeValueObjectsTest.java`
- `apps/api/src/test/java/io/github/trueruslan/zakupgotov/recipe/RecipeTest.java`
- `apps/api/src/test/java/io/github/trueruslan/zakupgotov/recipe/RecipeShoppingListConverterTest.java`

### Existing verification used unchanged unless a real failure requires adjustment

- `apps/api/src/test/java/io/github/trueruslan/zakupgotov/ApplicationArchitectureTest.java` — `ApplicationModules.of(ZakupGotovApplication.class).verify()` must continue to pass after `recipe` becomes a top-level application module.

---

### Task 1: Recipe Value Objects and Immutable Aggregate

**Files:**
- Create: `apps/api/src/test/java/io/github/trueruslan/zakupgotov/recipe/RecipeValueObjectsTest.java`
- Create: `apps/api/src/test/java/io/github/trueruslan/zakupgotov/recipe/RecipeTest.java`
- Create: `apps/api/src/main/java/io/github/trueruslan/zakupgotov/recipe/RecipeId.java`
- Create: `apps/api/src/main/java/io/github/trueruslan/zakupgotov/recipe/RecipeIngredientId.java`
- Create: `apps/api/src/main/java/io/github/trueruslan/zakupgotov/recipe/RecipeTitle.java`
- Create: `apps/api/src/main/java/io/github/trueruslan/zakupgotov/recipe/RecipeServings.java`
- Create: `apps/api/src/main/java/io/github/trueruslan/zakupgotov/recipe/RecipeIngredient.java`
- Create: `apps/api/src/main/java/io/github/trueruslan/zakupgotov/recipe/Recipe.java`

**Interfaces:**
- Consumes: existing `ShoppingRequirement` and `Quantity`.
- Produces:
  - `record RecipeId(UUID value)`
  - `record RecipeIngredientId(UUID value)`
  - `record RecipeTitle(String value)`
  - `record RecipeServings(int value)`
  - `record RecipeIngredient(RecipeIngredientId id, ShoppingRequirement requirement, Quantity quantity)`
  - `Recipe(RecipeId id, RecipeTitle title, RecipeServings baseServings, List<RecipeIngredient> ingredients)` with accessors and immutable ingredient exposure.

- [ ] **Step 1: Write failing value-object tests**

```java
@Test
void normalizesRecipeTitleWhitespaceWithoutChangingCase() {
    assertThat(new RecipeTitle("  Pasta   Carbonara  ").value())
            .isEqualTo("Pasta Carbonara");
}

@Test
void rejectsBlankTitleAndNonPositiveServings() {
    assertThatThrownBy(() -> new RecipeTitle("   "))
            .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> new RecipeServings(0))
            .isInstanceOf(IllegalArgumentException.class);
}

@Test
void rejectsNullIds() {
    assertThatThrownBy(() -> new RecipeId(null))
            .isInstanceOf(NullPointerException.class);
    assertThatThrownBy(() -> new RecipeIngredientId(null))
            .isInstanceOf(NullPointerException.class);
}
```

- [ ] **Step 2: Run RED for missing Recipe value objects**

Run:

```bash
cd apps/api
./mvnw -Dtest=RecipeValueObjectsTest test
```

Expected: FAIL at test compilation because Recipe types do not exist.

- [ ] **Step 3: Implement the minimal value objects**

Use compact record constructors. `RecipeTitle` must exactly mirror the established whitespace rule:

```java
value = Objects.requireNonNull(value, "value must not be null")
        .strip()
        .replaceAll("\\s+", " ");
if (value.isBlank()) {
    throw new IllegalArgumentException("value must not be blank");
}
```

`RecipeServings` rejects `value <= 0`.

- [ ] **Step 4: Run value-object GREEN**

```bash
cd apps/api
./mvnw -Dtest=RecipeValueObjectsTest test
```

Expected: PASS.

- [ ] **Step 5: Write failing aggregate tests**

Cover:

```java
@Test
void preservesIngredientOrderAndExposesImmutableList() {
    var recipe = new Recipe(recipeId, title, servings, List.of(first, second));
    assertThat(recipe.ingredients()).containsExactly(first, second);
    assertThatThrownBy(() -> recipe.ingredients().add(first))
            .isInstanceOf(UnsupportedOperationException.class);
}

@Test
void rejectsEmptyIngredientsAndDuplicateIngredientIds() {
    assertThatThrownBy(() -> new Recipe(recipeId, title, servings, List.of()))
            .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> new Recipe(recipeId, title, servings, List.of(first, duplicateIdIngredient)))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("duplicate");
}
```

Also reject null recipe fields and null ingredient elements.

- [ ] **Step 6: Run aggregate RED**

```bash
cd apps/api
./mvnw -Dtest=RecipeTest test
```

Expected: FAIL at compile or assertions until aggregate exists.

- [ ] **Step 7: Implement immutable Recipe aggregate**

Implementation requirements:

```java
this.ingredients = List.copyOf(ingredients);
```

Validate non-empty input and duplicate `RecipeIngredientId` before assignment. Do not add mutation methods or persistence annotations.

- [ ] **Step 8: Run Task 1 GREEN**

```bash
cd apps/api
./mvnw -Dtest=RecipeValueObjectsTest,RecipeTest test
```

Expected: PASS.

- [ ] **Step 9: Commit Task 1**

```bash
git add apps/api/src/main/java/io/github/trueruslan/zakupgotov/recipe \
        apps/api/src/test/java/io/github/trueruslan/zakupgotov/recipe
git commit -m "feat(m2): add immutable recipe domain"
```

---

### Task 2: Exact-Safe Grouping and Serving Scaling

**Files:**
- Create: `apps/api/src/main/java/io/github/trueruslan/zakupgotov/recipe/RecipeIngredientRef.java`
- Create: `apps/api/src/main/java/io/github/trueruslan/zakupgotov/recipe/RecipeShoppingListConversion.java`
- Create: `apps/api/src/main/java/io/github/trueruslan/zakupgotov/recipe/RecipeShoppingItemIdDeriver.java`
- Create: `apps/api/src/main/java/io/github/trueruslan/zakupgotov/recipe/RecipeShoppingListConverter.java`
- Create: `apps/api/src/test/java/io/github/trueruslan/zakupgotov/recipe/RecipeShoppingListConverterTest.java`

**Interfaces:**
- Consumes Task 1 `Recipe`, `RecipeServings`, `RecipeIngredient`, plus existing Shopping Core types.
- Produces:
  - `record RecipeIngredientRef(RecipeId recipeId, RecipeIngredientId ingredientId)`
  - `record RecipeShoppingListConversion(ShoppingList shoppingList, Map<ShoppingItemId, List<RecipeIngredientRef>> provenance)` with defensive immutable provenance copying.
  - `RecipeShoppingListConverter.convert(Recipe recipe, RecipeServings targetServings, ShoppingListId shoppingListId)`.

- [ ] **Step 1: Write one-to-one and canonicalization RED tests**

Start with one ingredient and no merge:

```java
@Test
void scalesOneIngredientIntoShoppingList() {
    var recipe = recipe(4, ingredient("Flour", "400", QuantityUnit.GRAM));

    var result = converter.convert(recipe, new RecipeServings(2), listId);

    assertThat(result.shoppingList().items()).singleElement().satisfies(item -> {
        assertThat(item.requirement()).isEqualTo(new ShoppingRequirement("Flour"));
        assertThat(item.quantity()).isEqualTo(new Quantity(new BigDecimal("200"), QuantityUnit.GRAM));
    });
}

@Test
void reusesShoppingQuantityCanonicalization() {
    var recipe = recipe(2, ingredient("Milk", "0.5", QuantityUnit.LITER));
    var result = converter.convert(recipe, new RecipeServings(4), listId);
    assertThat(result.shoppingList().items().getFirst().quantity())
            .isEqualTo(new Quantity(new BigDecimal("1000"), QuantityUnit.MILLILITER));
}
```

- [ ] **Step 2: Run converter RED**

```bash
cd apps/api
./mvnw -Dtest=RecipeShoppingListConverterTest test
```

Expected: FAIL because conversion types do not exist.

- [ ] **Step 3: Implement minimal one-to-one conversion with deterministic decimal scaling**

Inside the converter, operate on the already-canonical `Quantity.amount()` / `Quantity.unit()` values.

Use helper semantics equivalent to:

```java
private BigDecimal scale(BigDecimal summedBaseAmount, int target, int base) {
    var numerator = summedBaseAmount.multiply(BigDecimal.valueOf(target));
    try {
        return numerator.divide(BigDecimal.valueOf(base));
    } catch (ArithmeticException nonTerminating) {
        return numerator.divide(BigDecimal.valueOf(base), MathContext.DECIMAL128);
    }
}
```

Construct the output `Quantity` from that amount and canonical unit.

- [ ] **Step 4: Run one-to-one GREEN**

```bash
cd apps/api
./mvnw -Dtest=RecipeShoppingListConverterTest test
```

Expected: initial converter tests PASS.

- [ ] **Step 5: Add RED tests for exact-safe merge and non-merge behavior**

Required cases:

```java
// merge after ShoppingRequirement whitespace normalization
ingredient("  Milk  ", "500", MILLILITER)
ingredient("Milk", "0.5", LITER)
// => one Milk item, canonical 1000 ml before serving scaling

// do not merge case difference
"Milk" vs "milk"

// do not merge synonym-like text
"tomatoes" vs "tomato"

// do not merge physical dimension mismatch
"Eggs" in GRAM vs "Eggs" in PIECE
```

Also assert output group order follows first occurrence.

- [ ] **Step 6: Implement grouping before scaling**

Use a `LinkedHashMap<MergeKey, GroupAccumulator>` so first occurrence determines output order.

`MergeKey` must be:

```java
private record MergeKey(ShoppingRequirement requirement, QuantityUnit unit) {}
```

Because `Quantity` canonicalizes at construction, `ingredient.quantity().unit()` is already canonical. Sum each group's canonical amount first; scale once after grouping.

- [ ] **Step 7: Add and satisfy deterministic non-terminating ratio test**

```java
@Test
void usesDeterministicDecimal128ForNonTerminatingScaleRatio() {
    var recipe = recipe(3, ingredient("Spice", "100", QuantityUnit.GRAM));
    var first = converter.convert(recipe, new RecipeServings(1), listId);
    var second = converter.convert(recipe, new RecipeServings(1), listId);

    assertThat(first.shoppingList().items().getFirst().quantity())
            .isEqualTo(second.shoppingList().items().getFirst().quantity());
    assertThat(first.shoppingList().items().getFirst().quantity().amount())
            .isEqualByComparingTo(new BigDecimal("33.33333333333333333333333333333333"));
}
```

- [ ] **Step 8: Run Task 2 GREEN**

```bash
cd apps/api
./mvnw -Dtest=RecipeShoppingListConverterTest test
```

Expected: PASS for scaling, canonicalization, merge/non-merge and stable order.

- [ ] **Step 9: Commit Task 2**

```bash
git add apps/api/src/main/java/io/github/trueruslan/zakupgotov/recipe \
        apps/api/src/test/java/io/github/trueruslan/zakupgotov/recipe/RecipeShoppingListConverterTest.java
git commit -m "feat(m2): convert recipes into shopping lists"
```

---

### Task 3: Deterministic Item Identity and Complete Provenance

**Files:**
- Modify: `apps/api/src/main/java/io/github/trueruslan/zakupgotov/recipe/RecipeShoppingItemIdDeriver.java`
- Modify: `apps/api/src/main/java/io/github/trueruslan/zakupgotov/recipe/RecipeShoppingListConverter.java`
- Modify: `apps/api/src/main/java/io/github/trueruslan/zakupgotov/recipe/RecipeShoppingListConversion.java`
- Modify: `apps/api/src/test/java/io/github/trueruslan/zakupgotov/recipe/RecipeShoppingListConverterTest.java`

**Interfaces:**
- Consumes: Task 2 merge groups.
- Produces deterministic `ShoppingItemId` and immutable `Map<ShoppingItemId, List<RecipeIngredientRef>>`.

- [ ] **Step 1: Write deterministic identity RED tests**

Test these invariants independently:

```java
same recipe + same listId + same targetServings -> same item IDs
same listId + changed targetServings -> same item IDs
same logical requirement expressed kg vs g -> same item ID
different listId -> different item ID
different requirement or canonical unit -> different item ID
```

- [ ] **Step 2: Implement default deterministic ID derivation**

The identity payload is exactly:

```text
<shoppingListUuid>\n<normalizedRequirementText>\n<CANONICAL_UNIT_NAME>
```

Use:

```java
UUID.nameUUIDFromBytes(payload.getBytes(StandardCharsets.UTF_8))
```

Return `new ShoppingItemId(uuid)`.

`RecipeShoppingItemIdDeriver` should be a package-private functional interface so tests can inject a collision-producing implementation without exposing the seam outside the recipe package.

- [ ] **Step 3: Run identity GREEN**

```bash
cd apps/api
./mvnw -Dtest=RecipeShoppingListConverterTest test
```

Expected: PASS identity tests.

- [ ] **Step 4: Write provenance RED tests**

Required assertions:

```java
@Test
void recordsSelfContainedProvenanceForMergedIngredients() {
    var result = converter.convert(recipeWithTwoMergeableIngredients, target, listId);
    var item = result.shoppingList().items().getFirst();

    assertThat(result.provenance().get(item.id()))
            .containsExactly(
                    new RecipeIngredientRef(recipeId, firstIngredientId),
                    new RecipeIngredientRef(recipeId, secondIngredientId));
}
```

Also prove:
- every output item has exactly one map entry;
- every contributing ingredient appears exactly once;
- provenance order follows source ingredient order;
- map and nested lists reject mutation.

- [ ] **Step 5: Implement defensive provenance copying**

Build in `LinkedHashMap` order, then copy every value with `List.copyOf` and expose an unmodifiable copy preserving insertion order, e.g.:

```java
var copy = new LinkedHashMap<ShoppingItemId, List<RecipeIngredientRef>>();
provenance.forEach((id, refs) -> copy.put(id, List.copyOf(refs)));
this.provenance = Collections.unmodifiableMap(copy);
```

Do not use a mutable list/map reference supplied by converter internals.

- [ ] **Step 6: Write collision RED test through the package-private ID seam**

Inject an ID deriver that always returns the same fixed `ShoppingItemId` for two different merge keys. Expected conversion behavior:

```java
assertThatThrownBy(() -> collisionConverter.convert(recipeWithTwoDifferentKeys, target, listId))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("collision");
```

- [ ] **Step 7: Implement fail-closed collision detection**

Before adding a generated item, track `ShoppingItemId -> MergeKey`. If the same ID is already associated with a different merge key, throw `IllegalStateException`; do not overwrite/merge the groups.

- [ ] **Step 8: Run Task 3 GREEN**

```bash
cd apps/api
./mvnw -Dtest=RecipeShoppingListConverterTest test
```

Expected: PASS all identity/provenance/collision tests.

- [ ] **Step 9: Commit Task 3**

```bash
git add apps/api/src/main/java/io/github/trueruslan/zakupgotov/recipe \
        apps/api/src/test/java/io/github/trueruslan/zakupgotov/recipe/RecipeShoppingListConverterTest.java
git commit -m "test(m2): harden recipe identity and provenance"
```

---

### Task 4: Architecture Gate, Full Verification, and Project State

**Files:**
- Verify: `apps/api/src/test/java/io/github/trueruslan/zakupgotov/ApplicationArchitectureTest.java`
- Modify after code GREEN: `docs/PROJECT_STATE.md`
- Modify after code GREEN: `docs/ROADMAP.md`
- Modify after code GREEN: `CHANGELOG.md`
- Create: `docs/superpowers/plans/2026-08-13-m2-1-recipe-domain-shipping.md` only at shipping marker stage.

**Interfaces:**
- Consumes: all Tasks 1–3.
- Produces: verified M2.1 candidate with durable project-state evidence.

- [ ] **Step 1: Run focused recipe suite**

```bash
cd apps/api
./mvnw -Dtest=RecipeValueObjectsTest,RecipeTest,RecipeShoppingListConverterTest test
```

Expected: PASS.

- [ ] **Step 2: Run Spring Modulith architecture verification explicitly**

```bash
cd apps/api
./mvnw -Dtest=ApplicationArchitectureTest test
```

Expected: PASS. The new top-level `recipe` module may depend on `shopping`; no reverse dependency/cycle may appear.

- [ ] **Step 3: Run full API verification**

```bash
cd apps/api
./mvnw verify
```

Expected: `BUILD SUCCESS` with all API tests green.

- [ ] **Step 4: Review production code for forbidden dependencies/scope**

Check the changed production files and confirm:

```text
recipe imports shopping: allowed
shopping imports recipe: none
Spring annotations in recipe converter/domain: none
network/database/clock dependencies: none
new Maven dependency: none
REST/OpenAPI/web changes: none
```

If any forbidden dependency exists, remove it and repeat Steps 1–3.

- [ ] **Step 5: Update canonical docs only after code is GREEN**

Record:
- #93 / M2.1 `IMPLEMENTED / TESTED / SHIPPING` until merge;
- exact-safe merge semantics;
- deterministic DECIMAL128 fallback for non-terminating serving ratios;
- deterministic list-scoped item identity;
- provenance remains outside Shopping Core;
- next slice is Recipe application/API boundary, not AI import or fuzzy matching.

- [ ] **Step 6: Run repository PR gates on exact head**

Required workflow groups:

```text
API CI
Contract CI
Web CI / Web E2E
CodeQL
Dependency Review
Container Security CI
Retailer Bridge CI
Release Contract CI
Release Bundle CI
```

Expected: 9/9 workflow groups success; no P0/P1/P2 review findings.

- [ ] **Step 7: Add shipping evidence marker and re-run exact-head gate**

Shipping marker must record:
- reviewed implementation SHA;
- focused recipe tests PASS;
- full `./mvnw verify` PASS;
- architecture verification PASS;
- review verdict;
- final 9/9 workflow status.

The marker commit itself must receive a second exact-head 9/9 PASS before merge.

- [ ] **Step 8: Squash merge and verify main**

Merge strictly against the verified final head SHA. Then require all push-triggered main workflows to complete successfully before closing #93 as `completed`.

- [ ] **Step 9: Commit project-state/shipping docs**

```bash
git add docs/PROJECT_STATE.md docs/ROADMAP.md CHANGELOG.md docs/superpowers/plans
git commit -m "docs(m2): record recipe domain shipping evidence"
```

---

## Plan Self-Review

- Spec coverage: all domain validation, scaling, merge, identity, provenance, module-boundary, network-free and shipping requirements are mapped to Tasks 1–4.
- Placeholder scan: no TBD/TODO/"implement later" instructions remain.
- Type consistency: converter, provenance and identity signatures are defined once and reused consistently.
- Scope: one domain/conversion subsystem only; REST/OpenAPI/UI/persistence remain explicit follow-up work.
- Numerical correctness: exact division is attempted first; only non-terminating division uses `MathContext.DECIMAL128`; group amounts are summed before scaling.
- Shopping boundary: no production Shopping Core file is planned for modification.
