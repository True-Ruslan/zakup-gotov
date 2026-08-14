package io.github.trueruslan.zakupgotov.recipe;

import io.github.trueruslan.zakupgotov.shopping.Quantity;
import io.github.trueruslan.zakupgotov.shopping.ShoppingItem;
import io.github.trueruslan.zakupgotov.shopping.ShoppingItemId;
import io.github.trueruslan.zakupgotov.shopping.ShoppingList;
import io.github.trueruslan.zakupgotov.shopping.ShoppingListId;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class RecipeShoppingListAggregator {

    private final RecipeShoppingListConverter converter;
    private final RecipeShoppingItemIdDeriver itemIdDeriver;

    public RecipeShoppingListAggregator() {
        this(new RecipeShoppingListConverter(), RecipeShoppingItemIds::derive);
    }

    RecipeShoppingListAggregator(
            RecipeShoppingListConverter converter,
            RecipeShoppingItemIdDeriver itemIdDeriver) {
        this.converter = Objects.requireNonNull(converter, "converter must not be null");
        this.itemIdDeriver = Objects.requireNonNull(itemIdDeriver, "itemIdDeriver must not be null");
    }

    public RecipeShoppingListAggregation aggregate(
            List<RecipeAggregationEntry> entries,
            ShoppingListId aggregateShoppingListId) {
        Objects.requireNonNull(entries, "entries must not be null");
        Objects.requireNonNull(aggregateShoppingListId, "aggregateShoppingListId must not be null");

        var groups = new LinkedHashMap<RecipeShoppingMergeKey, GroupAccumulator>();
        for (var entry : entries) {
            Objects.requireNonNull(entry, "entry must not be null");
            var conversion = converter.convert(
                    entry.recipe(),
                    entry.targetServings(),
                    deriveEntryShoppingListId(aggregateShoppingListId, entry.id()));
            for (var item : conversion.shoppingList().items()) {
                var refs = Objects.requireNonNull(
                        conversion.provenance().get(item.id()),
                        "converted item provenance must not be null");
                if (refs.isEmpty()) {
                    throw new IllegalStateException("converted item provenance must not be empty");
                }
                var key = new RecipeShoppingMergeKey(item.requirement(), item.quantity().unit());
                groups.computeIfAbsent(key, ignored -> new GroupAccumulator())
                        .add(item.quantity().amount(), entry.id(), refs);
            }
        }

        var shoppingList = new ShoppingList(aggregateShoppingListId);
        var provenance = new LinkedHashMap<ShoppingItemId, List<RecipeAggregationIngredientRef>>();
        var itemKeys = new LinkedHashMap<ShoppingItemId, RecipeShoppingMergeKey>();
        for (var group : groups.entrySet()) {
            var key = group.getKey();
            var accumulator = group.getValue();
            var itemId = Objects.requireNonNull(
                    itemIdDeriver.derive(aggregateShoppingListId, key.requirement(), key.unit()),
                    "derived item id must not be null");
            var previousKey = itemKeys.putIfAbsent(itemId, key);
            if (previousKey != null && !previousKey.equals(key)) {
                throw new IllegalStateException("generated shopping item id collision");
            }
            shoppingList.add(new ShoppingItem(
                    itemId,
                    key.requirement(),
                    new Quantity(accumulator.totalAmount(), key.unit())));
            provenance.put(itemId, accumulator.refs());
        }

        return new RecipeShoppingListAggregation(shoppingList, provenance);
    }

    private static ShoppingListId deriveEntryShoppingListId(
            ShoppingListId aggregateShoppingListId,
            RecipeAggregationEntryId entryId) {
        var payload = "recipe-aggregation-entry-list\n"
                + aggregateShoppingListId.value()
                + "\n"
                + entryId.value();
        return new ShoppingListId(UUID.nameUUIDFromBytes(payload.getBytes(StandardCharsets.UTF_8)));
    }

    private static final class GroupAccumulator {
        private BigDecimal totalAmount = BigDecimal.ZERO;
        private final ArrayList<RecipeAggregationIngredientRef> refs = new ArrayList<>();

        void add(
                BigDecimal amount,
                RecipeAggregationEntryId entryId,
                List<RecipeIngredientRef> ingredientRefs) {
            totalAmount = totalAmount.add(amount);
            for (var ingredientRef : ingredientRefs) {
                refs.add(new RecipeAggregationIngredientRef(entryId, ingredientRef));
            }
        }

        BigDecimal totalAmount() {
            return totalAmount;
        }

        List<RecipeAggregationIngredientRef> refs() {
            return refs;
        }
    }
}
