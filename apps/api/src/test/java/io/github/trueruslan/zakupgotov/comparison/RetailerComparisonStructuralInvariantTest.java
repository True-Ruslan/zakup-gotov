package io.github.trueruslan.zakupgotov.comparison;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.trueruslan.zakupgotov.basket.BasketTotal;
import io.github.trueruslan.zakupgotov.retailer.RetailerId;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class RetailerComparisonStructuralInvariantTest {

    private static final BasketTotal TOTAL = new BasketTotal(new BigDecimal("123.45"), "RUB");
    private static final Instant OBSERVED_AT = Instant.parse("2026-08-12T10:00:00Z");
    private static final Instant PROVIDER_UPDATED_AT = Instant.parse("2026-08-12T09:59:00Z");
    private static final RetailerFreshness OBSERVATION_FRESHNESS = new RetailerFreshness(
            RetailerFreshnessBasis.OBSERVATION_ONLY,
            OBSERVED_AT,
            Optional.empty());

    @Test
    void rejectsReadyViewWithoutTotalAndFreshness() {
        assertThatThrownBy(() -> view(
                        RetailerCoverageStatus.CONNECTED,
                        RetailerProductionAccessStatus.READY,
                        RetailerComparisonStatus.READY,
                        List.of(),
                        Optional.empty(),
                        Optional.empty()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("READY");
    }

    @Test
    void rejectsUncertainViewWithoutReasonTotalOrFreshness() {
        assertThatThrownBy(() -> view(
                        RetailerCoverageStatus.CONNECTED,
                        RetailerProductionAccessStatus.READY,
                        RetailerComparisonStatus.UNCERTAIN,
                        List.of(),
                        Optional.empty(),
                        Optional.empty()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("UNCERTAIN");
    }

    @Test
    void rejectsIncompleteViewWithAggregateTotalOrFreshness() {
        assertThatThrownBy(() -> view(
                        RetailerCoverageStatus.CONNECTED,
                        RetailerProductionAccessStatus.READY,
                        RetailerComparisonStatus.INCOMPLETE,
                        List.of(RetailerComparisonReason.ITEM_UNMATCHED),
                        Optional.of(TOTAL),
                        Optional.of(OBSERVATION_FRESHNESS)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("INCOMPLETE");
    }

    @Test
    void rejectsUnavailableViewWithAggregateTotalOrFreshness() {
        assertThatThrownBy(() -> view(
                        RetailerCoverageStatus.CONNECTED,
                        RetailerProductionAccessStatus.PENDING,
                        RetailerComparisonStatus.UNAVAILABLE,
                        List.of(RetailerComparisonReason.PRODUCTION_ACCESS_PENDING),
                        Optional.of(TOTAL),
                        Optional.of(OBSERVATION_FRESHNESS)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("UNAVAILABLE");
    }

    @Test
    void rejectsRuntimeComparisonWhenCoverageOrProductionAccessIsNotReady() {
        assertThatThrownBy(() -> view(
                        RetailerCoverageStatus.BLOCKED,
                        RetailerProductionAccessStatus.READY,
                        RetailerComparisonStatus.INCOMPLETE,
                        List.of(RetailerComparisonReason.ITEM_UNMATCHED),
                        Optional.empty(),
                        Optional.empty()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("CONNECTED");

        assertThatThrownBy(() -> view(
                        RetailerCoverageStatus.CONNECTED,
                        RetailerProductionAccessStatus.BLOCKED,
                        RetailerComparisonStatus.INCOMPLETE,
                        List.of(RetailerComparisonReason.ITEM_UNMATCHED),
                        Optional.empty(),
                        Optional.empty()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("READY");
    }

    @Test
    void rejectsObservationOnlyFreshnessWithProviderTimestamp() {
        assertThatThrownBy(() -> new RetailerFreshness(
                        RetailerFreshnessBasis.OBSERVATION_ONLY,
                        OBSERVED_AT,
                        Optional.of(PROVIDER_UPDATED_AT)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("OBSERVATION_ONLY");
    }

    @Test
    void rejectsProviderTimestampFreshnessWithoutProviderTimestamp() {
        assertThatThrownBy(() -> new RetailerFreshness(
                        RetailerFreshnessBasis.PROVIDER_TIMESTAMP,
                        OBSERVED_AT,
                        Optional.empty()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("PROVIDER_TIMESTAMP");
    }

    @Test
    void rejectsProviderTimestampAfterObservationTime() {
        assertThatThrownBy(() -> new RetailerFreshness(
                        RetailerFreshnessBasis.PROVIDER_TIMESTAMP,
                        OBSERVED_AT,
                        Optional.of(OBSERVED_AT.plusSeconds(1))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("after observedAt");
    }

    private static RetailerComparisonView view(
            RetailerCoverageStatus coverage,
            RetailerProductionAccessStatus productionAccess,
            RetailerComparisonStatus status,
            List<RetailerComparisonReason> reasons,
            Optional<BasketTotal> total,
            Optional<RetailerFreshness> freshness) {
        return new RetailerComparisonView(
                RetailerId.PYATEROCHKA,
                "Пятёрочка",
                coverage,
                productionAccess,
                status,
                reasons,
                total,
                freshness);
    }
}
