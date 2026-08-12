package io.github.trueruslan.zakupgotov.preview;

import io.github.trueruslan.zakupgotov.basket.BasketItemResolutionStatus;
import io.github.trueruslan.zakupgotov.shopping.Quantity;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public record ComparisonPreviewItem(
        UUID id,
        String requirement,
        Quantity requestedQuantity,
        BasketItemResolutionStatus status,
        List<String> candidateProductNames,
        Optional<ComparisonPreviewSelection> selection) {

    public ComparisonPreviewItem {
        id = Objects.requireNonNull(id, "id must not be null");
        if (requirement == null || requirement.isBlank()) {
            throw new IllegalArgumentException("requirement must not be blank");
        }
        requestedQuantity = Objects.requireNonNull(requestedQuantity, "requestedQuantity must not be null");
        status = Objects.requireNonNull(status, "status must not be null");
        candidateProductNames = List.copyOf(Objects.requireNonNull(candidateProductNames, "candidateProductNames must not be null"));
        selection = Objects.requireNonNull(selection, "selection must not be null");
        if (candidateProductNames.size() > 10) {
            throw new IllegalArgumentException("candidateProductNames must not exceed 10 entries");
        }
    }
}
