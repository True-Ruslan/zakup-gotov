package io.github.trueruslan.zakupgotov.provider;

import java.util.List;
import java.util.Objects;

public final class ProviderFeasibilityHarness {

    private final boolean allowLive;

    private ProviderFeasibilityHarness(boolean allowLive) {
        this.allowLive = allowLive;
    }

    public static ProviderFeasibilityHarness offline() {
        return new ProviderFeasibilityHarness(false);
    }

    public static ProviderFeasibilityHarness liveProbe() {
        return new ProviderFeasibilityHarness(true);
    }

    public List<ObservedOffer> search(RetailerProvider provider, LocationContext location, ProductQuery query) {
        Objects.requireNonNull(provider, "provider");
        Objects.requireNonNull(location, "location");
        Objects.requireNonNull(query, "query");

        if (!provider.providerId().equals(location.providerId())) {
            throw new IllegalArgumentException("providerId must match location context providerId");
        }
        if (provider.executionMode() == ProviderExecutionMode.LIVE && !allowLive) {
            throw new IllegalStateException("LIVE provider execution is forbidden by the offline feasibility harness");
        }
        if (!provider.capabilities().contains(ProviderCapability.PRODUCT_SEARCH)) {
            throw new IllegalStateException("provider must declare PRODUCT_SEARCH capability");
        }

        var offers = List.copyOf(provider.search(location, query));
        for (var offer : offers) {
            if (!provider.providerId().equals(offer.providerId())) {
                throw new IllegalStateException("offer providerId does not match provider");
            }
            if (!location.fulfillmentContextId().equals(offer.fulfillmentContextId())) {
                throw new IllegalStateException("offer fulfillmentContextId does not match requested location context");
            }
        }
        return offers;
    }
}
