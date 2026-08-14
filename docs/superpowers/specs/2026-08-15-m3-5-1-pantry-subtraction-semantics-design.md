# M3.5.1 Pantry Subtraction Semantics — Design

Date: 2026-08-15  
Status: **APPROVED FOR IMPLEMENTATION**  
Issue: #121  
Baseline: `e11fd532c8d1f927a14cb886abaa9e9988f9b21b`

## Goal

Introduce the first Pantry / exclusions capability as a pure, deterministic domain layer that subtracts household pantry availability from an already canonical `ShoppingList` while preserving item identity, source order and explicit audit evidence.

M3.5.1 deliberately stops before transport, WeeklyPlan/Comparison integration, browser UI or persistence. Its purpose is to make the subtraction semantics independently testable and reusable before any API contract exposes them.

## Placement

Accepted flow before M3.5:

`Recipe → canonical ShoppingList → WeeklyPlan composition → Comparison Preview`

M3.5.1 adds a pure boundary that can later be composed as:

`canonical ShoppingList → Pantry adjustment → remaining ShoppingList → Comparison Preview`

The Pantry package must depend only on the accepted `shopping` package. It must not depend on Recipe, WeeklyPlan, preview/comparison, retailer/provider, Spring or persistence packages.

## Alternatives considered

### A. Subtract pantry at Recipe / ingredient level

Rejected. This would alter accepted M3.1/M3.2 serving scale, merge and provenance semantics and could cause the same pantry stock to be consumed separately by multiple Recipe occurrences before canonical aggregation.

### B. Dedicated pure ShoppingList adjustment layer

**Selected.** It operates on the canonical Shopping vocabulary after all Recipe/WeeklyPlan aggregation and before retailer comparison. It has one responsibility: household stock subtraction plus audit evidence.

### C. Subtract inside retailer comparison

Rejected. Household inventory is not retailer evidence. Coupling it to comparison would contaminate basket/ranking semantics and make pantry behavior retailer-dependent.

## Domain vocabulary

### `PantryItem`

A positive amount of an exact shopping requirement already available at home:

- `ShoppingRequirement requirement`
- `Quantity quantity`

`Quantity` remains authoritative for validation and unit canonicalization. Therefore `1 kg` enters Pantry semantics as `1000 g`, and `1 l` as `1000 ml`, exactly as in existing Shopping/Recipe semantics.

Pantry entries have no persistence identity in M3.5.1. Their purpose is input stock, not a saved inventory model.

### Exact match key

Pantry matching uses the same semantic key already used by Recipe aggregation:

`(ShoppingRequirement, canonical QuantityUnit)`

Consequences:

- whitespace normalization comes from `ShoppingRequirement`;
- kg/g and l/ml compatibility comes from `Quantity` canonicalization;
- `PIECE`, `GRAM` and `MILLILITER` remain distinct dimensions;
- matching remains case-sensitive because accepted `ShoppingRequirement` equality is case-sensitive;
- there is no synonym, fuzzy, stemming, transliteration or AI equivalence.

M3.5.1 must not introduce a second ingredient-normalization algorithm.

### `PantryAdjustmentStatus`

Per shopping item:

- `UNCHANGED` — no matching pantry quantity was consumed;
- `PARTIALLY_COVERED` — pantry consumed a positive amount smaller than the requirement;
- `FULLY_COVERED` — pantry consumed the entire requirement and the item is omitted from the remaining ShoppingList.

### `PantryAdjustmentEvidence`

Audit evidence for every source `ShoppingItem`, in source order:

- source `ShoppingItemId`;
- `ShoppingRequirement`;
- original required `Quantity`;
- optional positive pantry-used `Quantity`;
- optional positive remaining `Quantity`;
- `PantryAdjustmentStatus`.

Zero is never encoded as `Quantity`, preserving the accepted invariant that `Quantity.amount > 0`.

Evidence rules:

- `UNCHANGED`: `pantryUsed = empty`, `remaining = required`;
- `PARTIALLY_COVERED`: both values are present and positive;
- `FULLY_COVERED`: `pantryUsed = required`, `remaining = empty`.

Evidence is immutable and must validate these structural invariants.

### `PantryAdjustment`

Result containing:

- `ShoppingList remainingShoppingList`;
- immutable ordered `List<PantryAdjustmentEvidence> evidence`.

The remaining list keeps the source `ShoppingListId`. Every surviving item keeps the source `ShoppingItemId` and `ShoppingRequirement`; only the quantity may decrease. Fully covered items are absent from the remaining list but remain visible in evidence.

## Subtraction algorithm

1. Validate non-null source list and pantry list.
2. Aggregate duplicate pantry entries in insertion order by exact match key, summing canonical amounts.
3. Iterate source `ShoppingList.items()` in source order.
4. For each item, look up currently unconsumed pantry amount for its exact key.
5. Consume `min(required, available)`.
6. Decrement the matching pantry stock so the same household quantity can never cover two source requirements.
7. If consumed is zero, copy the item unchanged to the remaining list and emit `UNCHANGED` evidence.
8. If consumed is less than required, copy the item with the same ID/requirement and a positive reduced `Quantity`; emit `PARTIALLY_COVERED` evidence.
9. If consumed equals required, do not add an item to the remaining list; emit `FULLY_COVERED` evidence.
10. Ignore unmatched or excess pantry stock. M3.5.1 is a shopping-requirement adjustment, not a persisted pantry inventory ledger.

The algorithm never mutates the input `ShoppingList` or caller-owned pantry list.

## Duplicate behavior

Duplicate pantry entries with the same exact key are intentionally additive. This permits callers to describe household stock in multiple rows without changing semantics.

A source ShoppingList normally already has one row per accepted Recipe merge key, but the adjuster must still behave safely for arbitrary valid ShoppingLists containing multiple IDs with the same requirement/unit: pantry stock is allocated sequentially in source order and is consumed only once.

## Identity and provenance

M3.5.1 does not derive new Shopping identities:

- source `ShoppingListId` is preserved;
- surviving `ShoppingItemId` values are preserved;
- fully covered source IDs remain in evidence;
- no Pantry UUID is introduced.

This allows later M3.5 composition to retain the accepted WeeklyPlan ingredient provenance keyed by the same `ShoppingItemId` without rewriting M3.2 identity rules.

## Validation and failure model

Programmer/domain misuse fails immediately:

- null inputs/items are rejected;
- `PantryItem` delegates positive amount/unit validation to accepted Shopping types;
- evidence constructors reject impossible status/value combinations;
- unexpected arithmetic/unit drift is an `IllegalStateException`, not silent coercion.

There is no HTTP validation model in M3.5.1 because no endpoint is introduced.

## Architecture boundary

Create production package:

`io.github.trueruslan.zakupgotov.pantry`

Allowed project dependency:

- `io.github.trueruslan.zakupgotov.shopping`

Forbidden dependencies include:

- `recipe`, `weeklyplan`, `weeklyplanpreview`, `weeklyplancomparisonpreview`;
- `preview`, `comparison`, `basket`, `matching`;
- `retailer`, `provider`;
- `database` / persistence;
- Spring web/framework transport code.

Accepted `shopping`, `recipe` and `weeklyplan` packages must remain independent from `pantry` in this slice.

## Test contract

TDD coverage must include at least:

1. unmatched pantry leaves a source item unchanged;
2. partial coverage reduces quantity while preserving IDs/order;
3. full coverage removes the item from remaining ShoppingList but keeps full evidence;
4. kg/g canonical compatibility;
5. l/ml canonical compatibility;
6. incompatible dimensions do not match;
7. duplicate pantry entries aggregate deterministically;
8. pantry stock is consumed once across duplicate source keys in source order;
9. excess pantry never creates negative/zero Shopping quantities;
10. input ShoppingList and input pantry collection are not mutated;
11. evidence structural invariants reject impossible states;
12. ArchUnit enforces `pantry → shopping only` and prevents reverse dependencies.

## Non-goals

M3.5.1 does **not** add:

- endpoint/OpenAPI/generated-client changes;
- integration into `WeeklyPlanComparisonPreviewService`;
- browser UI;
- saved pantry inventory, history or database schema;
- explicit dietary/medical exclusion rules;
- a boolean “never buy this” rule hidden inside Pantry stock;
- fuzzy/synonym/AI equivalence;
- nutrition/macros;
- retailer/provider changes;
- calendar/time-zone semantics;
- changes to accepted Recipe, Shopping, WeeklyPlan or Comparison algorithms.

Explicit non-stock exclusions, if still needed after Pantry subtraction is accepted, must be designed as a separate semantic rule rather than being overloaded onto a zero/negative Pantry quantity.

## Acceptance decision

Implement M3.5.1 as the dedicated pure `pantry` domain package described above. A later M3.5 slice may expose and compose it with WeeklyPlan/Comparison only after this semantic layer is independently accepted.