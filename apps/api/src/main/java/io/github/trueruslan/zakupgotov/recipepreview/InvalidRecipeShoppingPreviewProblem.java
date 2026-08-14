package io.github.trueruslan.zakupgotov.recipepreview;

import java.util.List;
import java.util.Objects;

public record InvalidRecipeShoppingPreviewProblem(
        String type,
        String title,
        int status,
        String code,
        List<RecipeShoppingPreviewValidationError> errors) {

    public static final String TYPE = "https://zakup-gotov.dev/problems/invalid-recipe-shopping-preview";
    public static final String TITLE = "Invalid recipe shopping preview request";
    public static final String CODE = "INVALID_RECIPE_SHOPPING_PREVIEW";

    public InvalidRecipeShoppingPreviewProblem {
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

    public static InvalidRecipeShoppingPreviewProblem of(List<RecipeShoppingPreviewValidationError> errors) {
        return new InvalidRecipeShoppingPreviewProblem(TYPE, TITLE, 400, CODE, errors);
    }
}
