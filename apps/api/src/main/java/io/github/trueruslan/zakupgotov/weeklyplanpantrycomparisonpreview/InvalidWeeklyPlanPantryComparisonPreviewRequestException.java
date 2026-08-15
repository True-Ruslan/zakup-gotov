package io.github.trueruslan.zakupgotov.weeklyplanpantrycomparisonpreview;

import java.util.List;

public final class InvalidWeeklyPlanPantryComparisonPreviewRequestException extends RuntimeException {
    private final List<WeeklyPlanPantryComparisonPreviewValidationError> errors;

    public InvalidWeeklyPlanPantryComparisonPreviewRequestException(
            List<WeeklyPlanPantryComparisonPreviewValidationError> errors) {
        super("invalid weekly plan pantry comparison preview request");
        this.errors = List.copyOf(errors);
    }

    public List<WeeklyPlanPantryComparisonPreviewValidationError> errors() {
        return errors;
    }
}
