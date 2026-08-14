# M2.5 Deterministic Multi-Recipe Aggregation — Design

Date: 2026-08-14  
Issue: #106  
Baseline: `main=3c8483a71124eb9acf94f956a14d9db635286a05`  
Status: **AUTHORITATIVE DESIGN — approved for implementation by delegated development authority**

## Goal

Provide the deterministic Recipe-domain foundation required by M3 Weekly Planning:

`ordered Recipe occurrences + target servings → accepted per-recipe conversion → one canonical aggregated ShoppingList + occurrence-aware provenance`

M2.5 is deliberately a pure domain/core slice. It does not add an HTTP endpoint, generated client, UI, persistence, weekly-plan aggregate, pantry semantics, retailer traffic or AI/fuzzy interpretation.

## Why aggregation entry identity is required

`RecipeId` identifies a Recipe, not one occurrence of that Recipe in a future plan.

A weekly plan may intentionally contain the same Recipe twice, potentially with different target servings. Existing `RecipeIngredientRef(RecipeId, RecipeIngredientId)` cannot distinguish those occurrences.

Introduce:

```text
RecipeAggregationEntryId
RecipeAggregationEntry(
  RecipeAggregationEntryId id,
  Recipe recipe,
  RecipeServings targetServings
)
```

Entry IDs must be unique inside one aggregation request. Repeating the same `RecipeId` under different entry IDs is valid.

## Output

Introduce:

```text
RecipeAggregationIngredientRef(
  RecipeAggregationEntryId entryId,
  RecipeIngredientRef ingredientRef
)

RecipeShoppingListAggregation(
  ShoppingList shoppingList,
  Map<ShoppingItemId, List<RecipeAggregationIngredientRef>> provenance
)
```

The provenance map and every nested list are deeply immutable and ordered.

Every output ShoppingItem must have at least one provenance ref. Every provenance ref must resolve to the Recipe/ingredient of the corresponding aggregation entry.

## Aggregation algorithm

`RecipeShoppingListAggregator.aggregate(entries, aggregateShoppingListId)`:

1. Reject null aggregate list ID, null/empty entries, null entries and duplicate `RecipeAggregationEntryId` values.
2. Preserve caller entry order.
3. For each entry, derive a deterministic internal conversion-list ID from:
   `aggregateShoppingListId + entryId`.
4. Call the accepted `RecipeShoppingListConverter` with that entry's Recipe, target servings and derived internal list ID.
5. Traverse converted ShoppingItems in accepted converter order.
6. Merge only by the same exact key as M2.1:
   normalized `ShoppingRequirement` + canonical `QuantityUnit`.
7. Sum already-scaled canonical amounts with exact `BigDecimal.add`.
8. Preserve first compatible occurrence order across entry order, then converted-item order.
9. Convert each accepted `RecipeIngredientRef` into `RecipeAggregationIngredientRef(entryId, ingredientRef)` and append it in the same order.
10. Create one output `ShoppingItem` per aggregate group using the caller-supplied aggregate ShoppingListId.
11. Derive its ID from aggregate list ID + requirement + canonical unit using the exact same accepted algorithm as M2.1.
12. Fail closed if two different merge keys derive the same ShoppingItem ID.

The aggregator must not recompute serving ratios, canonicalize source ingredient units itself or reinterpret Recipe ingredient meaning.

## Shared internal seams

M2.1 currently keeps the merge-key and default ShoppingItem ID algorithm private inside `RecipeShoppingListConverter`. M2.5 needs identical semantics and must not copy them.

Extract package-private helpers inside `io.github.trueruslan.zakupgotov.recipe`:

```text
RecipeShoppingMergeKey(ShoppingRequirement requirement, QuantityUnit unit)
RecipeShoppingItemIds.derive(ShoppingListId, ShoppingRequirement, QuantityUnit)
```

The converter and aggregator both use these helpers.

This is a refactor only: existing single-recipe generated UUIDs must remain byte-for-byte identical for the same `ShoppingListId + requirement + unit` input.

A package-private aggregator constructor may accept a `RecipeShoppingItemIdDeriver` to preserve deterministic collision testing. Production/default construction uses `RecipeShoppingItemIds::derive`.

## Internal per-entry conversion-list identity

The accepted converter requires a ShoppingListId even though its intermediate ShoppingItem IDs are not exposed by the aggregation result.

Derive that internal ID deterministically with a distinct namespace payload containing:

```text
recipe-aggregation-entry-list
aggregateShoppingListId
entryId
```

using UTF-8 and `UUID.nameUUIDFromBytes`.

Properties:

- no randomness;
- no persistence;
- stable for one aggregate list + entry occurrence;
- distinct from the final aggregate ShoppingListId;
- not part of public/product output;
- changing target servings does not change the intermediate list identity.

## Merge semantics

Automatic merge remains intentionally strict:

- same normalized requirement object/value;
- same canonical quantity unit;
- no case folding beyond existing ShoppingRequirement normalization;
- no synonyms;
- no categories;
- no fuzzy matching;
- no embeddings/LLM;
- no mass/volume/count interchange.

Examples:

- Recipe A `milk 500 ml` + Recipe B `milk 1 l` after accepted canonicalization → same requirement + `MILLILITER`, merge.
- `Milk` and `milk` remain separate if accepted ShoppingRequirement semantics consider them different.
- `2 PIECE onion` and `200 GRAM onion` remain separate.

## Identity semantics

Final aggregate ShoppingItem identity is list-scoped and merge-key-scoped exactly like M2.1.

For a fixed aggregate ShoppingListId + normalized requirement + canonical unit:

- amount changes do not change item ID;
- target-serving changes do not change item ID;
- adding/removing other unrelated recipes does not change that item's ID;
- source entry order does not change the ID, although output list ordering remains first-occurrence based.

Changing aggregate ShoppingListId changes derived ShoppingItem IDs.

## Provenance semantics

For each final aggregate item, provenance order is:

1. aggregation entry order;
2. accepted per-recipe converter provenance order within each entry.

Repeated Recipe example:

```text
entry-A → Recipe R → ingredient I
entry-B → Recipe R → ingredient I
```

Both refs remain present and distinct because `entry-A != entry-B`, even though the underlying Recipe/ingredient identity is the same.

No Recipe/provenance field is added to neutral Shopping Core types.

## Validation and fail-closed invariants

Reject:

- null aggregate ShoppingListId;
- null entries list;
- empty entries list;
- null entry;
- null entry ID / Recipe / target servings through value-type constructors;
- duplicate entry IDs;
- null derived internal/final item IDs;
- generated final ShoppingItem ID collision across different merge keys;
- impossible missing/empty provenance from an accepted conversion if encountered.

Do not silently skip invalid entries or source items.

## TDD requirements

Explicit RED→GREEN checkpoints must cover:

1. two compatible recipes merge with exact summed canonical quantity;
2. incompatible requirement/unit stays separate;
3. first compatible occurrence controls aggregate item order;
4. same Recipe included twice under different entry IDs is valid and provenance distinguishes occurrences;
5. duplicate entry IDs fail closed;
6. final item ID is stable across amount and target-serving changes;
7. changing aggregate list ID changes final item ID;
8. existing single-recipe item-ID fixture remains unchanged after helper extraction;
9. provenance map/lists are deeply immutable;
10. null/empty inputs fail closed;
11. injected item-ID collision fails closed;
12. Shopping Core remains independent from Recipe aggregation.

## Architecture

All new production types live in the existing `recipe` package because this is Recipe composition logic over accepted Shopping value objects.

Allowed dependencies remain:

`recipe → shopping`

Forbidden:

`shopping → recipe`

M2.5 introduces no dependency on preview/application HTTP adapters, provider, retailer, matching, basket, comparison, database or web code.

## Verification / acceptance

Before merge:

- focused Recipe aggregation tests green after explicit RED evidence;
- existing `RecipeShoppingListConverterTest` remains green and proves identity compatibility;
- full Maven `verify` including Modulith/Testcontainers baseline;
- no OpenAPI/generated-client diff;
- existing Web/Playwright regression remains green;
- CodeQL, Dependency Review, Container Security, Retailer Bridge, Release Contract and Release Bundle green;
- read-only review with no unresolved P0/P1/P2;
- exact PR head green across all normal workflow groups.

Squash merge with expected-head protection. M2.5 becomes COMPLETE / ACCEPTED only after all normal push workflows succeed on the merged main SHA and canonical docs are synchronized.

## Non-goals

No public API, TypeScript client, Recipe UI changes, weekly-plan domain/UI, persistence, saved recipes, pantry/exclusions, multi-user state, exact-address behavior, retailer/provider changes, nutrition, arbitrary recipe import, fuzzy/synonym/semantic/AI merging or optimization/ranking changes.

## Next milestone

After M2.5 acceptance, deterministic M2 foundations are sufficient to begin **M3 Weekly Planning**. M3 may introduce planner-specific occurrence ownership, pantry/exclusions, API/UI and evidence-driven persistence without changing the accepted multi-recipe merge/provenance semantics.
