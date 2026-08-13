package io.github.trueruslan.zakupgotov.recipe;

import java.util.Objects;
import java.util.UUID;

public record RecipeIngredientId(UUID value) {

    public RecipeIngredientId {
        value = Objects.requireNonNull(value, "value must not be null");
    }
}
