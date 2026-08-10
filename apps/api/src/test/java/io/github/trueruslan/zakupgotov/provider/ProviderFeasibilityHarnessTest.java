package io.github.trueruslan.zakupgotov.provider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;

class ProviderFeasibilityHarnessTest {

    private static final Instant OBSERVED_AT = Instant.parse("2026-08-10T08:30:00Z");
    private static final LocationContext LOCATION = new LocationContext("provider-a", "store-42", "Москва");
    private static final ProductQuery QUERY = new ProductQuery("молоко");

    @Test
    void offlineHarnessAcceptsFixtureProviderAndReturnsValidatedOffers() {
        var provider = fixtureProvider("provider-a", Set.of(ProviderCapability.PRODUCT_SEARCH, ProviderCapability.PRICE));

        var offers = ProviderFeasibilityHarness.offline().search(provider, LOCATION, QUERY);

        assertThat(offers).hasSize(1);
        assertThat(offers.getFirst().providerId()).isEqualTo("provider-a");
        assertThat(offers.getFirst().fulfillmentContextId()).isEqualTo("store-42");
    }

    @Test
    void offlineHarnessRejectsLiveProviderBeforeNetworkCapableCodeRuns() {
        var invoked = new AtomicBoolean(false);
        RetailerProvider provider = new FakeProvider(
                "provider-a",
                ProviderAccessType.PUBLIC_UNOFFICIAL_API,
                ProviderExecutionMode.LIVE,
                Set.of(ProviderCapability.PRODUCT_SEARCH, ProviderCapability.PRICE),
                invoked);

        assertThatThrownBy(() -> ProviderFeasibilityHarness.offline().search(provider, LOCATION, QUERY))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("LIVE");
        assertThat(invoked).isFalse();
    }

    @Test
    void rejectsLocationContextOwnedByAnotherProvider() {
        var provider = fixtureProvider("provider-a", Set.of(ProviderCapability.PRODUCT_SEARCH, ProviderCapability.PRICE));
        var foreignLocation = new LocationContext("provider-b", "store-42", "Москва");

        assertThatThrownBy(() -> ProviderFeasibilityHarness.offline().search(provider, foreignLocation, QUERY))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("providerId");
    }

    @Test
    void rejectsProviderWithoutProductSearchCapability() {
        var provider = fixtureProvider("provider-a", Set.of(ProviderCapability.PRICE));

        assertThatThrownBy(() -> ProviderFeasibilityHarness.offline().search(provider, LOCATION, QUERY))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("PRODUCT_SEARCH");
    }

    @Test
    void rejectsOfferThatDoesNotBelongToRequestedFulfillmentContext() {
        RetailerProvider provider = new RetailerProvider() {
            @Override
            public String providerId() {
                return "provider-a";
            }

            @Override
            public ProviderAccessType accessType() {
                return ProviderAccessType.PUBLIC_UNOFFICIAL_API;
            }

            @Override
            public ProviderExecutionMode executionMode() {
                return ProviderExecutionMode.FIXTURE;
            }

            @Override
            public Set<ProviderCapability> capabilities() {
                return Set.of(ProviderCapability.PRODUCT_SEARCH, ProviderCapability.PRICE);
            }

            @Override
            public List<ObservedOffer> search(LocationContext location, ProductQuery query) {
                return List.of(offer("provider-a", "store-99"));
            }
        };

        assertThatThrownBy(() -> ProviderFeasibilityHarness.offline().search(provider, LOCATION, QUERY))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("fulfillmentContextId");
    }

    private static RetailerProvider fixtureProvider(String providerId, Set<ProviderCapability> capabilities) {
        return new FakeProvider(providerId, ProviderAccessType.PUBLIC_UNOFFICIAL_API, ProviderExecutionMode.FIXTURE,
                capabilities, new AtomicBoolean());
    }

    private static ObservedOffer offer(String providerId, String fulfillmentContextId) {
        return new ObservedOffer(
                providerId,
                fulfillmentContextId,
                "sku-milk-1",
                new BigDecimal("99.90"),
                "RUB",
                AvailabilityStatus.AVAILABLE,
                OBSERVED_AT,
                "fixture://provider-a/search/milk.json");
    }

    private record FakeProvider(
            String providerId,
            ProviderAccessType accessType,
            ProviderExecutionMode executionMode,
            Set<ProviderCapability> capabilities,
            AtomicBoolean invoked) implements RetailerProvider {

        @Override
        public List<ObservedOffer> search(LocationContext location, ProductQuery query) {
            invoked.set(true);
            return List.of(offer(providerId, location.fulfillmentContextId()));
        }
    }
}
