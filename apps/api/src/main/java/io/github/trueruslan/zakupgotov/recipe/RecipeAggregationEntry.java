package io.github.trueruslan.zakupgotov.recipe;

import java.util.Objects;

public record RecipeAggregationEntry(
        RecipeAggregationEntryId id,
        Recipe recipe,
        RecipeServings targetServings) {

    public RecipeAggregationEntry {
        id = Objects.requireNonNull(id, "id must not be null");
        recipe = Objects.requireNonNull(recipe, "recipe must not be null");
        targetServings = Objects.requireNonNull(targetServings, "targetServings must not be null");
    }
}
