package io.github.trueruslan.zakupgotov.weeklyplan;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;

public record WeeklyPlan(
        WeeklyPlanId id,
        List<WeeklyMealOccurrence> occurrences) {

    public WeeklyPlan {
        id = Objects.requireNonNull(id, "id must not be null");
        occurrences = Objects.requireNonNull(occurrences, "occurrences must not be null");
        if (occurrences.isEmpty()) {
            throw new IllegalArgumentException("occurrences must not be empty");
        }

        var seenIds = new HashSet<WeeklyMealOccurrenceId>();
        for (var occurrence : occurrences) {
            Objects.requireNonNull(occurrence, "occurrence must not be null");
            if (!seenIds.add(occurrence.id())) {
                throw new IllegalArgumentException("duplicate weekly meal occurrence id");
            }
        }
        occurrences = List.copyOf(occurrences);
    }
}
