package io.github.trueruslan.zakupgotov.shopping;

import java.math.BigDecimal;

public enum QuantityUnit {
    PIECE,
    GRAM,
    KILOGRAM,
    MILLILITER,
    LITER;

    QuantityUnit canonicalUnit() {
        return switch (this) {
            case KILOGRAM -> GRAM;
            case LITER -> MILLILITER;
            default -> this;
        };
    }

    BigDecimal toCanonicalAmount(BigDecimal amount) {
        return switch (this) {
            case KILOGRAM, LITER -> amount.movePointRight(3);
            default -> amount;
        };
    }
}
