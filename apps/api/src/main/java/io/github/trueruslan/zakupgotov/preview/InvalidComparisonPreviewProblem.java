package io.github.trueruslan.zakupgotov.preview;

import java.util.List;
import java.util.Objects;

public record InvalidComparisonPreviewProblem(
        String type,
        String title,
        int status,
        String code,
        List<ComparisonPreviewValidationError> errors) {

    public static final String TYPE = "https://zakup-gotov.dev/problems/invalid-comparison-preview";
    public static final String TITLE = "Invalid comparison preview request";
    public static final String CODE = "INVALID_COMPARISON_PREVIEW";

    public InvalidComparisonPreviewProblem {
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

    public static InvalidComparisonPreviewProblem of(List<ComparisonPreviewValidationError> errors) {
        return new InvalidComparisonPreviewProblem(TYPE, TITLE, 400, CODE, errors);
    }
}
