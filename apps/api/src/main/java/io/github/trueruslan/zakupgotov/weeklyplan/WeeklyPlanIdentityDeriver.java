package io.github.trueruslan.zakupgotov.weeklyplan;

import io.github.trueruslan.zakupgotov.recipe.RecipeAggregationEntryId;
import io.github.trueruslan.zakupgotov.shopping.ShoppingListId;

interface WeeklyPlanIdentityDeriver {
    ShoppingListId shoppingListId(WeeklyPlanId planId);

    RecipeAggregationEntryId aggregationEntryId(
            WeeklyPlanId planId,
            WeeklyMealOccurrenceId occurrenceId);
}
