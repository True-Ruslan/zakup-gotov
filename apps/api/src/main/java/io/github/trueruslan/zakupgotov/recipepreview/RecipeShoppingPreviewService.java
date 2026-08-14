package io.github.trueruslan.zakupgotov.recipepreview;

import io.github.trueruslan.zakupgotov.recipe.RecipeShoppingListConversion;
import io.github.trueruslan.zakupgotov.recipe.RecipeShoppingListConverter;
import java.util.ArrayList;
import java.util.HashSet;

public final class RecipeShoppingPreviewService {
    private final RecipeShoppingPreviewRequestFactory factory;
    private final RecipeShoppingListConverter converter;

    public RecipeShoppingPreviewService(
            RecipeShoppingPreviewRequestFactory factory,
            RecipeShoppingListConverter converter) {
        this.factory = factory;
        this.converter = converter;
    }

    public RecipeShoppingPreview create(RecipeShoppingPreviewRequest request) {
        var input = factory.create(request);
        var converted = converter.convert(
                input.recipe(), input.targetServings(), input.shoppingListId());
        return project(input, converted);
    }

    static RecipeShoppingPreview project(
            RecipeShoppingPreviewInput input,
            RecipeShoppingListConversion converted) {
        if (!converted.shoppingList().id().equals(input.shoppingListId())) {
            throw new IllegalStateException("unexpected shopping list id");
        }

        var sourceIds = new HashSet<java.util.UUID>();
        var source = input.recipe().ingredients().stream()
                .map(ingredient -> {
                    var ingredientId = ingredient.id().value();
                    if (!sourceIds.add(ingredientId)) {
                        throw new IllegalStateException("duplicate source ingredient id");
                    }
                    return new RecipeShoppingPreviewRecipeIngredient(
                            ingredientId,
                            ingredient.requirement().text(),
                            ingredient.quantity());
                })
                .toList();

        var items = new ArrayList<RecipeShoppingPreviewShoppingItem>();
        for (var item : converted.shoppingList().items()) {
            var refs = converted.provenance().get(item.id());
            if (refs == null || refs.isEmpty()) {
                throw new IllegalStateException("missing shopping item provenance");
            }
            var ingredientIds = new ArrayList<java.util.UUID>();
            for (var ref : refs) {
                if (!ref.recipeId().equals(input.recipe().id())) {
                    throw new IllegalStateException("cross-recipe provenance");
                }
                var ingredientId = ref.ingredientId().value();
                if (!sourceIds.contains(ingredientId)) {
                    throw new IllegalStateException("orphan source ingredient provenance");
                }
                ingredientIds.add(ingredientId);
            }
            items.add(new RecipeShoppingPreviewShoppingItem(
                    item.id().value(),
                    item.requirement().text(),
                    item.quantity(),
                    ingredientIds));
        }

        return new RecipeShoppingPreview(
                new RecipeShoppingPreviewRecipe(
                        input.recipe().id().value(),
                        input.recipe().title().value(),
                        input.recipe().baseServings().value(),
                        input.targetServings().value(),
                        source),
                new RecipeShoppingPreviewShoppingList(
                        converted.shoppingList().id().value(),
                        items));
    }
}
