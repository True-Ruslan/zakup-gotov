package io.github.trueruslan.zakupgotov.recipecomparisonpreview;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import io.github.trueruslan.zakupgotov.recipepreview.RecipeShoppingPreviewRequest;

public record RecipeComparisonPreviewRequest(
        String locality,
        RecipeShoppingPreviewRequest recipe) {

    @JsonAnySetter
    public void rejectUnknownProperty(String property, Object value) {
        throw new IllegalArgumentException("Unknown recipe comparison preview property");
    }
}
