package io.github.trueruslan.zakupgotov.weeklyplan;

import io.github.trueruslan.zakupgotov.recipe.RecipeAggregationEntry;
import io.github.trueruslan.zakupgotov.recipe.RecipeAggregationEntryId;
import io.github.trueruslan.zakupgotov.recipe.RecipeShoppingListAggregation;
import io.github.trueruslan.zakupgotov.recipe.RecipeShoppingListAggregator;
import io.github.trueruslan.zakupgotov.shopping.ShoppingItemId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class WeeklyPlanShoppingListComposer {

    private final RecipeAggregationBoundary aggregationBoundary;
    private final WeeklyPlanIdentityDeriver identityDeriver;

    public WeeklyPlanShoppingListComposer() {
        this(new RecipeShoppingListAggregator()::aggregate, WeeklyPlanIds.INSTANCE);
    }

    WeeklyPlanShoppingListComposer(
            RecipeAggregationBoundary aggregationBoundary,
            WeeklyPlanIdentityDeriver identityDeriver) {
        this.aggregationBoundary = Objects.requireNonNull(
                aggregationBoundary, "aggregationBoundary must not be null");
        this.identityDeriver = Objects.requireNonNull(
                identityDeriver, "identityDeriver must not be null");
    }

    public WeeklyPlanShoppingListComposition compose(WeeklyPlan plan) {
        Objects.requireNonNull(plan, "plan must not be null");

        var shoppingListId = Objects.requireNonNull(
                identityDeriver.shoppingListId(plan.id()),
                "derived shoppingListId must not be null");
        var entries = new ArrayList<RecipeAggregationEntry>(plan.occurrences().size());
        var occurrenceByEntryId = new LinkedHashMap<RecipeAggregationEntryId, WeeklyMealOccurrenceId>();

        for (var occurrence : plan.occurrences()) {
            var entryId = Objects.requireNonNull(
                    identityDeriver.aggregationEntryId(plan.id(), occurrence.id()),
                    "derived aggregation entry id must not be null");
            if (occurrenceByEntryId.putIfAbsent(entryId, occurrence.id()) != null) {
                throw new IllegalStateException("generated aggregation entry id collision");
            }
            entries.add(new RecipeAggregationEntry(entryId, occurrence.recipe(), occurrence.targetServings()));
        }

        var aggregation = Objects.requireNonNull(
                aggregationBoundary.aggregate(List.copyOf(entries), shoppingListId),
                "aggregation result must not be null");
        return project(aggregation, occurrenceByEntryId);
    }

    private static WeeklyPlanShoppingListComposition project(
            RecipeShoppingListAggregation aggregation,
            Map<RecipeAggregationEntryId, WeeklyMealOccurrenceId> occurrenceByEntryId) {
        validateProvenanceStructure(aggregation);

        var projected = new LinkedHashMap<ShoppingItemId, List<WeeklyPlanIngredientRef>>();
        for (var item : aggregation.shoppingList().items()) {
            var refs = aggregation.provenance().get(item.id());
            var itemRefs = new ArrayList<WeeklyPlanIngredientRef>(refs.size());
            for (var ref : refs) {
                var occurrenceId = occurrenceByEntryId.get(ref.entryId());
                if (occurrenceId == null) {
                    throw new IllegalStateException("unknown aggregation entry id in provenance");
                }
                itemRefs.add(new WeeklyPlanIngredientRef(occurrenceId, ref.ingredientRef()));
            }
            projected.put(item.id(), List.copyOf(itemRefs));
        }

        return new WeeklyPlanShoppingListComposition(aggregation.shoppingList(), projected);
    }

    private static void validateProvenanceStructure(RecipeShoppingListAggregation aggregation) {
        var itemIds = new LinkedHashSet<ShoppingItemId>();
        for (var item : aggregation.shoppingList().items()) {
            itemIds.add(item.id());
        }

        if (!itemIds.equals(aggregation.provenance().keySet())) {
            throw new IllegalStateException("shopping list and provenance keys must match exactly");
        }
        aggregation.provenance().forEach((itemId, refs) -> {
            if (refs.isEmpty()) {
                throw new IllegalStateException("aggregate provenance must not be empty for item " + itemId.value());
            }
        });
    }
}
