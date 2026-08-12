package io.github.trueruslan.zakupgotov.provider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.trueruslan.zakupgotov.retailer.RetailerId;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class OfferSnapshotTest {

    private static final Instant OBSERVED_AT = Instant.parse("2026-08-12T06:30:00Z");
    private static final Instant PROVIDER_UPDATED_AT = Instant.parse("2026-08-12T06:10:00Z");
    private static final OfferSnapshotId SNAPSHOT_ID = new OfferSnapshotId(
            UUID.fromString("77777777-7777-7777-7777-777777777777"));

    @Test
    void observationOnlySnapshotPreservesValidatedOfferExactly() {
        var offer = offer(AvailabilityStatus.AVAILABLE);

        var snapshot = OfferSnapshot.observationOnly(SNAPSHOT_ID, offer);

        assertThat(snapshot.id()).isEqualTo(SNAPSHOT_ID);
        assertThat(snapshot.retailerId()).isEqualTo(RetailerId.PYATEROCHKA);
        assertThat(snapshot.sourceProviderId()).isEqualTo("kuper");
        assertThat(snapshot.sourceMode()).isEqualTo(AcquisitionMode.AGGREGATOR);
        assertThat(snapshot.fulfillmentContextId()).isEqualTo("store-42");
        assertThat(snapshot.sku()).isEqualTo("sku-milk-1");
        assertThat(snapshot.price()).isEqualByComparingTo("99.90");
        assertThat(snapshot.currencyCode()).isEqualTo("RUB");
        assertThat(snapshot.availability()).isEqualTo(AvailabilityStatus.AVAILABLE);
        assertThat(snapshot.freshness()).isEqualTo(FreshnessEvidence.observationOnly(OBSERVED_AT));
        assertThat(snapshot.sourceReference()).isEqualTo("fixture://kuper/search/milk.json");
    }

    @Test
    void providerTimestampSnapshotKeepsProviderFreshnessDistinct() {
        var snapshot = OfferSnapshot.withProviderUpdatedAt(
                SNAPSHOT_ID,
                offer(AvailabilityStatus.AVAILABLE),
                PROVIDER_UPDATED_AT);

        assertThat(snapshot.freshness())
                .isEqualTo(FreshnessEvidence.providerUpdatedAt(OBSERVED_AT, PROVIDER_UPDATED_AT));
        assertThat(snapshot.freshness().observedAt()).isEqualTo(OBSERVED_AT);
        assertThat(snapshot.freshness().providerUpdatedAt()).contains(PROVIDER_UPDATED_AT);
    }

    @Test
    void preservesUnknownAvailabilityWithoutGuessing() {
        var snapshot = OfferSnapshot.observationOnly(SNAPSHOT_ID, offer(AvailabilityStatus.UNKNOWN));

        assertThat(snapshot.availability()).isEqualTo(AvailabilityStatus.UNKNOWN);
    }

    @Test
    void snapshotIdentityIsIndependentFromOtherwiseIdenticalObservation() {
        var offer = offer(AvailabilityStatus.AVAILABLE);
        var otherId = new OfferSnapshotId(UUID.fromString("88888888-8888-8888-8888-888888888888"));

        var first = OfferSnapshot.observationOnly(SNAPSHOT_ID, offer);
        var second = OfferSnapshot.observationOnly(otherId, offer);

        assertThat(first.id()).isNotEqualTo(second.id());
        assertThat(first.retailerId()).isEqualTo(second.retailerId());
        assertThat(first.sku()).isEqualTo(second.sku());
        assertThat(first.freshness()).isEqualTo(second.freshness());
    }

    @Test
    void snapshotCanOnlyBeCreatedFromValidatedObservationFactories() {
        assertThat(OfferSnapshot.class.getConstructors()).isEmpty();
    }

    @Test
    void rejectsMissingIdentityObservationAndInvalidProviderTime() {
        assertThatThrownBy(() -> OfferSnapshot.observationOnly(null, offer(AvailabilityStatus.AVAILABLE)))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("id");
        assertThatThrownBy(() -> OfferSnapshot.observationOnly(SNAPSHOT_ID, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("observation");
        assertThatThrownBy(() -> OfferSnapshot.withProviderUpdatedAt(
                        SNAPSHOT_ID,
                        offer(AvailabilityStatus.AVAILABLE),
                        Instant.parse("2026-08-12T06:30:01Z")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("providerUpdatedAt");
    }

    private static ObservedOffer offer(AvailabilityStatus availability) {
        return new ObservedOffer(
                RetailerId.PYATEROCHKA,
                "kuper",
                AcquisitionMode.AGGREGATOR,
                "store-42",
                "sku-milk-1",
                new BigDecimal("99.90"),
                "RUB",
                availability,
                OBSERVED_AT,
                "fixture://kuper/search/milk.json");
    }
}
