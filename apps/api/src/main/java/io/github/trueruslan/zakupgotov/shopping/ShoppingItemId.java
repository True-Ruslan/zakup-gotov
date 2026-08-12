package io.github.trueruslan.zakupgotov.shopping;

import java.util.Objects;
import java.util.UUID;

public record ShoppingItemId(UUID value) {

    public ShoppingItemId {
        value = Objects.requireNonNull(value, "value must not be null");
    }
}
