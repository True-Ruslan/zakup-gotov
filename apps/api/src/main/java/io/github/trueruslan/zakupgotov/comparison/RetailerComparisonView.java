package io.github.trueruslan.zakupgotov.comparison;

import io.github.trueruslan.zakupgotov.basket.BasketTotal;
import io.github.trueruslan.zakupgotov.retailer.RetailerId;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public record RetailerComparisonView(
        RetailerId retailerId,
        String displayName,
        RetailerCoverageStatus coverage,
        RetailerProductionAccessStatus productionAccess,
        RetailerComparisonStatus comparisonStatus,
        List<RetailerComparisonReason> reasons,
        Optional<BasketTotal> total,
        Optional<RetailerFreshness> freshness) {

    public RetailerComparisonView {
        retailerId = Objects.requireNonNull(retailerId, "retailerId must not be null");
        if (displayName == null || displayName.isBlank()) {
            throw new IllegalArgumentException("displayName must not be blank");
        }
        coverage = Objects.requireNonNull(coverage, "coverage must not be null");
        productionAccess = Objects.requireNonNull(productionAccess, "productionAccess must not be null");
        comparisonStatus = Objects.requireNonNull(comparisonStatus, "comparisonStatus must not be null");
        reasons = List.copyOf(Objects.requireNonNull(reasons, "reasons must not be null"));
        total = Objects.requireNonNull(total, "total must not be null");
        freshness = Objects.requireNonNull(freshness, "freshness must not be null");

        if (comparisonStatus != RetailerComparisonStatus.UNAVAILABLE
                && coverage != RetailerCoverageStatus.CONNECTED) {
            throw new IllegalArgumentException(
                    comparisonStatus + " comparison requires CONNECTED retailer coverage");
        }
        if (comparisonStatus != RetailerComparisonStatus.UNAVAILABLE
                && productionAccess != RetailerProductionAccessStatus.READY) {
            throw new IllegalArgumentException(
                    comparisonStatus + " comparison requires READY production access");
        }

        switch (comparisonStatus) {
            case READY -> {
                if (!reasons.isEmpty() || total.isEmpty() || freshness.isEmpty()) {
                    throw new IllegalArgumentException(
                            "READY comparison requires no reasons plus total and freshness");
                }
            }
            case UNCERTAIN -> {
                if (reasons.isEmpty() || total.isEmpty() || freshness.isEmpty()) {
                    throw new IllegalArgumentException(
                            "UNCERTAIN comparison requires reasons, total and freshness");
                }
            }
            case INCOMPLETE -> {
                if (reasons.isEmpty() || total.isPresent() || freshness.isPresent()) {
                    throw new IllegalArgumentException(
                            "INCOMPLETE comparison requires reasons and no aggregate total or freshness");
                }
            }
            case UNAVAILABLE -> {
                if (reasons.isEmpty() || total.isPresent() || freshness.isPresent()) {
                    throw new IllegalArgumentException(
                            "UNAVAILABLE comparison requires reasons and no aggregate total or freshness");
                }
            }
        }
    }
}
