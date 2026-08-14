package io.github.trueruslan.zakupgotov.pantry;

import io.github.trueruslan.zakupgotov.shopping.Quantity;
import io.github.trueruslan.zakupgotov.shopping.ShoppingRequirement;
import java.util.Objects;

public record PantryItem(ShoppingRequirement requirement, Quantity quantity) {

    public PantryItem {
        requirement = Objects.requireNonNull(requirement, "requirement must not be null");
        quantity = Objects.requireNonNull(quantity, "quantity must not be null");
    }
}
