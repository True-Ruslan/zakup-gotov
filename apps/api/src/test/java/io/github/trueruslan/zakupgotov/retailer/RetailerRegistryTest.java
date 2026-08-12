package io.github.trueruslan.zakupgotov.retailer;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class RetailerRegistryTest {

    private final RetailerRegistry registry = RetailerRegistry.initial();

    @Test
    void exposesEveryInitialRetailerBannerInStableProductOrder() {
        assertThat(registry.entries())
                .extracting(entry -> entry.retailer().id())
                .containsExactly(
                        RetailerId.PYATEROCHKA,
                        RetailerId.PEREKRESTOK,
                        RetailerId.CHIZHIK,
                        RetailerId.MAGNIT,
                        RetailerId.LENTA,
                        RetailerId.VKUSVILL,
                        RetailerId.OZON_FRESH,
                        RetailerId.SAMOKAT);
    }

    @Test
    void preservesAcceptedTechnicalConnectivityWithoutClaimingProductionClearance() {
        assertCoverage(
                RetailerId.PYATEROCHKA,
                RetailerCoverageState.AVAILABLE_BROWSER_BRIDGE,
                ProductionAccessStatus.NOT_ASSESSED);
        assertCoverage(
                RetailerId.PEREKRESTOK,
                RetailerCoverageState.AVAILABLE_BROWSER_BRIDGE,
                ProductionAccessStatus.NOT_ASSESSED);
        assertCoverage(
                RetailerId.MAGNIT,
                RetailerCoverageState.AVAILABLE_PUBLIC_WEB,
                ProductionAccessStatus.BLOCKED);
    }

    @Test
    void keepsMandatoryUnimplementedRetailersVisibleAsDiscoveryWork() {
        assertThat(List.of(
                        RetailerId.CHIZHIK,
                        RetailerId.LENTA,
                        RetailerId.VKUSVILL,
                        RetailerId.OZON_FRESH,
                        RetailerId.SAMOKAT))
                .allSatisfy(retailerId -> assertCoverage(
                        retailerId,
                        RetailerCoverageState.DISCOVERY,
                        ProductionAccessStatus.NOT_ASSESSED));
    }

    @Test
    void doesNotConflateAggregatorProviderWithRetailerIdentity() {
        assertThat(registry.entries())
                .extracting(entry -> entry.retailer().canonicalId())
                .doesNotContain("kuper");
    }

    @Test
    void keepsMagnitTechnicallyConnectedButNotProductionReady() {
        var magnit = registry.require(RetailerId.MAGNIT);

        assertThat(magnit.coverageState().isTechnicallyAvailable()).isTrue();
        assertThat(magnit.productionAccessStatus()).isEqualTo(ProductionAccessStatus.BLOCKED);
        assertThat(magnit.isProductionReady()).isFalse();
    }

    private void assertCoverage(
            RetailerId retailerId,
            RetailerCoverageState expectedCoverage,
            ProductionAccessStatus expectedProductionAccess) {
        var entry = registry.require(retailerId);
        assertThat(entry.coverageState()).isEqualTo(expectedCoverage);
        assertThat(entry.productionAccessStatus()).isEqualTo(expectedProductionAccess);
    }
}
