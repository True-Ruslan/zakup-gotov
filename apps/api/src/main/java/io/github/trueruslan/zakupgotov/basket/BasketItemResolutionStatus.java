package io.github.trueruslan.zakupgotov.basket;

public enum BasketItemResolutionStatus {
    FULFILLED,
    AVAILABILITY_UNKNOWN,
    UNMATCHED,
    AMBIGUOUS,
    UNAVAILABLE,
    PACKAGE_QUANTITY_UNKNOWN,
    QUANTITY_UNIT_MISMATCH
}
