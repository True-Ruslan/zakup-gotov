# M2.2 Recipe shopping preview acceptance — 2026-08-14

## Decision

**ACCEPTED.** M2.2 stateless Recipe application/API boundary is complete on merged `main` commit `8f0c1d8d31cfc1673656780a7989512d38788aff` (`feat(m2): add stateless recipe shopping preview API (#97)`).

Issue #96 is closed `completed`. PR #97 was squash-merged from exact reviewed head `318a48c569d0d001a4c27b5792e1681f7884e518` after all 9 normal PR workflow groups succeeded.

## Accepted product/application behavior

- stateless `POST /api/v1/recipe-shopping-previews`;
- server-owned transient Recipe, ingredient and ShoppingList UUID identities;
- normalized title/requirements, positive integer base/target servings and explicit quantities;
- recipe-scoped strict integer JSON binding for serving counts;
- accepted M2.1 `RecipeShoppingListConverter` remains the sole owner of serving scaling, canonical quantity conversion, exact-safe merge grouping, deterministic ordering and generated ShoppingItem identity;
- response returns canonical recipe ingredients, canonical ShoppingList items and ordered self-contained `sourceIngredientIds` provenance;
- missing/orphan/cross-recipe/mismatched-list provenance fails closed as an internal invariant failure;
- semantic validation and unreadable JSON produce sanitized `INVALID_RECIPE_SHOPPING_PREVIEW` 400 problems without leaking internal exception details;
- thin controller + controller-scoped advice; no catch-all remapping of internal failures to request errors;
- OpenAPI 3.1 contract and generated TypeScript client/schema are synchronized;
- architecture guards preserve `recipepreview → recipe → shopping` and prevent provider/retailer/matching/basket/comparison/database coupling;
- no recipe persistence, retailer traffic, Recipe→Comparison orchestration, fuzzy/AI matching or Recipe UI was introduced.

## Verification before merge

Final PR head: `318a48c569d0d001a4c27b5792e1681f7884e518`.

All 9 normal PR workflow groups succeeded:

- API CI;
- Contract CI;
- Web CI, including Web E2E/Playwright;
- Retailer Bridge CI;
- CodeQL;
- Dependency Review;
- Container Security CI;
- Release Contract CI;
- Release Bundle CI.

Independent read-only review verdict on that exact head: **Looks good**. The earlier P3 structural drift was corrected before final verification. Review threads were empty and no unresolved P0/P1/P2/P3 finding remained.

## Post-merge proof

Merged `main`: `8f0c1d8d31cfc1673656780a7989512d38788aff`.

GitHub created exactly 8 normal push-triggered workflows for that SHA. All **8/8 completed successfully** with zero failures:

- API CI;
- Contract CI;
- Web CI;
- Retailer Bridge CI;
- CodeQL;
- Container Security CI;
- Release Contract CI;
- Release Bundle CI.

This satisfies the project rule distinguishing implemented → tested → reviewed → merged → accepted.

## Next development target

Proceed to M2.3 deterministic composition:

`Recipe input → recipe-shopping preview → generated shopping requirements → comparison preview`

The next slice must compose the two accepted stateless boundaries without duplicating recipe/conversion/comparison semantics or weakening production-access and fail-closed invariants. Real Recipe UI follows only after the composed application flow is accepted.
