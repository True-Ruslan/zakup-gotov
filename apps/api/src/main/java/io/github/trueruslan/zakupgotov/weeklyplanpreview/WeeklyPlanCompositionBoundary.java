package io.github.trueruslan.zakupgotov.weeklyplanpreview;

import io.github.trueruslan.zakupgotov.weeklyplan.WeeklyPlan;
import io.github.trueruslan.zakupgotov.weeklyplan.WeeklyPlanShoppingListComposition;

@FunctionalInterface
interface WeeklyPlanCompositionBoundary {
    WeeklyPlanShoppingListComposition compose(WeeklyPlan plan);
}
