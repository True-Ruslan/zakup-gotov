package io.github.trueruslan.zakupgotov.weeklyplanpreview;

import io.github.trueruslan.zakupgotov.weeklyplan.WeeklyPlan;
import java.util.Objects;

public record WeeklyPlanShoppingPreviewInput(WeeklyPlan weeklyPlan) {
    public WeeklyPlanShoppingPreviewInput {
        weeklyPlan = Objects.requireNonNull(weeklyPlan, "weeklyPlan must not be null");
    }
}
