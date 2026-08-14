# M2.5 Deterministic Multi-Recipe Aggregation — Acceptance Decision

Date: 2026-08-14  
Issue: #106  
Implementation PR: #107  
Accepted merge SHA: `0854fc5bf76ad2976986537d6b4f5f3b8ebd18f0`  
Decision: **COMPLETE / ACCEPTED**

## Decision

M2.5 is accepted as the deterministic multi-recipe aggregation foundation required by M3 Weekly Planning.

Accepted flow:

`ordered Recipe occurrences + target servings → accepted per-recipe conversion → one canonical aggregate ShoppingList + occurrence-aware Recipe/ingredient provenance`

M2.5 remains a pure Recipe-domain/core capability. It introduces no public API, planner UI, persistence, pantry semantics, retailer/provider behavior or fuzzy/AI interpretation.

## Accepted model and behavior

- `RecipeAggregationEntryId` identifies one occurrence of a Recipe independently from `RecipeId`.
- `RecipeAggregationEntry` binds occurrence ID, accepted Recipe and target servings.
- The same Recipe may be intentionally included more than once when occurrence IDs differ.
- Duplicate occurrence IDs fail closed.
- `RecipeShoppingListAggregator` delegates serving scaling and source-unit canonicalization to the accepted `RecipeShoppingListConverter` for every occurrence.
- Per-occurrence intermediate ShoppingList IDs are deterministic and internal.
- Cross-Recipe automatic merge remains exact normalized ShoppingRequirement + canonical QuantityUnit only.
- Already-canonical scaled quantities are summed exactly with `BigDecimal.add`.
- First compatible occurrence controls aggregate item order.
- Aggregate ShoppingItem IDs reuse the accepted M2.1 list+requirement+canonical-unit derivation and remain independent of amount and target-serving changes.
- `RecipeAggregationIngredientRef` adds occurrence identity around the accepted `RecipeIngredientRef`, so repeated inclusion of the same Recipe remains unambiguous.
- Aggregate provenance preserves occurrence order and accepted converter lineage order and is deeply immutable.
- Empty input, null entries, duplicate occurrence IDs, missing/empty converted provenance, null derived IDs and generated-ID collisions fail closed.
- Shopping Core remains Recipe-free.

## Compatibility proof

Before extracting the shared M2.1 identity helper, a literal characterization fixture locked this accepted identity:

- ShoppingListId: `4ea3a925-1d2a-4246-a970-7a82ffc96402`;
- requirement: `Flour`;
- canonical unit: `GRAM`;
- ShoppingItem UUID: `3d737f10-a263-39b3-b90a-fe7868c035b9`.

The fixture passed before and after extraction of package-private `RecipeShoppingMergeKey` and `RecipeShoppingItemIds.derive(...)`. Existing single-Recipe ShoppingItem IDs therefore remain byte-for-byte compatible.

## TDD evidence

Aggregation RED:

`d337ec12a739e0c9cc20be8233a69de435cb72ee`

- compilation failed on intentionally absent aggregation production types.

Aggregation GREEN:

`e447e8bfdbcc55ddef20f4d9445de1c5e4080474`

- full API verification succeeded;
- two independently converted Recipe occurrences merged into one exact canonical item with ordered occurrence-aware provenance.

Hardening RED:

`3c377bc331c4a2a2bb8c0c8af57da2d62536a31c`

- eight aggregation tests executed;
- exactly two intended failures: empty aggregation input and duplicate occurrence ID;
- zero errors;
- all other new aggregation invariants passed;
- all ten existing converter tests passed.

Hardening GREEN:

`42b0a7c22141f770d58e9bbdca35a26f62b1d2ae`

- only the two missing fail-closed checks were added;
- full API/Maven verification succeeded.

## Review and exact-head CI

Final reviewed PR head:

`a6e1095696ebfd67fafe7675a37b125ae02b3170`

Review verdict: **REVIEWED_READY / Looks good**.

Review covered:

- M2.1 ShoppingItem-ID compatibility;
- strict merge semantics;
- repeated-Recipe occurrence lineage;
- first-occurrence ordering;
- identity stability;
- deep provenance immutability;
- fail-closed validation/collision paths;
- dependency direction;
- absence of API/UI/persistence/planner scope creep.

No unresolved P0/P1/P2 finding remained and review threads were empty.

All **9/9 normal PR workflow groups SUCCESS** on the exact reviewed head, including API/Maven verification, Contract, responsive Web/E2E regression, CodeQL Java + JS/TS, Dependency Review, Container Security, Retailer Bridge, Release Contract and Release Bundle.

## Post-merge acceptance proof

PR #107 was squash-merged using expected-head protection.

Exact merged `main` SHA:

`0854fc5bf76ad2976986537d6b4f5f3b8ebd18f0`

Issue #106 closed with state reason `completed`.

GitHub created exactly eight normal push-triggered workflows for this SHA. Final result:

- API CI — SUCCESS;
- Contract CI — SUCCESS;
- Web CI / responsive E2E — SUCCESS;
- CodeQL Java + JavaScript/TypeScript — SUCCESS;
- Container Security CI — SUCCESS;
- Retailer Bridge CI — SUCCESS;
- Release Contract CI — SUCCESS;
- Release Bundle CI — SUCCESS.

At workflow-group level: **8/8 SUCCESS, 0 failures**.

## M2 exit decision

M2 deterministic Recipe foundations are now complete:

1. one Recipe → canonical ShoppingList;
2. stateless Recipe application/API boundary;
3. Recipe → retailer comparison composition;
4. responsive Recipe-first product UI;
5. several Recipe occurrences → one canonical aggregate ShoppingList with complete occurrence-aware lineage.

Decision: **advance to M3 Weekly Planning**.

The next slice should design the smallest weekly-plan domain/application boundary over accepted M2.5 aggregation semantics. Planner-specific meal/day ownership, pantry/exclusions, API/UI and any persistence decision belong to M3 and must not modify accepted M2 merge/provenance rules implicitly.
