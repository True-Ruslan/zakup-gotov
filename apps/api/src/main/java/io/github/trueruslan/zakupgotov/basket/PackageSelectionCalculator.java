package io.github.trueruslan.zakupgotov.basket;

import io.github.trueruslan.zakupgotov.provider.OfferSnapshot;
import io.github.trueruslan.zakupgotov.shopping.Quantity;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

final class PackageSelectionCalculator {

    private PackageSelectionCalculator() {}

    static PackageSelection calculate(
            OfferSnapshot snapshot,
            Quantity requiredQuantity,
            Quantity packageQuantity) {
        Objects.requireNonNull(snapshot, "snapshot must not be null");
        Objects.requireNonNull(requiredQuantity, "requiredQuantity must not be null");
        Objects.requireNonNull(packageQuantity, "packageQuantity must not be null");

        if (requiredQuantity.unit() != packageQuantity.unit()) {
            throw new IllegalArgumentException("requiredQuantity unit must match packageQuantity unit");
        }

        var packageCountDecimal = requiredQuantity.amount()
                .divide(packageQuantity.amount(), 0, RoundingMode.CEILING);
        var packageCount = packageCountDecimal.toBigIntegerExact();
        var packageCountAmount = new BigDecimal(packageCount);
        var providedQuantity = new Quantity(
                packageQuantity.amount().multiply(packageCountAmount),
                packageQuantity.unit());
        var lineTotal = snapshot.price().multiply(packageCountAmount);

        return new PackageSelection(
                snapshot,
                packageQuantity,
                packageCount,
                providedQuantity,
                lineTotal);
    }
}
