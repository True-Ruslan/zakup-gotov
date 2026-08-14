package io.github.trueruslan.zakupgotov.pantry;

import io.github.trueruslan.zakupgotov.shopping.Quantity;
import io.github.trueruslan.zakupgotov.shopping.ShoppingItem;
import io.github.trueruslan.zakupgotov.shopping.ShoppingList;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class PantryShoppingListAdjuster {

    public PantryAdjustment adjust(ShoppingList source, List<PantryItem> pantryItems) {
        Objects.requireNonNull(source, "source must not be null");
        Objects.requireNonNull(pantryItems, "pantryItems must not be null");

        var stock = aggregatePantryStock(pantryItems);
        var remainingShoppingList = new ShoppingList(source.id());
        var evidence = new ArrayList<PantryAdjustmentEvidence>(source.items().size());

        for (var item : source.items()) {
            adjustItem(item, stock, remainingShoppingList, evidence);
        }

        return new PantryAdjustment(remainingShoppingList, evidence);
    }

    private static LinkedHashMap<PantryMatchKey, BigDecimal> aggregatePantryStock(
            List<PantryItem> pantryItems) {
        var stock = new LinkedHashMap<PantryMatchKey, BigDecimal>();
        for (var pantryItem : pantryItems) {
            var item = Objects.requireNonNull(pantryItem, "pantry item must not be null");
            var key = new PantryMatchKey(item.requirement(), item.quantity().unit());
            stock.merge(key, item.quantity().amount(), BigDecimal::add);
        }
        return stock;
    }

    private static void adjustItem(
            ShoppingItem item,
            LinkedHashMap<PantryMatchKey, BigDecimal> stock,
            ShoppingList remainingShoppingList,
            List<PantryAdjustmentEvidence> evidence) {
        var required = item.quantity();
        var key = new PantryMatchKey(item.requirement(), required.unit());
        var available = stock.getOrDefault(key, BigDecimal.ZERO);
        var usedAmount = required.amount().min(available);

        if (usedAmount.signum() == 0) {
            remainingShoppingList.add(item);
            evidence.add(new PantryAdjustmentEvidence(
                    item.id(),
                    item.requirement(),
                    required,
                    Optional.empty(),
                    Optional.of(required),
                    PantryAdjustmentStatus.UNCHANGED));
            return;
        }

        stock.put(key, available.subtract(usedAmount));
        var used = new Quantity(usedAmount, required.unit());
        var remainingAmount = required.amount().subtract(usedAmount);

        if (remainingAmount.signum() == 0) {
            evidence.add(new PantryAdjustmentEvidence(
                    item.id(),
                    item.requirement(),
                    required,
                    Optional.of(used),
                    Optional.empty(),
                    PantryAdjustmentStatus.FULLY_COVERED));
            return;
        }

        var remaining = new Quantity(remainingAmount, required.unit());
        remainingShoppingList.add(new ShoppingItem(
                item.id(),
                item.requirement(),
                remaining));
        evidence.add(new PantryAdjustmentEvidence(
                item.id(),
                item.requirement(),
                required,
                Optional.of(used),
                Optional.of(remaining),
                PantryAdjustmentStatus.PARTIALLY_COVERED));
    }
}
