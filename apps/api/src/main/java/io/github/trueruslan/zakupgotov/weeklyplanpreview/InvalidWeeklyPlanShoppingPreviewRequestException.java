package io.github.trueruslan.zakupgotov.weeklyplanpreview;

import java.util.List;

public final class InvalidWeeklyPlanShoppingPreviewRequestException extends RuntimeException {
    private final List<WeeklyPlanShoppingPreviewValidationError> errors;

    public InvalidWeeklyPlanShoppingPreviewRequestException(List<WeeklyPlanShoppingPreviewValidationError> errors) {
        super("invalid weekly plan shopping preview request");
        this.errors = List.copyOf(errors);
    }

    public List<WeeklyPlanShoppingPreviewValidationError> errors() {
        return errors;
    }
}
