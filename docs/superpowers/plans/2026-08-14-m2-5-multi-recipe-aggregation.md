# M2.5 Deterministic Multi-Recipe Aggregation Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Aggregate several ordered Recipe occurrences into one deterministic canonical ShoppingList with occurrence-aware Recipe/ingredient provenance for M3 Weekly Planning.

**Architecture:** Keep all behavior inside the existing `recipe` domain package. Reuse `RecipeShoppingListConverter` per aggregation entry, extract only the accepted merge-key/item-ID semantics into package-private shared helpers, and build one final ShoppingList whose provenance adds an aggregation-entry identity around the existing `RecipeIngredientRef`.

**Tech Stack:** Java 25, JUnit 5, AssertJ, Spring Modulith verification, Maven/Testcontainers repository baseline.

## Global Constraints

- M2.5 is pure domain/core: no HTTP/OpenAPI/generated client/web/persistence/provider/retailer changes.
- `shopping` must remain independent from `recipe`.
- Serving scaling and source unit canonicalization remain owned by accepted `RecipeShoppingListConverter`.
- Automatic merge key remains exact normalized ShoppingRequirement + canonical QuantityUnit only.
- Repeated RecipeId is allowed under distinct aggregation-entry IDs.
- Duplicate aggregation-entry ID fails closed.
- Final ShoppingItem identity remains aggregate-list + requirement + canonical-unit scoped and independent of amount/servings.
- Provenance remains outside neutral Shopping Core types and is deeply immutable.

---

### Task 1: Characterize and share accepted ShoppingItem identity semantics

**Files:**
- Create: `apps/api/src/main/java/io/github/trueruslan/zakupgotov/recipe/RecipeShoppingMergeKey.java`
- Create: `apps/api/src/main/java/io/github/trueruslan/zakupgotov/recipe/RecipeShoppingItemIds.java`
- Modify: `apps/api/src/main/java/io/github/trueruslan/zakupgotov/recipe/RecipeShoppingListConverter.java`
- Modify: `apps/api/src/test/java/io/github/trueruslan/zakupgotov/recipe/RecipeShoppingListConverterTest.java`

**Interfaces:**
- `RecipeShoppingMergeKey(ShoppingRequirement requirement, QuantityUnit unit)` package-private record.
- `RecipeShoppingItemIds.derive(ShoppingListId, ShoppingRequirement, QuantityUnit)` package-private deterministic helper.
- Existing `RecipeShoppingItemIdDeriver` remains the test seam.

- [ ] **Step 1: Add a literal regression fixture before refactoring**

Add a converter test with fixed ShoppingListId/requirement/unit and assert the exact currently produced UUID, not merely equality across two executions.

- [ ] **Step 2: Run focused converter tests and preserve GREEN characterization**

This is a refactor characterization gate: test must pass against current production code before extraction.

- [ ] **Step 3: Extract shared key and ID helper without changing payload bytes**

Move the exact existing payload:

```text
shoppingListId.value() + "\n" + requirement.text() + "\n" + unit.name()
```

into `RecipeShoppingItemIds.derive`; replace private converter `MergeKey` with `RecipeShoppingMergeKey`.

- [ ] **Step 4: Re-run converter tests**

Require the literal UUID fixture and all existing M2.1 converter behavior to remain green.

### Task 2: Define aggregation contract with RED tests

**Files:**
- Create: `apps/api/src/main/java/io/github/trueruslan/zakupgotov/recipe/RecipeAggregationEntryId.java`
- Create: `apps/api/src/main/java/io/github/trueruslan/zakupgotov/recipe/RecipeAggregationEntry.java`
- Create: `apps/api/src/main/java/io/github/trueruslan/zakupgotov/recipe/RecipeAggregationIngredientRef.java`
- Create: `apps/api/src/main/java/io/github/trueruslan/zakupgotov/recipe/RecipeShoppingListAggregation.java`
- Create: `apps/api/src/main/java/io/github/trueruslan/zakupgotov/recipe/RecipeShoppingListAggregator.java`
- Create: `apps/api/src/test/java/io/github/trueruslan/zakupgotov/recipe/RecipeShoppingListAggregatorTest.java`

**Interfaces:**

```java
public record RecipeAggregationEntryId(UUID value) {}
public record RecipeAggregationEntry(
    RecipeAggregationEntryId id,
    Recipe recipe,
    RecipeServings targetServings) {}
public record RecipeAggregationIngredientRef(
    RecipeAggregationEntryId entryId,
    RecipeIngredientRef ingredientRef) {}
public record RecipeShoppingListAggregation(
    ShoppingList shoppingList,
    Map<ShoppingItemId, List<RecipeAggregationIngredientRef>> provenance) {}
public final class RecipeShoppingListAggregator {
    public RecipeShoppingListAggregation aggregate(
        List<RecipeAggregationEntry> entries,
        ShoppingListId aggregateShoppingListId);
}
```

- [ ] **Step 1: Add RED test for compatible cross-recipe merge**

Two different recipes contribute normalized `milk` quantities in LITER/MILLILITER. After accepted per-recipe canonicalization/scaling, require one aggregate `milk` item with exact summed MILLILITER quantity and provenance ordered by entry.

- [ ] **Step 2: Run focused test and preserve RED**

Expected failure: aggregation production types/service do not exist.

- [ ] **Step 3: Add RED coverage for ordering and non-merge**

Require first compatible occurrence order across entries; case-different requirements and incompatible units remain separate.

- [ ] **Step 4: Add repeated-Recipe occurrence RED**

Use the same Recipe object/RecipeId twice with distinct `RecipeAggregationEntryId` values and different target servings. Require aggregation success and two distinct provenance refs for the same underlying RecipeIngredientRef.

- [ ] **Step 5: Implement minimal aggregation value types and service**

For every entry derive an internal conversion ShoppingListId via UTF-8 `UUID.nameUUIDFromBytes` over namespace line + aggregate list UUID + entry UUID, invoke accepted converter, accumulate by `RecipeShoppingMergeKey`, sum canonical quantities, derive final item IDs with `RecipeShoppingItemIds.derive`, and wrap lineage with the entry ID.

- [ ] **Step 6: Run focused aggregation + converter tests GREEN**

No other production package changes are allowed in this task.

### Task 3: Fail-closed, identity and immutability hardening

**Files:**
- Modify: `RecipeShoppingListAggregator.java`
- Modify: `RecipeShoppingListAggregation.java`
- Modify: `RecipeShoppingListAggregatorTest.java`
- Create/modify: `apps/api/src/test/java/io/github/trueruslan/zakupgotov/recipe/RecipeArchitectureTest.java` only if an existing architecture test does not already cover shopping→recipe independence.

- [ ] **Step 1: RED duplicate-entry and null/empty inputs**

Require null aggregate ID, null/empty entry list, null entry and duplicate entry IDs to reject instead of skip.

- [ ] **Step 2: RED identity stability**

For a fixed aggregate list ID + merge key, change ingredient amount/target servings and require the final ShoppingItem ID to remain identical. Change aggregate list ID and require a different final item ID.

- [ ] **Step 3: RED collision path**

Use a package-private aggregator constructor with injected `RecipeShoppingItemIdDeriver` returning one fixed ID for two distinct merge keys. Require `IllegalStateException("generated shopping item id collision")`.

- [ ] **Step 4: RED deep immutability**

Require attempts to mutate the returned provenance map and nested lineage list to fail; changes to caller-owned input lists after aggregation cannot change output.

- [ ] **Step 5: Implement minimal fail-closed checks / defensive copies**

Do not add recovery, logging, fallback or fuzzy reconciliation.

- [ ] **Step 6: Architecture regression**

Require `shopping` production classes to remain dependency-free from `recipe`; M2.5 must not add preview/provider/retailer/matching/basket/comparison/database dependencies.

- [ ] **Step 7: Focused GREEN**

Run all Recipe tests and architecture tests.

### Task 4: Full verification and shipping

**Files:**
- Update: `docs/PROJECT_STATE.md` to `M2.5 IMPLEMENTED / TESTED / SHIPPING` only before merge.
- Update: `docs/ROADMAP.md` conservatively; do not mark M2.5 accepted before post-merge proof.
- Update: `CHANGELOG.md`.
- Create: `docs/superpowers/plans/2026-08-14-m2-5-multi-recipe-aggregation-shipping.md`.

- [ ] **Step 1: Full backend verification**

Run repository API/Maven verify through CI, including Modulith and existing PostgreSQL/Testcontainers baseline.

- [ ] **Step 2: Contract/web regression**

Require no OpenAPI/generated-client diff and existing Contract/Web/Playwright workflows green.

- [ ] **Step 3: Exact-head CI**

All normal PR workflow groups must succeed on the same head: API, Contract, Web/E2E, CodeQL, Dependency Review, Container Security, Retailer Bridge, Release Contract and Release Bundle.

- [ ] **Step 4: Read-only review**

Review merge semantics, repeated-Recipe lineage, ID stability, helper extraction compatibility, defensive immutability, dependency direction and scope containment. Block P0/P1/P2.

- [ ] **Step 5: Squash merge with expected-head protection**

Only after review threads are clear and exact-head CI remains green.

- [ ] **Step 6: Post-merge acceptance**

Require all normal push workflows on the merged main SHA to succeed. Only then close #106 as COMPLETE / ACCEPTED and synchronize canonical state/roadmap/changelog. The next milestone becomes M3 Weekly Planning.

## Self-review

- Spec coverage: repeated Recipe occurrence, exact merge semantics, ordering, stable IDs, shared identity helper, provenance, fail-closed validation, architecture and acceptance gates are all mapped.
- Scope containment: no API/UI/persistence/planner/AI work is included.
- Type consistency: occurrence-aware provenance wraps the existing accepted `RecipeIngredientRef` rather than duplicating Recipe/ingredient identity fields.
