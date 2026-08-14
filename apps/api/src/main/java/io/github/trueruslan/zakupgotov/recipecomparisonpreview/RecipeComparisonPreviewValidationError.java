package io.github.trueruslan.zakupgotov.recipecomparisonpreview;

public record RecipeComparisonPreviewValidationError(
        String field,
        String message) {}
