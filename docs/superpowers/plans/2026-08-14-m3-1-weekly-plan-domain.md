# M3.1 WeeklyPlan Domain + Deterministic Shopping Composition Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Introduce an immutable WeeklyPlan domain and a deterministic planner-to-shopping composer that delegates all Recipe scaling/canonicalization/merge/ShoppingItem identity behavior to accepted M2.5 multi-recipe aggregation while projecting complete lineage back to WeeklyPlan meal occurrences.

**Architecture:** Add a new inward-facing `weeklyplan` package. `WeeklyPlan` owns planner identity, day metadata, ordered meal occurrences and target servings. `WeeklyPlanShoppingListComposer` deterministically maps occurrences to accepted `RecipeAggregationEntry` inputs, invokes M2.5 exactly once through a narrow package-private boundary, and projects aggregation provenance to planner provenance. Deterministic WeeklyPlan ShoppingList/aggregation-entry UUID derivation lives behind a package-private identity seam so collision/failure behavior can be tested without modifying accepted Recipe code.

**Tech Stack:** Java 25, JUnit 5, AssertJ, Spring Modulith verification, Maven/Testcontainers repository baseline.

## Global Constraints

- M3.1 is pure domain/application work: no REST/OpenAPI/generated client/web/persistence/database/provider/retailer/comparison changes.
- `weeklyplan` may depend only on accepted `recipe`, the finite `shopping` value/aggregate types required by its result, and JDK classes.
- `recipe` and `shopping` must not depend on `weeklyplan`.
- WeeklyPlan day metadata never participates in Recipe merge keys, quantity arithmetic or final ShoppingItem identity.
- No breakfast/lunch/dinner/snack slot vocabulary.
- Same Recipe may appear repeatedly under distinct `WeeklyMealOccurrenceId` values.
- WeeklyPlan occurrence order is explicit caller order and is never auto-sorted by day.
- M2.5 remains authoritative for serving scaling, source-unit canonicalization, exact cross-Recipe merge, first-compatible ordering and final ShoppingItem IDs.
- Planner provenance contains `WeeklyMealOccurrenceId + RecipeIngredientRef`; internal `RecipeAggregationEntryId` does not escape the composer.
- All public outputs are deeply immutable and invalid identity/provenance states fail closed.

---

### Task 1: Lock WeeklyPlan domain contract with RED tests

**Files:**
- Create: `apps/api/src/test/java/io/github/trueruslan/zakupgotov/weeklyplan/WeeklyPlanTest.java`
- Create later in GREEN: `apps/api/src/main/java/io/github/trueruslan/zakupgotov/weeklyplan/WeeklyPlanId.java`
- Create later in GREEN: `apps/api/src/main/java/io/github/trueruslan/zakupgotov/weeklyplan/WeeklyMealOccurrenceId.java`
- Create later in GREEN: `apps/api/src/main/java/io/github/trueruslan/zakupgotov/weeklyplan/WeeklyPlanDay.java`
- Create later in GREEN: `apps/api/src/main/java/io/github/trueruslan/zakupgotov/weeklyplan/WeeklyMealOccurrence.java`
- Create later in GREEN: `apps/api/src/main/java/io/github/trueruslan/zakupgotov/weeklyplan/WeeklyPlan.java`

**Public interfaces:**

```java
public record WeeklyPlanId(UUID value) {}
public record WeeklyMealOccurrenceId(UUID value) {}
public enum WeeklyPlanDay {
    MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY
}
public record WeeklyMealOccurrence(
    WeeklyMealOccurrenceId id,
    WeeklyPlanDay day,
    Recipe recipe,
    RecipeServings targetServings) {}
public record WeeklyPlan(
    WeeklyPlanId id,
    List<WeeklyMealOccurrence> occurrences) {}
```

- [ ] **Step 1: Add RED domain tests only**

Cover:
- valid multi-day plan;
- multiple occurrences on one day;
- same Recipe object/RecipeId repeated under distinct occurrence IDs;
- exact caller occurrence order preservation;
- defensive copy/immutable occurrence list;
- null WeeklyPlanId;
- null/empty occurrence list;
- null occurrence;
- duplicate `WeeklyMealOccurrenceId`;
- null occurrence ID/day/Recipe/target servings;
- non-positive target servings through accepted `RecipeServings` construction.

- [ ] **Step 2: Run API CI and preserve expected RED**

Expected failure: compilation errors for intentionally absent `weeklyplan` production types, not infrastructure failures or unrelated regressions.

- [ ] **Step 3: Implement minimal domain types**

Use `Objects.requireNonNull`, `List.copyOf`, a `HashSet` duplicate-occurrence check and existing `RecipeServings` validation. Do not add dates, labels, slots, persistence fields or helper behavior not required by tests/spec.

- [ ] **Step 4: Run API verification GREEN**

Require all new domain tests and existing Recipe/M2.5 tests to pass.

### Task 2: Define deterministic planner identity seam and composition result

**Files:**
- Create: `apps/api/src/main/java/io/github/trueruslan/zakupgotov/weeklyplan/WeeklyPlanIngredientRef.java`
- Create: `apps/api/src/main/java/io/github/trueruslan/zakupgotov/weeklyplan/WeeklyPlanShoppingListComposition.java`
- Create: `apps/api/src/main/java/io/github/trueruslan/zakupgotov/weeklyplan/WeeklyPlanIdentityDeriver.java`
- Create: `apps/api/src/main/java/io/github/trueruslan/zakupgotov/weeklyplan/WeeklyPlanIds.java`
- Create: `apps/api/src/main/java/io/github/trueruslan/zakupgotov/weeklyplan/RecipeAggregationBoundary.java`
- Create later in GREEN: `apps/api/src/main/java/io/github/trueruslan/zakupgotov/weeklyplan/WeeklyPlanShoppingListComposer.java`
- Create RED tests: `apps/api/src/test/java/io/github/trueruslan/zakupgotov/weeklyplan/WeeklyPlanShoppingListComposerTest.java`

**Public result:**

```java
public record WeeklyPlanIngredientRef(
    WeeklyMealOccurrenceId occurrenceId,
    RecipeIngredientRef recipeIngredient) {}

public record WeeklyPlanShoppingListComposition(
    ShoppingList shoppingList,
    Map<ShoppingItemId, List<WeeklyPlanIngredientRef>> provenance) {}
```

**Package-private seams:**

```java
interface RecipeAggregationBoundary {
    RecipeShoppingListAggregation aggregate(
        List<RecipeAggregationEntry> entries,
        ShoppingListId shoppingListId);
}

interface WeeklyPlanIdentityDeriver {
    ShoppingListId shoppingListId(WeeklyPlanId planId);
    RecipeAggregationEntryId aggregationEntryId(
        WeeklyPlanId planId,
        WeeklyMealOccurrenceId occurrenceId);
}
```

Default `WeeklyPlanIds` derivation uses UTF-8 `UUID.nameUUIDFromBytes` over exactly:

```text
zakup-gotov:weekly-plan-shopping-list:v1:<weeklyPlanId>
zakup-gotov:weekly-plan-aggregation-entry:v1:<weeklyPlanId>:<weeklyMealOccurrenceId>
```

- [ ] **Step 1: Add composition RED for one accepted M2.5 merge path**

Construct two WeeklyPlan occurrences contributing compatible `Milk` quantities through different recipes/days and target servings. Require one canonical final item with exact M2.5 quantity and planner provenance ordered by occurrence.

- [ ] **Step 2: Add repeated-Recipe and ordering RED**

Require:
- same Recipe used twice is distinguishable via two WeeklyMealOccurrenceIds in provenance;
- first compatible WeeklyPlan occurrence determines final group order;
- day value does not sort/reorder aggregation input.

- [ ] **Step 3: Add deterministic identity RED**

For same WeeklyPlanId + requirement + canonical unit:
- target-serving changes preserve ShoppingItem ID;
- day-only changes preserve ShoppingItem ID and quantity;
- occurrence reordering preserves IDs for unchanged merge keys while output order may change;
- changing WeeklyPlanId changes final ShoppingListId and ShoppingItem IDs.

- [ ] **Step 4: Run focused API CI and preserve RED**

Expected failure is absence of composer/result/identity seam production types.

- [ ] **Step 5: Implement minimal result types, identity seam and composer**

Default composer constructor:

```java
public WeeklyPlanShoppingListComposer() {
    this(new RecipeShoppingListAggregator()::aggregate, WeeklyPlanIds.INSTANCE);
}
```

Package-private constructor accepts `RecipeAggregationBoundary` and `WeeklyPlanIdentityDeriver` for invariant/collision tests.

Composition algorithm:
1. reject null plan;
2. derive one weekly ShoppingListId;
3. derive one unique internal `RecipeAggregationEntryId` per occurrence and retain `entryId -> occurrenceId` in insertion order;
4. fail closed if two distinct occurrences derive the same internal ID or any derived ID is null;
5. build ordered `RecipeAggregationEntry` list using occurrence Recipe + target servings;
6. invoke `RecipeAggregationBoundary.aggregate(...)` exactly once;
7. validate aggregate ShoppingList/provenance key completeness;
8. translate every `RecipeAggregationIngredientRef` through the internal map into `WeeklyPlanIngredientRef` without modifying `RecipeIngredientRef`;
9. preserve aggregate provenance order and return exact aggregate ShoppingList;
10. do not read/use day for aggregation semantics.

- [ ] **Step 6: Run focused composer + M2.5 regression GREEN**

### Task 3: Fail-closed provenance, collision and immutability hardening

**Files:**
- Modify: `apps/api/src/test/java/io/github/trueruslan/zakupgotov/weeklyplan/WeeklyPlanShoppingListComposerTest.java`
- Modify as needed: `apps/api/src/main/java/io/github/trueruslan/zakupgotov/weeklyplan/WeeklyPlanShoppingListComposer.java`
- Modify as needed: `apps/api/src/main/java/io/github/trueruslan/zakupgotov/weeklyplan/WeeklyPlanShoppingListComposition.java`

- [ ] **Step 1: Add hardening RED tests**

Inject seams to require fail-closed behavior for:
- null plan;
- null derived ShoppingListId;
- two different occurrences deriving the same `RecipeAggregationEntryId`;
- aggregate result with missing provenance for a final ShoppingItem;
- aggregate provenance with an orphan ShoppingItemId;
- empty aggregate provenance list;
- aggregate provenance containing unknown internal `RecipeAggregationEntryId`;
- null aggregate result / null lineage values if reachable through injected boundary;
- returned planner provenance map and nested lists are immutable.

- [ ] **Step 2: Run API CI and confirm failures are only intended hardening gaps**

Existing happy-path composer/domain/M2.5 tests must remain green.

- [ ] **Step 3: Implement only missing validation/defensive-copy behavior**

No fallback, silent skipping, logging-driven recovery or semantic repair. Invalid aggregate evidence is an internal invariant failure (`IllegalStateException`); invalid caller input remains `NullPointerException`/`IllegalArgumentException` consistent with existing domain style.

- [ ] **Step 4: Re-run focused + full API verification GREEN**

### Task 4: Architecture boundary regression

**Files:**
- Create: `apps/api/src/test/java/io/github/trueruslan/zakupgotov/weeklyplan/WeeklyPlanArchitectureTest.java` if a direct package dependency assertion can be expressed with existing test dependencies.
- Always rely on existing: `apps/api/src/test/java/io/github/trueruslan/zakupgotov/ApplicationArchitectureTest.java`

- [ ] **Step 1: Inspect available architecture-test primitives before adding a dependency**

Do not add ArchUnit or another library solely for M3.1 if it is not already present. Prefer existing Spring Modulith `ApplicationModules.verify()` plus a small source/class dependency guard using existing repository facilities.

- [ ] **Step 2: Add the smallest guard that proves scope**

Require weeklyplan production code to avoid dependencies on:
`preview`, `recipepreview`, `recipecomparisonpreview`, `provider`, `retailer`, `matching`, `basket`, `comparison`, `database` and HTTP/controller packages; verify existing `recipe`/`shopping` code has no dependency on `weeklyplan`.

- [ ] **Step 3: Full Maven/Testcontainers/Modulith GREEN**

No changes to Spring runtime wiring, database schema, OpenAPI or web are expected.

### Task 5: Shipping evidence and exact-head acceptance gate

**Files:**
- Update: `CHANGELOG.md`
- Create: `docs/superpowers/plans/2026-08-14-m3-1-weekly-plan-domain-shipping.md`
- Update `docs/PROJECT_STATE.md` only to **M3.1 IMPLEMENTED / TESTED / SHIPPING — acceptance pending** before merge if needed for current-state accuracy; do not mark ACCEPTED pre-merge.
- Update `docs/ROADMAP.md` only conservatively; M3.2 is not active until M3.1 post-merge acceptance.

- [ ] **Step 1: Record explicit RED→GREEN commit evidence**

Shipping memo must identify domain RED/GREEN, composition RED/GREEN and hardening RED/GREEN heads and what each failure/proof established.

- [ ] **Step 2: Exact-head full PR CI**

Require all nine normal PR workflow groups SUCCESS on one final head:
- API CI;
- Contract CI;
- Web CI / responsive E2E;
- CodeQL;
- Dependency Review;
- Container Security CI;
- Retailer Bridge CI;
- Release Contract CI;
- Release Bundle CI.

- [ ] **Step 3: Read-only final review**

Review:
- WeeklyPlan invariants;
- day-vs-order semantics;
- repeated Recipe occurrence behavior;
- deterministic list/entry identities;
- exact M2.5 delegation;
- planner provenance completeness/order/immutability;
- collision/failure paths;
- dependency direction and scope containment.

Block unresolved P0/P1/P2 and clear all review threads.

- [ ] **Step 4: Squash merge with exact-head protection**

Issue #109 may become merged/closed only through the PR once the exact reviewed head is green.

- [ ] **Step 5: Post-merge acceptance**

Require exactly the normal push workflows on merged `main` to finish green. Only then mark M3.1 `COMPLETE / ACCEPTED` in a separate canonical docs-only acceptance PR and advance current focus to **M3.2 stateless WeeklyPlan application/API boundary**.

## Plan Self-Review

- Spec coverage: WeeklyPlan identity/day/order, repeated Recipe occurrences, per-occurrence servings, M2.5 delegation, deterministic IDs, provenance projection, immutability, fail-closed behavior, architecture and shipping gates are mapped to concrete tasks.
- Placeholder scan: no TODO/TBD or undecided semantics remain.
- Type consistency: WeeklyMealOccurrence uses accepted `Recipe` and `RecipeServings`; planner provenance wraps accepted `RecipeIngredientRef`; Shopping result reuses accepted `ShoppingList`/`ShoppingItemId`.
- Scope containment: no persistence, API, UI, pantry, retailer, comparison, nutrition or AI work is included.
- Testability: package-private aggregation/identity seams allow malformed provenance and deterministic-ID collision tests without modifying accepted M2.5 production behavior.
