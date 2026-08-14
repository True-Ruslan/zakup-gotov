package io.github.trueruslan.zakupgotov.weeklyplanpreview;

import io.github.trueruslan.zakupgotov.weeklyplan.WeeklyPlanDay;
import java.util.Objects;
import java.util.UUID;

public record WeeklyPlanShoppingPreviewOccurrence(
        UUID id,
        WeeklyPlanDay day,
        int targetServings,
        WeeklyPlanShoppingPreviewRecipe recipe) {
    public WeeklyPlanShoppingPreviewOccurrence {
        id = Objects.requireNonNull(id, "id must not be null");
        day = Objects.requireNonNull(day, "day must not be null");
        if (targetServings <= 0) {
            throw new IllegalArgumentException("targetServings must be greater than 0");
        }
        recipe = Objects.requireNonNull(recipe, "recipe must not be null");
    }
}
