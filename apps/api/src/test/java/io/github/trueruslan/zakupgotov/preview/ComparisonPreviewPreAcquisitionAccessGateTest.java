package io.github.trueruslan.zakupgotov.preview;

import static io.github.trueruslan.zakupgotov.comparison.RetailerComparisonReason.PRODUCTION_ACCESS_BLOCKED;
import static io.github.trueruslan.zakupgotov.comparison.RetailerComparisonStatus.UNAVAILABLE;
import static io.github.trueruslan.zakupgotov.comparison.RetailerCoverageStatus.CONNECTED;
import static io.github.trueruslan.zakupgotov.comparison.RetailerProductionAccessStatus.BLOCKED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.trueruslan.zakupgotov.retailer.ProductionAccessStatus;
import io.github.trueruslan.zakupgotov.retailer.Retailer;
import io.github.trueruslan.zakupgotov.retailer.RetailerCoverageState;
import io.github.trueruslan.zakupgotov.retailer.RetailerId;
import io.github.trueruslan.zakupgotov.retailer.RetailerRegistry;
import io.github.trueruslan.zakupgotov.retailer.RetailerRegistryEntry;
import io.github.trueruslan.zakupgotov.shopping.QuantityUnit;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class ComparisonPreviewPreAcquisitionAccessGateTest {

    @Test
    void emptyProductionReadyScopeDoesNotInvokeRuntimeEvidenceSource() {
        ComparisonRuntimeEvidenceSource source = (shoppingList, productLocation, requestedRetailers) -> {
            throw new AssertionError("runtime evidence source must not be invoked without production-ready retailers");
        };
        var service = new ComparisonPreviewService(RetailerRegistry.initial(), source);

        var preview = service.create(request());

        var magnit = preview.require("magnit");
        assertThat(magnit.coverage()).isEqualTo(CONNECTED);
        assertThat(magnit.productionAccess()).isEqualTo(BLOCKED);
        assertThat(magnit.comparisonStatus()).isEqualTo(UNAVAILABLE);
        assertThat(magnit.reasons()).containsExactly(PRODUCTION_ACCESS_BLOCKED);
        assertThat(magnit.total()).isEmpty();
        assertThat(magnit.freshness()).isEmpty();
    }

    @Test
    void evidenceSourceReceivesExactlyTheImmutableProductionReadyScope() {
        var receivedScope = new AtomicReference<Set<RetailerId>>();
        ComparisonRuntimeEvidenceSource source = (shoppingList, productLocation, requestedRetailers) -> {
            receivedScope.set(requestedRetailers);
            return ComparisonRuntimeEvidence.empty();
        };
        var service = new ComparisonPreviewService(
                registryWithProductionReady(Set.of(RetailerId.PYATEROCHKA, RetailerId.PEREKRESTOK)),
                source);

        service.create(request());

        assertThat(receivedScope.get())
                .containsExactlyInAnyOrder(RetailerId.PYATEROCHKA, RetailerId.PEREKRESTOK)
                .doesNotContain(RetailerId.MAGNIT);
        assertThatThrownBy(() -> receivedScope.get().add(RetailerId.MAGNIT))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    private static ComparisonPreviewRequest request() {
        return new ComparisonPreviewRequest(
                "Москва",
                List.of(new ComparisonPreviewItemRequest(
                        UUID.fromString("90909090-9090-9090-9090-909090909090"),
                        "Молоко",
                        BigDecimal.ONE,
                        QuantityUnit.LITER)));
    }

    private static RetailerRegistry registryWithProductionReady(Set<RetailerId> readyRetailers) {
        return RetailerRegistry.of(Arrays.stream(RetailerId.values())
                .map(id -> new RetailerRegistryEntry(
                        new Retailer(id),
                        readyRetailers.contains(id)
                                ? RetailerCoverageState.AVAILABLE_DIRECT
                                : RetailerCoverageState.DISCOVERY,
                        readyRetailers.contains(id)
                                ? ProductionAccessStatus.ACCEPTABLE
                                : ProductionAccessStatus.NOT_ASSESSED))
                .toList());
    }
}
