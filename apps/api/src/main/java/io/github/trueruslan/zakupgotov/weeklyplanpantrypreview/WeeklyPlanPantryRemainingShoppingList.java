package io.github.trueruslan.zakupgotov.weeklyplanpantrypreview;

import io.github.trueruslan.zakupgotov.weeklyplanpreview.WeeklyPlanShoppingPreviewShoppingItem;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record WeeklyPlanPantryRemainingShoppingList(
        UUID id,
        List<WeeklyPlanShoppingPreviewShoppingItem> items) {

    public WeeklyPlanPantryRemainingShoppingList {
        id = Objects.requireNonNull(id, "id must not be null");
        items = List.copyOf(Objects.requireNonNull(items, "items must not be null"));
    }
}
