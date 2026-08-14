package io.github.trueruslan.zakupgotov.weeklyplan;

import java.util.Objects;
import java.util.UUID;

public record WeeklyPlanId(UUID value) {
    public WeeklyPlanId {
        value = Objects.requireNonNull(value, "value must not be null");
    }
}
