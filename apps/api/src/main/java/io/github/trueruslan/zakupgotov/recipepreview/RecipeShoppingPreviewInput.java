package io.github.trueruslan.zakupgotov.recipepreview;

import io.github.trueruslan.zakupgotov.recipe.Recipe;
import io.github.trueruslan.zakupgotov.recipe.RecipeServings;
import io.github.trueruslan.zakupgotov.shopping.ShoppingListId;

public record RecipeShoppingPreviewInput(
        Recipe recipe,
        RecipeServings targetServings,
        ShoppingListId shoppingListId) {}
