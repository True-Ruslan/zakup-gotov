package io.github.trueruslan.zakupgotov.provider;

import java.util.List;
import java.util.Objects;

final class ProviderProbeSupport {

    private ProviderProbeSupport() {}

    static List<ObservedOffer> search(
            RetailerProvider provider,
            LocationContext location,
            ProductQuery query) {
        Objects.requireNonNull(provider, "provider");
        Objects.requireNonNull(location, "location");
        Objects.requireNonNull(query, "query");

        if (!provider.providerId().equals(location.providerId())) {
            throw new IllegalArgumentException("providerId must match location context providerId");
        }
        var capabilities = Objects.requireNonNull(provider.capabilities(), "capabilities");
        requireCapability(capabilities, ProviderCapability.PRODUCT_SEARCH);
        requireCapability(capabilities, ProviderCapability.PRICE);

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

    private static void requireCapability(java.util.Set<ProviderCapability> capabilities, ProviderCapability capability) {
        if (!capabilities.contains(capability)) {
            throw new IllegalStateException("provider must declare " + capability + " capability");
        }
    }
}
