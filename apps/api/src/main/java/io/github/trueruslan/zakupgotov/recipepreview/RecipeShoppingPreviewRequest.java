package io.github.trueruslan.zakupgotov.recipepreview;

import java.util.List;

public record RecipeShoppingPreviewRequest(
        String title,
        Integer baseServings,
        Integer targetServings,
        List<RecipeShoppingPreviewIngredientRequest> ingredients) {}
