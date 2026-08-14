package io.github.trueruslan.zakupgotov.weeklyplanpreview;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import io.github.trueruslan.zakupgotov.recipepreview.RecipeShoppingPreviewIngredientRequest;
import java.util.List;
import tools.jackson.databind.annotation.JsonDeserialize;

public record WeeklyPlanShoppingPreviewRecipeRequest(
        String title,
        @JsonDeserialize(using = WeeklyPlanStrictIntegerDeserializer.class) Integer baseServings,
        List<RecipeShoppingPreviewIngredientRequest> ingredients) {

    @JsonAnySetter
    public void rejectUnknownProperty(String property, Object value) {
        throw new IllegalArgumentException("Unknown weekly plan shopping preview recipe property");
    }
}
