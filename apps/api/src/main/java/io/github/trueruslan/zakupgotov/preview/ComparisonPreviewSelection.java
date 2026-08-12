package io.github.trueruslan.zakupgotov.preview;

import io.github.trueruslan.zakupgotov.shopping.Quantity;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Objects;

public record ComparisonPreviewSelection(
        String productName,
        Quantity packageQuantity,
        BigInteger packageCount,
        Quantity coveredQuantity,
        BigDecimal lineTotal,
        String currencyCode) {

    public ComparisonPreviewSelection {
        if (productName == null || productName.isBlank()) {
            throw new IllegalArgumentException("productName must not be blank");
        }
        packageQuantity = Objects.requireNonNull(packageQuantity, "packageQuantity must not be null");
        packageCount = Objects.requireNonNull(packageCount, "packageCount must not be null");
        coveredQuantity = Objects.requireNonNull(coveredQuantity, "coveredQuantity must not be null");
        lineTotal = Objects.requireNonNull(lineTotal, "lineTotal must not be null");
        if (currencyCode == null || currencyCode.isBlank()) {
            throw new IllegalArgumentException("currencyCode must not be blank");
        }
    }
}
