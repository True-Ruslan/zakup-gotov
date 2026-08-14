package io.github.trueruslan.zakupgotov.weeklyplanpreview;

import io.github.trueruslan.zakupgotov.shopping.Quantity;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record WeeklyPlanShoppingPreviewShoppingItem(
        UUID id,
        String requirement,
        Quantity quantity,
        List<WeeklyPlanShoppingPreviewSource> sources) {
    public WeeklyPlanShoppingPreviewShoppingItem {
        id = Objects.requireNonNull(id, "id must not be null");
        if (requirement == null || requirement.isBlank()) {
            throw new IllegalArgumentException("requirement must not be blank");
        }
        quantity = Objects.requireNonNull(quantity, "quantity must not be null");
        sources = List.copyOf(Objects.requireNonNull(sources, "sources must not be null"));
        if (sources.isEmpty()) {
            throw new IllegalArgumentException("sources must not be empty");
        }
    }
}
