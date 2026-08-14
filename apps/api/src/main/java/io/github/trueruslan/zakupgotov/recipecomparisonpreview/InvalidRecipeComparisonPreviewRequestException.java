package io.github.trueruslan.zakupgotov.recipecomparisonpreview;

import java.util.List;
import java.util.Objects;

public final class InvalidRecipeComparisonPreviewRequestException extends RuntimeException {

    private final List<RecipeComparisonPreviewValidationError> errors;

    public InvalidRecipeComparisonPreviewRequestException(
            List<RecipeComparisonPreviewValidationError> errors) {
        super("invalid recipe comparison preview request");
        this.errors = List.copyOf(Objects.requireNonNull(errors, "errors must not be null"));
        if (this.errors.isEmpty()) {
            throw new IllegalArgumentException("errors must not be empty");
        }
    }

    public List<RecipeComparisonPreviewValidationError> errors() {
        return errors;
    }
}
