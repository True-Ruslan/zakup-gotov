# M3.1 — WeeklyPlan Domain + Deterministic Shopping Composition

Date: 2026-08-14  
Status: APPROVED FOR IMPLEMENTATION PLANNING  
Issue: #109  
Baseline: `main=7d7df863e8b9218b018f343e46dd7fd7c03396f1`

## Goal

Introduce the first Weekly Planning domain boundary without weakening or duplicating accepted M2 Recipe and multi-recipe aggregation semantics.

A user must be able to express an ordered week of meal occurrences, choose the target servings for each occurrence, and deterministically compose the whole week into one canonical ShoppingList while retaining lineage back to the specific weekly occurrence and Recipe ingredient that caused each shopping requirement.

M3.1 is a pure domain/application slice. It does not expose a public API or UI.

## User and success criteria

Primary user: a person planning several meals for a week before comparing retailer baskets.

M3.1 succeeds when:

1. a weekly plan can contain one or more ordered meal occurrences across Monday through Sunday;
2. the same Recipe can be included more than once through distinct occurrence identities;
3. each occurrence can choose its own positive target serving count;
4. composing the plan delegates all Recipe scaling, canonicalization, merge and final ShoppingItem identity semantics to accepted M2.5 behavior;
5. every final ShoppingItem has complete ordered provenance back to `WeeklyMealOccurrenceId + RecipeIngredientRef`;
6. changing only occurrence day or target servings does not change final ShoppingItem identity when `WeeklyPlanId + normalized requirement + canonical unit` are unchanged;
7. invalid or ambiguous planner identity/provenance states fail closed;
8. no persistence, transport, web, retailer or comparison semantics are introduced.

## Approved product model

### WeeklyPlan

`WeeklyPlan` is an immutable aggregate with:

- `WeeklyPlanId id`;
- ordered non-empty `List<WeeklyMealOccurrence> occurrences`.

Rules:

- `id` is required;
- occurrences are required, non-null and non-empty;
- no occurrence may be null;
- `WeeklyMealOccurrenceId` must be unique inside one plan;
- two occurrences may reference the same `RecipeId` intentionally;
- two or more occurrences may share the same day;
- occurrence order is explicit user order and is not automatically re-sorted by day.

### WeeklyPlanDay

`WeeklyPlanDay` is a finite planner-domain enum:

`MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY`.

The day is planner metadata. It does not participate in Recipe merge keys, ShoppingItem identity or quantity arithmetic.

### WeeklyMealOccurrence

Immutable value with:

- `WeeklyMealOccurrenceId id`;
- `WeeklyPlanDay day`;
- `Recipe recipe`;
- `int targetServings`.

Rules:

- all fields are required;
- `targetServings > 0`;
- Recipe base-serving validation remains owned by the accepted Recipe aggregate;
- no fixed `BREAKFAST / LUNCH / DINNER / SNACK` slot exists in M3.1;
- identical Recipe + day + servings is still valid when occurrence IDs differ.

## Why no fixed meal slots

Three approaches were considered:

1. **Approved: required day + ordered occurrence, no fixed slot.**
   - Expresses a real weekly plan.
   - Supports arbitrary numbers of meals per day.
   - Avoids premature breakfast/lunch/dinner assumptions.
   - Keeps the first M3 slice small.

2. Ordered Recipe list without day.
   - Simpler, but does not yet model a week and would immediately require another planner-domain migration.

3. Day + fixed meal slot vocabulary.
   - More structured, but over-constrains users and introduces conflict/ordering semantics not yet required by evidence.

The approved model preserves day information while keeping slot semantics deferred.

## Package and dependency boundary

New production package:

`io.github.trueruslan.zakupgotov.weeklyplan`

Allowed inward dependencies:

- `recipe`;
- `shopping` only for canonical ShoppingList/ShoppingItem value types exposed by the composition result.

The `weeklyplan` package owns planner-specific types and composition. Existing `recipe` and `shopping` packages must not depend on `weeklyplan`.

The accepted `recipe` package remains the owner of M2.5 aggregation semantics.

No dependency on provider, retailer, matching, basket, comparison, database, HTTP or web layers is allowed.

## Deterministic composition

### Public application operation

`WeeklyPlanShoppingListComposer.compose(WeeklyPlan plan)`

returns immutable:

`WeeklyPlanShoppingListComposition`

containing:

- canonical `ShoppingList shoppingList`;
- ordered deep-immutable `Map<ShoppingItemId, List<WeeklyPlanIngredientRef>> provenance`.

### Composition flow

For each occurrence in plan order:

1. derive one internal `RecipeAggregationEntryId` from `WeeklyPlanId + WeeklyMealOccurrenceId`;
2. create a `RecipeAggregationEntry` with the occurrence Recipe and target servings;
3. retain an internal one-to-one mapping from derived aggregation entry ID to WeeklyMealOccurrenceId;
4. derive the final aggregate `ShoppingListId` from `WeeklyPlanId`;
5. call the accepted `RecipeShoppingListAggregator` exactly once with the ordered aggregation entries and derived ShoppingListId;
6. project M2.5 provenance from `RecipeAggregationEntryId + RecipeIngredientRef` into `WeeklyMealOccurrenceId + RecipeIngredientRef`;
7. return the exact ShoppingList produced by M2.5 plus the projected planner provenance.

The composer does not rescale quantities, canonicalize units, merge requirements or re-derive final ShoppingItem IDs itself.

## Deterministic identity rules

### Weekly plan ShoppingList identity

The final `ShoppingListId` is deterministically derived from `WeeklyPlanId` with a versioned namespaced payload.

Logical payload:

`zakup-gotov:weekly-plan-shopping-list:v1:<weeklyPlanId>`

Properties:

- same WeeklyPlanId -> same ShoppingListId;
- day/order/servings/content changes do not directly alter ShoppingListId;
- a different WeeklyPlanId produces a different list identity under ordinary deterministic UUID behavior.

### Internal aggregation-entry identity

Each internal `RecipeAggregationEntryId` is deterministically derived from:

`WeeklyPlanId + WeeklyMealOccurrenceId`

with a versioned namespaced payload.

Logical payload:

`zakup-gotov:weekly-plan-aggregation-entry:v1:<weeklyPlanId>:<weeklyMealOccurrenceId>`

Properties:

- the same occurrence identity in another WeeklyPlan is plan-scoped and produces a different internal aggregation identity;
- changing day, Recipe content or target servings does not alter the internal entry identity;
- duplicate WeeklyMealOccurrenceId is rejected by WeeklyPlan before composition;
- any impossible generated-ID collision across distinct occurrences must fail closed rather than overwrite the internal mapping.

The concrete UUID derivation must use the repository's deterministic UUID approach and be isolated behind a package-private seam so collision behavior can be tested without changing the public model.

### Final ShoppingItem identity

Final ShoppingItem identity remains entirely owned by M2.5:

`derived weekly ShoppingListId + normalized requirement + canonical unit`

Therefore amount, target servings, Recipe occurrence day and occurrence position do not participate directly in ShoppingItem identity.

## Provenance

### Planner provenance type

`WeeklyPlanIngredientRef` contains:

- `WeeklyMealOccurrenceId occurrenceId`;
- accepted `RecipeIngredientRef recipeIngredient`.

No `RecipeAggregationEntryId` is exposed in the final planner result.

### Projection invariants

For every ShoppingItem in the composed ShoppingList:

- provenance entry exists;
- provenance list is non-empty;
- every internal aggregation entry ID resolves to exactly one WeeklyMealOccurrenceId in the same plan;
- projected RecipeIngredientRef is preserved exactly from M2.5;
- source order from M2.5 is preserved;
- no provenance key exists for a ShoppingItem absent from the final list;
- no final ShoppingItem exists without provenance;
- maps and nested lists are defensively copied and immutable.

Unknown internal aggregation IDs, missing provenance or impossible cardinality/key drift fail closed as internal invariant violations.

## Ordering semantics

WeeklyPlan preserves explicit occurrence input order.

The composer sends aggregation entries to M2.5 in exactly that order.

M2.5 remains authoritative for final ShoppingItem order: first compatible normalized requirement + canonical unit occurrence wins the group position.

`WeeklyPlanDay` is not an implicit sort key.

This separation is intentional: the planner records calendar placement; shopping aggregation remains deterministic over explicit planner order.

## Error handling

Construction fails fast for:

- missing WeeklyPlanId;
- missing/null/empty occurrence list;
- null occurrence;
- duplicate WeeklyMealOccurrenceId;
- missing occurrence ID/day/Recipe;
- non-positive target servings.

Composition fails closed for:

- missing plan;
- deterministic internal ID collision;
- accepted M2.5 aggregation failure;
- missing, orphan or empty aggregate provenance;
- unknown aggregation-entry identity during planner provenance projection;
- ShoppingList/provenance key drift.

These are domain/application invariant failures. M3.1 does not introduce a public HTTP problem vocabulary.

## Immutability and thread safety

All new public domain/result types are immutable.

- constructor inputs are defensively copied where applicable;
- returned collections are unmodifiable;
- nested provenance lists are immutable;
- composer keeps no mutable shared state;
- deterministic ID derivation is stateless.

The composer is safe for concurrent reuse assuming the accepted stateless M2.5 aggregator remains so.

## Tests and acceptance criteria

### WeeklyPlan domain tests

Must prove:

- valid multi-day plan construction;
- multiple occurrences on one day;
- same Recipe used multiple times through distinct occurrence IDs;
- explicit occurrence order preservation;
- rejection of null/missing/empty state;
- rejection of duplicate occurrence IDs;
- rejection of non-positive target servings.

### Composition tests

Must prove:

- two compatible requirements across different weekly occurrences merge through M2.5;
- incompatible requirement/unit combinations remain separate exactly as M2.5 dictates;
- final quantities match accepted M2.5 canonical arithmetic;
- final item order follows first compatible occurrence in WeeklyPlan input order;
- provenance resolves to occurrence ID + exact RecipeIngredientRef;
- repeated use of the same Recipe remains unambiguous in provenance;
- same plan identity + requirement + unit yields stable ShoppingItem ID across target-serving changes;
- changing only day does not alter ShoppingItem ID or quantity;
- changing WeeklyPlanId changes the derived ShoppingList scope and therefore final ShoppingItem IDs;
- generated internal aggregation-ID collision fails closed through the package-private derivation seam;
- provenance is deeply immutable;
- null plan and provenance drift fail closed.

### Architecture/full verification

Before acceptance:

- focused M3.1 tests GREEN;
- existing M2.1/M2.5 identity and aggregation regression tests GREEN;
- full Maven/Testcontainers/Modulith verification GREEN;
- architecture guard confirms `weeklyplan -> recipe/shopping` only and prevents reverse/downstream coupling;
- exact-head PR workflow groups GREEN;
- independent read-only review has no unresolved P0/P1/P2;
- squash merge is protected by exact head SHA;
- normal post-merge push workflows on `main` are all GREEN before M3.1 is marked COMPLETE / ACCEPTED.

## Non-goals

M3.1 explicitly does not add:

- persistence or database schema;
- saved/reusable plan history;
- REST/OpenAPI/generated TypeScript contracts;
- weekly planner web UI;
- breakfast/lunch/dinner/snack slots;
- timestamps, calendar dates, week-number/time-zone semantics;
- pantry inventory or exclusion/subtraction semantics;
- nutrition/macros/calories;
- Recipe search/catalog/import;
- AI, fuzzy, synonym or semantic ingredient equivalence;
- retailer/provider acquisition;
- comparison orchestration;
- optimization or multi-store checkout logic.

## Follow-on direction

After M3.1 acceptance, the next evidence-driven slice should be a stateless WeeklyPlan application/API boundary that owns transient planner identities and exposes the accepted weekly composition result without persistence. A responsive Weekly Planning UI follows only after that contract is accepted.
