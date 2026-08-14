package io.github.trueruslan.zakupgotov.weeklyplanpreview;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record WeeklyPlanShoppingPreviewRecipe(
        UUID id,
        String title,
        int baseServings,
        List<WeeklyPlanShoppingPreviewIngredient> ingredients) {
    public WeeklyPlanShoppingPreviewRecipe {
        id = Objects.requireNonNull(id, "id must not be null");
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("title must not be blank");
        }
        if (baseServings <= 0) {
            throw new IllegalArgumentException("baseServings must be greater than 0");
        }
        ingredients = List.copyOf(Objects.requireNonNull(ingredients, "ingredients must not be null"));
        if (ingredients.isEmpty()) {
            throw new IllegalArgumentException("ingredients must not be empty");
        }
    }
}
