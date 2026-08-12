package io.github.trueruslan.zakupgotov.provider;

import io.github.trueruslan.zakupgotov.retailer.RetailerId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class ProviderPathOrchestrator {

    private ProviderPathOrchestrator() {}

    public static ProviderPathOrchestrator offline() {
        return new ProviderPathOrchestrator();
    }

    public ProviderSearchOutcome search(
            RetailerId retailerId,
            List<FixtureRetailerProvider> providers,
            Map<String, LocationContext> contextsBySourceProvider,
            ProductQuery query) {
        Objects.requireNonNull(retailerId, "retailerId must not be null");
        Objects.requireNonNull(providers, "providers must not be null");
        Objects.requireNonNull(contextsBySourceProvider, "contextsBySourceProvider must not be null");
        Objects.requireNonNull(query, "query must not be null");

        var candidates = providers.stream()
                .filter(Objects::nonNull)
                .filter(provider -> retailerId.equals(provider.retailerId()))
                .sorted(Comparator
                        .comparingInt((FixtureRetailerProvider provider) -> priority(provider.acquisitionMode()))
                        .thenComparing(FixtureRetailerProvider::sourceProviderId))
                .toList();
        var attempts = new ArrayList<ProviderPathAttempt>();

        for (var provider : candidates) {
            var capabilities = Objects.requireNonNull(provider.capabilities(), "provider capabilities must not be null");
            if (!capabilities.contains(ProviderCapability.PRODUCT_SEARCH)
                    || !capabilities.contains(ProviderCapability.PRICE)) {
                attempts.add(attempt(provider, ProviderPathAttemptStatus.INELIGIBLE_CAPABILITIES));
                continue;
            }

            var location = contextsBySourceProvider.get(provider.sourceProviderId());
            if (location == null) {
                attempts.add(attempt(provider, ProviderPathAttemptStatus.MISSING_CONTEXT));
                continue;
            }

            try {
                var offers = ProviderFeasibilityHarness.offline().search(provider, location, query);
                attempts.add(attempt(provider, ProviderPathAttemptStatus.SUCCESS));
                return ProviderSearchOutcome.success(
                        retailerId,
                        new ProviderPathSelection(provider.sourceProviderId(), provider.acquisitionMode()),
                        offers,
                        attempts);
            } catch (ProviderPathUnavailableException exception) {
                attempts.add(attempt(provider, ProviderPathAttemptStatus.FAILED));
            }
        }

        return ProviderSearchOutcome.unavailable(retailerId, attempts);
    }

    private static ProviderPathAttempt attempt(
            FixtureRetailerProvider provider,
            ProviderPathAttemptStatus status) {
        return new ProviderPathAttempt(provider.sourceProviderId(), provider.acquisitionMode(), status);
    }

    private static int priority(AcquisitionMode mode) {
        return switch (Objects.requireNonNull(mode, "acquisitionMode must not be null")) {
            case DIRECT_API -> 0;
            case AGGREGATOR -> 1;
            case PUBLIC_WEB -> 2;
            case BROWSER_BRIDGE -> 3;
        };
    }
}
