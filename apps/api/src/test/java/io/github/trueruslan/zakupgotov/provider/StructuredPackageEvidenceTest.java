package io.github.trueruslan.zakupgotov.provider;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.trueruslan.zakupgotov.retailer.RetailerId;
import io.github.trueruslan.zakupgotov.shopping.Quantity;
import io.github.trueruslan.zakupgotov.shopping.QuantityUnit;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class StructuredPackageEvidenceTest {

    private static final Instant OBSERVED_AT = Instant.parse("2026-08-12T16:30:00Z");
    private static final Instant PROVIDER_UPDATED_AT = Instant.parse("2026-08-12T16:25:00Z");
    private static final OfferSnapshotId SNAPSHOT_ID = new OfferSnapshotId(
            UUID.fromString("91919191-9191-9191-9191-919191919191"));

    @Test
    void structuredPackageQuantityIsCanonicalizedAndPreservedByEverySnapshotFactory() {
        var offer = new ObservedOffer(
                RetailerId.PEREKRESTOK,
                "perekrestok-browser",
                AcquisitionMode.BROWSER_BRIDGE,
                "656",
                "3431579",
                "Молоко 3,2%, 970мл",
                new BigDecimal("89.99"),
                "RUB",
                AvailabilityStatus.UNKNOWN,
                OBSERVED_AT,
                "https://www.perekrestok.ru/cat/114/p/moloko-3431579",
                Optional.of(new Quantity(new BigDecimal("0.97"), QuantityUnit.LITER)));

        var expected = new Quantity(new BigDecimal("970"), QuantityUnit.MILLILITER);

        assertThat(offer.packageQuantity()).contains(expected);
        assertThat(OfferSnapshot.observationOnly(SNAPSHOT_ID, offer).packageQuantity())
                .contains(expected);
        assertThat(OfferSnapshot.withProviderUpdatedAt(SNAPSHOT_ID, offer, PROVIDER_UPDATED_AT).packageQuantity())
                .contains(expected);
    }

    @Test
    void presentationTextNeverCreatesPackageEvidence() {
        var offer = new ObservedOffer(
                RetailerId.PEREKRESTOK,
                "perekrestok-browser",
                AcquisitionMode.BROWSER_BRIDGE,
                "656",
                "3431579",
                "Молоко 3,2%, 970мл",
                new BigDecimal("89.99"),
                "RUB",
                AvailabilityStatus.UNKNOWN,
                OBSERVED_AT,
                "https://www.perekrestok.ru/cat/114/p/moloko-3431579");

        assertThat(offer.packageQuantity()).isEmpty();
        assertThat(OfferSnapshot.observationOnly(SNAPSHOT_ID, offer).packageQuantity()).isEmpty();
    }
}
