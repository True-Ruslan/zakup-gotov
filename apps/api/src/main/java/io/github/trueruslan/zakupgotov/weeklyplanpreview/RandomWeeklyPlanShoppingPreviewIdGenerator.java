package io.github.trueruslan.zakupgotov.weeklyplanpreview;

import io.github.trueruslan.zakupgotov.weeklyplan.WeeklyMealOccurrenceId;
import io.github.trueruslan.zakupgotov.weeklyplan.WeeklyPlanId;
import java.util.UUID;

final class RandomWeeklyPlanShoppingPreviewIdGenerator implements WeeklyPlanShoppingPreviewIdGenerator {
    @Override
    public WeeklyPlanId nextWeeklyPlanId() {
        return new WeeklyPlanId(UUID.randomUUID());
    }

    @Override
    public WeeklyMealOccurrenceId nextOccurrenceId() {
        return new WeeklyMealOccurrenceId(UUID.randomUUID());
    }
}
