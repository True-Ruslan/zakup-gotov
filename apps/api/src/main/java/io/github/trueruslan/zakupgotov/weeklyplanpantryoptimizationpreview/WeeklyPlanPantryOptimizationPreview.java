package io.github.trueruslan.zakupgotov.weeklyplanpantryoptimizationpreview;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.github.trueruslan.zakupgotov.optimizationpreview.CheckoutOptimizationPreview;
import io.github.trueruslan.zakupgotov.weeklyplanpantrycomparisonpreview.WeeklyPlanPantryComparisonOutcome;
import io.github.trueruslan.zakupgotov.weeklyplanpantrycomparisonpreview.WeeklyPlanPantryComparisonPreview;
import java.util.Objects;

public record WeeklyPlanPantryOptimizationPreview(
        WeeklyPlanPantryComparisonPreview pantryComparisonPreview,
        @JsonInclude(JsonInclude.Include.NON_NULL) CheckoutOptimizationPreview optimizationPreview) {

    public WeeklyPlanPantryOptimizationPreview {
        pantryComparisonPreview = Objects.requireNonNull(
                pantryComparisonPreview,
                "pantryComparisonPreview must not be null");

        var compared = pantryComparisonPreview.comparisonOutcome() == WeeklyPlanPantryComparisonOutcome.COMPARED;
        if (compared && optimizationPreview == null) {
            throw new IllegalArgumentException("COMPARED requires optimizationPreview");
        }
        if (!compared && optimizationPreview != null) {
            throw new IllegalArgumentException("NO_REMAINING_DEMAND must not include optimizationPreview");
        }
    }
}
