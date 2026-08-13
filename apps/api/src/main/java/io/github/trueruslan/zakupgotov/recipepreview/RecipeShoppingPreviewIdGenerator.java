package io.github.trueruslan.zakupgotov.recipepreview;

import io.github.trueruslan.zakupgotov.recipe.RecipeId;
import io.github.trueruslan.zakupgotov.recipe.RecipeIngredientId;
import io.github.trueruslan.zakupgotov.shopping.ShoppingListId;

public interface RecipeShoppingPreviewIdGenerator {
    RecipeId nextRecipeId();
    RecipeIngredientId nextIngredientId();
    ShoppingListId nextShoppingListId();
}
