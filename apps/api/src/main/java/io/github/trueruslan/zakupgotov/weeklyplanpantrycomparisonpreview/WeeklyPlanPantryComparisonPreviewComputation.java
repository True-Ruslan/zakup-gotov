package io.github.trueruslan.zakupgotov.weeklyplanpantrycomparisonpreview;

import io.github.trueruslan.zakupgotov.preview.ComparisonPreviewComputation;
import java.util.Objects;
import java.util.Optional;

public record WeeklyPlanPantryComparisonPreviewComputation(
        WeeklyPlanPantryComparisonPreview preview,
        Optional<ComparisonPreviewComputation> comparisonComputation) {

    public WeeklyPlanPantryComparisonPreviewComputation {
        preview = Objects.requireNonNull(preview, "preview must not be null");
        comparisonComputation = Objects.requireNonNull(
                comparisonComputation,
                "comparisonComputation must not be null");

        var compared = preview.comparisonOutcome() == WeeklyPlanPantryComparisonOutcome.COMPARED;
        if (comparisonComputation.isPresent() != compared) {
            throw new IllegalArgumentException(
                    "detailed comparison presence must match Pantry comparison outcome");
        }
        if (comparisonComputation.isPresent()
                && !comparisonComputation.orElseThrow().preview().equals(preview.comparisonPreview())) {
            throw new IllegalArgumentException(
                    "detailed comparison preview must match accepted Pantry comparison projection");
        }
    }
}
