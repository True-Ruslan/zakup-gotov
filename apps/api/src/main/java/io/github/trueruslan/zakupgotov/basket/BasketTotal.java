package io.github.trueruslan.zakupgotov.basket;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Objects;

public record BasketTotal(BigDecimal amount, String currencyCode) {

    public BasketTotal {
        amount = Objects.requireNonNull(amount, "amount must not be null");
        if (amount.signum() < 0) {
            throw new IllegalArgumentException("amount must not be negative");
        }
        currencyCode = Objects.requireNonNull(currencyCode, "currencyCode must not be null");
        if (currencyCode.isBlank()) {
            throw new IllegalArgumentException("currencyCode must not be blank");
        }
        try {
            Currency.getInstance(currencyCode);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("currencyCode must be an ISO 4217 currency code", exception);
        }
    }
}
