package io.github.trueruslan.zakupgotov.recipepreview;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import java.util.List;

public record RecipeShoppingPreviewRequest(
        String title,
        Integer baseServings,
        Integer targetServings,
        List<RecipeShoppingPreviewIngredientRequest> ingredients) {

    @JsonAnySetter
    public void rejectUnknownProperty(String property, Object value) {
        throw new IllegalArgumentException("Unknown recipe shopping preview property");
    }
}
