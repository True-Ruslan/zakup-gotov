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
    void offlineHarnessExposesFixtureOnlyProviderBoundary() throws NoSuchMethodException {
        var search = ProviderFeasibilityHarness.class.getMethod(
                "search", FixtureRetailerProvider.class, LocationContext.class, ProductQuery.class);

        assertThat(search.getParameterTypes()[0]).isEqualTo(FixtureRetailerProvider.class);
    }

    @Test
    void liveProviderRunsOnlyThroughExplicitLiveProbe() {
        var invoked = new AtomicBoolean(false);
        LiveRetailerProvider provider = new FakeLiveProvider(
                "provider-a",
                ProviderAccessType.PUBLIC_UNOFFICIAL_API,
                Set.of(ProviderCapability.PRODUCT_SEARCH, ProviderCapability.PRICE),
                invoked);

        var offers = ProviderLiveProbe.create().search(provider, LOCATION, QUERY);

        assertThat(invoked).isTrue();
        assertThat(offers).hasSize(1);
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
    void rejectsProviderWithoutPriceCapability() {
        var provider = fixtureProvider("provider-a", Set.of(ProviderCapability.PRODUCT_SEARCH));

        assertThatThrownBy(() -> ProviderFeasibilityHarness.offline().search(provider, LOCATION, QUERY))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("PRICE");
    }

    @Test
    void rejectsOfferThatDoesNotBelongToRequestedFulfillmentContext() {
        FixtureRetailerProvider provider = new FixtureRetailerProvider() {
            @Override
            public String providerId() {
                return "provider-a";
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
                return List.of(offer("provider-a", "store-99"));
            }
        };

        assertThatThrownBy(() -> ProviderFeasibilityHarness.offline().search(provider, LOCATION, QUERY))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("fulfillmentContextId");
    }

    private static FixtureRetailerProvider fixtureProvider(String providerId, Set<ProviderCapability> capabilities) {
        return new FakeFixtureProvider(providerId, ProviderAccessType.PUBLIC_UNOFFICIAL_API, capabilities);
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

    private record FakeFixtureProvider(
            String providerId,
            ProviderAccessType accessType,
            Set<ProviderCapability> capabilities) implements FixtureRetailerProvider {

        @Override
        public List<ObservedOffer> search(LocationContext location, ProductQuery query) {
            return List.of(offer(providerId, location.fulfillmentContextId()));
        }
    }

    private record FakeLiveProvider(
            String providerId,
            ProviderAccessType accessType,
            Set<ProviderCapability> capabilities,
            AtomicBoolean invoked) implements LiveRetailerProvider {

        @Override
        public List<ObservedOffer> search(LocationContext location, ProductQuery query) {
            invoked.set(true);
            return List.of(offer(providerId, location.fulfillmentContextId()));
        }
    }
}
