package io.github.trueruslan.zakupgotov.provider;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class ObservedOfferTest {

    private static final Instant OBSERVED_AT = Instant.parse("2026-08-10T07:00:00Z");

    @Test
    void acceptsCompleteLocationSpecificOffer() {
        new ObservedOffer(
                "provider-a",
                "fulfillment-context-1",
                "sku-1",
                new BigDecimal("149.90"),
                "RUB",
                AvailabilityStatus.AVAILABLE,
                OBSERVED_AT,
                "fixture://provider-a/search/milk.json");
    }

    @Test
    void rejectsOfferWithoutFulfillmentContext() {
        assertThatThrownBy(() -> new ObservedOffer(
                        "provider-a",
                        " ",
                        "sku-1",
                        new BigDecimal("149.90"),
                        "RUB",
                        AvailabilityStatus.AVAILABLE,
                        OBSERVED_AT,
                        "fixture://provider-a/search/milk.json"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("fulfillmentContextId");
    }

    @Test
    void rejectsOfferWithoutObservationTime() {
        assertThatThrownBy(() -> new ObservedOffer(
                        "provider-a",
                        "fulfillment-context-1",
                        "sku-1",
                        new BigDecimal("149.90"),
                        "RUB",
                        AvailabilityStatus.AVAILABLE,
                        null,
                        "fixture://provider-a/search/milk.json"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("observedAt");
    }

    @Test
    void rejectsOfferWithoutSourceReference() {
        assertThatThrownBy(() -> new ObservedOffer(
                        "provider-a",
                        "fulfillment-context-1",
                        "sku-1",
                        new BigDecimal("149.90"),
                        "RUB",
                        AvailabilityStatus.AVAILABLE,
                        OBSERVED_AT,
                        ""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("sourceReference");
    }
}
