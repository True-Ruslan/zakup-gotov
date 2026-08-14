package io.github.trueruslan.zakupgotov.weeklyplanpreview;

import java.util.Objects;
import java.util.UUID;

public record WeeklyPlanShoppingPreviewSource(
        UUID occurrenceId,
        UUID recipeId,
        UUID recipeIngredientId) {
    public WeeklyPlanShoppingPreviewSource {
        occurrenceId = Objects.requireNonNull(occurrenceId, "occurrenceId must not be null");
        recipeId = Objects.requireNonNull(recipeId, "recipeId must not be null");
        recipeIngredientId = Objects.requireNonNull(recipeIngredientId, "recipeIngredientId must not be null");
    }
}
