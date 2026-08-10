package io.github.trueruslan.zakupgotov.provider;

import java.util.List;

public final class ProviderFeasibilityHarness {

    private ProviderFeasibilityHarness() {}

    public static ProviderFeasibilityHarness offline() {
        return new ProviderFeasibilityHarness();
    }

    public List<ObservedOffer> search(
            FixtureRetailerProvider provider,
            LocationContext location,
            ProductQuery query) {
        return ProviderProbeSupport.search(provider, location, query);
    }
}
