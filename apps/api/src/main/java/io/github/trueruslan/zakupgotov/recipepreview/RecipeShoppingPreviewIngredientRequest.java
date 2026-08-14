package io.github.trueruslan.zakupgotov.recipepreview;

import com.fasterxml.jackson.annotation.JsonAnySetter;

public record RecipeShoppingPreviewIngredientRequest(
        String requirement,
        RecipeShoppingPreviewQuantityRequest quantity) {

    @JsonAnySetter
    public void rejectUnknownProperty(String property, Object value) {
        throw new IllegalArgumentException("Unknown recipe shopping preview property");
    }
}
