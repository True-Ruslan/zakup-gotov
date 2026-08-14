package io.github.trueruslan.zakupgotov.weeklyplan;

import io.github.trueruslan.zakupgotov.recipe.RecipeAggregationEntryId;
import io.github.trueruslan.zakupgotov.shopping.ShoppingListId;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

final class WeeklyPlanIds implements WeeklyPlanIdentityDeriver {

    static final WeeklyPlanIds INSTANCE = new WeeklyPlanIds();

    private static final String LIST_PREFIX = "zakup-gotov:weekly-plan-shopping-list:v1:";
    private static final String ENTRY_PREFIX = "zakup-gotov:weekly-plan-aggregation-entry:v1:";

    private WeeklyPlanIds() {}

    @Override
    public ShoppingListId shoppingListId(WeeklyPlanId planId) {
        return new ShoppingListId(nameUuid(LIST_PREFIX + planId.value()));
    }

    @Override
    public RecipeAggregationEntryId aggregationEntryId(
            WeeklyPlanId planId,
            WeeklyMealOccurrenceId occurrenceId) {
        return new RecipeAggregationEntryId(
                nameUuid(ENTRY_PREFIX + planId.value() + ":" + occurrenceId.value()));
    }

    private static UUID nameUuid(String payload) {
        return UUID.nameUUIDFromBytes(payload.getBytes(StandardCharsets.UTF_8));
    }
}
