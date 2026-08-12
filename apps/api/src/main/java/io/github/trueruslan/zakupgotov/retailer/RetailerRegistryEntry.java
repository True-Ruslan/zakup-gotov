package io.github.trueruslan.zakupgotov.retailer;

import java.util.Objects;

public record RetailerRegistryEntry(
        Retailer retailer,
        RetailerCoverageState coverageState,
        ProductionAccessStatus productionAccessStatus) {

    public RetailerRegistryEntry {
        retailer = Objects.requireNonNull(retailer, "retailer must not be null");
        coverageState = Objects.requireNonNull(coverageState, "coverageState must not be null");
        productionAccessStatus = Objects.requireNonNull(productionAccessStatus, "productionAccessStatus must not be null");
    }

    public boolean isProductionReady() {
        return coverageState.isTechnicallyAvailable() && productionAccessStatus.isProductionReady();
    }
}
