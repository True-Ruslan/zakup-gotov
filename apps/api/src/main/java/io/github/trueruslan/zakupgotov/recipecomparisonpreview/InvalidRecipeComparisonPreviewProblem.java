package io.github.trueruslan.zakupgotov.recipecomparisonpreview;

import java.util.List;
import java.util.Objects;

public record InvalidRecipeComparisonPreviewProblem(
        String type,
        String title,
        int status,
        String code,
        List<RecipeComparisonPreviewValidationError> errors) {

    public static final String TYPE = "https://zakup-gotov.dev/problems/invalid-recipe-comparison-preview";
    public static final String TITLE = "Invalid recipe comparison preview request";
    public static final String CODE = "INVALID_RECIPE_COMPARISON_PREVIEW";

    public InvalidRecipeComparisonPreviewProblem {
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

    public static InvalidRecipeComparisonPreviewProblem of(
            List<RecipeComparisonPreviewValidationError> errors) {
        return new InvalidRecipeComparisonPreviewProblem(TYPE, TITLE, 400, CODE, errors);
    }

    public static InvalidRecipeComparisonPreviewProblem malformedJson() {
        return of(List.of(new RecipeComparisonPreviewValidationError(
                "$request",
                "malformed JSON request")));
    }
}
