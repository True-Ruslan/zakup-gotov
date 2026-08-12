package io.github.trueruslan.zakupgotov.shopping;

import java.util.Objects;
import java.util.UUID;

public record ShoppingListId(UUID value) {

    public ShoppingListId {
        value = Objects.requireNonNull(value, "value must not be null");
    }
}
