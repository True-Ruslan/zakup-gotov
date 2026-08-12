package io.github.trueruslan.zakupgotov.provider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.trueruslan.zakupgotov.retailer.RetailerId;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class ObservedOfferProvenanceTest {

    private static final Instant OBSERVED_AT = Instant.parse("2026-08-12T06:15:00Z");

    @Test
    void preservesRetailerProviderAndAcquisitionModeAsIndependentProvenance() {
        var offer = offer(RetailerId.PYATEROCHKA, "kuper", AcquisitionMode.AGGREGATOR);

        assertThat(offer.retailerId()).isEqualTo(RetailerId.PYATEROCHKA);
        assertThat(offer.sourceProviderId()).isEqualTo("kuper");
        assertThat(offer.sourceMode()).isEqualTo(AcquisitionMode.AGGREGATOR);
    }

    @Test
    void keepsDirectPublicWebAndBrowserBridgeModesDistinct() {
        assertThat(offer(RetailerId.MAGNIT, "magnit-public-web", AcquisitionMode.PUBLIC_WEB).sourceMode())
                .isEqualTo(AcquisitionMode.PUBLIC_WEB);
        assertThat(offer(RetailerId.PEREKRESTOK, "perekrestok-browser", AcquisitionMode.BROWSER_BRIDGE).sourceMode())
                .isEqualTo(AcquisitionMode.BROWSER_BRIDGE);
        assertThat(offer(RetailerId.PYATEROCHKA, "x5-supported", AcquisitionMode.DIRECT_API).sourceMode())
                .isEqualTo(AcquisitionMode.DIRECT_API);
    }

    @Test
    void rejectsIncompleteProvenance() {
        assertThatThrownBy(() -> offer(null, "kuper", AcquisitionMode.AGGREGATOR))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("retailerId");
        assertThatThrownBy(() -> offer(RetailerId.PYATEROCHKA, " ", AcquisitionMode.AGGREGATOR))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("sourceProviderId");
        assertThatThrownBy(() -> offer(RetailerId.PYATEROCHKA, "kuper", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("sourceMode");
    }

    private static ObservedOffer offer(
            RetailerId retailerId,
            String sourceProviderId,
            AcquisitionMode sourceMode) {
        return new ObservedOffer(
                retailerId,
                sourceProviderId,
                sourceMode,
                "store-42",
                "sku-milk-1",
                "Молоко 3,2%",
                new BigDecimal("99.90"),
                "RUB",
                AvailabilityStatus.AVAILABLE,
                OBSERVED_AT,
                "fixture://provider/search/milk.json");
    }
}
