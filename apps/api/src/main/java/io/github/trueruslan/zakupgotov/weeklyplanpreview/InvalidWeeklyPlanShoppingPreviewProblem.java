package io.github.trueruslan.zakupgotov.weeklyplanpreview;

import java.util.List;
import java.util.Objects;

public record InvalidWeeklyPlanShoppingPreviewProblem(
        String type,
        String title,
        int status,
        String code,
        List<WeeklyPlanShoppingPreviewValidationError> errors) {

    public static final String TYPE = "https://zakup-gotov.dev/problems/invalid-weekly-plan-shopping-preview";
    public static final String TITLE = "Invalid weekly plan shopping preview request";
    public static final String CODE = "INVALID_WEEKLY_PLAN_SHOPPING_PREVIEW";

    public InvalidWeeklyPlanShoppingPreviewProblem {
        if (type == null || type.isBlank()) throw new IllegalArgumentException("type must not be blank");
        if (title == null || title.isBlank()) throw new IllegalArgumentException("title must not be blank");
        if (status != 400) throw new IllegalArgumentException("status must be 400");
        if (code == null || code.isBlank()) throw new IllegalArgumentException("code must not be blank");
        errors = List.copyOf(Objects.requireNonNull(errors, "errors must not be null"));
        if (errors.isEmpty()) throw new IllegalArgumentException("errors must not be empty");
    }

    public static InvalidWeeklyPlanShoppingPreviewProblem of(
            List<WeeklyPlanShoppingPreviewValidationError> errors) {
        return new InvalidWeeklyPlanShoppingPreviewProblem(TYPE, TITLE, 400, CODE, errors);
    }
}
