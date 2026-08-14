# M3.5.1 Pantry Subtraction Semantics Implementation Plan

> **Execution workflow:** superpowers:executing-plans, TDD-first, isolated GitHub feature branch.

**Goal:** Add a pure deterministic Pantry domain layer that subtracts household stock from a canonical `ShoppingList` with explicit immutable audit evidence.

**Architecture:** `io.github.trueruslan.zakupgotov.pantry` depends only on accepted `shopping` types. Pantry stock is aggregated by exact `(ShoppingRequirement, canonical QuantityUnit)`, consumed sequentially in source `ShoppingList` order, and projected to a remaining `ShoppingList` preserving source IDs plus per-item adjustment evidence.

**Tech stack / verification:** Java 25, Maven 3.9.16 via repository `./mvnw`, JUnit 5, AssertJ, ArchUnit, GitHub Actions API CI.

## Global constraints

- Baseline: `e11fd532c8d1f927a14cb886abaa9e9988f9b21b`.
- Issue: #121.
- PR: #122.
- No production code before the corresponding failing behavior test.
- `pantry` may depend only on the `shopping` project package.
- No endpoint/OpenAPI/generated-client/UI/persistence/provider/retailer changes.
- Exact matching only; no case folding, fuzzy, synonym or AI equivalence.
- `Quantity` remains authoritative for positive values and kg/g + l/ml canonicalization.
- Input `ShoppingList` and caller-owned pantry collection must never be mutated.
- Preserve source `ShoppingListId`, surviving `ShoppingItemId` values and source item order.

## Verification commands

Focused tests may be run with Surefire selectors, while the authoritative gate is the same full Maven verification used by API CI:

```bash
./mvnw --batch-mode --no-transfer-progress -Dtest=PantryAdjustmentEvidenceTest test
./mvnw --batch-mode --no-transfer-progress -Dtest=PantryShoppingListAdjusterTest test
./mvnw --batch-mode --no-transfer-progress -Dtest=PantryArchitectureTest test
./mvnw --batch-mode --no-transfer-progress verify
```

GitHub Actions on exact commit SHAs is authoritative in this connector-only execution environment.

---

### Task 1: Pantry value objects and evidence invariants

**Files:**
- `apps/api/src/test/java/io/github/trueruslan/zakupgotov/pantry/PantryAdjustmentEvidenceTest.java`
- `apps/api/src/main/java/io/github/trueruslan/zakupgotov/pantry/PantryItem.java`
- `apps/api/src/main/java/io/github/trueruslan/zakupgotov/pantry/PantryAdjustmentStatus.java`
- `apps/api/src/main/java/io/github/trueruslan/zakupgotov/pantry/PantryAdjustmentEvidence.java`

- [x] Define valid `UNCHANGED`, `PARTIALLY_COVERED`, `FULLY_COVERED` evidence and reject impossible status/value/unit/arithmetic combinations.
- [x] RED: `e95b076825278a4653939fe06599d5b42b3097f5` — API verification failed before Pantry production types existed.
- [x] Implement immutable value types.
- [x] Correct Java compact-record constructor capture issue without changing the test contract.
- [x] GREEN: `0b04b775b80e480c7082872b70729ec01663109d` — full API CI / Maven `verify` SUCCESS.

---

### Task 2: Deterministic Pantry subtraction core

**Files:**
- `apps/api/src/test/java/io/github/trueruslan/zakupgotov/pantry/PantryShoppingListAdjusterTest.java`
- `apps/api/src/main/java/io/github/trueruslan/zakupgotov/pantry/PantryMatchKey.java`
- `apps/api/src/main/java/io/github/trueruslan/zakupgotov/pantry/PantryAdjustment.java`
- `apps/api/src/main/java/io/github/trueruslan/zakupgotov/pantry/PantryShoppingListAdjuster.java`

Required behavior:

1. unmatched pantry leaves source requirement unchanged;
2. partial coverage reduces only quantity while preserving IDs/order;
3. full coverage omits item from remaining list while retaining audit evidence;
4. kg/g and l/ml compatibility comes only from accepted `Quantity` canonicalization;
5. incompatible dimensions do not match;
6. duplicate pantry rows aggregate by exact accepted match key;
7. pantry stock is consumed once across duplicate source keys in source order;
8. excess pantry never creates zero/negative `Quantity`;
9. exact requirement equality remains case-sensitive;
10. input shopping/pantry collections are not mutated;
11. null inputs/rows fail closed as domain misuse.

- [x] RED: `289e973463bf2d391442a9645651851ad587e177` — API test compilation failed because `PantryShoppingListAdjuster` did not exist.
- [x] Implement `LinkedHashMap<PantryMatchKey, BigDecimal>` stock aggregation and sequential `min(required, available)` consumption.
- [x] Preserve source `ShoppingListId`, surviving `ShoppingItemId`, requirement and source order.
- [x] Emit one evidence row per source item in source order.
- [x] GREEN: `a88092c914ffe5c80e4d4ad1da672ba8dcd2033d` — full API CI / Maven `verify` SUCCESS.

---

### Task 3: Architecture hardening

**File:**
- `apps/api/src/test/java/io/github/trueruslan/zakupgotov/pantry/PantryArchitectureTest.java`

Rules:

- production `pantry` package must exist;
- direct project dependencies from `pantry` may target only `shopping`;
- no `pantry` dependency on Recipe, WeeklyPlan, preview/comparison, retailer/provider, matching/basket, database or Spring packages;
- accepted `shopping`, `recipe` and `weeklyplan` packages must not depend on `pantry` in M3.5.1.

- [x] Add ArchUnit characterization/hardening tests after the TDD core exists.
- [ ] Confirm full API CI / Maven `verify` SUCCESS on exact architecture-test head `2066135d8a275feb78904bba71fec0dce7cf9625`.

---

### Task 4: Full verification and shipping evidence

**File:**
- `docs/superpowers/plans/2026-08-15-m3-5-1-pantry-subtraction-semantics-shipping.md`

- [ ] Confirm architecture gate.
- [ ] Confirm final scope contains no endpoint/OpenAPI/generated client/web/database/provider/retailer changes and no accepted M3.1–M3.4 production edits.
- [ ] Record RED→GREEN evidence and exact changed files.
- [ ] Require all 9 normal PR workflow groups SUCCESS on the exact final head, with failure count 0.
- [ ] Perform read-only review; resolve any P0–P3 findings before merge.
- [ ] Mark PR ready only after exact-head CI/review gate.
- [ ] Squash-merge with expected-head protection.
- [ ] Require all normal `main` push workflows SUCCESS before declaring implementation accepted.

## Self-review

- Spec coverage: matching, arithmetic, identity, ordering, provenance, immutability, architecture and non-goals all map to Tasks 1–4.
- Tooling correction: this project uses Maven/API CI, not Gradle; all executable commands above match the repository CI toolchain.
- Scope remains one independently testable subsystem: pure Pantry subtraction semantics only.