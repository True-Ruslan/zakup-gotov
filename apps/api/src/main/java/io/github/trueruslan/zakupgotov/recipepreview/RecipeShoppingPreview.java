package io.github.trueruslan.zakupgotov.recipepreview;

import java.util.Objects;

public record RecipeShoppingPreview(
        RecipeShoppingPreviewRecipe recipe,
        RecipeShoppingPreviewShoppingList shoppingList) {

    public RecipeShoppingPreview {
        Objects.requireNonNull(recipe, "recipe must not be null");
        Objects.requireNonNull(shoppingList, "shoppingList must not be null");
    }
}
