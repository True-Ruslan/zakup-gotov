package io.github.trueruslan.zakupgotov.weeklyplanpreview;

import java.util.Objects;

public record WeeklyPlanShoppingPreview(
        WeeklyPlanShoppingPreviewPlan weeklyPlan,
        WeeklyPlanShoppingPreviewShoppingList shoppingList) {
    public WeeklyPlanShoppingPreview {
        weeklyPlan = Objects.requireNonNull(weeklyPlan, "weeklyPlan must not be null");
        shoppingList = Objects.requireNonNull(shoppingList, "shoppingList must not be null");
    }
}
