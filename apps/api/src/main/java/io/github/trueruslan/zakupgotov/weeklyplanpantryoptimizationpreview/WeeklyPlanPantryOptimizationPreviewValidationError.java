package io.github.trueruslan.zakupgotov.weeklyplanpantryoptimizationpreview;

public record WeeklyPlanPantryOptimizationPreviewValidationError(
        String field,
        String message) {

    public WeeklyPlanPantryOptimizationPreviewValidationError {
        if (field == null || field.isBlank()) {
            throw new IllegalArgumentException("field must not be blank");
        }
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("message must not be blank");
        }
    }
}
