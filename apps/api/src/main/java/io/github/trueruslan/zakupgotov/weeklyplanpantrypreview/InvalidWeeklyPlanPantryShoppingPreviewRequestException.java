package io.github.trueruslan.zakupgotov.weeklyplanpantrypreview;

import java.util.List;

public final class InvalidWeeklyPlanPantryShoppingPreviewRequestException extends RuntimeException {
    private final List<WeeklyPlanPantryShoppingPreviewValidationError> errors;

    public InvalidWeeklyPlanPantryShoppingPreviewRequestException(List<WeeklyPlanPantryShoppingPreviewValidationError> errors) {
        super("invalid weekly plan pantry shopping preview request");
        this.errors = List.copyOf(errors);
    }

    public List<WeeklyPlanPantryShoppingPreviewValidationError> errors() {
        return errors;
    }
}
