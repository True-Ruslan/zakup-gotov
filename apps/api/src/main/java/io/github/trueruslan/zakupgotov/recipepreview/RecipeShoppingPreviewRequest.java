package io.github.trueruslan.zakupgotov.recipepreview;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import java.util.List;
import tools.jackson.databind.annotation.JsonDeserialize;

public record RecipeShoppingPreviewRequest(
        String title,
        @JsonDeserialize(using = StrictIntegerDeserializer.class) Integer baseServings,
        @JsonDeserialize(using = StrictIntegerDeserializer.class) Integer targetServings,
        List<RecipeShoppingPreviewIngredientRequest> ingredients) {

    @JsonAnySetter
    public void rejectUnknownProperty(String property, Object value) {
        throw new IllegalArgumentException("Unknown recipe shopping preview property");
    }
}
