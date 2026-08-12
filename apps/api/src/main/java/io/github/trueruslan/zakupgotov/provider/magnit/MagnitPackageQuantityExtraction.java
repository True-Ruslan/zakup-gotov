package io.github.trueruslan.zakupgotov.provider.magnit;

import io.github.trueruslan.zakupgotov.shopping.Quantity;
import java.util.Objects;
import java.util.Optional;

public record MagnitPackageQuantityExtraction(
        MagnitPackageQuantityStatus status,
        Optional<Quantity> packageQuantity) {

    public MagnitPackageQuantityExtraction {
        status = Objects.requireNonNull(status, "status must not be null");
        packageQuantity = Objects.requireNonNull(packageQuantity, "packageQuantity must not be null");
        if ((status == MagnitPackageQuantityStatus.FOUND) != packageQuantity.isPresent()) {
            throw new IllegalArgumentException("packageQuantity must be present exactly for FOUND status");
        }
    }

    public static MagnitPackageQuantityExtraction found(Quantity quantity) {
        return new MagnitPackageQuantityExtraction(
                MagnitPackageQuantityStatus.FOUND,
                Optional.of(Objects.requireNonNull(quantity, "quantity must not be null")));
    }

    public static MagnitPackageQuantityExtraction empty(MagnitPackageQuantityStatus status) {
        if (status == MagnitPackageQuantityStatus.FOUND) {
            throw new IllegalArgumentException("FOUND requires package quantity");
        }
        return new MagnitPackageQuantityExtraction(status, Optional.empty());
    }
}
