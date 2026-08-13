package io.github.trueruslan.zakupgotov.recipe;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;

public record Recipe(
        RecipeId id,
        RecipeTitle title,
        RecipeServings baseServings,
        List<RecipeIngredient> ingredients) {

    public Recipe {
        id = Objects.requireNonNull(id, "id must not be null");
        title = Objects.requireNonNull(title, "title must not be null");
        baseServings = Objects.requireNonNull(baseServings, "baseServings must not be null");
        ingredients = Objects.requireNonNull(ingredients, "ingredients must not be null");
        if (ingredients.isEmpty()) {
            throw new IllegalArgumentException("ingredients must not be empty");
        }

        var ingredientIds = new HashSet<RecipeIngredientId>();
        for (var ingredient : ingredients) {
            Objects.requireNonNull(ingredient, "ingredient must not be null");
            if (!ingredientIds.add(ingredient.id())) {
                throw new IllegalArgumentException("duplicate recipe ingredient id: " + ingredient.id().value());
            }
        }

        ingredients = List.copyOf(ingredients);
    }
}
