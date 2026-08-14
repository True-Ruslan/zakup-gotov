# M2.2 Stateless Recipe Shopping Preview — Design v2

Date: 2026-08-13  
Issue: #96  
Baseline: `main=3a20969a027ba7b0ee0075455857c78e23575e23`  
Status: **AUTHORITATIVE DESIGN — implementation requires explicit user approval**

This v2 supersedes the first draft after self-review found that provenance IDs must be resolvable inside the same response.

## Goal and boundary

Expose accepted M2.1 `Recipe → RecipeShoppingListConversion` through a stateless contract-first API:

`HTTP recipe request → validation + server IDs → Recipe domain → RecipeShoppingListConverter → ShoppingList projection`

Create package `io.github.trueruslan.zakupgotov.recipepreview`.

Allowed dependencies: `recipepreview → recipe → shopping` and `recipepreview → shopping`. Shopping Core must not depend on Recipe/recipepreview; Recipe must not depend on recipepreview. M2.2 must not depend on provider, retailer, matching, basket, comparison or database packages.

No persistence, retailer calls, candidate suitability, comparison orchestration or recipe UI.

## Endpoint

`POST /api/v1/recipe-shopping-previews`  
Operation ID: `createRecipeShoppingPreview`  
Success: `200 OK` because this is a transient calculation, not creation of a persisted resource.

## Request

```json
{
  "title": "Курица с овощами",
  "baseServings": 2,
  "targetServings": 4,
  "ingredients": [
    {"requirement": "курица", "quantity": {"amount": 500, "unit": "GRAM"}},
    {"requirement": "лук", "quantity": {"amount": 2, "unit": "PIECE"}},
    {"requirement": "морковь", "quantity": {"amount": 0.3, "unit": "KILOGRAM"}}
  ]
}
```

Rules:
- normalized title length `1..240`;
- integer `baseServings >= 1` and `targetServings >= 1`;
- ingredients `1..100`;
- normalized requirement length `1..240`;
- quantity amount is a positive decimal;
- unit reuses existing `QuantityInputUnit`: `PIECE`, `GRAM`, `KILOGRAM`, `MILLILITER`, `LITER`;
- client sends no Recipe, ingredient, ShoppingList or ShoppingItem UUIDs.

## Server-owned transient identities

Application layer owns IDs through an injectable generator equivalent to:

```java
interface RecipeShoppingPreviewIdGenerator {
    RecipeId nextRecipeId();
    RecipeIngredientId nextIngredientId();
    ShoppingListId nextShoppingListId();
}
```

Production uses fresh UUIDs; tests use fixed/queued UUIDs for exact assertions.

Identical requests need not return identical UUIDs. They must return identical normalized/canonical business semantics, merge grouping, order and provenance modulo generated IDs. No idempotency-key semantics are introduced.

The application creates one Recipe ID, one ingredient ID per request ingredient in request order, and one ShoppingList ID, then constructs existing Recipe/Shopping value objects and delegates to the accepted converter. It must not duplicate M2.1 scaling, unit normalization, merge-key, ShoppingItem identity, collision or provenance logic.

## Self-contained response

```json
{
  "recipe": {
    "id": "recipe-uuid",
    "title": "Курица с овощами",
    "baseServings": 2,
    "targetServings": 4,
    "ingredients": [
      {
        "id": "ingredient-a",
        "requirement": "курица",
        "quantity": {"amount": 500, "unit": "GRAM"}
      },
      {
        "id": "ingredient-b",
        "requirement": "лук",
        "quantity": {"amount": 2, "unit": "PIECE"}
      }
    ]
  },
  "shoppingList": {
    "id": "list-uuid",
    "items": [
      {
        "id": "item-uuid",
        "requirement": "курица",
        "quantity": {"amount": 1000, "unit": "GRAM"},
        "sourceIngredientIds": ["ingredient-a"]
      }
    ]
  }
}
```

Response guarantees:
- `recipe.ingredients` preserves request order;
- returned recipe ingredient quantities are canonical base-recipe quantities;
- shopping items contain canonical scaled/merged quantities;
- every `sourceIngredientIds` entry resolves to exactly one `recipe.ingredients[].id` in the same response;
- no orphan provenance ID is allowed;
- every shopping item has at least one source ingredient;
- source ingredient IDs preserve M2.1 provenance order;
- shopping item order preserves first merge-group occurrence.

Public provenance is item-local instead of a JSON object keyed by UUID. This is equivalent to `ShoppingItemId → ingredient IDs`, simpler for OpenAPI/TypeScript, and does not leak internal `RecipeIngredientRef` shape.

All response quantities reuse existing `CanonicalQuantity`, so output units are only `PIECE`, `GRAM`, `MILLILITER`. `0.5 KILOGRAM → 500 GRAM`; `1.5 LITER → 1500 MILLILITER`. Fractional `PIECE` is preserved without hidden rounding.

## Matching/merge semantics

M2.2 does not reinterpret ingredient meaning. Merge remains exactly M2.1: equal normalized `ShoppingRequirement` plus equal canonical quantity unit.

No case folding, synonym logic, fuzzy matching, categories, embeddings, LLM, price or retailer evidence is added. Candidate Retrieval & Suitability remains a later explicit layer.

## Validation and public errors

Known request errors produce `400 application/problem+json`:

- type: `https://zakup-gotov.dev/problems/invalid-recipe-shopping-preview`
- title: `Invalid recipe shopping preview request`
- status: `400`
- code: `INVALID_RECIPE_SHOPPING_PREVIEW`
- errors: ordered `{field,message}` array.

Tests must cover null/blank/overlong title; null/empty/>100 ingredients; null ingredient; blank/overlong requirement; null quantity; null/zero/negative amount; null/unknown unit; zero/negative/non-integer servings; malformed JSON; unknown root/ingredient/quantity properties.

When traversal is possible, multiple errors accumulate deterministically in request order. Binding/malformed failures return one sanitized `$request` error.

Public errors must not expose stack traces, Java/Jackson internals, ID-generator details, provider/database information or arbitrary internal exception text.

Impossible server-side states are not mislabeled as user errors. Do not catch arbitrary `IllegalArgumentException` or `IllegalStateException` and convert them to 400. Only known request-validation failures map to the dedicated problem.

Controller only delegates. A controller-scoped advice maps known validation failures and unreadable request bodies.

## OpenAPI/generated client

`openapi/zakup-gotov.yaml` remains source of truth. New objects use `additionalProperties: false` and explicit required fields. Reuse existing `QuantityInputUnit`, `CanonicalQuantity`, `CanonicalQuantityUnit`.

Regenerate `packages/api-client/src/schema.d.ts` with the existing generator; never edit generated schema manually.

Expose `RECIPE_SHOPPING_PREVIEWS_PATH = "/api/v1/recipe-shopping-previews"` from the API client.

TypeScript contract TDD must first fail because the path/operation/request/response does not exist, then pass only after OpenAPI regeneration and client synchronization.

## Required TDD cycles

1. **Request/mapping RED→GREEN:** validation bounds, deterministic error ordering, normalization, five input units, server-owned IDs.
2. **Service/projection RED→GREEN:** fixed generated IDs, returned source ingredient IDs/order, scaling, kg/g and l/ml canonicalization, merge/non-merge, first-occurrence order, complete/no-orphan provenance, fractional PIECE.
3. **Controller RED→GREEN:** successful JSON, dedicated problem JSON, malformed/unknown fields, unknown enum, sanitized failures.
4. **Architecture RED→GREEN:** Shopping Core stays Recipe-free; Recipe stays recipepreview-free; recipepreview has no provider/retailer/matching/basket/comparison/database dependency; Spring Modulith verification passes.
5. **OpenAPI/client RED→GREEN:** typed path and self-contained provenance response fail first, then generated-schema freshness, typecheck, Vitest and build pass.

Every RED must be executed and fail for the intended missing/incorrect behavior before production code is added.

## Testcontainers

M2.2 has no database behavior. Adding persistence merely to obtain a Testcontainers test would be dishonest scope expansion.

No M2.2-specific database production code/test is added. Full backend `./mvnw verify` still executes the repository's existing PostgreSQL/Testcontainers baseline. The first real Recipe-persistence slice must start with Testcontainers RED tests for migration, repository round-trip, constraints, transactions and applicable concurrency semantics.

## Frontend and Playwright

M2.2 changes the generated frontend contract but introduces no browser-visible Recipe UI. `packages/api-client` gets real TDD coverage; existing Web unit/build/Playwright regression must remain green.

Do not fabricate a screen only to claim Playwright coverage.

The next real frontend vertical slice must use frontend RED→GREEN and Playwright RED-first for desktop/mobile: recipe title/servings, ingredient add/remove, quantity/unit editing, generated list, validation/API failures and transition into comparison.

## Verification and acceptance

Required before merge:
- focused backend unit tests for each RED/GREEN cycle;
- controller and architecture tests;
- full Maven `verify`, including Modulith and existing PostgreSQL/Testcontainers;
- generated-schema freshness check;
- API-client typecheck, Vitest and build;
- existing Web CI and Playwright regression;
- CodeQL Java + JS/TS, Dependency Review, Container Security, Retailer Bridge, Release Contract and Release Bundle CI;
- independent review with no unresolved P0/P1/P2;
- final exact PR head green across all normal workflow groups.

Then squash-merge using the reviewed exact head and require all normal post-merge `main` workflows to succeed. Only after that may #96 be marked **COMPLETE / ACCEPTED**. Preserve state distinctions: implemented → tested → reviewed → shipping → merged → accepted.

## Security/privacy

M2.2 remains retailer-network-free, location/address-free, credential/session-free and persistence-free. Do not log raw recipe request bodies by default. User recipe text is user-controlled data. Public output exposes no provider/acquisition/fulfillment identifiers.

## Non-goals

No persisted Recipe CRUD/history/sync; no Recipe→Comparison call; no locality/store selection; no recipe UI; no new browser behavior for absent UI; no retailer traffic; no candidate retrieval/suitability/fuzzy/synonym/AI logic; no package optimizer changes; no nutrition/pantry logic; no fractional serving-count input; no multi-recipe aggregation.

Fractional ingredient quantities remain valid; only serving counts are positive integers.

## Next slice

After M2.2 acceptance:

`Recipe input → recipe-shopping preview → generated shopping requirements → comparison preview`.

Then implement the real responsive Recipe UI with frontend TDD and desktop/mobile Playwright. Candidate Retrieval & Suitability remains a later dedicated semantic layer.