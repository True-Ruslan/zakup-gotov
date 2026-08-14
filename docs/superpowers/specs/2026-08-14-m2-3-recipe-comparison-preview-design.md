# M2.3 Recipe → Comparison Preview Composition — Design

Date: 2026-08-14  
Issue: #100  
Baseline: `main=bcfa16e1497f72cc36aa379e0effb75b0c2f3532`  
Status: **AUTHORITATIVE DESIGN — approved by user on 2026-08-14**

## Goal

Deliver the next deterministic M2 vertical slice:

`Recipe input → RecipeShoppingPreview → generated canonical shopping items → ComparisonPreview`

The slice composes the two accepted stateless application boundaries from M2.2 and M1 without duplicating recipe conversion or comparison semantics.

## Public endpoint

`POST /api/v1/recipe-comparison-previews`  
Operation ID: `createRecipeComparisonPreview`  
Success: `200 OK` because the result is transient and not persisted.

Request:

```json
{
  "locality": "Москва",
  "recipe": {
    "title": "Курица с овощами",
    "baseServings": 2,
    "targetServings": 4,
    "ingredients": [
      {"requirement": "курица", "quantity": {"amount": 500, "unit": "GRAM"}},
      {"requirement": "лук", "quantity": {"amount": 2, "unit": "PIECE"}}
    ]
  }
}
```

Response:

```json
{
  "recipeShoppingPreview": {
    "recipe": {},
    "shoppingList": {}
  },
  "comparisonPreview": {
    "locality": "Москва",
    "items": [],
    "retailers": []
  }
}
```

The nested Recipe request and both nested response projections reuse the already accepted application DTOs and their OpenAPI schemas rather than redefining parallel shapes.

## Composition rules

1. The new service calls `RecipeShoppingPreviewService.create(request.recipe())` exactly once.
2. It constructs `ComparisonPreviewItemRequest` values only from `recipeShoppingPreview.shoppingList().items()`.
3. Each generated shopping item UUID is preserved unchanged as the comparison request item UUID.
4. Requirement text is preserved unchanged from the recipe shopping projection.
5. Canonical quantity amount/unit is preserved unchanged from the recipe shopping projection.
6. The service calls `ComparisonPreviewService.create(...)` exactly once with the normalized locality and generated items.
7. The service never reconstructs Recipe domain objects, never rescales servings and never recalculates recipe merge/provenance logic.
8. The service never performs matching, basket planning, retailer-state assembly or runtime evidence acquisition itself.
9. Existing comparison production-access gating remains authoritative and occurs before runtime evidence acquisition inside the accepted comparison service.
10. Ordinary CI remains retailer-network-free.

## Cross-boundary invariants

The composition must fail closed if impossible drift appears between the generated recipe shopping projection and comparison result.

For every index `i`:

- `recipeShoppingPreview.shoppingList.items[i].id == comparisonPreview.items[i].id`;
- requirement text is identical;
- canonical quantity is identical;
- list cardinality and order are identical.

The composed response keeps M2.2 provenance self-contained: every `sourceIngredientIds` value still resolves inside `recipeShoppingPreview.recipe.ingredients`. Comparison does not duplicate or reinterpret Recipe provenance.

## Locality and validation

The composed request owns `locality` because Recipe preview intentionally has no location context while comparison requires provider-neutral locality.

Locality validation must reuse the comparison request boundary rather than introducing a second normalization vocabulary. Blank or over-limit locality therefore produces the existing comparison-preview validation problem.

Recipe validation/binding failures remain the existing Recipe-shopping-preview validation semantics. The composed controller must sanitize malformed/unknown input and must not expose stack traces or internal exception text.

## Architecture

Create package `io.github.trueruslan.zakupgotov.recipecomparisonpreview`.

Allowed direct dependencies:

- `recipecomparisonpreview → recipepreview`;
- `recipecomparisonpreview → preview`.

Forbidden direct dependencies:

- `recipe`;
- `shopping`;
- `provider`;
- `retailer`;
- `matching`;
- `basket`;
- `comparison`;
- `database`/persistence.

The application boundary composes accepted application services, not lower-level domain internals. Existing lower-level dependency graphs remain unchanged.

## Production composition

Spring configuration injects the accepted `RecipeShoppingPreviewService` and `ComparisonPreviewService` into the new composition service. No new runtime evidence adapter is added. Production remains fail-closed exactly as comparison preview is today.

## Error contract

Known malformed composed request/binding errors return `400 application/problem+json` with:

- type: `https://zakup-gotov.dev/problems/invalid-recipe-comparison-preview`;
- title: `Invalid recipe comparison preview request`;
- status: `400`;
- code: `INVALID_RECIPE_COMPARISON_PREVIEW`;
- one sanitized `$request` error for unreadable/unknown composed-wrapper JSON.

Nested request semantic validation continues to be emitted by the accepted nested service problems where possible. Internal invariant failures must remain server errors and must never be relabeled as user 400 responses.

## OpenAPI and generated client

`openapi/zakup-gotov.yaml` remains source of truth.

Add the new path and wrapper schemas while referencing existing Recipe-shopping and Comparison-preview schemas. Use `additionalProperties: false` and explicit required fields.

Regenerate `packages/api-client/src/schema.d.ts`; never edit generated schema manually. Export:

```ts
export const RECIPE_COMPARISON_PREVIEWS_PATH = "/api/v1/recipe-comparison-previews" as const;
```

## Required TDD cycles

1. **Service composition RED→GREEN:** preservation of shopping-item ID/order/requirement/canonical quantity and returned nested projections.
2. **Invariant RED→GREEN:** mismatch/cardinality/order drift fails closed.
3. **Controller RED→GREEN:** success JSON and sanitized wrapper binding failures.
4. **Architecture RED→GREEN:** only accepted application-boundary dependencies.
5. **OpenAPI/client RED→GREEN:** path, request/response types and generated-schema freshness.

Every behavior-changing production step starts with a failing test executed against the branch before implementation.

## Verification and acceptance

Before merge:

- focused M2.3 tests pass;
- full `./mvnw verify` passes, including Modulith and PostgreSQL/Testcontainers baseline;
- OpenAPI generated-schema freshness, API-client typecheck/Vitest/build pass;
- existing Web unit/build/Playwright regression remains green;
- CodeQL Java + JS/TS, Dependency Review, Container Security, Retailer Bridge, Release Contract and Release Bundle CI are green;
- independent review has no unresolved P0/P1/P2;
- final exact PR head is green across all normal PR workflow groups.

Then squash-merge the reviewed exact head. Mark #100 and M2.3 COMPLETE / ACCEPTED only after all normal post-merge `main` workflows succeed.

## Non-goals

No Recipe persistence/CRUD/history/sync, saved recipes, recipe UI, fuzzy/synonym/semantic/AI ingredient matching, retailer activation, provider-specific acquisition changes, multi-recipe aggregation, exact-address input, database changes, ranking redesign or package optimizer changes.

## Next slice

After M2.3 acceptance, implement the real responsive Recipe UI with frontend component TDD and desktop/mobile Playwright RED-first, using this composed endpoint as its primary product boundary.
