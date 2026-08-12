package io.github.trueruslan.zakupgotov.shopping;

import java.math.BigDecimal;
import java.util.Objects;

public record Quantity(BigDecimal amount, QuantityUnit unit) {

    public Quantity {
        amount = Objects.requireNonNull(amount, "amount must not be null");
        unit = Objects.requireNonNull(unit, "unit must not be null");
        if (amount.signum() <= 0) {
            throw new IllegalArgumentException("amount must be positive");
        }

        amount = normalize(unit.toCanonicalAmount(amount));
        unit = unit.canonicalUnit();
    }

    private static BigDecimal normalize(BigDecimal value) {
        var normalized = value.stripTrailingZeros();
        return normalized.scale() < 0 ? normalized.setScale(0) : normalized;
    }
}
