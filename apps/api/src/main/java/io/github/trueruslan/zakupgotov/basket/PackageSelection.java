package io.github.trueruslan.zakupgotov.basket;

import io.github.trueruslan.zakupgotov.provider.OfferSnapshot;
import io.github.trueruslan.zakupgotov.shopping.Quantity;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Objects;

public record PackageSelection(
        OfferSnapshot snapshot,
        Quantity packageQuantity,
        BigInteger packageCount,
        Quantity providedQuantity,
        BigDecimal lineTotal) {

    public PackageSelection {
        snapshot = Objects.requireNonNull(snapshot, "snapshot must not be null");
        packageQuantity = Objects.requireNonNull(packageQuantity, "packageQuantity must not be null");
        packageCount = Objects.requireNonNull(packageCount, "packageCount must not be null");
        providedQuantity = Objects.requireNonNull(providedQuantity, "providedQuantity must not be null");
        lineTotal = Objects.requireNonNull(lineTotal, "lineTotal must not be null");

        if (packageCount.signum() <= 0) {
            throw new IllegalArgumentException("packageCount must be positive");
        }
        if (packageQuantity.unit() != providedQuantity.unit()) {
            throw new IllegalArgumentException("providedQuantity unit must match packageQuantity unit");
        }

        var expectedProvided = packageQuantity.amount().multiply(new BigDecimal(packageCount));
        if (expectedProvided.compareTo(providedQuantity.amount()) != 0) {
            throw new IllegalArgumentException("providedQuantity must equal packageQuantity multiplied by packageCount");
        }

        var expectedTotal = snapshot.price().multiply(new BigDecimal(packageCount));
        if (expectedTotal.compareTo(lineTotal) != 0) {
            throw new IllegalArgumentException("lineTotal must equal snapshot price multiplied by packageCount");
        }
    }
}
