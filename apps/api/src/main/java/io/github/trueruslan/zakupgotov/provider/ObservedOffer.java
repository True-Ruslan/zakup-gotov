package io.github.trueruslan.zakupgotov.provider;

import io.github.trueruslan.zakupgotov.retailer.RetailerId;
import io.github.trueruslan.zakupgotov.shopping.Quantity;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.Optional;

public record ObservedOffer(
        RetailerId retailerId,
        String sourceProviderId,
        AcquisitionMode sourceMode,
        String fulfillmentContextId,
        String sku,
        String productName,
        BigDecimal price,
        String currencyCode,
        AvailabilityStatus availability,
        Instant observedAt,
        String sourceReference,
        Optional<Quantity> packageQuantity) {

    public ObservedOffer {
        retailerId = requireValue(retailerId, "retailerId");
        sourceProviderId = requireText(sourceProviderId, "sourceProviderId");
        sourceMode = requireValue(sourceMode, "sourceMode");
        fulfillmentContextId = requireText(fulfillmentContextId, "fulfillmentContextId");
        sku = requireText(sku, "sku");
        productName = requireText(productName, "productName").strip();
        price = requireValue(price, "price");
        if (price.signum() < 0) {
            throw new IllegalArgumentException("price must not be negative");
        }
        currencyCode = requireCurrencyCode(currencyCode);
        availability = requireValue(availability, "availability");
        observedAt = requireValue(observedAt, "observedAt");
        sourceReference = requireText(sourceReference, "sourceReference");
        packageQuantity = requireValue(packageQuantity, "packageQuantity");
    }

    public ObservedOffer(
            RetailerId retailerId,
            String sourceProviderId,
            AcquisitionMode sourceMode,
            String fulfillmentContextId,
            String sku,
            String productName,
            BigDecimal price,
            String currencyCode,
            AvailabilityStatus availability,
            Instant observedAt,
            String sourceReference) {
        this(
                retailerId,
                sourceProviderId,
                sourceMode,
                fulfillmentContextId,
                sku,
                productName,
                price,
                currencyCode,
                availability,
                observedAt,
                sourceReference,
                Optional.empty());
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
