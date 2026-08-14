package io.github.trueruslan.zakupgotov.weeklyplanpreview;

import io.github.trueruslan.zakupgotov.shopping.Quantity;
import java.util.Objects;
import java.util.UUID;

public record WeeklyPlanShoppingPreviewIngredient(
        UUID id,
        String requirement,
        Quantity quantity) {
    public WeeklyPlanShoppingPreviewIngredient {
        id = Objects.requireNonNull(id, "id must not be null");
        if (requirement == null || requirement.isBlank()) {
            throw new IllegalArgumentException("requirement must not be blank");
        }
        quantity = Objects.requireNonNull(quantity, "quantity must not be null");
    }
}
