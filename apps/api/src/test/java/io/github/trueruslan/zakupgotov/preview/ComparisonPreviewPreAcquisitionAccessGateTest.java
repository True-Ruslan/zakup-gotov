package io.github.trueruslan.zakupgotov.preview;

import static io.github.trueruslan.zakupgotov.comparison.RetailerComparisonReason.PRODUCTION_ACCESS_BLOCKED;
import static io.github.trueruslan.zakupgotov.comparison.RetailerComparisonStatus.UNAVAILABLE;
import static io.github.trueruslan.zakupgotov.comparison.RetailerCoverageStatus.CONNECTED;
import static io.github.trueruslan.zakupgotov.comparison.RetailerProductionAccessStatus.BLOCKED;
import static org.assertj.core.api.Assertions.assertThat;

import io.github.trueruslan.zakupgotov.retailer.RetailerRegistry;
import io.github.trueruslan.zakupgotov.shopping.QuantityUnit;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ComparisonPreviewPreAcquisitionAccessGateTest {

    @Test
    void emptyProductionReadyScopeDoesNotInvokeRuntimeEvidenceSource() {
        ComparisonRuntimeEvidenceSource source = (shoppingList, productLocation) -> {
            throw new AssertionError("runtime evidence source must not be invoked without production-ready retailers");
        };
        var service = new ComparisonPreviewService(RetailerRegistry.initial(), source);

        var preview = service.create(new ComparisonPreviewRequest(
                "Москва",
                List.of(new ComparisonPreviewItemRequest(
                        UUID.fromString("90909090-9090-9090-9090-909090909090"),
                        "Молоко",
                        BigDecimal.ONE,
                        QuantityUnit.LITER))));

        var magnit = preview.require("magnit");
        assertThat(magnit.coverage()).isEqualTo(CONNECTED);
        assertThat(magnit.productionAccess()).isEqualTo(BLOCKED);
        assertThat(magnit.comparisonStatus()).isEqualTo(UNAVAILABLE);
        assertThat(magnit.reasons()).containsExactly(PRODUCTION_ACCESS_BLOCKED);
        assertThat(magnit.total()).isEmpty();
        assertThat(magnit.freshness()).isEmpty();
    }
}
