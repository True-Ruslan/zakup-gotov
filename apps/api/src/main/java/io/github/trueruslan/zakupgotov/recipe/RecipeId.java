package io.github.trueruslan.zakupgotov.recipe;

import java.util.Objects;
import java.util.UUID;

public record RecipeId(UUID value) {

    public RecipeId {
        value = Objects.requireNonNull(value, "value must not be null");
    }
}
