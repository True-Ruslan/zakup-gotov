package io.github.trueruslan.zakupgotov.weeklyplanpreview;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import io.github.trueruslan.zakupgotov.weeklyplan.WeeklyPlanDay;
import tools.jackson.databind.annotation.JsonDeserialize;

public record WeeklyPlanShoppingPreviewOccurrenceRequest(
        WeeklyPlanDay day,
        @JsonDeserialize(using = WeeklyPlanStrictIntegerDeserializer.class) Integer targetServings,
        WeeklyPlanShoppingPreviewRecipeRequest recipe) {

    @JsonAnySetter
    public void rejectUnknownProperty(String property, Object value) {
        throw new IllegalArgumentException("Unknown weekly plan shopping preview occurrence property");
    }
}
