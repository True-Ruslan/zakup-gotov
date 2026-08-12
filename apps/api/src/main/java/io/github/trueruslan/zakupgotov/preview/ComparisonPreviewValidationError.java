package io.github.trueruslan.zakupgotov.preview;

public record ComparisonPreviewValidationError(String field, String message) {

    public ComparisonPreviewValidationError {
        if (field == null || field.isBlank()) {
            throw new IllegalArgumentException("field must not be blank");
        }
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("message must not be blank");
        }
    }
}
