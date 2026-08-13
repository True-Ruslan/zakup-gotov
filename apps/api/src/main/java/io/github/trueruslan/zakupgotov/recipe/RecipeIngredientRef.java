package io.github.trueruslan.zakupgotov.recipe;

import java.util.Objects;

public record RecipeIngredientRef(
        RecipeId recipeId,
        RecipeIngredientId ingredientId) {

    public RecipeIngredientRef {
        recipeId = Objects.requireNonNull(recipeId, "recipeId must not be null");
        ingredientId = Objects.requireNonNull(ingredientId, "ingredientId must not be null");
    }
}
