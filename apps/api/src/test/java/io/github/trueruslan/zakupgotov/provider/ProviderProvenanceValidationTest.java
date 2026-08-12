package io.github.trueruslan.zakupgotov.provider;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.trueruslan.zakupgotov.retailer.RetailerId;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class ProviderProvenanceValidationTest {

    private static final LocationContext LOCATION = new LocationContext("provider-a", "store-42", "Москва");
    private static final ProductQuery QUERY = new ProductQuery("молоко");
    private static final Instant OBSERVED_AT = Instant.parse("2026-08-12T06:45:00Z");

    @Test
    void rejectsOfferForDifferentRetailerThanProviderDeclares() {
        var provider = provider(RetailerId.PYATEROCHKA, AcquisitionMode.DIRECT_API,
                offer(RetailerId.PEREKRESTOK, AcquisitionMode.DIRECT_API));

        assertThatThrownBy(() -> ProviderFeasibilityHarness.offline().search(provider, LOCATION, QUERY))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("retailerId");
    }

    @Test
    void rejectsOfferWithDifferentAcquisitionModeThanProviderDeclares() {
        var provider = provider(RetailerId.PYATEROCHKA, AcquisitionMode.DIRECT_API,
                offer(RetailerId.PYATEROCHKA, AcquisitionMode.AGGREGATOR));

        assertThatThrownBy(() -> ProviderFeasibilityHarness.offline().search(provider, LOCATION, QUERY))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sourceMode");
    }

    private static FixtureRetailerProvider provider(
            RetailerId retailerId,
            AcquisitionMode mode,
            ObservedOffer returnedOffer) {
        return new FixtureRetailerProvider() {
            @Override
            public RetailerId retailerId() {
                return retailerId;
            }

            @Override
            public String sourceProviderId() {
                return "provider-a";
            }

            @Override
            public AcquisitionMode acquisitionMode() {
                return mode;
            }

            @Override
            public ProviderAccessType accessType() {
                return ProviderAccessType.PUBLIC_UNOFFICIAL_API;
            }

            @Override
            public Set<ProviderCapability> capabilities() {
                return Set.of(ProviderCapability.PRODUCT_SEARCH, ProviderCapability.PRICE);
            }

            @Override
            public List<ObservedOffer> search(LocationContext location, ProductQuery query) {
                return List.of(returnedOffer);
            }
        };
    }

    private static ObservedOffer offer(RetailerId retailerId, AcquisitionMode mode) {
        return new ObservedOffer(
                retailerId,
                "provider-a",
                mode,
                "store-42",
                "sku-milk-1",
                new BigDecimal("99.90"),
                "RUB",
                AvailabilityStatus.AVAILABLE,
                OBSERVED_AT,
                "fixture://provider-a/search/milk.json");
    }
}
