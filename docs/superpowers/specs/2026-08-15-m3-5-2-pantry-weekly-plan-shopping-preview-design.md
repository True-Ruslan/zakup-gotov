# M3.5.2 Pantry-aware WeeklyPlan Shopping Preview — Design

Date: 2026-08-15
Status: APPROVED FOR IMPLEMENTATION

## Goal

Expose accepted M3.5.1 Pantry subtraction above the accepted M3.2 WeeklyPlan shopping projection through a new stateless API boundary, without silently changing M3.2 or M3.3.

## Boundary

New endpoint:

`POST /api/v1/weekly-plan-pantry-shopping-previews`

Request:

- `weeklyPlan`: the existing `WeeklyPlanShoppingPreviewRequest` vocabulary;
- `pantry`: explicit request-scoped Pantry rows, each containing a Shopping requirement and positive quantity.

Response:

- `weeklyPlan`: accepted M3.2 WeeklyPlan projection;
- `originalShoppingList`: accepted M3.2 canonical ShoppingList with provenance;
- `pantryAdjustments`: one ordered evidence row per original ShoppingItem;
- `remainingShoppingList`: zero-or-more remaining ShoppingItems after Pantry subtraction, preserving source item IDs/order/provenance.

The remaining list uses a dedicated M3.5.2 projection because accepted M3.2 intentionally requires a non-empty ShoppingList, while valid Pantry input may fully cover every weekly requirement.

## Composition

1. Delegate WeeklyPlan construction/scaling/aggregation/provenance entirely to `WeeklyPlanShoppingPreviewService`.
2. Adapt the accepted M3.2 ShoppingList projection back to neutral Shopping domain objects using the generated list/item UUIDs, normalized requirement strings and canonical `Quantity` values unchanged.
3. Adapt request Pantry rows into accepted `PantryItem` objects. Domain `ShoppingRequirement` and `Quantity` remain authoritative for normalization/canonicalization/validation.
4. Invoke `PantryShoppingListAdjuster` exactly once.
5. Join adjustment evidence and remaining items back to the original M3.2 projection by source `ShoppingItemId`.
6. Copy original planner provenance unchanged onto each remaining item.
7. Fail closed on cardinality, identity/order, requirement, quantity, evidence or source-provenance drift.

## Invariants

- Existing M3.2 and M3.3 endpoints/classes remain behaviorally unchanged.
- Original ShoppingItem UUIDs are the only identity bridge through Pantry adjustment.
- Evidence cardinality/order equals the original ShoppingList cardinality/order.
- `UNCHANGED` and `PARTIALLY_COVERED` items appear exactly once in the remaining list; `FULLY_COVERED` items do not.
- Remaining items form an ordered subsequence of original items.
- Remaining requirement and quantity exactly match accepted Pantry evidence.
- Original `sources` are copied exactly and never recomputed.
- Full Pantry coverage is represented by `remainingShoppingList.items = []`; it is not an error.
- Pantry rows never carry server-owned Shopping/Recipe/WeeklyPlan identities.
- No persistence, retailer comparison, UI, fuzzy/AI matching, provider/network traffic or omit-all exclusion semantics are introduced.

## Validation and HTTP safety

- The new wrapper validates `weeklyPlan` and `pantry` presence before composition.
- Pantry rows reject null/blank requirements, null/non-positive quantities and unsupported units through accepted Shopping semantics.
- Unknown fields, malformed JSON and enum/deserialization failures return one sanitized `$request: malformed JSON request` problem.
- Nested M3.2 semantic validation remains authoritative for WeeklyPlan errors and is exposed through the new boundary without leaking internal exception detail.
- Error payload code: `INVALID_WEEKLY_PLAN_PANTRY_SHOPPING_PREVIEW`.

## OpenAPI/client

OpenAPI 3.1 gains only the new operation and schemas. The generated TypeScript client/types must be regenerated and checked into sync. Canonical quantity output remains `PIECE | GRAM | MILLILITER`; Pantry input accepts the existing input units including kilogram/liter canonicalization.

## Architecture

New package: `io.github.trueruslan.zakupgotov.weeklyplanpantrypreview`.

Allowed production dependencies: `weeklyplanpreview`, `pantry`, `shopping`, Java/Spring/Jackson only. No dependency on comparison, retailer, provider, persistence/database, web UI or acquisition code.

## Test strategy

RED→GREEN in layers:

1. service composition/provenance/full-coverage contract;
2. HTTP validation/sanitization contract;
3. OpenAPI/generated-client contract;
4. ArchUnit/regression guards proving M3.2/M3.3 remain unchanged.

Final gate: exact-head 9/9 PR workflows + clean review, squash merge, exact main 8/8 post-merge workflows.