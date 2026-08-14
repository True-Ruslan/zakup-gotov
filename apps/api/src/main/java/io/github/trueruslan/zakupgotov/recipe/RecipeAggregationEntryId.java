package io.github.trueruslan.zakupgotov.recipe;

import java.util.Objects;
import java.util.UUID;

public record RecipeAggregationEntryId(UUID value) {
    public RecipeAggregationEntryId {
        value = Objects.requireNonNull(value, "value must not be null");
    }
}
