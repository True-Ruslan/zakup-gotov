package io.github.trueruslan.zakupgotov.basket;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.trueruslan.zakupgotov.provider.AcquisitionMode;
import io.github.trueruslan.zakupgotov.provider.AvailabilityStatus;
import io.github.trueruslan.zakupgotov.provider.ObservedOffer;
import io.github.trueruslan.zakupgotov.provider.OfferSnapshot;
import io.github.trueruslan.zakupgotov.provider.OfferSnapshotId;
import io.github.trueruslan.zakupgotov.retailer.RetailerId;
import io.github.trueruslan.zakupgotov.shopping.Quantity;
import io.github.trueruslan.zakupgotov.shopping.QuantityUnit;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class PackageQuantityProjectionTest {

    private static final Instant OBSERVED_AT = Instant.parse("2026-08-12T16:30:00Z");
    private static final OfferSnapshotId FIRST_ID = new OfferSnapshotId(
            UUID.fromString("10101010-1010-1010-1010-101010101010"));
    private static final OfferSnapshotId SECOND_ID = new OfferSnapshotId(
            UUID.fromString("20202020-2020-2020-2020-202020202020"));
    private static final OfferSnapshotId THIRD_ID = new OfferSnapshotId(
            UUID.fromString("30303030-3030-3030-3030-303030303030"));

    @Test
    void projectsOnlyExplicitStructuredPackageEvidenceInSnapshotOrder() {
        var first = OfferSnapshot.observationOnly(
                FIRST_ID,
                offer("sku-flour", "Мука", Optional.of(new Quantity(new BigDecimal("0.5"), QuantityUnit.KILOGRAM))));
        var second = OfferSnapshot.observationOnly(
                SECOND_ID,
                offerWithoutPackage("sku-water", "Вода питьевая 1,5л"));
        var third = OfferSnapshot.observationOnly(
                THIRD_ID,
                offer("sku-eggs", "Яйца", Optional.of(new Quantity(new BigDecimal("2"), QuantityUnit.PIECE))));

        var set = PackageQuantitySet.fromSnapshots(List.of(first, second, third));

        assertThat(set.bindings())
                .extracting(PackageQuantityBinding::snapshotId)
                .containsExactly(FIRST_ID, THIRD_ID);
        assertThat(set.quantityFor(FIRST_ID))
                .contains(new Quantity(new BigDecimal("500"), QuantityUnit.GRAM));
        assertThat(set.quantityFor(SECOND_ID)).isEmpty();
        assertThat(set.quantityFor(THIRD_ID))
                .contains(new Quantity(new BigDecimal("2"), QuantityUnit.PIECE));
    }

    @Test
    void projectionFailsClosedForMissingInputOrSnapshot() {
        assertThatThrownBy(() -> PackageQuantitySet.fromSnapshots(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("snapshots");
        assertThatThrownBy(() -> PackageQuantitySet.fromSnapshots(Arrays.asList((OfferSnapshot) null)))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("snapshot");
    }

    private static ObservedOffer offer(String sku, String name, Optional<Quantity> packageQuantity) {
        return new ObservedOffer(
                RetailerId.PEREKRESTOK,
                "structured-fixture",
                AcquisitionMode.AGGREGATOR,
                "store-42",
                sku,
                name,
                new BigDecimal("100.00"),
                "RUB",
                AvailabilityStatus.AVAILABLE,
                OBSERVED_AT,
                "fixture://structured-package/" + sku,
                packageQuantity);
    }

    private static ObservedOffer offerWithoutPackage(String sku, String name) {
        return new ObservedOffer(
                RetailerId.PEREKRESTOK,
                "structured-fixture",
                AcquisitionMode.AGGREGATOR,
                "store-42",
                sku,
                name,
                new BigDecimal("100.00"),
                "RUB",
                AvailabilityStatus.AVAILABLE,
                OBSERVED_AT,
                "fixture://structured-package/" + sku);
    }
}
