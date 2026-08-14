package io.github.trueruslan.zakupgotov.weeklyplan;

import io.github.trueruslan.zakupgotov.recipe.RecipeAggregationEntry;
import io.github.trueruslan.zakupgotov.recipe.RecipeShoppingListAggregation;
import io.github.trueruslan.zakupgotov.shopping.ShoppingListId;
import java.util.List;

@FunctionalInterface
interface RecipeAggregationBoundary {
    RecipeShoppingListAggregation aggregate(
            List<RecipeAggregationEntry> entries,
            ShoppingListId shoppingListId);
}
