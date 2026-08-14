package io.github.trueruslan.zakupgotov.recipepreview;

import java.util.List;

public final class InvalidRecipeShoppingPreviewRequestException extends RuntimeException {
    private final List<RecipeShoppingPreviewValidationError> errors;

    public InvalidRecipeShoppingPreviewRequestException(List<RecipeShoppingPreviewValidationError> errors) {
        super("invalid recipe shopping preview request");
        this.errors = List.copyOf(errors);
    }

    public List<RecipeShoppingPreviewValidationError> errors() {
        return errors;
    }
}
