package io.github.trueruslan.zakupgotov.weeklyplanpantrypreview;

import io.github.trueruslan.zakupgotov.weeklyplanpreview.WeeklyPlanShoppingPreviewPlan;
import io.github.trueruslan.zakupgotov.weeklyplanpreview.WeeklyPlanShoppingPreviewShoppingList;
import java.util.List;
import java.util.Objects;

public record WeeklyPlanPantryShoppingPreview(
        WeeklyPlanShoppingPreviewPlan weeklyPlan,
        WeeklyPlanShoppingPreviewShoppingList originalShoppingList,
        List<WeeklyPlanPantryAdjustmentEvidence> pantryAdjustments,
        WeeklyPlanPantryRemainingShoppingList remainingShoppingList) {

    public WeeklyPlanPantryShoppingPreview {
        weeklyPlan = Objects.requireNonNull(weeklyPlan, "weeklyPlan must not be null");
        originalShoppingList = Objects.requireNonNull(originalShoppingList, "originalShoppingList must not be null");
        pantryAdjustments = List.copyOf(Objects.requireNonNull(pantryAdjustments, "pantryAdjustments must not be null"));
        remainingShoppingList = Objects.requireNonNull(remainingShoppingList, "remainingShoppingList must not be null");
        if (pantryAdjustments.size() != originalShoppingList.items().size()) {
            throw new IllegalArgumentException("pantry adjustment evidence cardinality must match original shopping list");
        }
    }
}
