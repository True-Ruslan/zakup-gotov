package io.github.trueruslan.zakupgotov.recipecomparisonpreview;

import io.github.trueruslan.zakupgotov.recipepreview.RecipeShoppingPreviewRequest;

public record RecipeComparisonPreviewRequest(
        String locality,
        RecipeShoppingPreviewRequest recipe) {}
