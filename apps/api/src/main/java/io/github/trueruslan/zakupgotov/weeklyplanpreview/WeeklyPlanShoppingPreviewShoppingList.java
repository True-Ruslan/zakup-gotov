package io.github.trueruslan.zakupgotov.weeklyplanpreview;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record WeeklyPlanShoppingPreviewShoppingList(
        UUID id,
        List<WeeklyPlanShoppingPreviewShoppingItem> items) {
    public WeeklyPlanShoppingPreviewShoppingList {
        id = Objects.requireNonNull(id, "id must not be null");
        items = List.copyOf(Objects.requireNonNull(items, "items must not be null"));
        if (items.isEmpty()) {
            throw new IllegalArgumentException("items must not be empty");
        }
    }
}
