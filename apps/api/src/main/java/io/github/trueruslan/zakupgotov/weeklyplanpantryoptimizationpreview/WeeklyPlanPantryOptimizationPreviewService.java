package io.github.trueruslan.zakupgotov.weeklyplanpantryoptimizationpreview;

import io.github.trueruslan.zakupgotov.optimizationpreview.CheckoutOptimizationPreviewService;
import io.github.trueruslan.zakupgotov.weeklyplanpantrycomparisonpreview.InvalidWeeklyPlanPantryComparisonPreviewRequestException;
import io.github.trueruslan.zakupgotov.weeklyplanpantrycomparisonpreview.WeeklyPlanPantryComparisonOutcome;
import io.github.trueruslan.zakupgotov.weeklyplanpantrycomparisonpreview.WeeklyPlanPantryComparisonPreviewRequest;
import io.github.trueruslan.zakupgotov.weeklyplanpantrycomparisonpreview.WeeklyPlanPantryComparisonPreviewService;
import java.util.Objects;

public final class WeeklyPlanPantryOptimizationPreviewService {

    private final WeeklyPlanPantryComparisonPreviewService pantryComparisonService;
    private final CheckoutOptimizationPreviewService optimizationService;

    public WeeklyPlanPantryOptimizationPreviewService(
            WeeklyPlanPantryComparisonPreviewService pantryComparisonService,
            CheckoutOptimizationPreviewService optimizationService) {
        this.pantryComparisonService = Objects.requireNonNull(
                pantryComparisonService,
                "pantryComparisonService must not be null");
        this.optimizationService = Objects.requireNonNull(
                optimizationService,
                "optimizationService must not be null");
    }

    public WeeklyPlanPantryOptimizationPreview create(WeeklyPlanPantryOptimizationPreviewRequest request) {
        if (request == null) {
            throw new InvalidWeeklyPlanPantryOptimizationPreviewRequestException(
                    java.util.List.of(new WeeklyPlanPantryOptimizationPreviewValidationError(
                            "$request", "must not be null")));
        }

        final var comparisonComputation = computeComparison(request);
        if (comparisonComputation.preview().comparisonOutcome()
                == WeeklyPlanPantryComparisonOutcome.NO_REMAINING_DEMAND) {
            return new WeeklyPlanPantryOptimizationPreview(
                    comparisonComputation.preview(),
                    null);
        }

        var detailedComparison = comparisonComputation.comparisonComputation().orElseThrow(
                () -> new IllegalStateException("COMPARED Pantry result requires detailed comparison computation"));
        var optimizationPreview = optimizationService.create(detailedComparison);
        return new WeeklyPlanPantryOptimizationPreview(
                comparisonComputation.preview(),
                optimizationPreview);
    }

    private io.github.trueruslan.zakupgotov.weeklyplanpantrycomparisonpreview.WeeklyPlanPantryComparisonPreviewComputation
            computeComparison(WeeklyPlanPantryOptimizationPreviewRequest request) {
        try {
            return pantryComparisonService.compute(new WeeklyPlanPantryComparisonPreviewRequest(
                    request.locality(),
                    request.weeklyPlan(),
                    request.pantry()));
        } catch (InvalidWeeklyPlanPantryComparisonPreviewRequestException exception) {
            throw new InvalidWeeklyPlanPantryOptimizationPreviewRequestException(
                    exception.errors().stream()
                            .map(error -> new WeeklyPlanPantryOptimizationPreviewValidationError(
                                    error.field(),
                                    error.message()))
                            .toList());
        }
    }
}
