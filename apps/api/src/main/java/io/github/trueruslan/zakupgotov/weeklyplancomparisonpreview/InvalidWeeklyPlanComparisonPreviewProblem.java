package io.github.trueruslan.zakupgotov.weeklyplancomparisonpreview;

import java.util.List;
import java.util.Objects;

public record InvalidWeeklyPlanComparisonPreviewProblem(
        String type,
        String title,
        int status,
        String code,
        List<WeeklyPlanComparisonPreviewValidationError> errors) {

    public static final String TYPE = "https://zakup-gotov.dev/problems/invalid-weekly-plan-comparison-preview";
    public static final String TITLE = "Invalid weekly plan comparison preview request";
    public static final String CODE = "INVALID_WEEKLY_PLAN_COMPARISON_PREVIEW";

    public InvalidWeeklyPlanComparisonPreviewProblem {
        if (type == null || type.isBlank()) {
            throw new IllegalArgumentException("type must not be blank");
        }
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("title must not be blank");
        }
        if (status != 400) {
            throw new IllegalArgumentException("status must be 400");
        }
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("code must not be blank");
        }
        errors = List.copyOf(Objects.requireNonNull(errors, "errors must not be null"));
        if (errors.isEmpty()) {
            throw new IllegalArgumentException("errors must not be empty");
        }
    }

    public static InvalidWeeklyPlanComparisonPreviewProblem malformedJson() {
        return new InvalidWeeklyPlanComparisonPreviewProblem(
                TYPE,
                TITLE,
                400,
                CODE,
                List.of(new WeeklyPlanComparisonPreviewValidationError(
                        "$request",
                        "malformed JSON request")));
    }
}
