package io.github.trueruslan.zakupgotov.shopping;

import java.util.Objects;

public record ShoppingItem(
        ShoppingItemId id,
        ShoppingRequirement requirement,
        Quantity quantity) {

    public ShoppingItem {
        id = Objects.requireNonNull(id, "id must not be null");
        requirement = Objects.requireNonNull(requirement, "requirement must not be null");
        quantity = Objects.requireNonNull(quantity, "quantity must not be null");
    }
}
