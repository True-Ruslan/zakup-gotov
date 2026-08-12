# M1 Deterministic Product Matching Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Preserve human product labels through the validated provider/snapshot pipeline and add a conservative deterministic exact/normalized matcher for one retailer + fulfillment context.

**Architecture:** `ObservedOffer` remains the fail-closed provider trust boundary and gains required `productName`; `OfferSnapshot` preserves that validated value. A new `matching` package owns all matching normalization and decision logic, consuming `ShoppingRequirement` + scoped `OfferSnapshot` candidates while never influencing provider ingestion. Exact text outranks matching-only normalized text; ambiguity is preserved rather than broken by price, availability, freshness, provider priority or SKU ordering.

**Tech Stack:** Java 25, JUnit 5, AssertJ, ArchUnit/Spring Modulith verification, Maven 3.9.16.

## Global Constraints

- TDD is mandatory: RED test commit before each behavior implementation, then minimal GREEN with the RED tests unchanged.
- Ordinary CI must make no live retailer requests.
- `productName` must be nonblank and originate from observed provider evidence; no query/SKU-derived synthetic label.
- Browser bridge already supplies `productName`; this slice does not broaden browser permissions or acquisition behavior.
- Matching-specific Unicode/case/punctuation normalization belongs only to the `matching` package.
- Match operations are scoped to exactly one retailer and one fulfillment context.
- No fuzzy/edit-distance, substring, stemming, token reordering, synonyms, transliteration, embeddings or LLM matching.
- Price, availability, freshness, acquisition mode and SKU ordering must not resolve semantic ambiguity.
- No persistence, REST API, UI, catalog canonical identity or basket optimization in this slice.

---

### Task 1: Preserve validated product labels through provider observations and snapshots

**Files:**
- Modify: `apps/api/src/main/java/io/github/trueruslan/zakupgotov/provider/ObservedOffer.java`
- Modify: `apps/api/src/main/java/io/github/trueruslan/zakupgotov/provider/OfferSnapshot.java`
- Modify: existing provider tests/fixture fakes that construct `ObservedOffer`
- Test: `apps/api/src/test/java/io/github/trueruslan/zakupgotov/provider/ObservedOfferTest.java`
- Test: `apps/api/src/test/java/io/github/trueruslan/zakupgotov/provider/OfferSnapshotTest.java`

**Interfaces:**
- Produces: `ObservedOffer.productName(): String`
- Produces: `OfferSnapshot.productName(): String`
- `ObservedOffer` constructor adds `String productName` immediately after `sku`.
- `OfferSnapshot` public factories keep the same signatures; the private constructor copies `observation.productName()`.

- [ ] **Step 1: Write RED provider-label tests**

Add assertions requiring:

```java
var offer = new ObservedOffer(
        RetailerId.PYATEROCHKA,
        "pyaterochka-browser",
        AcquisitionMode.BROWSER_BRIDGE,
        "store-42",
        "sku-1",
        "  Молоко Простоквашино 3,2%  ",
        new BigDecimal("99.90"),
        "RUB",
        AvailabilityStatus.UNKNOWN,
        Instant.parse("2026-08-12T08:30:00Z"),
        "https://5ka.ru/product/sku-1");

assertThat(offer.productName()).isEqualTo("Молоко Простоквашино 3,2%");
```

and:

```java
assertThatThrownBy(() -> new ObservedOffer(
        RetailerId.PYATEROCHKA,
        "pyaterochka-browser",
        AcquisitionMode.BROWSER_BRIDGE,
        "store-42",
        "sku-1",
        "   ",
        new BigDecimal("99.90"),
        "RUB",
        AvailabilityStatus.UNKNOWN,
        Instant.parse("2026-08-12T08:30:00Z"),
        "https://5ka.ru/product/sku-1"))
    .isInstanceOf(IllegalArgumentException.class)
    .hasMessageContaining("productName");
```

In `OfferSnapshotTest`, construct an observation with a product name and require both `observationOnly(...)` and `withProviderUpdatedAt(...)` snapshots to expose that exact validated name.

- [ ] **Step 2: Commit RED and run Maven verify**

Run: `cd apps/api && ./mvnw --batch-mode --no-transfer-progress verify`

Expected: `testCompile` failure because production `ObservedOffer` / `OfferSnapshot` do not yet expose the new product-name constructor/accessor contract. No unrelated test failures should be accepted as RED evidence.

Commit message: `test(provider): require product labels in offer snapshots`

- [ ] **Step 3: Implement minimal product-label preservation**

In `ObservedOffer`:

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
        String sourceReference) {

    public ObservedOffer {
        // existing validation unchanged
        productName = requireText(productName, "productName");
    }
}
```

Do not lowercase or remove punctuation here.

In `OfferSnapshot`, add a final `String productName`, private-constructor parameter/accessor, and copy `observation.productName()` from both public factories.

- [ ] **Step 4: Migrate existing provider fixtures/tests**

For every existing Java `ObservedOffer` constructor, add a truthful deterministic fixture label. Use labels that describe the existing fixture/SKU; never use the search query as implicit production behavior. Test fakes may use explicit values such as `"Молоко 3,2%"`, `"Хлеб ржаной"`, or the product title already present in the fixture.

Keep all existing provenance/trust assertions unchanged.

- [ ] **Step 5: Run full Maven verify and commit GREEN**

Run: `cd apps/api && ./mvnw --batch-mode --no-transfer-progress verify`

Expected: PASS.

Commit message: `feat(provider): preserve product labels in offer snapshots`

---

### Task 2: Add deterministic matching-only text normalization

**Files:**
- Create: `apps/api/src/main/java/io/github/trueruslan/zakupgotov/matching/MatchTextNormalizer.java`
- Create: `apps/api/src/test/java/io/github/trueruslan/zakupgotov/matching/MatchTextNormalizerTest.java`

**Interfaces:**
- Produces: `static String MatchTextNormalizer.normalize(String raw)`
- The method is matching-layer behavior and must not be reused by `shopping` or `provider` production code in this slice.

- [ ] **Step 1: Write RED normalization tests**

Required examples:

```java
assertThat(MatchTextNormalizer.normalize("  МОЛОКО\t3,2%  "))
        .isEqualTo("молоко 3 2");

assertThat(MatchTextNormalizer.normalize("Ёжик ёлка"))
        .isEqualTo("ежик елка");

assertThat(MatchTextNormalizer.normalize("Молоко－ультра"))
        .isEqualTo("молоко ультра");

assertThat(MatchTextNormalizer.normalize("ＡＢＣ Молоко"))
        .isEqualTo("abc молоко");
```

Also require null rejection and blank-after-normalization rejection.

Add a regression proving the normalizer does not create synonym/stemming equivalence by asserting, for example, that normalized `"томаты"` is not equal to normalized `"помидоры"` and normalized `"молоко"` is not equal to normalized `"молочный"`.

- [ ] **Step 2: Commit RED and run Maven verify**

Expected: `testCompile` fails only because `MatchTextNormalizer` does not exist.

Commit message: `test(matching): define deterministic text normalization`

- [ ] **Step 3: Implement minimal normalizer**

Algorithm:

```java
Objects.requireNonNull(raw, "raw must not be null");
var normalized = Normalizer.normalize(raw, Normalizer.Form.NFKC)
        .toLowerCase(Locale.ROOT)
        .replace('ё', 'е');

var builder = new StringBuilder(normalized.length());
var previousWasSeparator = true;
for (int offset = 0; offset < normalized.length(); ) {
    int codePoint = normalized.codePointAt(offset);
    offset += Character.charCount(codePoint);

    if (Character.isLetterOrDigit(codePoint)) {
        builder.appendCodePoint(codePoint);
        previousWasSeparator = false;
    } else if (!previousWasSeparator) {
        builder.append(' ');
        previousWasSeparator = true;
    }
}
var result = builder.toString().strip();
if (result.isBlank()) {
    throw new IllegalArgumentException("normalized text must not be blank");
}
return result;
```

- [ ] **Step 4: Run Maven verify and commit GREEN**

Expected: PASS.

Commit message: `feat(matching): add deterministic text normalization`

---

### Task 3: Add scoped exact/normalized product matcher and explicit result model

**Files:**
- Create: `apps/api/src/main/java/io/github/trueruslan/zakupgotov/matching/MatchScope.java`
- Create: `apps/api/src/main/java/io/github/trueruslan/zakupgotov/matching/ProductMatchStatus.java`
- Create: `apps/api/src/main/java/io/github/trueruslan/zakupgotov/matching/ProductMatchStrength.java`
- Create: `apps/api/src/main/java/io/github/trueruslan/zakupgotov/matching/ProductMatchReason.java`
- Create: `apps/api/src/main/java/io/github/trueruslan/zakupgotov/matching/ProductMatchResult.java`
- Create: `apps/api/src/main/java/io/github/trueruslan/zakupgotov/matching/DeterministicProductMatcher.java`
- Create: `apps/api/src/test/java/io/github/trueruslan/zakupgotov/matching/DeterministicProductMatcherTest.java`

**Interfaces:**

```java
public record MatchScope(RetailerId retailerId, String fulfillmentContextId) {}
```

```java
public enum ProductMatchStatus { MATCHED, AMBIGUOUS, UNMATCHED }
public enum ProductMatchStrength { EXACT, NORMALIZED, NONE }
public enum ProductMatchReason {
    SINGLE_EXACT_TEXT_MATCH,
    MULTIPLE_EXACT_TEXT_MATCHES,
    SINGLE_NORMALIZED_TEXT_MATCH,
    MULTIPLE_NORMALIZED_TEXT_MATCHES,
    NO_TEXT_MATCH
}
```

```java
public record ProductMatchResult(
        ProductMatchStatus status,
        ProductMatchStrength strength,
        ProductMatchReason reason,
        List<OfferSnapshot> candidates) {}
```

```java
public final class DeterministicProductMatcher {
    public ProductMatchResult match(
            MatchScope scope,
            ShoppingRequirement requirement,
            List<OfferSnapshot> candidates) { ... }
}
```

- [ ] **Step 1: Write RED result-model and matcher tests**

Cover all required cases:

1. one exact name → `MATCHED / EXACT / SINGLE_EXACT_TEXT_MATCH`;
2. exact match outranks a different candidate that only becomes equal after normalization;
3. two exact names → `AMBIGUOUS / EXACT / MULTIPLE_EXACT_TEXT_MATCHES`;
4. no exact, one normalized match → `MATCHED / NORMALIZED`;
5. no exact, two normalized matches → `AMBIGUOUS / NORMALIZED`;
6. no matches → `UNMATCHED / NONE / NO_TEXT_MATCH`;
7. input candidate order preserved among ambiguous candidates;
8. returned candidates immutable;
9. cheaper/more-available/fresher candidate does not break semantic ambiguity;
10. candidate from another retailer throws `IllegalArgumentException` containing `retailer`;
11. candidate from another fulfillment context throws `IllegalArgumentException` containing `fulfillmentContextId`;
12. null scope/requirement/candidate list and null candidate element fail closed.

Use deterministic `OfferSnapshot` fixtures created from valid `ObservedOffer` values; do not mock snapshot fields.

- [ ] **Step 2: Commit RED and run Maven verify**

Expected: `testCompile` fails only on absent matching result/matcher types.

Commit message: `test(matching): define scoped deterministic matching contract`

- [ ] **Step 3: Implement scope/result invariants**

`MatchScope` validates non-null retailer and nonblank fulfillment context.

`ProductMatchResult` canonical constructor:
- `List.copyOf(candidates)`;
- `MATCHED` requires exactly one candidate and strength not `NONE`;
- `AMBIGUOUS` requires at least two candidates and strength not `NONE`;
- `UNMATCHED` requires zero candidates and strength `NONE`;
- reject null status/strength/reason/candidates.

- [ ] **Step 4: Implement deterministic matcher**

Pseudo-code:

```java
validateAllCandidatesBelongTo(scope, candidates);

var exact = candidates.stream()
        .filter(snapshot -> snapshot.productName().equals(requirement.text()))
        .toList();
if (exact.size() == 1) return matchedExact(exact.getFirst());
if (exact.size() > 1) return ambiguousExact(exact);

var normalizedRequirement = MatchTextNormalizer.normalize(requirement.text());
var normalized = candidates.stream()
        .filter(snapshot -> MatchTextNormalizer.normalize(snapshot.productName())
                .equals(normalizedRequirement))
        .toList();
if (normalized.size() == 1) return matchedNormalized(normalized.getFirst());
if (normalized.size() > 1) return ambiguousNormalized(normalized);
return unmatched();
```

Do not sort candidates and do not inspect price, availability, freshness, source mode or SKU for tie-breaking.

- [ ] **Step 5: Run Maven verify and commit GREEN**

Expected: PASS.

Commit message: `feat(matching): add scoped deterministic product matcher`

---

### Task 4: Lock architecture boundaries, synchronize durable docs and ship

**Files:**
- Create: `apps/api/src/test/java/io/github/trueruslan/zakupgotov/matching/MatchingBoundaryArchitectureTest.java`
- Modify: `docs/PROJECT_STATE.md`
- Modify: `docs/ROADMAP.md`
- Modify: `CHANGELOG.md`
- Modify: `docs/superpowers/plans/2026-08-12-m1-deterministic-matching.md`

**Interfaces:**
- Architecture rule protects upstream packages from reverse dependency on `matching`.
- After merge, next active M1 focus becomes complete single-store basket comparison/package selection baseline.

- [ ] **Step 1: Write architecture RED/contract test**

Use ArchUnit to assert production classes in `..provider..`, `..shopping..`, and `..retailer..` do not depend on classes in `..matching..`.

Because the GREEN implementation should already respect this rule, introduce the test after Task 3 and require it to pass immediately; if it fails, treat the dependency as a real architecture defect and refactor before shipping.

- [ ] **Step 2: Run full Maven verify**

Run: `cd apps/api && ./mvnw --batch-mode --no-transfer-progress verify`

Expected: PASS.

- [ ] **Step 3: Synchronize durable docs**

`PROJECT_STATE.md`:
- mark Slice 6 deterministic matching COMPLETE;
- record product-name preservation prerequisite;
- record exact/normalized scoped semantics and ambiguity behavior;
- move active focus to single-store basket comparison.

`ROADMAP.md`:
- mark matching COMPLETE;
- move basket comparison/package selection NEXT.

`CHANGELOG.md`:
- add provider product-name preservation;
- add deterministic matching result model/normalizer/scope;
- state no hidden price/availability tie-break and no AI/fuzzy matching.

Update this plan with actual RED/GREEN commit SHAs and mark implementation steps complete while leaving final shipping gate pending until it is proven.

- [ ] **Step 4: Run final exact-head repository gate**

Require all current repository gates on the exact final head:
- API CI;
- Contract CI;
- Web CI + Web E2E;
- CodeQL Java + JavaScript/TypeScript;
- Dependency Review;
- Retailer Bridge CI;
- Container Security API + Web;
- Release Bundle CI;
- Release Contract CI.

No failures or in-progress checks may be called complete.

- [ ] **Step 5: Perform read-only Change Review**

Review for:
- label provenance not synthesized from query/SKU;
- no matching normalization in provider/shopping models;
- no cross-retailer/context mixing;
- no semantic ambiguity tie-break by price/availability/freshness/source mode/SKU;
- no fuzzy/AI behavior hidden in baseline;
- no reverse provider/shopping/retailer dependency on matching;
- docs match actual code/gates.

Blocking P0/P1/P2 findings must be fixed with a regression test before merge.

- [ ] **Step 6: Record final shipping evidence and re-run marker-head gate**

Update only this plan with the proven exact-head gate and review verdict, then require branch-protection checks to pass again on that docs-only marker head.

- [ ] **Step 7: Mark PR ready and squash merge with expected-head SHA guard**

Use squash merge only after the marker head is fully green and unchanged.
