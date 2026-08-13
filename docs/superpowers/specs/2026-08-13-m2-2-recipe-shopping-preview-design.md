# M2.2 Stateless Recipe Shopping Preview — Design

Date: 2026-08-13
Issue: #96
Baseline: `main=3a20969a027ba7b0ee0075455857c78e23575e23`
Status: **DESIGN — implementation requires explicit approval of this file**

## Goal

Expose accepted M2.1 `Recipe → RecipeShoppingListConversion` semantics through a stateless, contract-first application/API boundary.

Flow:

`HTTP recipe request → validation/identity allocation → Recipe domain → RecipeShoppingListConverter → public ShoppingList projection`

No persistence, retailer acquisition, candidate suitability, comparison orchestration or recipe UI belongs to M2.2.

## Architecture

Add top-level application package `io.github.trueruslan.zakupgotov.recipepreview`.

Allowed dependency direction:

`recipepreview → recipe → shopping`, plus `recipepreview → shopping` for projection.

Forbidden: `recipe → recipepreview`, `shopping → recipe`, `shopping → recipepreview`, and any M2.2 production dependency on provider/retailer/matching/basket/comparison/database packages.

The existing `recipe` package stays pure: no Spring MVC, persistence or public HTTP DTOs.

## Endpoint

`POST /api/v1/recipe-shopping-previews`

Operation ID: `createRecipeShoppingPreview`.
Success: `200 OK` because this is a transient calculation, not persisted resource creation.

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

Request fields:
- `title: string`
- `baseServings: integer >= 1`
- `targetServings: integer >= 1`
- `ingredients: array`, 1..100
- ingredient: `requirement` + `quantity`
- quantity amount: positive decimal
- quantity unit reuses existing `QuantityInputUnit`: `PIECE`, `GRAM`, `KILOGRAM`, `MILLILITER`, `LITER`.

The client sends no Recipe/ingredient/ShoppingList/ShoppingItem UUIDs. M2.2 is transient and does not define synchronization/offline identity ownership.

## Identity allocation

Application layer owns transient IDs through an injectable abstraction conceptually equivalent to:

```java
interface RecipeShoppingPreviewIdGenerator {
    RecipeId nextRecipeId();
    RecipeIngredientId nextIngredientId();
    ShoppingListId nextShoppingListId();
}
```

Production uses fresh UUIDs. Tests use deterministic fixed/queued UUIDs.

Identical requests need not return identical UUIDs. They must return the same normalized title, canonical quantities, merge grouping, item order and provenance structure modulo generated identities. No idempotency-key contract is added.

## Application mapping

After validation, the service creates one Recipe ID, one ingredient ID per request ingredient in request order, and one ShoppingList ID. It constructs existing `RecipeTitle`, `RecipeServings`, `ShoppingRequirement`, `Quantity`, `RecipeIngredient`, `Recipe`, then delegates to accepted `RecipeShoppingListConverter`.

M2.2 must not duplicate scaling, canonical-unit conversion, merge-key, generated ShoppingItem identity, collision or provenance algorithms.

## Response

```json
{
  "recipe": {
    "id": "uuid",
    "title": "Курица с овощами",
    "baseServings": 2,
    "targetServings": 4
  },
  "shoppingList": {
    "id": "uuid",
    "items": [
      {
        "id": "uuid",
        "requirement": "курица",
        "quantity": {"amount": 1000, "unit": "GRAM"},
        "sourceIngredientIds": ["uuid"]
      }
    ]
  }
}
```

Response objects:
- recipe: generated ID, normalized title, base/target servings;
- shoppingList: generated ID + ordered items;
- item: generated ShoppingItem ID, normalized requirement, canonical quantity, ordered `sourceIngredientIds`.

Public provenance is item-local rather than a JSON object keyed by UUID. It is semantically equivalent to M2.1 `ShoppingItemId → ingredient IDs`, easier to type in OpenAPI/TypeScript, and does not leak internal `RecipeIngredientRef` structure.

Requests accept five units. Responses reuse existing `CanonicalQuantity`, therefore only `PIECE`, `GRAM`, `MILLILITER` are emitted. `0.5 KILOGRAM → 500 GRAM`, `1.5 LITER → 1500 MILLILITER`. Fractional `PIECE` is preserved; no hidden rounding.

## Ordering and merge semantics

- ingredient IDs allocated in request order;
- M2.1 group order follows first source occurrence;
- response items preserve ShoppingList order;
- `sourceIngredientIds` preserve ordered M2.1 provenance.

Merge remains exactly M2.1: exact normalized `ShoppingRequirement` equality plus equal canonical unit. No case folding, synonyms, fuzzy matching, category inference, embeddings, LLM, price or retailer data.

## Validation

Fail closed with deterministic field paths.

Limits:
- normalized title length 1..240;
- ingredients 1..100;
- normalized requirement length 1..240;
- base/target servings integer >=1;
- quantity amount non-null and >0;
- quantity unit non-null/known.

Explicit tests must cover null/blank/overlong title; null/empty/too-many ingredients; null ingredient; blank/overlong requirement; null quantity; null/zero/negative amount; null/unknown unit; zero/negative/non-integer servings; malformed JSON; unknown root/ingredient/quantity fields.

Where structural traversal is possible, multiple validation errors are accumulated in deterministic request order. Binding failures return one sanitized `$request` error.

## Public error contract

Dedicated `400 application/problem+json`:

- type: `https://zakup-gotov.dev/problems/invalid-recipe-shopping-preview`
- title: `Invalid recipe shopping preview request`
- status: `400`
- code: `INVALID_RECIPE_SHOPPING_PREVIEW`
- errors: ordered `{field,message}` list.

Public errors must not expose Java/Jackson internals, stack traces, ID-generator details, provider/database details or raw internal exception text.

Impossible server-side states are not relabeled as client 400 errors. Do not catch arbitrary `IllegalArgumentException`/`IllegalStateException` and convert them to validation failures. Only known request-validation failures become this problem type.

## Controller boundary

Controller only delegates. It does not normalize, allocate IDs or construct domain objects.

A controller-scoped advice maps known request-validation failures and `HttpMessageNotReadableException`. Unknown/internal failures remain fail-closed server errors.

## OpenAPI and TypeScript client

`openapi/zakup-gotov.yaml` remains source of truth. Add the new operation and explicit `additionalProperties: false` schemas. Reuse `QuantityInputUnit`, `CanonicalQuantity`, `CanonicalQuantityUnit` instead of duplicating units.

Regenerate `packages/api-client/src/schema.d.ts` with the repository generator; never hand-edit it.

Add `RECIPE_SHOPPING_PREVIEWS_PATH = "/api/v1/recipe-shopping-previews"` to the client surface.

Client TDD must first fail because the typed path/operation/schema does not exist, then become green after OpenAPI + generated schema synchronization.

## TDD plan requirements

Implementation plan must preserve separate RED→GREEN cycles:

1. **Request/application mapping:** bounds/null/blank rules, multi-error ordering, normalization, five input units, server-owned IDs.
2. **Service/projection:** fixed injected IDs, scaling, kg/g and l/ml canonicalization, exact-safe merge/non-merge, first-occurrence order, complete ordered provenance, fractional PIECE.
3. **Controller:** success JSON, dedicated problem JSON, malformed/unknown fields, unknown enum, sanitized errors.
4. **Architecture:** Shopping Core stays recipe/recipepreview-agnostic; recipe stays recipepreview-agnostic; recipepreview has no provider/retailer/matching/basket/comparison/database dependency; Modulith remains green.
5. **OpenAPI/client:** typed path/request/response RED, then generated schema freshness/typecheck/Vitest/build GREEN.

Every RED must be executed and shown to fail for the intended missing/incorrect behavior before production code closes it.

## Testcontainers policy

M2.2 intentionally has no database path, migration, repository, transaction, query or DB invariant. Adding persistence merely to manufacture a Testcontainers test would broaden product semantics and create dishonest coverage.

Therefore no M2.2-specific database production code/test is added. Full backend `./mvnw verify` still runs the existing PostgreSQL/Testcontainers integration baseline. The first real Recipe-persistence slice must begin with Testcontainers RED tests for migrations, repository round-trip, constraints, transactions and any concurrency semantics.

## Frontend and Playwright policy

M2.2 changes the generated TypeScript contract but adds no browser-visible Recipe UI. Frontend-facing contract is TDD-covered in `packages/api-client`, while existing Web unit/build/Playwright regression must remain green.

Do not create a fake screen only to claim a Playwright test.

In the next real frontend vertical slice, Playwright is RED-first and must cover desktop/mobile recipe entry, ingredient row add/remove, servings, quantity/unit editing, generated list, validation/API failures, and transition into comparison. Component behavior is likewise frontend TDD RED→GREEN.

## Verification and shipping

Before shipping:
- focused backend unit tests after every cycle;
- controller tests;
- architecture tests;
- full Maven `verify` including Modulith and existing PostgreSQL/Testcontainers;
- generated-schema freshness check;
- API-client typecheck, Vitest and build;
- existing Web CI/Playwright regression;
- CodeQL Java + JS/TS, Dependency Review, Container Security, Retailer Bridge, Release Contract and Release Bundle CI;
- independent diff/spec review with no unresolved P0/P1/P2;
- final exact PR head green across all normal workflow groups;
- squash merge with expected head SHA;
- resulting `main` green on all normal push workflows.

Only after post-merge main proof may #96/M2.2 be marked COMPLETE / ACCEPTED. Keep statuses distinct: implemented → tested → reviewed → shipping → merged → accepted.

## Security/privacy/operational invariants

M2.2 is retailer-network-free, address/location-free, credential/session-free and persistence-free. It must not log raw recipe request bodies by default. User recipe text stays user-controlled data. Public response contains no provider/acquisition/fulfillment identifiers.

## Non-goals

No persisted Recipe CRUD/history/accounts/sync; no Recipe→Comparison orchestration; no locality/store selection; no recipe UI; no new browser behavior for absent UI; no retailer traffic; no candidate retrieval/suitability/fuzzy/synonym/AI layer; no package-optimizer change; no nutrition/pantry logic; no fractional serving-count input; no multi-recipe aggregation.

Fractional ingredient quantities remain valid; only serving counts are positive integers in M2.2.

## Acceptance criteria

M2.2 is accepted only if the endpoint, server-owned transient identities, exhaustive fail-closed validation, canonical response quantities, exact M2.1 scaling/merge/order/provenance, sanitized problem responses, architecture direction, OpenAPI/generated-client synchronization, RED→GREEN evidence, full Maven/Testcontainers/Modulith verification, existing Web/Playwright regression, all PR gates, clean review, exact-head merge, and post-merge main checks are all proven.

## Next slice

After M2.2 acceptance, compose:

`Recipe input → recipe-shopping preview → generated shopping requirements → comparison preview`.

Then add the real responsive recipe UI with frontend TDD and desktop/mobile Playwright. Candidate Retrieval & Suitability remains a later dedicated semantic layer.