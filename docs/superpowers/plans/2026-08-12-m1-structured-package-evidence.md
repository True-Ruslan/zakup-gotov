# M1 Structured Package Evidence Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Carry optional trusted structured package quantity from provider observations into immutable snapshots and project it into the existing basket `PackageQuantitySet` without any presentation-text inference.

**Architecture:** Extend the existing provider trust boundary with optional canonical `Quantity` evidence while preserving the legacy constructor as empty evidence. `OfferSnapshot` copies that value unchanged. Basket owns the one-way projection from snapshots into existing `PackageQuantityBinding` records; the planner itself stays unchanged.

**Tech Stack:** Java 25, Spring Boot 4.1, JUnit 5, AssertJ, Spring Modulith, ArchUnit, Maven Wrapper.

## Global Constraints

- No parsing of `productName`, title, slug, SKU, source URL, visible free-form text, or arbitrary HTML for package size.
- No new live retailer requests.
- No response-body interception or broader browser-extension permissions.
- No retailer production-access status changes.
- Ordinary CI remains live-retailer-free.
- Existing callers of the 11-argument `ObservedOffer` constructor remain source-compatible and receive empty package evidence.
- Present package values use the existing canonical positive `shopping.Quantity` semantics.

---

### Task 1: Define provider/snapshot package evidence by test

**Files:**
- Modify: `apps/api/src/test/java/io/github/trueruslan/zakupgotov/provider/ObservedOfferTest.java`
- Modify: `apps/api/src/test/java/io/github/trueruslan/zakupgotov/provider/OfferSnapshotTest.java`
- Modify: `apps/api/src/main/java/io/github/trueruslan/zakupgotov/provider/ObservedOffer.java`
- Modify: `apps/api/src/main/java/io/github/trueruslan/zakupgotov/provider/OfferSnapshot.java`

**Interfaces:**
- Produces: `ObservedOffer.packageQuantity(): Optional<Quantity>`
- Produces: compatibility constructor with the existing 11 arguments and implicit `Optional.empty()`
- Produces: `OfferSnapshot.packageQuantity(): Optional<Quantity>`

- [ ] **Step 1: Write failing provider tests**

Add tests that construct an observation with explicit `new Quantity(new BigDecimal("0.97"), QuantityUnit.LITER)` and assert it is canonicalized/preserved as `970 MILLILITER`. Add a separate regression observation named `"Молоко 3,2%, 970мл"` through the legacy constructor and assert `packageQuantity()` is empty.

Expected API shape:

```java
var offer = new ObservedOffer(
        RetailerId.PEREKRESTOK,
        "perekrestok-browser",
        AcquisitionMode.BROWSER_BRIDGE,
        "656",
        "3431579",
        "Молоко 3,2%, 970мл",
        new BigDecimal("89.99"),
        "RUB",
        AvailabilityStatus.UNKNOWN,
        OBSERVED_AT,
        "https://www.perekrestok.ru/cat/114/p/moloko-3431579",
        Optional.of(new Quantity(new BigDecimal("0.97"), QuantityUnit.LITER)));

assertThat(offer.packageQuantity())
        .contains(new Quantity(new BigDecimal("970"), QuantityUnit.MILLILITER));
```

- [ ] **Step 2: Write failing snapshot tests**

Create observation-only and provider-updated snapshots from the explicit package observation and assert both expose the exact canonical optional quantity. Also assert the legacy title-only offer produces an empty snapshot package quantity.

- [ ] **Step 3: Run focused tests to verify RED**

Run:

```bash
cd apps/api
./mvnw -q -Dtest=ObservedOfferTest,OfferSnapshotTest test
```

Expected: compilation/test failure because the package-aware constructor/accessors do not exist yet.

- [ ] **Step 4: Implement minimal provider boundary**

Change the record to:

```java
public record ObservedOffer(
        RetailerId retailerId,
        String sourceProviderId,
        AcquisitionMode sourceMode,
        String fulfillmentContextId,
        String sku,
        String productName,
        BigDecimal price,
        String currencyCode,
        AvailabilityStatus availability,
        Instant observedAt,
        String sourceReference,
        Optional<Quantity> packageQuantity) {
```

Validate the optional itself as non-null and copy/map it without parsing strings. Add an 11-argument compatibility constructor delegating to `Optional.empty()`.

Add an `Optional<Quantity>` field/accessor to `OfferSnapshot` and assign `observation.packageQuantity()` in its private constructor.

- [ ] **Step 5: Run focused provider tests to verify GREEN**

Run the same Maven command. Expected: PASS.

- [ ] **Step 6: Commit provider/snapshot boundary**

Commit message:

```text
feat(provider): preserve structured package evidence
```

---

### Task 2: Project snapshot evidence into basket package bindings

**Files:**
- Modify: `apps/api/src/test/java/io/github/trueruslan/zakupgotov/basket/PackageQuantitySetTest.java`
- Modify: `apps/api/src/main/java/io/github/trueruslan/zakupgotov/basket/PackageQuantitySet.java`

**Interfaces:**
- Consumes: `OfferSnapshot.packageQuantity(): Optional<Quantity>`
- Produces: `PackageQuantitySet.fromSnapshots(List<OfferSnapshot>)`

- [ ] **Step 1: Write failing projection test**

Build three snapshots in stable order:

1. explicit `500 GRAM` package evidence;
2. no package evidence despite a title containing `1,5л`;
3. explicit `2 PIECE` package evidence.

Assert:

```java
var set = PackageQuantitySet.fromSnapshots(List.of(first, second, third));

assertThat(set.bindings()).extracting(PackageQuantityBinding::snapshotId)
        .containsExactly(first.id(), third.id());
assertThat(set.quantityFor(first.id()))
        .contains(new Quantity(new BigDecimal("500"), QuantityUnit.GRAM));
assertThat(set.quantityFor(second.id())).isEmpty();
assertThat(set.quantityFor(third.id()))
        .contains(new Quantity(new BigDecimal("2"), QuantityUnit.PIECE));
```

Add null-list and null-snapshot fail-closed assertions.

- [ ] **Step 2: Run focused basket test to verify RED**

Run:

```bash
cd apps/api
./mvnw -q -Dtest=PackageQuantitySetTest test
```

Expected: compilation failure because `fromSnapshots` does not exist.

- [ ] **Step 3: Implement minimal projection factory**

Add:

```java
public static PackageQuantitySet fromSnapshots(List<OfferSnapshot> snapshots) {
    var input = Objects.requireNonNull(snapshots, "snapshots must not be null");
    var bindings = new ArrayList<PackageQuantityBinding>();
    for (var snapshot : input) {
        Objects.requireNonNull(snapshot, "snapshot must not be null");
        snapshot.packageQuantity().ifPresent(quantity ->
                bindings.add(new PackageQuantityBinding(snapshot.id(), quantity)));
    }
    return of(bindings);
}
```

Do not inspect any other snapshot field.

- [ ] **Step 4: Run focused basket test to verify GREEN**

Run the same Maven command. Expected: PASS.

- [ ] **Step 5: Run basket planner regressions**

Run:

```bash
cd apps/api
./mvnw -q -Dtest=PackageQuantitySetTest,PackageSelectionCalculatorTest,SingleStoreBasketPlannerTest test
```

Expected: PASS, preserving `PACKAGE_QUANTITY_UNKNOWN` for absent evidence.

- [ ] **Step 6: Commit basket projection**

Commit message:

```text
feat(basket): project package evidence from snapshots
```

---

### Task 3: Verify architecture and source compatibility

**Files:**
- Test existing: `apps/api/src/test/java/io/github/trueruslan/zakupgotov/ApplicationArchitectureTest.java`
- Test existing: `apps/api/src/test/java/io/github/trueruslan/zakupgotov/basket/BasketBoundaryArchitectureTest.java`
- Test existing: provider/matching/preview regression suites

**Interfaces:**
- Confirms provider -> canonical quantity introduces no Modulith cycle.
- Confirms basket remains downstream of provider/shopping.

- [ ] **Step 1: Run provider/basket/matching/preview regression set**

```bash
cd apps/api
./mvnw -q -Dtest='ObservedOffer*,OfferSnapshot*,ProviderPath*,PackageQuantitySetTest,PackageSelectionCalculatorTest,SingleStoreBasketPlannerTest,DeterministicProductMatcherTest,ComparisonPreview*' test
```

Expected: PASS.

- [ ] **Step 2: Run application architecture verification**

```bash
cd apps/api
./mvnw -q -Dtest=ApplicationArchitectureTest,BasketBoundaryArchitectureTest test
```

Expected: PASS with no new module cycle or upstream basket dependency.

- [ ] **Step 3: Run complete API verification**

```bash
cd apps/api
./mvnw -q verify
```

Expected: PASS.

- [ ] **Step 4: Commit any test-only hardening if required**

If no additional hardening is required, do not create an empty commit. If a real regression test is added, use:

```text
test(package): harden structured evidence boundary
```

---

### Task 4: Synchronize durable project state and open review PR

**Files:**
- Modify: `docs/PROJECT_STATE.md`
- Modify: `docs/ROADMAP.md`
- Modify: `CHANGELOG.md`

**Interfaces:**
- Documents this slice as evidence plumbing, not a retailer extraction claim.

- [ ] **Step 1: Update durable docs**

Record that provider/snapshot/basket plumbing now accepts trusted structured package quantity, while every currently accepted retailer path still remains package-unknown until a separate source-specific extractor is proven.

The roadmap next action must become: prove the first retailer/source structured package field and add its extractor without widening browser permissions or parsing names.

- [ ] **Step 2: Commit documentation**

Commit message:

```text
docs(state): record structured package evidence plumbing
```

- [ ] **Step 3: Open a draft PR**

Title:

```text
feat(m1): add structured package evidence plumbing
```

PR body must explicitly state:

- no retailer extractor is claimed;
- no live requests or production activation were added;
- presentation-text parsing remains forbidden;
- accepted retailer paths remain package-unknown until source-specific evidence exists.

- [ ] **Step 4: Verify exact-head CI/security gate**

Require API CI, Web CI/E2E, Contract CI, Retailer Bridge CI, CodeQL, Dependency Review, Container Security CI, Release Bundle CI and Release Contract CI all to succeed on the exact PR head.

- [ ] **Step 5: Run independent read-only change review**

Review spec compliance, constructor compatibility, package-evidence semantics, architecture direction, privacy/security and regression coverage. Do not merge if P0/P1/P2 blockers remain.

- [ ] **Step 6: If review and exact-head CI pass, mark ready and squash merge**

Use exact-head protection and then verify push-triggered `main` CI before considering the slice accepted.
