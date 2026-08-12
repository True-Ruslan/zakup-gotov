package io.github.trueruslan.zakupgotov.basket;

import io.github.trueruslan.zakupgotov.provider.OfferSnapshotId;
import io.github.trueruslan.zakupgotov.shopping.Quantity;
import java.util.Objects;

public record PackageQuantityBinding(
        OfferSnapshotId snapshotId,
        Quantity packageQuantity) {

    public PackageQuantityBinding {
        snapshotId = Objects.requireNonNull(snapshotId, "snapshotId must not be null");
        packageQuantity = Objects.requireNonNull(packageQuantity, "packageQuantity must not be null");
    }
}
