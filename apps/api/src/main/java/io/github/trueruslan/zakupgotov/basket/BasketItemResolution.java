package io.github.trueruslan.zakupgotov.basket;

import io.github.trueruslan.zakupgotov.matching.ProductMatchResult;
import io.github.trueruslan.zakupgotov.matching.ProductMatchStatus;
import io.github.trueruslan.zakupgotov.shopping.ShoppingItem;
import java.util.Objects;
import java.util.Optional;

public record BasketItemResolution(
        ShoppingItem item,
        ProductMatchResult match,
        BasketItemResolutionStatus status,
        Optional<PackageSelection> selection) {

    public BasketItemResolution {
        item = Objects.requireNonNull(item, "item must not be null");
        match = Objects.requireNonNull(match, "match must not be null");
        status = Objects.requireNonNull(status, "status must not be null");
        selection = Objects.requireNonNull(selection, "selection must not be null");

        switch (status) {
            case FULFILLED, AVAILABILITY_UNKNOWN -> validateSelected(match, selection, status);
            case UNMATCHED -> validateUnselected(match, selection, ProductMatchStatus.UNMATCHED, status);
            case AMBIGUOUS -> validateUnselected(match, selection, ProductMatchStatus.AMBIGUOUS, status);
            case UNAVAILABLE, PACKAGE_QUANTITY_UNKNOWN, QUANTITY_UNIT_MISMATCH ->
                    validateUnselected(match, selection, ProductMatchStatus.MATCHED, status);
        }
    }

    private static void validateSelected(
            ProductMatchResult match,
            Optional<PackageSelection> selection,
            BasketItemResolutionStatus status) {
        if (match.status() != ProductMatchStatus.MATCHED || selection.isEmpty()) {
            throw new IllegalArgumentException(status + " requires MATCHED result and package selection");
        }
        var matched = match.candidates().getFirst();
        if (selection.orElseThrow().snapshot() != matched) {
            throw new IllegalArgumentException("package selection snapshot must be the matched candidate");
        }
    }

    private static void validateUnselected(
            ProductMatchResult match,
            Optional<PackageSelection> selection,
            ProductMatchStatus requiredMatchStatus,
            BasketItemResolutionStatus status) {
        if (match.status() != requiredMatchStatus || selection.isPresent()) {
            throw new IllegalArgumentException(status + " requires " + requiredMatchStatus + " result without selection");
        }
    }
}
