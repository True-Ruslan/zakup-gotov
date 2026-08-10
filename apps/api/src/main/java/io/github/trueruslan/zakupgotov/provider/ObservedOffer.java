package io.github.trueruslan.zakupgotov.provider;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;

public record ObservedOffer(
        String providerId,
        String fulfillmentContextId,
        String sku,
        BigDecimal price,
        String currencyCode,
        AvailabilityStatus availability,
        Instant observedAt,
        String sourceReference) {

    public ObservedOffer {
        providerId = requireText(providerId, "providerId");
        fulfillmentContextId = requireText(fulfillmentContextId, "fulfillmentContextId");
        sku = requireText(sku, "sku");
        price = requireValue(price, "price");
        if (price.signum() < 0) {
            throw new IllegalArgumentException("price must not be negative");
        }
        currencyCode = requireCurrencyCode(currencyCode);
        availability = requireValue(availability, "availability");
        observedAt = requireValue(observedAt, "observedAt");
        sourceReference = requireText(sourceReference, "sourceReference");
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return value;
    }

    private static <T> T requireValue(T value, String field) {
        if (value == null) {
            throw new IllegalArgumentException(field + " must not be null");
        }
        return value;
    }

    private static String requireCurrencyCode(String value) {
        var code = requireText(value, "currencyCode");
        try {
            Currency.getInstance(code);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("currencyCode must be an ISO 4217 currency code", exception);
        }
        return code;
    }
}
