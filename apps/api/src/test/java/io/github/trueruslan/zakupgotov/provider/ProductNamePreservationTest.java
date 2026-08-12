package io.github.trueruslan.zakupgotov.provider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.trueruslan.zakupgotov.retailer.RetailerId;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ProductNamePreservationTest {

    private static final Instant OBSERVED_AT = Instant.parse("2026-08-12T08:30:00Z");

    @Test
    void requiresAndNormalizesObservedProductNameAtTheProviderTrustBoundary() {
        var offer = observed("  Молоко Простоквашино 3,2%  ");

        assertThat(offer.productName()).isEqualTo("Молоко Простоквашино 3,2%");

        assertThatThrownBy(() -> observed("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("productName");
    }

    @Test
    void observationOnlySnapshotPreservesValidatedProductName() {
        var offer = observed("Молоко Простоквашино 3,2%");
        var snapshot = OfferSnapshot.observationOnly(
                new OfferSnapshotId(UUID.fromString("11111111-1111-1111-1111-111111111111")),
                offer);

        assertThat(snapshot.productName()).isEqualTo(offer.productName());
    }

    @Test
    void providerTimestampSnapshotPreservesValidatedProductName() {
        var offer = observed("Молоко Простоквашино 3,2%");
        var snapshot = OfferSnapshot.withProviderUpdatedAt(
                new OfferSnapshotId(UUID.fromString("22222222-2222-2222-2222-222222222222")),
                offer,
                Instant.parse("2026-08-12T08:20:00Z"));

        assertThat(snapshot.productName()).isEqualTo(offer.productName());
    }

    private static ObservedOffer observed(String productName) {
        return new ObservedOffer(
                RetailerId.PYATEROCHKA,
                "pyaterochka-browser",
                AcquisitionMode.BROWSER_BRIDGE,
                "store-42",
                "sku-1",
                productName,
                new BigDecimal("99.90"),
                "RUB",
                AvailabilityStatus.UNKNOWN,
                OBSERVED_AT,
                "https://5ka.ru/product/sku-1");
    }
}
