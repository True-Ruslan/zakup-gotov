# M3.2 Stateless WeeklyPlan Shopping Preview — Design

Date: 2026-08-14  
Issue: #112  
Status: **APPROVED / AUTHORITATIVE**

## Goal

Expose the accepted M3.1 WeeklyPlan domain/composition through one stateless, contract-first API boundary without introducing persistence, retailer acquisition or comparison semantics.

Accepted public journey:

`explicit ordered weekly occurrences → server-owned transient identities → accepted WeeklyPlan domain → accepted M3.1 composition → self-contained WeeklyPlan + canonical ShoppingList + occurrence-aware Recipe provenance`

## Endpoint

`POST /api/v1/weekly-plan-shopping-previews`

Success: `200 OK` with a transient calculation result. No resource is stored.

## Request

The client sends no UUIDs or server identities.

```json
{
  "occurrences": [
    {
      "day": "MONDAY",
      "targetServings": 4,
      "recipe": {
        "title": "Паста",
        "baseServings": 2,
        "ingredients": [
          {
            "requirement": "Молоко",
            "quantity": { "amount": 0.5, "unit": "LITER" }
          }
        ]
      }
    }
  ]
}
```

Rules:

- `occurrences`: required, ordered, `1..35`;
- occurrence: required `day`, positive integer `targetServings`, required `recipe`;
- `day`: accepted M3.1 `MONDAY..SUNDAY` only;
- recipe: title, positive integer base servings, `1..100` explicit ingredients;
- ingredient quantity remains positive decimal and reuses accepted Recipe/Shopping quantity units;
- unknown JSON fields, unknown enum values, non-integer servings and malformed JSON fail closed;
- occurrence order is caller order and is never sorted by day.

## Validation reuse

M3.2 must not duplicate accepted M2.2 Recipe validation semantics.

For every occurrence, the application factory adapts its nested Recipe input into an existing `RecipeShoppingPreviewRequest`, supplies the occurrence target servings, and delegates to `RecipeShoppingPreviewRequestFactory`.

M2.2 therefore remains authoritative for:

- Recipe title normalization/limits;
- positive integer base/target servings;
- ingredient count `1..100`;
- requirement normalization/limits;
- positive decimal quantity and supported units;
- transient Recipe and ingredient identity creation.

Nested M2.2 validation errors are translated into M3.2 fields such as `occurrences[0].recipe.title` and `occurrences[0].recipe.ingredients[0].quantity.amount`. The M3.2 public problem vocabulary remains its own boundary vocabulary.

The transient ShoppingListId produced internally by the M2.2 request factory is ignored; accepted M3.1 deterministically derives the weekly ShoppingListId from WeeklyPlanId.

## Server-owned identity

M3.2 creates:

- one transient `WeeklyPlanId` per request;
- one transient `WeeklyMealOccurrenceId` per occurrence;
- Recipe and RecipeIngredient identities through accepted M2.2 request construction;
- weekly ShoppingList identity through accepted M3.1 composition.

No client-supplied identity is accepted.

## Application composition

New package: `io.github.trueruslan.zakupgotov.weeklyplanpreview`.

Primary service flow:

1. validate/build request through `WeeklyPlanShoppingPreviewRequestFactory`;
2. construct accepted `WeeklyPlan` with generated occurrence IDs and generated Recipe inputs;
3. call `WeeklyPlanShoppingListComposer` exactly once;
4. project the accepted WeeklyPlan plus resulting ShoppingList and planner provenance;
5. validate that every public provenance source resolves to exactly one returned occurrence/Recipe/ingredient and every final ShoppingItem has non-empty provenance;
6. fail closed on impossible projection drift.

M3.2 does not reimplement Recipe scaling, unit canonicalization, merge, quantity summation, ordering or ShoppingItem identity.

## Response

```json
{
  "weeklyPlan": {
    "id": "...",
    "occurrences": [
      {
        "id": "...",
        "day": "MONDAY",
        "targetServings": 4,
        "recipe": {
          "id": "...",
          "title": "Паста",
          "baseServings": 2,
          "ingredients": [
            {
              "id": "...",
              "requirement": "Молоко",
              "quantity": { "amount": 0.5, "unit": "LITER" }
            }
          ]
        }
      }
    ]
  },
  "shoppingList": {
    "id": "...",
    "items": [
      {
        "id": "...",
        "requirement": "Молоко",
        "quantity": { "amount": 1000, "unit": "MILLILITER" },
        "sources": [
          {
            "occurrenceId": "...",
            "recipeId": "...",
            "recipeIngredientId": "..."
          }
        ]
      }
    ]
  }
}
```

Public provenance exposes only:

- `WeeklyMealOccurrenceId`;
- `RecipeId`;
- `RecipeIngredientId`.

`RecipeAggregationEntryId` remains internal and must never appear in the API/OpenAPI/client projection.

## Failure contract

Semantic request failures return `400` Problem Details with code:

`INVALID_WEEKLY_PLAN_SHOPPING_PREVIEW`

Errors are ordered and shaped as `{ field, message }`.

Malformed JSON, unknown fields/units/days and non-integer serving JSON use the same sanitized problem code and expose only `$request` as the field; Jackson/internal exception details never become public.

Impossible server-side composition/projection invariants are server defects and fail as 5xx, not client 400s.

## Contract and generated client

OpenAPI 3.1 is the source of truth.

Add:

- `/api/v1/weekly-plan-shopping-previews`;
- operation `createWeeklyPlanShoppingPreview`;
- request/response/problem component schemas;
- generated TypeScript types;
- exported path constant `WEEKLY_PLAN_SHOPPING_PREVIEWS_PATH`.

Generated-client freshness remains verified by existing contract CI.

## Architecture

Allowed project dependencies for `weeklyplanpreview`:

- accepted `weeklyplan`;
- accepted `recipepreview` application construction semantics;
- accepted `recipe` and neutral `shopping` value/projection types only where required to expose the self-contained result.

Forbidden direct dependencies:

- provider;
- retailer;
- matching;
- basket;
- comparison;
- preview/comparison application packages;
- database/persistence.

Accepted lower-level packages must not depend back on `weeklyplanpreview`.

## Non-goals

M3.2 does not add:

- persistence, saved plans or history;
- locality or retailer comparison orchestration;
- Weekly Planning UI;
- pantry/exclusions;
- nutrition;
- fixed breakfast/lunch/dinner/snack slot vocabulary;
- calendar week/date/time-zone semantics;
- fuzzy/synonym/AI interpretation;
- retailer/provider network traffic.

## Acceptance gates

M3.2 is ACCEPTED only after:

1. explicit RED→GREEN evidence for request/domain construction;
2. explicit RED→GREEN evidence for composition/provenance projection;
3. explicit RED→GREEN evidence for HTTP/problem contract;
4. synchronized OpenAPI/generated TypeScript client;
5. architecture guards and full Maven/Testcontainers/Modulith verification;
6. all 9 normal PR workflow groups succeed on one exact final head;
7. read-only review reports no unresolved P0/P1/P2 blocker and review threads are clear;
8. squash merge with expected-head protection;
9. all 8 normal push workflows succeed on the merged main SHA;
10. canonical PROJECT_STATE/ROADMAP/CHANGELOG acceptance synchronization.