package io.github.trueruslan.zakupgotov.preview;

import io.github.trueruslan.zakupgotov.basket.PackageQuantitySet;
import io.github.trueruslan.zakupgotov.provider.OfferSnapshot;
import io.github.trueruslan.zakupgotov.provider.OfferSnapshotId;
import io.github.trueruslan.zakupgotov.provider.ProviderSearchOutcome;
import io.github.trueruslan.zakupgotov.retailer.RetailerId;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public record RetailerRuntimeEvidence(
        RetailerId retailerId,
        Optional<String> fulfillmentContextId,
        ProviderSearchOutcome providerOutcome,
        List<OfferSnapshot> snapshots,
        PackageQuantitySet packageQuantities) {

    public RetailerRuntimeEvidence(
            RetailerId retailerId,
            ProviderSearchOutcome providerOutcome,
            List<OfferSnapshot> snapshots) {
        this(
                retailerId,
                inferFulfillmentContext(providerOutcome, snapshots),
                providerOutcome,
                snapshots,
                PackageQuantitySet.fromSnapshots(snapshots));
    }

    public RetailerRuntimeEvidence(
            RetailerId retailerId,
            ProviderSearchOutcome providerOutcome,
            List<OfferSnapshot> snapshots,
            PackageQuantitySet packageQuantities) {
        this(retailerId, inferFulfillmentContext(providerOutcome, snapshots), providerOutcome, snapshots, packageQuantities);
    }

    public RetailerRuntimeEvidence {
        retailerId = Objects.requireNonNull(retailerId, "retailerId must not be null");
        fulfillmentContextId = Objects.requireNonNull(fulfillmentContextId, "fulfillmentContextId must not be null")
                .map(RetailerRuntimeEvidence::requireContext);
        providerOutcome = Objects.requireNonNull(providerOutcome, "providerOutcome must not be null");
        snapshots = List.copyOf(Objects.requireNonNull(snapshots, "snapshots must not be null"));
        packageQuantities = Objects.requireNonNull(packageQuantities, "packageQuantities must not be null");

        if (providerOutcome.retailerId() != retailerId) {
            throw new IllegalArgumentException("provider outcome retailer must match retailerId");
        }
        if (providerOutcome.succeeded() && fulfillmentContextId.isEmpty()) {
            throw new IllegalArgumentException("successful provider outcome requires fulfillment context evidence");
        }
        if (!providerOutcome.succeeded() && !snapshots.isEmpty()) {
            throw new IllegalArgumentException("unavailable provider outcome must not carry snapshots");
        }

        var context = fulfillmentContextId.orElse(null);
        for (var offer : providerOutcome.offers()) {
            Objects.requireNonNull(offer, "provider outcome offer must not be null");
            if (offer.retailerId() != retailerId) {
                throw new IllegalArgumentException("provider outcome offer retailer must match retailerId");
            }
            if (context != null && !offer.fulfillmentContextId().equals(context)) {
                throw new IllegalArgumentException("provider outcome offer fulfillment context must match evidence context");
            }
        }

        Set<OfferSnapshotId> snapshotIds = new HashSet<>();
        for (var snapshot : snapshots) {
            Objects.requireNonNull(snapshot, "snapshot must not be null");
            if (snapshot.retailerId() != retailerId) {
                throw new IllegalArgumentException("snapshot retailer must match retailerId");
            }
            if (context != null && !snapshot.fulfillmentContextId().equals(context)) {
                throw new IllegalArgumentException("snapshot fulfillment context must match evidence context");
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

        var snapshotPackageQuantities = PackageQuantitySet.fromSnapshots(snapshots);
        if (!packageQuantities.bindings().equals(snapshotPackageQuantities.bindings())) {
            throw new IllegalArgumentException("package quantity evidence must match structured snapshot evidence");
        }
        packageQuantities = snapshotPackageQuantities;
    }

    public static RetailerRuntimeEvidence withFulfillmentContext(
            RetailerId retailerId,
            String fulfillmentContextId,
            ProviderSearchOutcome providerOutcome,
            List<OfferSnapshot> snapshots) {
        return new RetailerRuntimeEvidence(
                retailerId,
                Optional.of(requireContext(fulfillmentContextId)),
                providerOutcome,
                snapshots,
                PackageQuantitySet.fromSnapshots(snapshots));
    }

    public static RetailerRuntimeEvidence withFulfillmentContext(
            RetailerId retailerId,
            String fulfillmentContextId,
            ProviderSearchOutcome providerOutcome,
            List<OfferSnapshot> snapshots,
            PackageQuantitySet packageQuantities) {
        return new RetailerRuntimeEvidence(
                retailerId,
                Optional.of(requireContext(fulfillmentContextId)),
                providerOutcome,
                snapshots,
                packageQuantities);
    }

    private static Optional<String> inferFulfillmentContext(
            ProviderSearchOutcome providerOutcome,
            List<OfferSnapshot> snapshots) {
        if (snapshots != null) {
            for (var snapshot : snapshots) {
                if (snapshot != null) {
                    return Optional.of(snapshot.fulfillmentContextId());
                }
            }
        }
        if (providerOutcome != null) {
            for (var offer : providerOutcome.offers()) {
                if (offer != null) {
                    return Optional.of(offer.fulfillmentContextId());
                }
            }
        }
        return Optional.empty();
    }

    private static String requireContext(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("fulfillmentContextId must not be blank");
        }
        return value;
    }
}
