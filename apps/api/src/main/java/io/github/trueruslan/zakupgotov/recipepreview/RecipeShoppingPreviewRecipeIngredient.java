package io.github.trueruslan.zakupgotov.recipepreview;

import io.github.trueruslan.zakupgotov.shopping.Quantity;
import java.util.Objects;
import java.util.UUID;

public record RecipeShoppingPreviewRecipeIngredient(
        UUID id,
        String requirement,
        Quantity quantity) {

    public RecipeShoppingPreviewRecipeIngredient {
        Objects.requireNonNull(id, "id must not be null");
        if (requirement == null || requirement.isBlank()) {
            throw new IllegalArgumentException("requirement must not be blank");
        }
        Objects.requireNonNull(quantity, "quantity must not be null");
    }
}
