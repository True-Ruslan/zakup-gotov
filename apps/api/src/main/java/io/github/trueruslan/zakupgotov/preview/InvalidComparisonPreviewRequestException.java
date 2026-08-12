package io.github.trueruslan.zakupgotov.preview;

import java.util.List;
import java.util.Objects;

public final class InvalidComparisonPreviewRequestException extends IllegalArgumentException {

    private final List<ComparisonPreviewValidationError> errors;

    public InvalidComparisonPreviewRequestException(List<ComparisonPreviewValidationError> errors) {
        super(message(errors));
        this.errors = List.copyOf(Objects.requireNonNull(errors, "errors must not be null"));
        if (this.errors.isEmpty()) {
            throw new IllegalArgumentException("errors must not be empty");
        }
    }

    public List<ComparisonPreviewValidationError> errors() {
        return errors;
    }

    private static String message(List<ComparisonPreviewValidationError> errors) {
        Objects.requireNonNull(errors, "errors must not be null");
        if (errors.isEmpty()) {
            return "invalid comparison preview request";
        }
        return errors.stream()
                .map(error -> error.field() + ": " + error.message())
                .reduce((left, right) -> left + "; " + right)
                .orElse("invalid comparison preview request");
    }
}
