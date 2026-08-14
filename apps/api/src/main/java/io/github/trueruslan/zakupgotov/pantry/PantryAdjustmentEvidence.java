package io.github.trueruslan.zakupgotov.pantry;

import io.github.trueruslan.zakupgotov.shopping.Quantity;
import io.github.trueruslan.zakupgotov.shopping.ShoppingItemId;
import io.github.trueruslan.zakupgotov.shopping.ShoppingRequirement;
import java.util.Objects;
import java.util.Optional;

public record PantryAdjustmentEvidence(
        ShoppingItemId itemId,
        ShoppingRequirement requirement,
        Quantity required,
        Optional<Quantity> pantryUsed,
        Optional<Quantity> remaining,
        PantryAdjustmentStatus status) {

    public PantryAdjustmentEvidence {
        itemId = Objects.requireNonNull(itemId, "itemId must not be null");
        requirement = Objects.requireNonNull(requirement, "requirement must not be null");
        required = Objects.requireNonNull(required, "required must not be null");
        pantryUsed = Objects.requireNonNull(pantryUsed, "pantryUsed must not be null");
        remaining = Objects.requireNonNull(remaining, "remaining must not be null");
        status = Objects.requireNonNull(status, "status must not be null");

        if (pantryUsed.isPresent()) {
            requireSameUnit(required, pantryUsed.orElseThrow());
        }
        if (remaining.isPresent()) {
            requireSameUnit(required, remaining.orElseThrow());
        }

        switch (status) {
            case UNCHANGED -> validateUnchanged(required, pantryUsed, remaining);
            case PARTIALLY_COVERED -> validatePartial(required, pantryUsed, remaining);
            case FULLY_COVERED -> validateFull(required, pantryUsed, remaining);
        }
    }

    private static void validateUnchanged(
            Quantity required,
            Optional<Quantity> pantryUsed,
            Optional<Quantity> remaining) {
        if (pantryUsed.isPresent()
                || remaining.isEmpty()
                || !sameAmount(required, remaining.orElseThrow())) {
            throw new IllegalArgumentException(
                    "unchanged evidence must have no pantry usage and remaining equal to required");
        }
    }

    private static void validatePartial(
            Quantity required,
            Optional<Quantity> pantryUsed,
            Optional<Quantity> remaining) {
        if (pantryUsed.isEmpty() || remaining.isEmpty()) {
            throw new IllegalArgumentException(
                    "partial evidence must contain pantry usage and remaining quantity");
        }

        var used = pantryUsed.orElseThrow();
        var left = remaining.orElseThrow();
        if (used.amount().compareTo(required.amount()) >= 0
                || left.amount().compareTo(required.amount()) >= 0) {
            throw new IllegalArgumentException(
                    "partial evidence parts must each be smaller than required");
        }
        if (used.amount().add(left.amount()).compareTo(required.amount()) != 0) {
            throw new IllegalArgumentException(
                    "partial evidence pantry usage plus remaining must equal required");
        }
    }

    private static void validateFull(
            Quantity required,
            Optional<Quantity> pantryUsed,
            Optional<Quantity> remaining) {
        if (pantryUsed.isEmpty()
                || remaining.isPresent()
                || !sameAmount(required, pantryUsed.orElseThrow())) {
            throw new IllegalArgumentException(
                    "full evidence must use exactly the required quantity and have no remaining quantity");
        }
    }

    private static void requireSameUnit(Quantity expected, Quantity actual) {
        if (expected.unit() != actual.unit()) {
            throw new IllegalArgumentException("evidence quantity unit must match required unit");
        }
    }

    private static boolean sameAmount(Quantity first, Quantity second) {
        return first.amount().compareTo(second.amount()) == 0;
    }
}
