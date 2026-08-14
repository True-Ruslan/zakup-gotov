# M2.5 Deterministic Multi-Recipe Aggregation — Shipping Evidence

Date: 2026-08-14  
Issue: #106  
PR: #107  
Status: **IMPLEMENTED / TESTED / SHIPPING — acceptance pending**

Authoritative design: `docs/superpowers/specs/2026-08-14-m2-5-multi-recipe-aggregation-design.md`  
Implementation plan: `docs/superpowers/plans/2026-08-14-m2-5-multi-recipe-aggregation.md`

## Delivered scope

M2.5 delivers the final pure-domain Recipe aggregation foundation required before M3 Weekly Planning:

`ordered Recipe occurrences + target servings → accepted RecipeShoppingListConverter per occurrence → one canonical aggregate ShoppingList + occurrence-aware provenance`

Delivered production behavior:

- `RecipeAggregationEntryId` distinguishes one occurrence of a Recipe from the Recipe identity itself;
- `RecipeAggregationEntry` binds one occurrence ID, accepted Recipe and target servings;
- the same Recipe may appear multiple times under distinct occurrence IDs;
- `RecipeAggregationIngredientRef` preserves occurrence ID plus accepted `RecipeIngredientRef` lineage;
- `RecipeShoppingListAggregator` reuses the accepted `RecipeShoppingListConverter` per occurrence rather than duplicating serving scaling or source-unit canonicalization;
- each intermediate converter ShoppingListId is derived deterministically from aggregate list ID + occurrence ID and remains internal;
- aggregate merge key remains exact normalized ShoppingRequirement + canonical QuantityUnit;
- already-canonical scaled amounts are summed with exact `BigDecimal.add`;
- first compatible occurrence controls output ShoppingItem ordering;
- final aggregate ShoppingItem IDs use the same accepted list+requirement+canonical-unit derivation as M2.1 and remain independent of amount/target servings;
- aggregate provenance preserves entry order and accepted converter provenance order;
- empty entry sets, null entries, duplicate occurrence IDs, missing converted provenance and generated final-ID collisions fail closed;
- provenance map and nested lineage lists are defensive, ordered and immutable;
- no HTTP/OpenAPI/generated-client/web/persistence/planner/provider/retailer behavior was introduced.

## Shared-semantics compatibility proof

M2.5 needed the exact M2.1 merge-key and ShoppingItem-ID semantics without copying them.

Before refactoring, a literal characterization fixture locked the accepted identity:

- ShoppingListId `4ea3a925-1d2a-4246-a970-7a82ffc96402`;
- requirement `Flour`;
- canonical unit `GRAM`;
- expected ShoppingItem UUID `3d737f10-a263-39b3-b90a-fe7868c035b9`.

The characterization passed on the pre-refactor implementation.

Package-private shared helpers were then extracted:

- `RecipeShoppingMergeKey`;
- `RecipeShoppingItemIds.derive(...)`.

The accepted UUID payload remains byte-for-byte:

```text
shoppingListId.value() + "\n" + requirement.text() + "\n" + unit.name()
```

After extraction, full API verification and the literal UUID fixture remained green. Existing M2.1 item identities therefore did not drift.

## Explicit TDD evidence

### Aggregation behavior

RED head `d337ec12a739e0c9cc20be8233a69de435cb72ee`:

- new `RecipeShoppingListAggregatorTest` failed compilation on the intentionally absent aggregation production types.

GREEN head `e447e8bfdbcc55ddef20f4d9445de1c5e4080474`:

- full API verification SUCCESS;
- two different Recipe occurrences using `0.5 LITER` and `250 MILLILITER`, independently scaled by the accepted converter, aggregate to one exact `Milk 1500 MILLILITER` item;
- provenance remains ordered `entry A → ingredient A`, then `entry B → ingredient B`.

### Hardening

RED head `3c377bc331c4a2a2bb8c0c8af57da2d62536a31c`:

- `RecipeShoppingListAggregatorTest`: 8 tests, exactly 2 expected failures;
- `rejectsEmptyAggregationEntries` failed because empty input was still accepted;
- `rejectsDuplicateAggregationEntryIdentity` failed because duplicate occurrence ID was still accepted;
- all six other new aggregation tests passed;
- existing `RecipeShoppingListConverterTest`: 10 tests PASS;
- overall Maven failure was exactly 2 failures / 0 errors in the intended hardening cases.

GREEN head `42b0a7c22141f770d58e9bbdca35a26f62b1d2ae`:

- only the two missing fail-closed checks were added;
- full API CI / Maven verification SUCCESS.

The green suite covers:

- compatible cross-Recipe merge with exact canonical sum;
- strict non-merge for case-different requirements and quantity-unit differences;
- first compatible occurrence ordering;
- repeated same Recipe under two different occurrence IDs;
- duplicate occurrence rejection;
- empty input rejection;
- stable final item IDs across amount and target-serving changes;
- different aggregate ShoppingListId → different final item ID;
- injected final-ID collision fail-closed behavior;
- deep immutable aggregate provenance;
- accepted single-Recipe UUID compatibility.

## Architecture / scope evidence

All new production classes live in `io.github.trueruslan.zakupgotov.recipe` and depend only on accepted Recipe types, neutral Shopping value/aggregate types and JDK classes.

No M2.5 production code was added to:

- `shopping`;
- preview/application HTTP packages;
- provider/retailer/matching/basket/comparison;
- database/persistence;
- OpenAPI/generated client;
- web/browser code.

Existing architecture verification continues to enforce Shopping Core independence from Recipe code. Ordinary CI remains retailer-network-free.

## Repository-wide code checkpoint

On implementation/hardening GREEN head `42b0a7c22141f770d58e9bbdca35a26f62b1d2ae`:

- API CI — SUCCESS;
- Contract CI — SUCCESS;
- CodeQL — SUCCESS;
- Dependency Review — SUCCESS;
- Container Security CI — SUCCESS;
- Retailer Bridge CI — SUCCESS;
- Release Contract CI — SUCCESS;
- Release Bundle CI — SUCCESS;
- Web CI / E2E was still completing when this shipping record was authored.

This is implementation evidence, not acceptance evidence. The documentation commit creates a new exact PR head and all normal workflow groups must run again before review/merge.

## Remaining acceptance gates

M2.5 is **not ACCEPTED** until:

1. all 9 normal PR workflow groups succeed on the exact final head;
2. read-only review reports no unresolved P0/P1/P2 finding and review threads are clear;
3. PR #107 is marked ready and squash-merged using expected-head protection;
4. all normal push-triggered workflows succeed on the merged `main` SHA;
5. only then are #106, PROJECT_STATE, ROADMAP and CHANGELOG synchronized as COMPLETE / ACCEPTED and the next milestone becomes M3 Weekly Planning.
