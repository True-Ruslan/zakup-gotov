package io.github.trueruslan.zakupgotov.recipepreview;

import io.github.trueruslan.zakupgotov.shopping.Quantity;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record RecipeShoppingPreviewShoppingItem(
        UUID id,
        String requirement,
        Quantity quantity,
        List<UUID> sourceIngredientIds) {

    public RecipeShoppingPreviewShoppingItem {
        Objects.requireNonNull(id, "id must not be null");
        if (requirement == null || requirement.isBlank()) {
            throw new IllegalArgumentException("requirement must not be blank");
        }
        Objects.requireNonNull(quantity, "quantity must not be null");
        sourceIngredientIds = List.copyOf(
                Objects.requireNonNull(sourceIngredientIds, "sourceIngredientIds must not be null"));
        if (sourceIngredientIds.isEmpty()) {
            throw new IllegalArgumentException("sourceIngredientIds must not be empty");
        }
        if (sourceIngredientIds.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("sourceIngredientIds must not contain null");
        }
    }
}
