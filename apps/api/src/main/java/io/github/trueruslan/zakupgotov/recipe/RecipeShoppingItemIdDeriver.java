package io.github.trueruslan.zakupgotov.recipe;

import io.github.trueruslan.zakupgotov.shopping.QuantityUnit;
import io.github.trueruslan.zakupgotov.shopping.ShoppingItemId;
import io.github.trueruslan.zakupgotov.shopping.ShoppingListId;
import io.github.trueruslan.zakupgotov.shopping.ShoppingRequirement;

@FunctionalInterface
interface RecipeShoppingItemIdDeriver {

    ShoppingItemId derive(
            ShoppingListId shoppingListId,
            ShoppingRequirement requirement,
            QuantityUnit unit);
}
