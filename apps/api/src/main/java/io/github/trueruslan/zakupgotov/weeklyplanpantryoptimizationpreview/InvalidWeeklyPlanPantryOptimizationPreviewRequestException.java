package io.github.trueruslan.zakupgotov.weeklyplanpantryoptimizationpreview;

import java.util.List;
import java.util.Objects;

public final class InvalidWeeklyPlanPantryOptimizationPreviewRequestException extends RuntimeException {

    private final List<WeeklyPlanPantryOptimizationPreviewValidationError> errors;

    public InvalidWeeklyPlanPantryOptimizationPreviewRequestException(
            List<WeeklyPlanPantryOptimizationPreviewValidationError> errors) {
        super("invalid weekly plan pantry optimization preview request");
        this.errors = List.copyOf(Objects.requireNonNull(errors, "errors must not be null"));
        if (this.errors.isEmpty()) {
            throw new IllegalArgumentException("errors must not be empty");
        }
    }

    public List<WeeklyPlanPantryOptimizationPreviewValidationError> errors() {
        return errors;
    }
}
