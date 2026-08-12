package io.github.trueruslan.zakupgotov.comparison;

import io.github.trueruslan.zakupgotov.basket.BasketTotal;
import io.github.trueruslan.zakupgotov.retailer.RetailerId;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public record RetailerComparisonView(
        RetailerId retailerId,
        String displayName,
        RetailerCoverageStatus coverage,
        RetailerProductionAccessStatus productionAccess,
        RetailerComparisonStatus comparisonStatus,
        List<RetailerComparisonReason> reasons,
        Optional<BasketTotal> total,
        Optional<RetailerFreshness> freshness) {

    private static final Set<RetailerComparisonReason> INCOMPLETE_REASONS = Set.of(
            RetailerComparisonReason.ITEM_UNMATCHED,
            RetailerComparisonReason.ITEM_AMBIGUOUS,
            RetailerComparisonReason.ITEM_UNAVAILABLE,
            RetailerComparisonReason.PACKAGE_QUANTITY_UNKNOWN,
            RetailerComparisonReason.QUANTITY_UNIT_MISMATCH,
            RetailerComparisonReason.AVAILABILITY_UNKNOWN);

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
                if (!reasons.equals(List.of(RetailerComparisonReason.AVAILABILITY_UNKNOWN))) {
                    throw new IllegalArgumentException(
                            "UNCERTAIN comparison requires exactly AVAILABILITY_UNKNOWN reason");
                }
                if (total.isEmpty() || freshness.isEmpty()) {
                    throw new IllegalArgumentException(
                            "UNCERTAIN comparison requires reasons, total and freshness");
                }
            }
            case INCOMPLETE -> {
                if (reasons.isEmpty() || total.isPresent() || freshness.isPresent()) {
                    throw new IllegalArgumentException(
                            "INCOMPLETE comparison requires reasons and no aggregate total or freshness");
                }
                if (!INCOMPLETE_REASONS.containsAll(reasons)) {
                    throw new IllegalArgumentException(
                            "INCOMPLETE comparison accepts only item-level reasons");
                }
            }
            case UNAVAILABLE -> {
                if (reasons.isEmpty() || total.isPresent() || freshness.isPresent()) {
                    throw new IllegalArgumentException(
                            "UNAVAILABLE comparison requires reasons and no aggregate total or freshness");
                }
                validateUnavailableReason(coverage, productionAccess, reasons);
            }
        }
    }

    private static void validateUnavailableReason(
            RetailerCoverageStatus coverage,
            RetailerProductionAccessStatus productionAccess,
            List<RetailerComparisonReason> reasons) {
        if (reasons.size() != 1) {
            throw new IllegalArgumentException("UNAVAILABLE reason must contain exactly one cause");
        }

        var reason = reasons.getFirst();
        if (coverage != RetailerCoverageStatus.CONNECTED) {
            var expected = switch (coverage) {
                case DISCOVERY -> RetailerComparisonReason.COVERAGE_DISCOVERY;
                case DEGRADED -> RetailerComparisonReason.COVERAGE_DEGRADED;
                case BLOCKED -> RetailerComparisonReason.COVERAGE_BLOCKED;
                case CONNECTED -> throw new IllegalStateException("CONNECTED handled separately");
            };
            if (reason != expected) {
                throw new IllegalArgumentException(
                        "UNAVAILABLE reason for " + coverage + " coverage must be " + expected);
            }
            return;
        }

        if (productionAccess != RetailerProductionAccessStatus.READY) {
            var expected = switch (productionAccess) {
                case PENDING -> RetailerComparisonReason.PRODUCTION_ACCESS_PENDING;
                case BLOCKED -> RetailerComparisonReason.PRODUCTION_ACCESS_BLOCKED;
                case READY -> throw new IllegalStateException("READY handled separately");
            };
            if (reason != expected) {
                throw new IllegalArgumentException(
                        "UNAVAILABLE reason for " + productionAccess + " production access must be " + expected);
            }
            return;
        }

        if (reason != RetailerComparisonReason.DATA_NOT_AVAILABLE
                && reason != RetailerComparisonReason.SOURCE_UNAVAILABLE) {
            throw new IllegalArgumentException(
                    "UNAVAILABLE reason for connected READY retailer must be DATA_NOT_AVAILABLE or SOURCE_UNAVAILABLE");
        }
    }
}
