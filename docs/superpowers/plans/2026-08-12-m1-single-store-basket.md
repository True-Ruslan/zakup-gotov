# M1 Single-Store Basket Comparison Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development or superpowers:executing-plans task-by-task.

**Goal:** Build a deterministic single-store basket quote for one retailer + fulfillment context, using explicit package-quantity evidence and preserving complete/uncertain/incomplete semantics.

**Architecture:** Basket depends on shopping, provider snapshots and matching. Provider/shopping/matching/retailer remain upstream and must never depend back on basket. Package quantity evidence is basket-scoped and keyed by `OfferSnapshotId`; absence means unknown. No package data is inferred from product names.

**Design:** [`../specs/2026-08-12-m1-single-store-basket-design.md`](../specs/2026-08-12-m1-single-store-basket-design.md)

**Tech Stack:** Java 25, JUnit 5, AssertJ, ArchUnit, Maven 3.9.16.

## Global constraints

- TDD RED→GREEN for each behavioral slice.
- Ordinary CI stays fully offline from live retailers.
- No package-size parsing from `productName`.
- No assumption that one matched SKU satisfies one requirement.
- No semantic tie-breaking of ambiguous matches.
- `UNKNOWN` availability remains uncertainty, not availability.
- No partial basket total for an incomplete quote.
- No delivery/minimum-order/loyalty assumptions.
- No persistence/API/UI/multi-store ranking.

---

## Task 1 — typed package-quantity evidence

**Files:**
- Create `apps/api/src/main/java/io/github/trueruslan/zakupgotov/basket/PackageQuantityBinding.java`
- Create `apps/api/src/main/java/io/github/trueruslan/zakupgotov/basket/PackageQuantitySet.java`
- Create `apps/api/src/test/java/io/github/trueruslan/zakupgotov/basket/PackageQuantitySetTest.java`

**Contracts:**

```java
public record PackageQuantityBinding(
        OfferSnapshotId snapshotId,
        Quantity packageQuantity) {}
```

```java
public final class PackageQuantitySet {
    public static PackageQuantitySet of(List<PackageQuantityBinding> bindings);
    public Optional<Quantity> quantityFor(OfferSnapshotId snapshotId);
    public List<PackageQuantityBinding> bindings();
}
```

- [ ] RED: write tests for known lookup, kg/l canonicalization through `Quantity`, absence→empty, duplicate snapshot rejection, null rejection and immutable/stable bindings.
- [ ] Commit RED and run full Maven `verify`; expected failure only on absent basket evidence types.
- [ ] GREEN: implement immutable list + indexed lookup using stable insertion order; duplicate IDs fail closed.
- [ ] Run full Maven `verify` and commit GREEN with RED tests unchanged.

---

## Task 2 — package selection and item resolution

**Files:**
- Create `BasketItemResolutionStatus.java`
- Create `PackageSelection.java`
- Create `BasketItemResolution.java`
- Create `BasketTotal.java`
- Create tests initially in `SingleStoreBasketPlannerTest.java`

**Status values:**

```java
FULFILLED,
AVAILABILITY_UNKNOWN,
UNMATCHED,
AMBIGUOUS,
UNAVAILABLE,
PACKAGE_QUANTITY_UNKNOWN,
QUANTITY_UNIT_MISMATCH
```

**PackageSelection:**

```java
public record PackageSelection(
        OfferSnapshot snapshot,
        Quantity packageQuantity,
        BigInteger packageCount,
        Quantity providedQuantity,
        BigDecimal lineTotal) {}
```

Structural invariants:
- package count > 0;
- package/provided units equal;
- provided amount = package amount * package count;
- line total = snapshot price * package count;
- all values nonnull/nonnegative as appropriate.

**BasketItemResolution:**

```java
public record BasketItemResolution(
        ShoppingItem item,
        ProductMatchResult match,
        BasketItemResolutionStatus status,
        Optional<PackageSelection> selection) {}
```

Invariants:
- `FULFILLED` and `AVAILABILITY_UNKNOWN`: matcher `MATCHED`, exactly one selection;
- all failure states: no selection;
- `UNMATCHED` ↔ matcher `UNMATCHED`;
- `AMBIGUOUS` ↔ matcher `AMBIGUOUS`;
- remaining failure states require matcher `MATCHED`.

- [ ] RED: through planner-facing tests require 750g/500g=2, 1kg/400g=3, 7 pieces/6=2, exact size=1, correct provided quantity and line total.
- [ ] RED: require explicit unavailable/package-unknown/unit-mismatch/unknown-availability item statuses.
- [ ] Commit RED and confirm failure only on absent basket result/planner types.
- [ ] GREEN: implement minimal value/result invariants and item-selection math.
- [ ] Run full Maven `verify` and commit GREEN.

---

## Task 3 — single-store basket planner/quote

**Files:**
- Create `BasketQuoteStatus.java`
- Create `SingleStoreBasketQuote.java`
- Create `SingleStoreBasketPlanner.java`
- Complete `SingleStoreBasketPlannerTest.java`

**Planner API:**

```java
public final class SingleStoreBasketPlanner {
    public SingleStoreBasketQuote quote(
            MatchScope scope,
            ShoppingList shoppingList,
            List<OfferSnapshot> candidates,
            PackageQuantitySet packageQuantities) { ... }
}
```

**Basket quote:**

```java
public record SingleStoreBasketQuote(
        MatchScope scope,
        ShoppingListId shoppingListId,
        BasketQuoteStatus status,
        List<BasketItemResolution> items,
        Optional<BasketTotal> total) {}
```

Status rules:
- `COMPLETE`: every item `FULFILLED`, total required;
- `UNCERTAIN`: all items selected, at least one `AVAILABILITY_UNKNOWN`, total required;
- `INCOMPLETE`: at least one failure-state item, total must be empty.

Planner algorithm per item:
1. matcher.match(scope, item.requirement(), candidates);
2. UNMATCHED/AMBIGUOUS → corresponding resolution;
3. selected candidate `UNAVAILABLE` → `UNAVAILABLE`;
4. no package quantity → `PACKAGE_QUANTITY_UNKNOWN`;
5. canonical unit mismatch → `QUANTITY_UNIT_MISMATCH`;
6. calculate ceil package count, provided quantity, line total;
7. `UNKNOWN` availability → `AVAILABILITY_UNKNOWN`; otherwise `FULFILLED`.

Basket aggregation:
- preserve shopping item order;
- reject empty shopping list;
- COMPLETE/UNCERTAIN sum all line totals;
- selected currencies must be identical, otherwise fail closed;
- INCOMPLETE total empty even if some lines are priced.

- [ ] RED: multi-item complete basket + stable order + total.
- [ ] RED: uncertain basket has total but never COMPLETE.
- [ ] RED: unmatched/ambiguous/unavailable/package-unknown/unit-mismatch each make basket INCOMPLETE with no total.
- [ ] RED: mixed selected currencies fail closed; empty shopping list rejected; returned item list immutable.
- [ ] Commit RED and run Maven verify.
- [ ] GREEN: implement minimal planner/quote aggregation.
- [ ] Run full Maven verify and commit GREEN.

---

## Task 4 — architecture, docs and shipping

**Files:**
- Create `apps/api/src/test/java/io/github/trueruslan/zakupgotov/basket/BasketBoundaryArchitectureTest.java`
- Modify `docs/PROJECT_STATE.md`
- Modify `docs/ROADMAP.md`
- Modify root `CHANGELOG.md`
- Modify this plan with actual evidence.

Architecture rule:

```java
noClasses()
    .that().resideInAnyPackage("..provider..", "..shopping..", "..matching..", "..retailer..")
    .should().dependOnClassesThat().resideInAPackage("..basket..");
```

- [ ] Add architecture contract and run full Maven verify.
- [ ] Synchronize durable docs; next M1 focus becomes failure/coverage/freshness product/API/UX boundary before critical browser E2E.
- [ ] Run exact-head full repository gate: API, Contract, Web/E2E, CodeQL Java+JS/TS, Dependency Review, Retailer Bridge, Container Security API+Web, Release Bundle, Release Contract.
- [ ] Perform read-only Change Review. Verify no inferred package data, no partial total presented as complete, no ambiguity tie-break, no `UNKNOWN`→available coercion, no reverse dependencies.
- [ ] Record shipping evidence in this plan, rerun marker-head branch protection.
- [ ] Mark PR ready and squash merge with expected-head SHA guard.
