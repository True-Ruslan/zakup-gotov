package io.github.trueruslan.zakupgotov.recipepreview;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record RecipeShoppingPreviewRecipe(
        UUID id,
        String title,
        int baseServings,
        int targetServings,
        List<RecipeShoppingPreviewRecipeIngredient> ingredients) {

    public RecipeShoppingPreviewRecipe {
        Objects.requireNonNull(id, "id must not be null");
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("title must not be blank");
        }
        if (baseServings <= 0 || targetServings <= 0) {
            throw new IllegalArgumentException("servings must be greater than 0");
        }
        ingredients = List.copyOf(Objects.requireNonNull(ingredients, "ingredients must not be null"));
        if (ingredients.isEmpty()) {
            throw new IllegalArgumentException("ingredients must not be empty");
        }
    }
}
