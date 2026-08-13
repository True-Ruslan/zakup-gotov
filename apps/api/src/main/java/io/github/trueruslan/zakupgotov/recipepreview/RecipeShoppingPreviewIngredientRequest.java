package io.github.trueruslan.zakupgotov.recipepreview;

public record RecipeShoppingPreviewIngredientRequest(
        String requirement,
        RecipeShoppingPreviewQuantityRequest quantity) {}
