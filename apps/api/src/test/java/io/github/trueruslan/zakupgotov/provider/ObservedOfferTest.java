package io.github.trueruslan.zakupgotov.provider;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class ObservedOfferTest {

    private static final Instant OBSERVED_AT = Instant.parse("2026-08-10T07:00:00Z");
    private static final String SOURCE_REFERENCE = "fixture://provider-a/search/milk.json";

    @Test
    void acceptsCompleteLocationSpecificOffer() {
        offer("provider-a", "fulfillment-context-1", "sku-1", new BigDecimal("149.90"), "RUB",
                AvailabilityStatus.AVAILABLE, OBSERVED_AT, SOURCE_REFERENCE);
    }

    @Test
    void rejectsOfferWithoutProvider() {
        assertInvalid("providerId", () -> offer(" ", "fulfillment-context-1", "sku-1", new BigDecimal("149.90"),
                "RUB", AvailabilityStatus.AVAILABLE, OBSERVED_AT, SOURCE_REFERENCE));
    }

    @Test
    void rejectsOfferWithoutFulfillmentContext() {
        assertInvalid("fulfillmentContextId", () -> offer("provider-a", " ", "sku-1", new BigDecimal("149.90"),
                "RUB", AvailabilityStatus.AVAILABLE, OBSERVED_AT, SOURCE_REFERENCE));
    }

    @Test
    void rejectsOfferWithoutSku() {
        assertInvalid("sku", () -> offer("provider-a", "fulfillment-context-1", " ", new BigDecimal("149.90"),
                "RUB", AvailabilityStatus.AVAILABLE, OBSERVED_AT, SOURCE_REFERENCE));
    }

    @Test
    void rejectsOfferWithoutPrice() {
        assertInvalid("price", () -> offer("provider-a", "fulfillment-context-1", "sku-1", null,
                "RUB", AvailabilityStatus.AVAILABLE, OBSERVED_AT, SOURCE_REFERENCE));
    }

    @Test
    void rejectsNegativePrice() {
        assertInvalid("price", () -> offer("provider-a", "fulfillment-context-1", "sku-1", new BigDecimal("-0.01"),
                "RUB", AvailabilityStatus.AVAILABLE, OBSERVED_AT, SOURCE_REFERENCE));
    }

    @Test
    void rejectsInvalidCurrencyCode() {
        assertInvalid("currencyCode", () -> offer("provider-a", "fulfillment-context-1", "sku-1",
                new BigDecimal("149.90"), "NOT-A-CURRENCY", AvailabilityStatus.AVAILABLE, OBSERVED_AT,
                SOURCE_REFERENCE));
    }

    @Test
    void rejectsOfferWithoutAvailability() {
        assertInvalid("availability", () -> offer("provider-a", "fulfillment-context-1", "sku-1",
                new BigDecimal("149.90"), "RUB", null, OBSERVED_AT, SOURCE_REFERENCE));
    }

    @Test
    void rejectsOfferWithoutObservationTime() {
        assertInvalid("observedAt", () -> offer("provider-a", "fulfillment-context-1", "sku-1",
                new BigDecimal("149.90"), "RUB", AvailabilityStatus.AVAILABLE, null, SOURCE_REFERENCE));
    }

    @Test
    void rejectsOfferWithoutSourceReference() {
        assertInvalid("sourceReference", () -> offer("provider-a", "fulfillment-context-1", "sku-1",
                new BigDecimal("149.90"), "RUB", AvailabilityStatus.AVAILABLE, OBSERVED_AT, ""));
    }

    private static ObservedOffer offer(
            String providerId,
            String fulfillmentContextId,
            String sku,
            BigDecimal price,
            String currencyCode,
            AvailabilityStatus availability,
            Instant observedAt,
            String sourceReference) {
        return new ObservedOffer(
                providerId,
                fulfillmentContextId,
                sku,
                price,
                currencyCode,
                availability,
                observedAt,
                sourceReference);
    }

    private static void assertInvalid(String field, Runnable construction) {
        assertThatThrownBy(construction::run)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(field);
    }
}
