package io.github.trueruslan.zakupgotov.comparison;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.trueruslan.zakupgotov.retailer.ProductionAccessStatus;
import io.github.trueruslan.zakupgotov.retailer.Retailer;
import io.github.trueruslan.zakupgotov.retailer.RetailerCoverageState;
import io.github.trueruslan.zakupgotov.retailer.RetailerId;
import io.github.trueruslan.zakupgotov.retailer.RetailerRegistry;
import io.github.trueruslan.zakupgotov.retailer.RetailerRegistryEntry;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class RetailerComparisonReadModelAssemblerTest {

    private final RetailerComparisonReadModelAssembler assembler = new RetailerComparisonReadModelAssembler();

    @Test
    void exposesEveryCanonicalRetailerInStableOrderWithProductFacingNames() {
        var catalog = assembler.assemble(RetailerRegistry.initial(), Map.of());

        assertThat(catalog.retailers())
                .extracting(RetailerComparisonView::retailerId)
                .containsExactly(
                        RetailerId.PYATEROCHKA,
                        RetailerId.PEREKRESTOK,
                        RetailerId.CHIZHIK,
                        RetailerId.MAGNIT,
                        RetailerId.LENTA,
                        RetailerId.VKUSVILL,
                        RetailerId.OZON_FRESH,
                        RetailerId.SAMOKAT);
        assertThat(catalog.retailers())
                .extracting(RetailerComparisonView::displayName)
                .containsExactly(
                        "Пятёрочка",
                        "Перекрёсток",
                        "Чижик",
                        "Магнит",
                        "Лента",
                        "ВкусВилл",
                        "Ozon Fresh",
                        "Самокат");
        assertThatThrownBy(() -> catalog.retailers().clear())
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void keepsTechnicalCoverageAndProductionAccessIndependent() {
        var catalog = assembler.assemble(RetailerRegistry.initial(), Map.of());

        var pyaterochka = catalog.require(RetailerId.PYATEROCHKA);
        assertThat(pyaterochka.coverage()).isEqualTo(RetailerCoverageStatus.CONNECTED);
        assertThat(pyaterochka.productionAccess()).isEqualTo(RetailerProductionAccessStatus.PENDING);
        assertThat(pyaterochka.comparisonStatus()).isEqualTo(RetailerComparisonStatus.UNAVAILABLE);
        assertThat(pyaterochka.reasons())
                .containsExactly(RetailerComparisonReason.PRODUCTION_ACCESS_PENDING);
        assertThat(pyaterochka.total()).isEmpty();
        assertThat(pyaterochka.freshness()).isEmpty();

        var magnit = catalog.require(RetailerId.MAGNIT);
        assertThat(magnit.coverage()).isEqualTo(RetailerCoverageStatus.CONNECTED);
        assertThat(magnit.productionAccess()).isEqualTo(RetailerProductionAccessStatus.BLOCKED);
        assertThat(magnit.comparisonStatus()).isEqualTo(RetailerComparisonStatus.UNAVAILABLE);
        assertThat(magnit.reasons())
                .containsExactly(RetailerComparisonReason.PRODUCTION_ACCESS_BLOCKED);
        assertThat(magnit.total()).isEmpty();
        assertThat(magnit.freshness()).isEmpty();

        var chizhik = catalog.require(RetailerId.CHIZHIK);
        assertThat(chizhik.coverage()).isEqualTo(RetailerCoverageStatus.DISCOVERY);
        assertThat(chizhik.productionAccess()).isEqualTo(RetailerProductionAccessStatus.PENDING);
        assertThat(chizhik.comparisonStatus()).isEqualTo(RetailerComparisonStatus.UNAVAILABLE);
        assertThat(chizhik.reasons())
                .containsExactly(RetailerComparisonReason.COVERAGE_DISCOVERY);
    }

    @Test
    void coverageFailurePrecedesAccessAndRuntimeEvidence() {
        assertUnavailable(
                entry(RetailerCoverageState.REQUIRED_UNIMPLEMENTED, ProductionAccessStatus.BLOCKED),
                RetailerCoverageStatus.DISCOVERY,
                RetailerProductionAccessStatus.BLOCKED,
                RetailerComparisonReason.COVERAGE_DISCOVERY);
        assertUnavailable(
                entry(RetailerCoverageState.DEGRADED, ProductionAccessStatus.ACCEPTABLE),
                RetailerCoverageStatus.DEGRADED,
                RetailerProductionAccessStatus.READY,
                RetailerComparisonReason.COVERAGE_DEGRADED);
        assertUnavailable(
                entry(RetailerCoverageState.BLOCKED_EXTERNAL, ProductionAccessStatus.ACCEPTABLE),
                RetailerCoverageStatus.BLOCKED,
                RetailerProductionAccessStatus.READY,
                RetailerComparisonReason.COVERAGE_BLOCKED);
    }

    @Test
    void productionAccessFailurePrecedesMissingRuntimeData() {
        assertUnavailable(
                entry(RetailerCoverageState.AVAILABLE_DIRECT, ProductionAccessStatus.NOT_ASSESSED),
                RetailerCoverageStatus.CONNECTED,
                RetailerProductionAccessStatus.PENDING,
                RetailerComparisonReason.PRODUCTION_ACCESS_PENDING);
        assertUnavailable(
                entry(RetailerCoverageState.AVAILABLE_PUBLIC_WEB, ProductionAccessStatus.UNRESOLVED),
                RetailerCoverageStatus.CONNECTED,
                RetailerProductionAccessStatus.PENDING,
                RetailerComparisonReason.PRODUCTION_ACCESS_PENDING);
        assertUnavailable(
                entry(RetailerCoverageState.AVAILABLE_AGGREGATOR, ProductionAccessStatus.BLOCKED),
                RetailerCoverageStatus.CONNECTED,
                RetailerProductionAccessStatus.BLOCKED,
                RetailerComparisonReason.PRODUCTION_ACCESS_BLOCKED);
    }

    @Test
    void connectedProductionReadyRetailerWithoutRuntimeEvidenceIsExplicitlyUnavailable() {
        var catalog = assembler.assembleEntries(
                List.of(entry(RetailerCoverageState.AVAILABLE_BROWSER_BRIDGE, ProductionAccessStatus.ACCEPTABLE)),
                Map.of());

        var view = catalog.retailers().getFirst();
        assertThat(view.coverage()).isEqualTo(RetailerCoverageStatus.CONNECTED);
        assertThat(view.productionAccess()).isEqualTo(RetailerProductionAccessStatus.READY);
        assertThat(view.comparisonStatus()).isEqualTo(RetailerComparisonStatus.UNAVAILABLE);
        assertThat(view.reasons()).containsExactly(RetailerComparisonReason.DATA_NOT_AVAILABLE);
        assertThat(view.total()).isEmpty();
        assertThat(view.freshness()).isEmpty();
    }

    private void assertUnavailable(
            RetailerRegistryEntry entry,
            RetailerCoverageStatus expectedCoverage,
            RetailerProductionAccessStatus expectedAccess,
            RetailerComparisonReason expectedReason) {
        var catalog = assembler.assembleEntries(List.of(entry), Map.of());
        var view = catalog.retailers().getFirst();
        assertThat(view.coverage()).isEqualTo(expectedCoverage);
        assertThat(view.productionAccess()).isEqualTo(expectedAccess);
        assertThat(view.comparisonStatus()).isEqualTo(RetailerComparisonStatus.UNAVAILABLE);
        assertThat(view.reasons()).containsExactly(expectedReason);
        assertThat(view.total()).isEmpty();
        assertThat(view.freshness()).isEmpty();
    }

    private static RetailerRegistryEntry entry(
            RetailerCoverageState coverage,
            ProductionAccessStatus access) {
        return new RetailerRegistryEntry(new Retailer(RetailerId.PYATEROCHKA), coverage, access);
    }
}
