package io.github.trueruslan.zakupgotov.preview;

import io.github.trueruslan.zakupgotov.basket.PackageQuantitySet;
import io.github.trueruslan.zakupgotov.provider.OfferSnapshot;
import io.github.trueruslan.zakupgotov.provider.OfferSnapshotId;
import io.github.trueruslan.zakupgotov.provider.ProviderSearchOutcome;
import io.github.trueruslan.zakupgotov.retailer.RetailerId;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public record RetailerRuntimeEvidence(
        RetailerId retailerId,
        ProviderSearchOutcome providerOutcome,
        List<OfferSnapshot> snapshots,
        PackageQuantitySet packageQuantities) {

    public RetailerRuntimeEvidence {
        retailerId = Objects.requireNonNull(retailerId, "retailerId must not be null");
        providerOutcome = Objects.requireNonNull(providerOutcome, "providerOutcome must not be null");
        snapshots = List.copyOf(Objects.requireNonNull(snapshots, "snapshots must not be null"));
        packageQuantities = Objects.requireNonNull(packageQuantities, "packageQuantities must not be null");

        if (providerOutcome.retailerId() != retailerId) {
            throw new IllegalArgumentException("provider outcome retailer must match retailerId");
        }
        if (!providerOutcome.succeeded() && !snapshots.isEmpty()) {
            throw new IllegalArgumentException("unavailable provider outcome must not carry snapshots");
        }

        Set<OfferSnapshotId> snapshotIds = new HashSet<>();
        for (var snapshot : snapshots) {
            Objects.requireNonNull(snapshot, "snapshot must not be null");
            if (snapshot.retailerId() != retailerId) {
                throw new IllegalArgumentException("snapshot retailer must match retailerId");
            }
            if (!snapshotIds.add(snapshot.id())) {
                throw new IllegalArgumentException("duplicate snapshot id: " + snapshot.id().value());
            }
        }

        for (var binding : packageQuantities.bindings()) {
            if (!snapshotIds.contains(binding.snapshotId())) {
                throw new IllegalArgumentException(
                        "package quantity binding must reference a snapshot in retailer evidence: "
                                + binding.snapshotId().value());
            }
        }
    }
}
