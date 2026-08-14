package io.github.trueruslan.zakupgotov.weeklyplanpreview;

import io.github.trueruslan.zakupgotov.weeklyplan.WeeklyMealOccurrenceId;
import io.github.trueruslan.zakupgotov.weeklyplan.WeeklyPlanId;

public interface WeeklyPlanShoppingPreviewIdGenerator {
    WeeklyPlanId nextWeeklyPlanId();
    WeeklyMealOccurrenceId nextOccurrenceId();
}
