# M3.5.1 Pantry Subtraction Semantics Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a pure deterministic Pantry domain layer that subtracts household stock from a canonical ShoppingList with explicit immutable audit evidence.

**Architecture:** Create `io.github.trueruslan.zakupgotov.pantry` depending only on accepted `shopping` types. Pantry stock is aggregated by exact `(ShoppingRequirement, canonical QuantityUnit)`, consumed sequentially in source ShoppingList order, and projected to a new remaining ShoppingList preserving source IDs plus per-item adjustment evidence.

**Tech Stack:** Java 25, JUnit 5, AssertJ, ArchUnit, existing Gradle/API CI.

## Global Constraints

- Baseline: `e11fd532c8d1f927a14cb886abaa9e9988f9b21b`.
- Issue: #121.
- No production code without a failing test first.
- `pantry` may depend only on `shopping` project package.
- No endpoint/OpenAPI/generated-client/UI/persistence/provider/retailer changes.
- Exact matching only; no case folding, fuzzy, synonym or AI equivalence.
- `Quantity` remains authoritative for positive values and kg/g + l/ml canonicalization.
- Input ShoppingList and caller-owned pantry collection must never be mutated.
- Preserve source ShoppingListId, surviving ShoppingItemId values and source item order.

---

### Task 1: Pantry value objects and evidence invariants

**Files:**
- Create: `apps/api/src/test/java/io/github/trueruslan/zakupgotov/pantry/PantryAdjustmentEvidenceTest.java`
- Create: `apps/api/src/main/java/io/github/trueruslan/zakupgotov/pantry/PantryItem.java`
- Create: `apps/api/src/main/java/io/github/trueruslan/zakupgotov/pantry/PantryAdjustmentStatus.java`
- Create: `apps/api/src/main/java/io/github/trueruslan/zakupgotov/pantry/PantryAdjustmentEvidence.java`

**Interfaces:**
- Produces: `PantryItem(ShoppingRequirement requirement, Quantity quantity)`.
- Produces: enum `PantryAdjustmentStatus { UNCHANGED, PARTIALLY_COVERED, FULLY_COVERED }`.
- Produces: `PantryAdjustmentEvidence(ShoppingItemId itemId, ShoppingRequirement requirement, Quantity required, Optional<Quantity> pantryUsed, Optional<Quantity> remaining, PantryAdjustmentStatus status)`.

- [ ] **Step 1: Write failing evidence tests**

Cover valid UNCHANGED/PARTIAL/FULL states and reject impossible combinations such as `UNCHANGED` with used quantity, `PARTIALLY_COVERED` without both quantities, FULL with remaining quantity, mismatched units, used > required, or required != used + remaining.

Representative test:

```java
@Test
void fullyCoveredEvidenceKeepsRequiredAndUsedButHasNoRemainingQuantity() {
    var required = quantity("500", QuantityUnit.GRAM);

    var evidence = new PantryAdjustmentEvidence(
            itemId(1),
            new ShoppingRequirement("Rice"),
            required,
            Optional.of(required),
            Optional.empty(),
            PantryAdjustmentStatus.FULLY_COVERED);

    assertThat(evidence.status()).isEqualTo(PantryAdjustmentStatus.FULLY_COVERED);
    assertThat(evidence.pantryUsed()).contains(required);
    assertThat(evidence.remaining()).isEmpty();
}
```

- [ ] **Step 2: Verify RED**

Run:

```bash
./gradlew :apps:api:test --tests '*PantryAdjustmentEvidenceTest'
```

Expected: FAIL because Pantry production types do not exist.

- [ ] **Step 3: Implement minimal value objects**

`PantryItem` null-checks both fields. `PantryAdjustmentEvidence` defensively null-checks all fields and validates status structure, equal units, arithmetic consistency and positive `Quantity` invariants inherited from Shopping.

Use `BigDecimal.compareTo` for arithmetic equality after canonicalization.

- [ ] **Step 4: Verify GREEN**

Run the same focused test command; expected PASS.

- [ ] **Step 5: Commit**

```bash
git add apps/api/src/main/java/io/github/trueruslan/zakupgotov/pantry \
        apps/api/src/test/java/io/github/trueruslan/zakupgotov/pantry/PantryAdjustmentEvidenceTest.java
git commit -m "feat(m3): define Pantry adjustment evidence"
```

---

### Task 2: Deterministic Pantry subtraction core

**Files:**
- Create: `apps/api/src/test/java/io/github/trueruslan/zakupgotov/pantry/PantryShoppingListAdjusterTest.java`
- Create: `apps/api/src/main/java/io/github/trueruslan/zakupgotov/pantry/PantryMatchKey.java`
- Create: `apps/api/src/main/java/io/github/trueruslan/zakupgotov/pantry/PantryAdjustment.java`
- Create: `apps/api/src/main/java/io/github/trueruslan/zakupgotov/pantry/PantryShoppingListAdjuster.java`

**Interfaces:**
- `PantryAdjustment` exposes `ShoppingList remainingShoppingList()` and immutable `List<PantryAdjustmentEvidence> evidence()`.
- `PantryShoppingListAdjuster#adjust(ShoppingList source, List<PantryItem> pantryItems)` returns `PantryAdjustment`.
- `PantryMatchKey` is package-private and contains only `ShoppingRequirement requirement, QuantityUnit unit`.

- [ ] **Step 1: Write the first failing subtraction test**

Start with partial coverage and identity/order preservation:

```java
@Test
void partialCoverageReducesQuantityAndPreservesIdentity() {
    var source = shoppingList(
            item(1, "Rice", "1000", QuantityUnit.GRAM),
            item(2, "Milk", "1000", QuantityUnit.MILLILITER));

    var result = adjuster.adjust(source, List.of(
            pantry("Rice", "250", QuantityUnit.GRAM)));

    assertThat(result.remainingShoppingList().id()).isEqualTo(source.id());
    assertThat(result.remainingShoppingList().items())
            .extracting(item -> item.id().value())
            .containsExactly(itemId(1).value(), itemId(2).value());
    assertThat(result.remainingShoppingList().items().getFirst().quantity())
            .isEqualTo(quantity("750", QuantityUnit.GRAM));
    assertThat(source.items().getFirst().quantity())
            .isEqualTo(quantity("1000", QuantityUnit.GRAM));
}
```

- [ ] **Step 2: Verify RED**

```bash
./gradlew :apps:api:test --tests '*PantryShoppingListAdjusterTest'
```

Expected: FAIL because adjuster/result types do not exist.

- [ ] **Step 3: Implement minimal partial subtraction**

Create a new `ShoppingList(source.id())`, copy surviving items with original IDs/requirements and subtract using canonical amounts. Emit one evidence row per source item.

- [ ] **Step 4: Verify focused GREEN**

Run the focused test; expected PASS.

- [ ] **Step 5: Add RED cases incrementally**

Add one failing behavior at a time, running after each addition:

1. unmatched pantry → item unchanged + UNCHANGED evidence;
2. full coverage → source item absent from remaining list + FULLY_COVERED evidence;
3. kg pantry covers gram requirement;
4. liter pantry covers milliliter requirement;
5. incompatible dimensions do not match;
6. duplicate pantry entries with the same key aggregate;
7. duplicate source keys consume shared pantry only once in source order;
8. excess pantry never creates zero/negative Shopping quantities;
9. result evidence stays in source order;
10. input source/pantry list remains unchanged.

- [ ] **Step 6: Implement each GREEN minimally**

Maintain a `LinkedHashMap<PantryMatchKey, BigDecimal>` of remaining pantry amounts. Aggregate duplicate pantry entries by addition. For each source item, compute:

```java
var available = stock.getOrDefault(key, BigDecimal.ZERO);
var used = required.min(available);
var remaining = required.subtract(used);
stock.put(key, available.subtract(used));
```

Create `Quantity` only for strictly positive `used`/`remaining` amounts. Never mutate the source item/list or caller collection.

- [ ] **Step 7: Verify complete subtraction suite GREEN**

```bash
./gradlew :apps:api:test --tests '*PantryShoppingListAdjusterTest' --tests '*PantryAdjustmentEvidenceTest'
```

Expected: PASS.

- [ ] **Step 8: Commit**

```bash
git add apps/api/src/main/java/io/github/trueruslan/zakupgotov/pantry \
        apps/api/src/test/java/io/github/trueruslan/zakupgotov/pantry
git commit -m "feat(m3): add deterministic Pantry subtraction"
```

---

### Task 3: Architecture hardening

**Files:**
- Create: `apps/api/src/test/java/io/github/trueruslan/zakupgotov/pantry/PantryArchitectureTest.java`

**Interfaces:**
- No production API changes.
- Enforces package dependency direction.

- [ ] **Step 1: Write failing architecture test before any forbidden dependency exists**

The architecture test must assert the production package exists, collect direct project-package dependencies from `..pantry..`, allow only `..shopping..`, and assert no reverse dependencies from accepted `shopping`, `recipe` or `weeklyplan` packages.

Core rule:

```java
noClasses()
        .that().resideInAPackage("..pantry..")
        .should().dependOnClassesThat().resideInAnyPackage(
                "..recipe..",
                "..weeklyplan..",
                "..preview..",
                "..comparison..",
                "..basket..",
                "..matching..",
                "..provider..",
                "..retailer..",
                "..database..",
                "org.springframework..")
        .check(classes);
```

Also assert:

```java
noClasses()
        .that().resideInAnyPackage("..shopping..", "..recipe..", "..weeklyplan..")
        .should().dependOnClassesThat().resideInAPackage("..pantry..")
        .check(classes);
```

- [ ] **Step 2: Verify the architecture test runs GREEN against the already implemented allowed dependency graph**

This task is an architecture characterization/hardening test; no production change is expected if Task 2 respected the design.

```bash
./gradlew :apps:api:test --tests '*PantryArchitectureTest'
```

Expected: PASS. If it fails, fix the production dependency violation, not the rule.

- [ ] **Step 3: Commit**

```bash
git add apps/api/src/test/java/io/github/trueruslan/zakupgotov/pantry/PantryArchitectureTest.java
git commit -m "test(m3): harden Pantry architecture boundary"
```

---

### Task 4: Full verification and shipping evidence

**Files:**
- Create: `docs/superpowers/plans/2026-08-15-m3-5-1-pantry-subtraction-semantics-shipping.md`

**Interfaces:**
- No production API changes.

- [ ] **Step 1: Run focused and full API verification**

```bash
./gradlew :apps:api:test --tests '*Pantry*'
./gradlew :apps:api:check
```

Both must PASS.

- [ ] **Step 2: Confirm scope**

Verify no changes under OpenAPI/generated clients/web/database/provider/retailer and no edits to accepted M3.1–M3.4 production packages other than imports required by tests (prefer none).

- [ ] **Step 3: Write shipping evidence**

Record baseline, RED/GREEN commit SHAs, focused/full test results, exact changed files, explicit non-goals and the expected PR acceptance gate.

- [ ] **Step 4: Commit shipping evidence**

```bash
git add docs/superpowers/plans/2026-08-15-m3-5-1-pantry-subtraction-semantics-shipping.md
git commit -m "docs(m3): record Pantry subtraction shipping evidence"
```

- [ ] **Step 5: PR acceptance gate**

Open a draft PR closing #121. On the exact final head require all normal PR workflow groups to complete successfully, perform a read-only review with no unresolved P0–P3 findings, mark ready only after the exact-head gate, squash-merge with expected-head protection, then require all normal `main` push workflows to succeed before declaring M3.5.1 accepted.

## Self-review

- Spec coverage: all matching, arithmetic, identity, ordering, provenance, immutability, architecture and non-goal requirements map to Tasks 1–4.
- Placeholder scan: no TODO/TBD/unspecified implementation steps remain.
- Type consistency: `PantryItem`, `PantryAdjustmentEvidence`, `PantryAdjustment`, `PantryShoppingListAdjuster` signatures are consistent across tasks.
- Scope remains one independently testable subsystem: pure Pantry subtraction semantics only.