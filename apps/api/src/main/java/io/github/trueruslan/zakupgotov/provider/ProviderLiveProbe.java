package io.github.trueruslan.zakupgotov.provider;

import java.util.List;

/** Explicit opt-in entry point for provider checks that may use a live external service. */
public final class ProviderLiveProbe {

    private ProviderLiveProbe() {}

    public static ProviderLiveProbe create() {
        return new ProviderLiveProbe();
    }

    public List<ObservedOffer> search(
            LiveRetailerProvider provider,
            LocationContext location,
            ProductQuery query) {
        return ProviderProbeSupport.search(provider, location, query);
    }
}
