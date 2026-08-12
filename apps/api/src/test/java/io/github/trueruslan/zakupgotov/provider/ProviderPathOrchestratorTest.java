package io.github.trueruslan.zakupgotov.provider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.trueruslan.zakupgotov.retailer.RetailerId;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;

class ProviderPathOrchestratorTest {

    private static final Instant OBSERVED_AT = Instant.parse("2026-08-12T06:30:00Z");
    private static final ProductQuery QUERY = new ProductQuery("молоко");

    @Test
    void selectsHighestPriorityEligiblePathForRequestedRetailer() {
        var direct = provider("x5-supported", AcquisitionMode.DIRECT_API, Behavior.SUCCESS);
        var aggregator = provider("kuper", AcquisitionMode.AGGREGATOR, Behavior.SUCCESS);
        var publicWeb = provider("public-web", AcquisitionMode.PUBLIC_WEB, Behavior.SUCCESS);
        var browser = provider("pyaterochka-browser", AcquisitionMode.BROWSER_BRIDGE, Behavior.SUCCESS);

        var outcome = ProviderPathOrchestrator.offline().search(
                RetailerId.PYATEROCHKA,
                List.of(browser, publicWeb, aggregator, direct),
                contexts(direct, aggregator, publicWeb, browser),
                QUERY);

        assertThat(outcome.succeeded()).isTrue();
        assertThat(outcome.selectedPath()).contains(new ProviderPathSelection("x5-supported", AcquisitionMode.DIRECT_API));
        assertThat(outcome.offers()).hasSize(1);
        assertThat(outcome.offers().getFirst().retailerId()).isEqualTo(RetailerId.PYATEROCHKA);
        assertThat(outcome.attempts())
                .containsExactly(new ProviderPathAttempt(
                        "x5-supported", AcquisitionMode.DIRECT_API, ProviderPathAttemptStatus.SUCCESS));
    }

    @Test
    void recordsExpectedFailureAndFallsBackToNextEligiblePath() {
        var direct = provider("x5-supported", AcquisitionMode.DIRECT_API, Behavior.UNAVAILABLE);
        var aggregator = provider("kuper", AcquisitionMode.AGGREGATOR, Behavior.SUCCESS);

        var outcome = ProviderPathOrchestrator.offline().search(
                RetailerId.PYATEROCHKA,
                List.of(aggregator, direct),
                contexts(direct, aggregator),
                QUERY);

        assertThat(outcome.selectedPath()).contains(new ProviderPathSelection("kuper", AcquisitionMode.AGGREGATOR));
        assertThat(outcome.attempts()).containsExactly(
                new ProviderPathAttempt("x5-supported", AcquisitionMode.DIRECT_API, ProviderPathAttemptStatus.FAILED),
                new ProviderPathAttempt("kuper", AcquisitionMode.AGGREGATOR, ProviderPathAttemptStatus.SUCCESS));
        assertThat(outcome.offers().getFirst().sourceProviderId()).isEqualTo("kuper");
        assertThat(outcome.offers().getFirst().sourceMode()).isEqualTo(AcquisitionMode.AGGREGATOR);
    }

    @Test
    void skipsPathWithoutRequiredCapabilitiesAndKeepsReasonExplicit() {
        var direct = provider(
                "x5-supported",
                AcquisitionMode.DIRECT_API,
                Behavior.SUCCESS,
                Set.of(ProviderCapability.PRODUCT_SEARCH));
        var aggregator = provider("kuper", AcquisitionMode.AGGREGATOR, Behavior.SUCCESS);

        var outcome = ProviderPathOrchestrator.offline().search(
                RetailerId.PYATEROCHKA,
                List.of(direct, aggregator),
                contexts(direct, aggregator),
                QUERY);

        assertThat(outcome.attempts()).containsExactly(
                new ProviderPathAttempt(
                        "x5-supported",
                        AcquisitionMode.DIRECT_API,
                        ProviderPathAttemptStatus.INELIGIBLE_CAPABILITIES),
                new ProviderPathAttempt("kuper", AcquisitionMode.AGGREGATOR, ProviderPathAttemptStatus.SUCCESS));
    }

    @Test
    void skipsPathWithoutProviderScopedContextAndKeepsReasonExplicit() {
        var direct = provider("x5-supported", AcquisitionMode.DIRECT_API, Behavior.SUCCESS);
        var aggregator = provider("kuper", AcquisitionMode.AGGREGATOR, Behavior.SUCCESS);

        var outcome = ProviderPathOrchestrator.offline().search(
                RetailerId.PYATEROCHKA,
                List.of(direct, aggregator),
                Map.of("kuper", context(aggregator)),
                QUERY);

        assertThat(outcome.attempts()).containsExactly(
                new ProviderPathAttempt(
                        "x5-supported", AcquisitionMode.DIRECT_API, ProviderPathAttemptStatus.MISSING_CONTEXT),
                new ProviderPathAttempt("kuper", AcquisitionMode.AGGREGATOR, ProviderPathAttemptStatus.SUCCESS));
    }

    @Test
    void successfulEmptyResultDoesNotFallBackToAnotherProvider() {
        var aggregatorInvoked = new AtomicBoolean(false);
        var direct = provider("x5-supported", AcquisitionMode.DIRECT_API, Behavior.EMPTY);
        var aggregator = provider("kuper", AcquisitionMode.AGGREGATOR, Behavior.SUCCESS, defaultCapabilities(), aggregatorInvoked);

        var outcome = ProviderPathOrchestrator.offline().search(
                RetailerId.PYATEROCHKA,
                List.of(aggregator, direct),
                contexts(direct, aggregator),
                QUERY);

        assertThat(outcome.succeeded()).isTrue();
        assertThat(outcome.selectedPath()).contains(new ProviderPathSelection("x5-supported", AcquisitionMode.DIRECT_API));
        assertThat(outcome.offers()).isEmpty();
        assertThat(aggregatorInvoked).isFalse();
    }

    @Test
    void unexpectedProviderDefectPropagatesInsteadOfBeingHiddenByFallback() {
        var direct = provider("x5-supported", AcquisitionMode.DIRECT_API, Behavior.DEFECT);
        var aggregator = provider("kuper", AcquisitionMode.AGGREGATOR, Behavior.SUCCESS);

        assertThatThrownBy(() -> ProviderPathOrchestrator.offline().search(
                        RetailerId.PYATEROCHKA,
                        List.of(direct, aggregator),
                        contexts(direct, aggregator),
                        QUERY))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("fixture defect");
    }

    @Test
    void ignoresProvidersForOtherRetailers() {
        var perekrestok = provider(
                RetailerId.PEREKRESTOK,
                "perekrestok-browser",
                AcquisitionMode.BROWSER_BRIDGE,
                Behavior.SUCCESS,
                defaultCapabilities(),
                new AtomicBoolean());

        var outcome = ProviderPathOrchestrator.offline().search(
                RetailerId.PYATEROCHKA,
                List.of(perekrestok),
                contexts(perekrestok),
                QUERY);

        assertThat(outcome.succeeded()).isFalse();
        assertThat(outcome.selectedPath()).isEmpty();
        assertThat(outcome.offers()).isEmpty();
        assertThat(outcome.attempts()).isEmpty();
    }

    private static FakeFixtureProvider provider(String sourceProviderId, AcquisitionMode mode, Behavior behavior) {
        return provider(sourceProviderId, mode, behavior, defaultCapabilities());
    }

    private static FakeFixtureProvider provider(
            String sourceProviderId,
            AcquisitionMode mode,
            Behavior behavior,
            Set<ProviderCapability> capabilities) {
        return provider(
                RetailerId.PYATEROCHKA,
                sourceProviderId,
                mode,
                behavior,
                capabilities,
                new AtomicBoolean());
    }

    private static FakeFixtureProvider provider(
            String sourceProviderId,
            AcquisitionMode mode,
            Behavior behavior,
            Set<ProviderCapability> capabilities,
            AtomicBoolean invoked) {
        return provider(RetailerId.PYATEROCHKA, sourceProviderId, mode, behavior, capabilities, invoked);
    }

    private static FakeFixtureProvider provider(
            RetailerId retailerId,
            String sourceProviderId,
            AcquisitionMode mode,
            Behavior behavior,
            Set<ProviderCapability> capabilities,
            AtomicBoolean invoked) {
        return new FakeFixtureProvider(
                retailerId,
                sourceProviderId,
                mode,
                ProviderAccessType.PUBLIC_UNOFFICIAL_API,
                capabilities,
                behavior,
                invoked);
    }

    private static Set<ProviderCapability> defaultCapabilities() {
        return Set.of(ProviderCapability.PRODUCT_SEARCH, ProviderCapability.PRICE);
    }

    private static Map<String, LocationContext> contexts(FakeFixtureProvider... providers) {
        var builder = new java.util.LinkedHashMap<String, LocationContext>();
        for (var provider : providers) {
            builder.put(provider.sourceProviderId(), context(provider));
        }
        return Map.copyOf(builder);
    }

    private static LocationContext context(FakeFixtureProvider provider) {
        return new LocationContext(provider.sourceProviderId(), "store-42", "Москва");
    }

    private enum Behavior {
        SUCCESS,
        EMPTY,
        UNAVAILABLE,
        DEFECT
    }

    private record FakeFixtureProvider(
            RetailerId retailerId,
            String sourceProviderId,
            AcquisitionMode acquisitionMode,
            ProviderAccessType accessType,
            Set<ProviderCapability> capabilities,
            Behavior behavior,
            AtomicBoolean invoked) implements FixtureRetailerProvider {

        @Override
        public List<ObservedOffer> search(LocationContext location, ProductQuery query) {
            invoked.set(true);
            return switch (behavior) {
                case SUCCESS -> List.of(new ObservedOffer(
                        retailerId,
                        sourceProviderId,
                        acquisitionMode,
                        location.fulfillmentContextId(),
                        "sku-milk-1",
                        new BigDecimal("99.90"),
                        "RUB",
                        AvailabilityStatus.AVAILABLE,
                        OBSERVED_AT,
                        "fixture://" + sourceProviderId + "/search/milk.json"));
                case EMPTY -> List.of();
                case UNAVAILABLE -> throw new ProviderPathUnavailableException("fixture unavailable");
                case DEFECT -> throw new IllegalStateException("fixture defect");
            };
        }
    }
}
