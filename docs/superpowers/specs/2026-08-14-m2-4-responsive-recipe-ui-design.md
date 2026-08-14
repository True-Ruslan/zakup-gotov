# M2.4 Responsive Recipe UI — Design

Date: 2026-08-14  
Issue: #103  
Baseline: `main=09db5801ed03c3a9243f1a6e7285dbd7c6af3489`  
Status: **AUTHORITATIVE DESIGN — approved for implementation by the user**

## Goal

Deliver the first real responsive Recipe experience on the existing Zakup Gotov web surface:

`Recipe title/servings + ingredient editing + locality → POST /api/v1/recipe-comparison-previews → generated shopping requirements + truthful retailer comparison`

M2.4 is a frontend vertical slice over the accepted M2.3 API. It must not recreate Recipe conversion or comparison semantics in the browser.

## Product position

Recipe-first is now the primary user journey. The existing manual basket comparison remains available below it as a secondary entry path and regression surface.

The page keeps the existing restrained Zakup Gotov visual system: stone palette, readable typography, rounded inputs/panels, responsive single-column flow and existing retailer result cards. M2.4 is not a redesign and introduces no decorative imagery or unrelated marketing sections.

## Primary form

The Recipe form contains:

- Recipe title, required, max 240 characters;
- base servings, positive JSON integer;
- target servings, positive JSON integer;
- locality, required, max 160 characters;
- 1..100 ingredient rows;
- ingredient requirement, required, max 240 characters;
- positive decimal quantity;
- unit from generated `QuantityInputUnit`: `PIECE`, `GRAM`, `KILOGRAM`, `MILLILITER`, `LITER`;
- add ingredient;
- remove ingredient, disabled while only one row remains;
- submit action `Сравнить рецепт`.

Initial local UI state:

- title empty;
- base servings `2`;
- target servings `2`;
- locality empty;
- one ingredient with amount `1` and unit `PIECE`.

The browser owns only form state and user-friendly preflight checks. Backend validation remains authoritative.

## Generated result

On success, the UI renders two ordered sections:

1. `Список покупок из рецепта` using `recipeShoppingPreview.shoppingList.items`;
2. the existing `ComparisonPreviewResults` using `comparisonPreview` unchanged.

Generated shopping items display normalized requirement and canonical quantity. Raw Recipe, ingredient, ShoppingList and ShoppingItem UUIDs are not shown because they are not useful to the user, though they remain present in the typed response and are preserved by M2.3.

No cheapest/best/recommended winner is invented. `READY`, `UNCERTAIN`, `INCOMPLETE` and `UNAVAILABLE` retailer states continue to use the accepted comparison result renderer.

## Transport boundary

Create one server action using only the generated API client:

- `RECIPE_COMPARISON_PREVIEWS_PATH`;
- `components["schemas"]["RecipeComparisonPreviewRequest"]`;
- `components["schemas"]["RecipeComparisonPreviewResponse"]`;
- generated 400 problem union.

Do not define duplicate backend request/response DTOs in web code.

The action uses the existing `API_BASE_URL` pattern and a finite timeout. It maps:

- 200 → `ready`;
- 400 problem union → `invalid` with product-safe field/message errors;
- missing configuration, timeout, network failure and unexpected status → `unavailable`.

Do not surface problem `type`, stack traces, provider internals or arbitrary server exception text.

## Error UX

Client preflight covers:

- missing title;
- missing locality;
- non-positive/non-integer base/target servings;
- missing ingredient requirement;
- non-positive/non-finite quantity.

Known backend 400 errors render one accessible `role=alert` summary based on generated problem `errors`.

Unavailable service renders one accessible alert and no fabricated generated shopping list or comparison results.

## Responsive and accessibility

- no horizontal overflow at desktop or mobile widths;
- labels are programmatically associated with every field;
- keyboard focus remains visible;
- add/remove/submit are real buttons with minimum comfortable hit area;
- ingredient rows collapse to a vertical layout on narrow screens;
- base/target servings stay legible and editable on mobile;
- pending submit state is explicit and prevents duplicate submission;
- existing comparison cards continue their responsive one/two-column behavior.

## TDD / browser acceptance

Frontend implementation is RED-first.

Required component/server-action coverage:

- request shape uses generated M2.3 contract;
- default servings and ingredient row;
- ingredient add/remove;
- quantity/unit editing;
- serving changes;
- local validation;
- success result shows generated shopping requirements and comparison result;
- 400 and unavailable states do not fabricate results.

Required Playwright RED-first scenarios:

- desktop Recipe journey with at least two ingredients, serving scaling and generated shopping-list output;
- mobile Recipe journey with no horizontal overflow;
- unavailable API produces one accessible error and no results;
- keyboard-visible focus path through the Recipe form;
- existing manual basket comparison journey remains functional.

The deterministic E2E mock gains `POST /api/v1/recipe-comparison-previews`. It may deterministically emulate the accepted contract for browser testing, but production web code must never contain fixture retailer data.

## Architecture / security / privacy

- Recipe UI depends on the generated API client, not backend implementation packages;
- no retailer credentials, browser bridge, provider IDs, SKUs, acquisition modes or fulfillment-context IDs enter client code;
- locality only; no exact-address UI in M2.4;
- no request persistence/local storage by default;
- no analytics payload containing recipe/ingredient text is introduced;
- no live retailer requests originate from browser tests.

## Non-goals

No saved Recipe CRUD/history, accounts, persistence, pantry state, multi-recipe aggregation, meal planning, nutrition, fuzzy/synonym/AI ingredient parsing, recipe import, exact-address/store picker, retailer activation, provider/acquisition changes, cheapest-retailer recommendation or visual redesign.

## Acceptance

Before merge require:

- explicit component and Playwright RED checkpoints for the new journey;
- Web unit tests, typecheck, lint and build green;
- Playwright desktop/mobile regression green against deterministic mock API;
- API/Contract regression green despite no backend behavior changes;
- CodeQL, Dependency Review, Container Security, Retailer Bridge, Release Contract and Release Bundle green;
- read-only review with no unresolved P0/P1/P2;
- exact PR head green across all normal workflow groups;
- squash merge with expected-head protection;
- all normal post-merge `main` workflows green before M2.4 becomes COMPLETE / ACCEPTED.
