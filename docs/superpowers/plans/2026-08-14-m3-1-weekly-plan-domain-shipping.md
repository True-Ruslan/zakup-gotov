# M3.1 WeeklyPlan Domain + Deterministic Shopping Composition — Shipping Evidence

Date: 2026-08-14  
Issue: #109  
PR: #110  
Status: **IMPLEMENTED / TESTED / SHIPPING — acceptance pending**

Authoritative design: `docs/superpowers/specs/2026-08-14-m3-1-weekly-plan-domain-design.md`  
Implementation plan: `docs/superpowers/plans/2026-08-14-m3-1-weekly-plan-domain.md`

## Delivered scope

M3.1 introduces the first pure Weekly Planning domain/application boundary:

`ordered WeeklyPlan meal occurrences → deterministic M2.5 aggregation entries → one canonical weekly ShoppingList + WeeklyMealOccurrence provenance`

Delivered behavior:

- immutable `WeeklyPlanId`, `WeeklyMealOccurrenceId`, `WeeklyPlanDay`, `WeeklyMealOccurrence` and `WeeklyPlan`;
- ordered non-empty meal occurrences across Monday through Sunday;
- multiple occurrences may share one day;
- the same Recipe may be used more than once through distinct occurrence IDs;
- each occurrence owns an accepted positive `RecipeServings` target;
- there is deliberately no BREAKFAST/LUNCH/DINNER/SNACK slot vocabulary;
- caller occurrence order is preserved and is not re-sorted by day;
- deterministic weekly ShoppingList identity derives from WeeklyPlanId;
- deterministic internal M2.5 aggregation-entry identity derives from WeeklyPlanId + WeeklyMealOccurrenceId;
- `WeeklyPlanShoppingListComposer` invokes the accepted `RecipeShoppingListAggregator` exactly once;
- M2.5 remains authoritative for serving scaling, source-unit canonicalization, exact merge keys, canonical sums, first-compatible output order and final ShoppingItem IDs;
- planner output preserves the exact M2.5 ShoppingList instance and projects lineage to `WeeklyMealOccurrenceId + RecipeIngredientRef`;
- internal `RecipeAggregationEntryId` does not escape the planner result;
- day and serving changes do not perturb ShoppingItem identity when WeeklyPlanId + normalized requirement + canonical unit remain unchanged;
- reordering occurrences may change output group order but not IDs for unchanged merge keys in the same plan;
- invalid planner identity/provenance states fail closed;
- planner provenance maps and nested lineage lists are deep immutable;
- no persistence, HTTP/OpenAPI/generated client, web UI, pantry/exclusions, nutrition, provider/retailer, comparison or AI/fuzzy behavior was introduced.

## Explicit TDD evidence

### WeeklyPlan domain

RED head `4fc807aef66a2d861f3707477186ce29c3d7a022`:

- test-only `WeeklyPlanTest` was added before production types;
- API CI reached `testCompile` and failed only on intentionally absent `WeeklyPlanId`, `WeeklyMealOccurrenceId`, `WeeklyMealOccurrence`, `WeeklyPlanDay` and `WeeklyPlan` symbols;
- there was no infrastructure or unrelated regression failure.

GREEN head `f9f8f4372dab7c15aa4e5c08fc1f0d841ca3be04`:

- minimal five-type WeeklyPlan domain implementation added;
- full API/Maven verification SUCCESS;
- domain tests prove multi-day/multi-meal construction, repeated Recipe use, explicit order, defensive-copy immutability and fail-closed null/empty/duplicate identity behavior.

### WeeklyPlan → ShoppingList composition

RED head `3faa4c8a93ee725082264751079a23fbd43f28b4`:

- test-only `WeeklyPlanShoppingListComposerTest` was added before composer/result/identity types;
- API CI failed at `testCompile` on intentionally absent `WeeklyPlanShoppingListComposer` and `WeeklyPlanIngredientRef` plus downstream type-inference cascades caused by those absent result types;
- already-accepted WeeklyPlan domain types compiled.

GREEN head `5b2cf92f9f3c1caa071a953f977fb09fce090697`:

- composer, planner provenance/result types and package-private aggregation/identity seams were added;
- full API/Maven verification SUCCESS;
- two weekly occurrences contributing `0.5 LITER` and `250 MILLILITER`, with independent target-serving scaling, produce one exact `Milk 1500 MILLILITER` final item through accepted M2.5;
- repeated use of the same Recipe remains distinct through WeeklyMealOccurrence provenance;
- caller order remains authoritative rather than day sorting;
- ShoppingItem identity is stable across day/serving changes and scoped by WeeklyPlanId;
- reordering occurrences can reorder output groups without changing unchanged merge-key IDs.

### Fail-closed provenance hardening

RED head `a2aeb7e9fdb45b646cb0d19407acca1291bf6cf1`:

- hardening suite added seven invariant tests;
- full Maven run: **315 tests, exactly 3 failures, 0 errors, 5 skipped**;
- the only failures were the intended missing protections:
  1. final ShoppingItem without provenance;
  2. orphan provenance ShoppingItemId;
  3. empty provenance lineage list;
- existing tests already passed for unknown internal aggregation entry identity, generated internal-ID collision, null derived ShoppingListId/null plan and deep immutability/exact ShoppingList preservation;
- all M3.1 happy-path/domain tests and all existing Recipe/M2.5 regressions remained green.

GREEN head `25677c697292f63649579720e02aefd666093322`:

- only exact ShoppingList/provenance key-set validation and non-empty lineage validation were added;
- projection now iterates final ShoppingList order after structural validation;
- full API/Maven verification SUCCESS.

## Deterministic identity boundary

Default package-private `WeeklyPlanIds` uses UTF-8 `UUID.nameUUIDFromBytes` over versioned payloads:

```text
zakup-gotov:weekly-plan-shopping-list:v1:<weeklyPlanId>
zakup-gotov:weekly-plan-aggregation-entry:v1:<weeklyPlanId>:<weeklyMealOccurrenceId>
```

Final ShoppingItem IDs are not reimplemented in `weeklyplan`; M2.5 derives them from the resulting weekly ShoppingListId + accepted normalized requirement + canonical unit semantics.

## Architecture / scope evidence

Architecture checkpoint head `f3874d21b3569964a20d5b1bfb17c07ac250a821` adds `WeeklyPlanArchitectureTest` using the repository's existing ArchUnit test stack.

The guard proves:

- `weeklyplan` production package exists;
- project-level dependencies from `weeklyplan` are limited to `recipe` and `shopping`;
- `weeklyplan` does not reach into preview, recipe-preview/composition adapters, provider, retailer, matching, basket, comparison, database or Spring Web packages;
- accepted `recipe` and `shopping` packages remain independent from `weeklyplan`.

Full API/Maven/Testcontainers/Modulith/ArchUnit verification on `f3874d21b3569964a20d5b1bfb17c07ac250a821` — SUCCESS.

No production changes were made to OpenAPI, generated TypeScript client, web, database, retailer bridge, provider acquisition or comparison layers.

## Repository-wide shipping gate

This document creates a new final PR head. Earlier green workflow runs are implementation evidence only and are not reused as acceptance proof.

Before merge, the exact final head must have all nine normal PR workflow groups SUCCESS:

- API CI;
- Contract CI;
- Web CI including responsive E2E;
- CodeQL Java + JavaScript/TypeScript;
- Dependency Review;
- Container Security CI;
- Retailer Bridge CI;
- Release Contract CI;
- Release Bundle CI.

A read-only final review must also report no unresolved P0/P1/P2 findings and no unresolved review threads.

## Remaining acceptance gates

M3.1 is **not ACCEPTED** until:

1. all nine normal PR workflow groups succeed on one exact final head;
2. final read-only review is clean;
3. PR #110 is made ready and squash-merged using exact-head protection;
4. issue #109 closes as completed through the merged PR;
5. all normal push-triggered workflows succeed on the merged `main` SHA;
6. only then a separate canonical docs-only acceptance PR marks M3.1 `COMPLETE / ACCEPTED` and advances current focus to **M3.2 stateless WeeklyPlan application/API boundary**.
