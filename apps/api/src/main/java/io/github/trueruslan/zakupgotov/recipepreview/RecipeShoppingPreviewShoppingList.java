package io.github.trueruslan.zakupgotov.recipepreview;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record RecipeShoppingPreviewShoppingList(
        UUID id,
        List<RecipeShoppingPreviewShoppingItem> items) {

    public RecipeShoppingPreviewShoppingList {
        Objects.requireNonNull(id, "id must not be null");
        items = List.copyOf(Objects.requireNonNull(items, "items must not be null"));
        if (items.isEmpty()) {
            throw new IllegalArgumentException("items must not be empty");
        }
    }
}
