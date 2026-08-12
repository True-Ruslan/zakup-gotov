package io.github.trueruslan.zakupgotov.preview;

import io.github.trueruslan.zakupgotov.shopping.Quantity;
import java.util.Objects;
import java.util.UUID;

public record ComparisonPreviewRequestedItem(
        UUID id,
        String requirement,
        Quantity quantity) {

    public ComparisonPreviewRequestedItem {
        id = Objects.requireNonNull(id, "id must not be null");
        if (requirement == null || requirement.isBlank()) {
            throw new IllegalArgumentException("requirement must not be blank");
        }
        quantity = Objects.requireNonNull(quantity, "quantity must not be null");
    }
}
