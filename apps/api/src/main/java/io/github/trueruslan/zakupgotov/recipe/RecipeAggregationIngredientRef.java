package io.github.trueruslan.zakupgotov.recipe;

import java.util.Objects;

public record RecipeAggregationIngredientRef(
        RecipeAggregationEntryId entryId,
        RecipeIngredientRef ingredientRef) {

    public RecipeAggregationIngredientRef {
        entryId = Objects.requireNonNull(entryId, "entryId must not be null");
        ingredientRef = Objects.requireNonNull(ingredientRef, "ingredientRef must not be null");
    }
}
