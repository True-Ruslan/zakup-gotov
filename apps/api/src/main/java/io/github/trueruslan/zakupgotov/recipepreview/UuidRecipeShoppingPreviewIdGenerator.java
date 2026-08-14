package io.github.trueruslan.zakupgotov.recipepreview;

import io.github.trueruslan.zakupgotov.recipe.RecipeId;
import io.github.trueruslan.zakupgotov.recipe.RecipeIngredientId;
import io.github.trueruslan.zakupgotov.shopping.ShoppingListId;
import java.util.UUID;

public final class UuidRecipeShoppingPreviewIdGenerator implements RecipeShoppingPreviewIdGenerator {
    @Override
    public RecipeId nextRecipeId() {
        return new RecipeId(UUID.randomUUID());
    }

    @Override
    public RecipeIngredientId nextIngredientId() {
        return new RecipeIngredientId(UUID.randomUUID());
    }

    @Override
    public ShoppingListId nextShoppingListId() {
        return new ShoppingListId(UUID.randomUUID());
    }
}
