package io.github.trueruslan.zakupgotov.recipe;

import java.util.Objects;

public record RecipeTitle(String value) {

    public RecipeTitle {
        value = Objects.requireNonNull(value, "value must not be null")
                .strip()
                .replaceAll("\\s+", " ");
        if (value.isBlank()) {
            throw new IllegalArgumentException("value must not be blank");
        }
    }
}
