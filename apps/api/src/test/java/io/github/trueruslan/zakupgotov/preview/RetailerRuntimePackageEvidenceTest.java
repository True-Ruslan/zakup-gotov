package io.github.trueruslan.zakupgotov.preview;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.trueruslan.zakupgotov.basket.PackageQuantityBinding;
import io.github.trueruslan.zakupgotov.basket.PackageQuantitySet;
import io.github.trueruslan.zakupgotov.provider.AcquisitionMode;
import io.github.trueruslan.zakupgotov.provider.AvailabilityStatus;
import io.github.trueruslan.zakupgotov.provider.ObservedOffer;
import io.github.trueruslan.zakupgotov.provider.OfferSnapshot;
import io.github.trueruslan.zakupgotov.provider.OfferSnapshotId;
import io.github.trueruslan.zakupgotov.provider.ProviderPathSelection;
import io.github.trueruslan.zakupgotov.provider.ProviderSearchOutcome;
import io.github.trueruslan.zakupgotov.retailer.RetailerId;
import io.github.trueruslan.zakupgotov.shopping.Quantity;
import io.github.trueruslan.zakupgotov.shopping.QuantityUnit;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class RetailerRuntimePackageEvidenceTest {

    @Test
    void derivesRuntimePackageEvidenceFromStructuredSnapshotEvidence() {
        var packageQuantity = new Quantity(new BigDecimal("0.97"), QuantityUnit.LITER);
        var observation = observation(Optional.of(packageQuantity));
        var snapshotId = new OfferSnapshotId(UUID.fromString("42424242-4242-4242-4242-424242424242"));
        var snapshot = OfferSnapshot.observationOnly(snapshotId, observation);
        var outcome = successfulOutcome(observation);

        var evidence = new RetailerRuntimeEvidence(
                RetailerId.PEREKRESTOK,
                outcome,
                List.of(snapshot));

        assertThat(evidence.packageQuantities().quantityFor(snapshotId))
                .contains(new Quantity(new BigDecimal("970"), QuantityUnit.MILLILITER));
    }

    @Test
    void rejectsParallelPackageEvidenceThatIsAbsentFromSnapshot() {
        var observation = observation(Optional.empty());
        var snapshotId = new OfferSnapshotId(UUID.fromString("41414141-4141-4141-4141-414141414141"));
        var snapshot = OfferSnapshot.observationOnly(snapshotId, observation);
        var outcome = successfulOutcome(observation);
        var parallelEvidence = PackageQuantitySet.of(List.of(new PackageQuantityBinding(
                snapshotId,
                new Quantity(new BigDecimal("970"), QuantityUnit.MILLILITER))));

        assertThatThrownBy(() -> new RetailerRuntimeEvidence(
                        RetailerId.PEREKRESTOK,
                        outcome,
                        List.of(snapshot),
                        parallelEvidence))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("package quantity")
                .hasMessageContaining("snapshot");
    }

    private static ObservedOffer observation(Optional<Quantity> packageQuantity) {
        return new ObservedOffer(
                RetailerId.PEREKRESTOK,
                "fixture-perekrestok",
                AcquisitionMode.DIRECT_API,
                "store-42",
                "sku-milk",
                "Молоко 970мл",
                new BigDecimal("89.99"),
                "RUB",
                AvailabilityStatus.AVAILABLE,
                Instant.parse("2026-08-12T16:30:00Z"),
                "fixture://perekrestok/milk",
                packageQuantity);
    }

    private static ProviderSearchOutcome successfulOutcome(ObservedOffer observation) {
        return new ProviderSearchOutcome(
                RetailerId.PEREKRESTOK,
                Optional.of(new ProviderPathSelection("fixture-perekrestok", AcquisitionMode.DIRECT_API)),
                List.of(observation),
                List.of());
    }
}
