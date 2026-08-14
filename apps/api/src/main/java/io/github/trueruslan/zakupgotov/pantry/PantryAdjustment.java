package io.github.trueruslan.zakupgotov.pantry;

import io.github.trueruslan.zakupgotov.shopping.ShoppingList;
import java.util.List;
import java.util.Objects;

public record PantryAdjustment(
        ShoppingList remainingShoppingList,
        List<PantryAdjustmentEvidence> evidence) {

    public PantryAdjustment {
        remainingShoppingList = Objects.requireNonNull(
                remainingShoppingList,
                "remainingShoppingList must not be null");
        evidence = List.copyOf(Objects.requireNonNull(evidence, "evidence must not be null"));
    }
}
