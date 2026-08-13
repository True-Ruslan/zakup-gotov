package io.github.trueruslan.zakupgotov.recipe;

public record RecipeServings(int value) {

    public RecipeServings {
        if (value <= 0) {
            throw new IllegalArgumentException("value must be positive");
        }
    }
}
