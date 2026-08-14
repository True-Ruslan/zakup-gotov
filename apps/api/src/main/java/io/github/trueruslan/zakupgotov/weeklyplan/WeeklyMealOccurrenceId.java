package io.github.trueruslan.zakupgotov.weeklyplan;

import java.util.Objects;
import java.util.UUID;

public record WeeklyMealOccurrenceId(UUID value) {
    public WeeklyMealOccurrenceId {
        value = Objects.requireNonNull(value, "value must not be null");
    }
}
