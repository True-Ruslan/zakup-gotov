package io.github.trueruslan.zakupgotov.recipe;

import io.github.trueruslan.zakupgotov.shopping.ShoppingItemId;
import io.github.trueruslan.zakupgotov.shopping.ShoppingList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public record RecipeShoppingListConversion(
        ShoppingList shoppingList,
        Map<ShoppingItemId, List<RecipeIngredientRef>> provenance) {

    public RecipeShoppingListConversion {
        shoppingList = Objects.requireNonNull(shoppingList, "shoppingList must not be null");
        provenance = Objects.requireNonNull(provenance, "provenance must not be null");
    }
}
