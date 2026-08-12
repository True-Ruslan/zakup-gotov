package io.github.trueruslan.zakupgotov.basket;

import io.github.trueruslan.zakupgotov.matching.MatchScope;
import io.github.trueruslan.zakupgotov.shopping.ShoppingListId;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public record SingleStoreBasketQuote(
        MatchScope scope,
        ShoppingListId shoppingListId,
        BasketQuoteStatus status,
        List<BasketItemResolution> items,
        Optional<BasketTotal> total) {

    public SingleStoreBasketQuote {
        scope = Objects.requireNonNull(scope, "scope must not be null");
        shoppingListId = Objects.requireNonNull(shoppingListId, "shoppingListId must not be null");
        status = Objects.requireNonNull(status, "status must not be null");
        items = List.copyOf(Objects.requireNonNull(items, "items must not be null"));
        total = Objects.requireNonNull(total, "total must not be null");
        if (items.isEmpty()) {
            throw new IllegalArgumentException("basket quote items must not be empty");
        }

        switch (status) {
            case COMPLETE -> validateComplete(items, total);
            case UNCERTAIN -> validateUncertain(items, total);
            case INCOMPLETE -> validateIncomplete(items, total);
        }
    }

    private static void validateComplete(
            List<BasketItemResolution> items,
            Optional<BasketTotal> total) {
        if (total.isEmpty() || items.stream().anyMatch(item -> item.status() != BasketItemResolutionStatus.FULFILLED)) {
            throw new IllegalArgumentException("COMPLETE quote requires only FULFILLED items and a total");
        }
    }

    private static void validateUncertain(
            List<BasketItemResolution> items,
            Optional<BasketTotal> total) {
        var hasUnknown = items.stream()
                .anyMatch(item -> item.status() == BasketItemResolutionStatus.AVAILABILITY_UNKNOWN);
        var allSelected = items.stream().allMatch(item -> item.status() == BasketItemResolutionStatus.FULFILLED
                || item.status() == BasketItemResolutionStatus.AVAILABILITY_UNKNOWN);
        if (total.isEmpty() || !hasUnknown || !allSelected) {
            throw new IllegalArgumentException(
                    "UNCERTAIN quote requires selected items, at least one AVAILABILITY_UNKNOWN item and a total");
        }
    }

    private static void validateIncomplete(
            List<BasketItemResolution> items,
            Optional<BasketTotal> total) {
        var hasFailure = items.stream().anyMatch(item -> item.status() != BasketItemResolutionStatus.FULFILLED
                && item.status() != BasketItemResolutionStatus.AVAILABILITY_UNKNOWN);
        if (!hasFailure || total.isPresent()) {
            throw new IllegalArgumentException("INCOMPLETE quote requires an incomplete item and no total");
        }
    }
}
