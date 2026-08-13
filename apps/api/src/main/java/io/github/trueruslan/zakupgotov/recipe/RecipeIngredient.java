package io.github.trueruslan.zakupgotov.recipe;

import io.github.trueruslan.zakupgotov.shopping.Quantity;
import io.github.trueruslan.zakupgotov.shopping.ShoppingRequirement;
import java.util.Objects;

public record RecipeIngredient(
        RecipeIngredientId id,
        ShoppingRequirement requirement,
        Quantity quantity) {

    public RecipeIngredient {
        id = Objects.requireNonNull(id, "id must not be null");
        requirement = Objects.requireNonNull(requirement, "requirement must not be null");
        quantity = Objects.requireNonNull(quantity, "quantity must not be null");
    }
}
