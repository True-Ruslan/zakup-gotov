package io.github.trueruslan.zakupgotov.weeklyplanpreview;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record WeeklyPlanShoppingPreviewPlan(
        UUID id,
        List<WeeklyPlanShoppingPreviewOccurrence> occurrences) {
    public WeeklyPlanShoppingPreviewPlan {
        id = Objects.requireNonNull(id, "id must not be null");
        occurrences = List.copyOf(Objects.requireNonNull(occurrences, "occurrences must not be null"));
        if (occurrences.isEmpty()) {
            throw new IllegalArgumentException("occurrences must not be empty");
        }
    }
}
