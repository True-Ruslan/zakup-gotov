package io.github.trueruslan.zakupgotov.weeklyplanpantrycomparisonpreview;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.github.trueruslan.zakupgotov.preview.ComparisonPreview;
import io.github.trueruslan.zakupgotov.weeklyplanpantrypreview.WeeklyPlanPantryShoppingPreview;
import java.util.Objects;

public record WeeklyPlanPantryComparisonPreview(
        WeeklyPlanPantryShoppingPreview pantryShoppingPreview,
        WeeklyPlanPantryComparisonOutcome comparisonOutcome,
        @JsonInclude(JsonInclude.Include.NON_NULL) ComparisonPreview comparisonPreview) {

    public WeeklyPlanPantryComparisonPreview {
        pantryShoppingPreview = Objects.requireNonNull(
                pantryShoppingPreview,
                "pantryShoppingPreview must not be null");
        comparisonOutcome = Objects.requireNonNull(
                comparisonOutcome,
                "comparisonOutcome must not be null");

        var hasRemainingDemand = !pantryShoppingPreview.remainingShoppingList().items().isEmpty();
        switch (comparisonOutcome) {
            case COMPARED -> {
                if (!hasRemainingDemand) {
                    throw new IllegalArgumentException("COMPARED requires remaining demand");
                }
                Objects.requireNonNull(comparisonPreview, "COMPARED requires comparisonPreview");
            }
            case NO_REMAINING_DEMAND -> {
                if (hasRemainingDemand) {
                    throw new IllegalArgumentException("NO_REMAINING_DEMAND requires empty remaining demand");
                }
                if (comparisonPreview != null) {
                    throw new IllegalArgumentException("NO_REMAINING_DEMAND must not include comparisonPreview");
                }
            }
        }
    }
}
