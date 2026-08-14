# M3.5.1 Pantry Subtraction Semantics — Shipping Evidence

Date: 2026-08-15  
Issue: #121  
PR: #122  
Baseline: `e11fd532c8d1f927a14cb886abaa9e9988f9b21b`

Design: [`../specs/2026-08-15-m3-5-1-pantry-subtraction-semantics-design.md`](../specs/2026-08-15-m3-5-1-pantry-subtraction-semantics-design.md)  
Implementation plan: [`2026-08-15-m3-5-1-pantry-subtraction-semantics.md`](2026-08-15-m3-5-1-pantry-subtraction-semantics.md)

## Delivered boundary

M3.5.1 introduces one new pure production package:

`io.github.trueruslan.zakupgotov.pantry`

It adjusts an already canonical `ShoppingList` using caller-supplied household stock:

`ShoppingList + PantryItem[] → PantryAdjustment(remaining ShoppingList + ordered audit evidence)`

The layer does not compose with WeeklyPlan/Comparison yet and has no HTTP, persistence or browser surface.

## Accepted semantics implemented

- exact match key is `(ShoppingRequirement, canonical QuantityUnit)`, matching the accepted Recipe/WeeklyPlan aggregation vocabulary;
- `Quantity` remains the only unit canonicalizer, therefore kg→g and l→ml compatibility is reused rather than reimplemented;
- requirement equality remains case-sensitive and whitespace semantics remain owned by `ShoppingRequirement`;
- no fuzzy, synonym, transliteration, stemming or AI equivalence exists;
- duplicate Pantry rows with the same exact key are additive;
- Pantry stock is allocated sequentially in source ShoppingList order and consumed at most once;
- each source row consumes `min(required, available)` and can never produce negative demand;
- unmatched rows remain unchanged;
- partially covered rows preserve ShoppingListId, ShoppingItemId, ShoppingRequirement and order while reducing only Quantity;
- fully covered rows disappear from the remaining ShoppingList but retain explicit source-ID audit evidence;
- excess Pantry stock is ignored rather than represented as a zero/negative Shopping quantity;
- source ShoppingList and caller-owned Pantry collections are not mutated;
- audit evidence is immutable and validates `UNCHANGED`, `PARTIALLY_COVERED` and `FULLY_COVERED` structural/arithmetic invariants.

## TDD evidence

### Evidence model

RED head:

`e95b076825278a4653939fe06599d5b42b3097f5`

On that exact SHA, API CI failed in `Run API verification` before Pantry production types existed.

Initial implementation head:

`a3d502d0667b48c3a21bf8f4ac0c75e7b54c91f6`

This correctly exposed a Java compile issue in the compact record constructor: reassigned constructor parameters were captured from lambdas and therefore were not effectively final. The test contract was not changed.

Corrected GREEN head:

`0b04b775b80e480c7082872b70729ec01663109d`

On that exact SHA, full API CI / Maven `verify` succeeded.

### Subtraction core

RED head:

`289e973463bf2d391442a9645651851ad587e177`

On that exact SHA, API `testCompile` failed because `PantryShoppingListAdjuster` did not exist.

GREEN head:

`a88092c914ffe5c80e4d4ad1da672ba8dcd2033d`

On that exact SHA, full API CI / Maven `verify` succeeded with the complete subtraction test contract.

### Architecture boundary

Architecture-test head:

`2066135d8a275feb78904bba71fec0dce7cf9625`

On that exact SHA, full API CI / Maven `verify` succeeded. ArchUnit verifies:

- the Pantry production boundary exists;
- Pantry direct project dependencies target only `shopping`;
- Pantry does not depend on Recipe, WeeklyPlan, preview/comparison, matching/basket, provider/retailer, database or Spring packages;
- accepted Shopping, Recipe and WeeklyPlan packages do not depend on Pantry in M3.5.1.

## Test coverage

The Pantry test suite covers:

1. unmatched stock;
2. partial coverage;
3. full coverage;
4. kg/g canonical compatibility;
5. l/ml canonical compatibility;
6. incompatible dimensions;
7. duplicate Pantry aggregation;
8. single-consumption allocation across duplicate source keys;
9. excess stock without zero/negative Shopping quantities;
10. case-sensitive exact requirement matching;
11. source ShoppingList immutability;
12. caller-owned Pantry collection immutability;
13. null input/row rejection;
14. evidence status/unit/arithmetic invariants;
15. package dependency direction.

## Scope proof before final gate

PR #122 changed only:

- six production files under `apps/api/src/main/java/io/github/trueruslan/zakupgotov/pantry/`;
- three tests under `apps/api/src/test/java/io/github/trueruslan/zakupgotov/pantry/`;
- M3.5.1 design/plan/shipping documentation.

No changed file is under:

- OpenAPI/contracts/generated clients;
- `apps/web`;
- database/migrations;
- retailer/provider bridges;
- existing Shopping, Recipe, WeeklyPlan, preview/comparison production packages.

Therefore M3.5.1 does not silently alter accepted M3.1–M3.4 runtime behavior.

## Explicit non-goals preserved

M3.5.1 does not add:

- API endpoint/OpenAPI/generated-client exposure;
- WeeklyPlan → Pantry → Comparison composition;
- browser UI;
- saved Pantry inventory/history/database schema;
- dietary/medical or boolean “never buy” exclusions;
- fuzzy/synonym/AI equivalence;
- nutrition/macros;
- retailer/provider onboarding or activation;
- calendar/time-zone semantics.

## Final acceptance gate

The commit created by this shipping-evidence change becomes the final feature candidate. It must not be merged until:

1. all **9/9 normal PR workflow groups** succeed on that exact head;
2. failure count is zero;
3. a read-only change review finds no unresolved P0–P3 issues;
4. review threads are empty;
5. PR #122 is marked ready only after those exact-head checks;
6. squash merge uses expected-head protection;
7. all normal post-merge `main` push workflows succeed.

Only after those gates may M3.5.1 be called implemented → tested → reviewed → merged → accepted.