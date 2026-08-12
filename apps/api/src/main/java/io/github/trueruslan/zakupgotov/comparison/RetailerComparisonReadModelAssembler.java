package io.github.trueruslan.zakupgotov.comparison;

import io.github.trueruslan.zakupgotov.retailer.ProductionAccessStatus;
import io.github.trueruslan.zakupgotov.retailer.RetailerCoverageState;
import io.github.trueruslan.zakupgotov.retailer.RetailerId;
import io.github.trueruslan.zakupgotov.retailer.RetailerRegistry;
import io.github.trueruslan.zakupgotov.retailer.RetailerRegistryEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class RetailerComparisonReadModelAssembler {

    public RetailerComparisonCatalog assemble(
            RetailerRegistry registry,
            Map<RetailerId, RetailerComparisonEvidence> evidenceByRetailer) {
        Objects.requireNonNull(registry, "registry must not be null");
        return assembleEntries(registry.entries(), evidenceByRetailer);
    }

    RetailerComparisonCatalog assembleEntries(
            List<RetailerRegistryEntry> entries,
            Map<RetailerId, RetailerComparisonEvidence> evidenceByRetailer) {
        Objects.requireNonNull(entries, "entries must not be null");
        Objects.requireNonNull(evidenceByRetailer, "evidenceByRetailer must not be null");

        var views = new ArrayList<RetailerComparisonView>();
        for (var entry : entries) {
            Objects.requireNonNull(entry, "retailer entry must not be null");
            views.add(withoutRuntimeEvidence(entry));
        }
        return new RetailerComparisonCatalog(views);
    }

    private static RetailerComparisonView withoutRuntimeEvidence(RetailerRegistryEntry entry) {
        var coverage = coverage(entry.coverageState());
        var productionAccess = productionAccess(entry.productionAccessStatus());

        RetailerComparisonReason reason;
        if (coverage != RetailerCoverageStatus.CONNECTED) {
            reason = switch (coverage) {
                case DISCOVERY -> RetailerComparisonReason.COVERAGE_DISCOVERY;
                case DEGRADED -> RetailerComparisonReason.COVERAGE_DEGRADED;
                case BLOCKED -> RetailerComparisonReason.COVERAGE_BLOCKED;
                case CONNECTED -> throw new IllegalStateException("connected coverage handled separately");
            };
        } else if (productionAccess != RetailerProductionAccessStatus.READY) {
            reason = productionAccess == RetailerProductionAccessStatus.BLOCKED
                    ? RetailerComparisonReason.PRODUCTION_ACCESS_BLOCKED
                    : RetailerComparisonReason.PRODUCTION_ACCESS_PENDING;
        } else {
            reason = RetailerComparisonReason.DATA_NOT_AVAILABLE;
        }

        return new RetailerComparisonView(
                entry.retailer().id(),
                displayName(entry.retailer().id()),
                coverage,
                productionAccess,
                RetailerComparisonStatus.UNAVAILABLE,
                List.of(reason),
                Optional.empty(),
                Optional.empty());
    }

    private static RetailerCoverageStatus coverage(RetailerCoverageState state) {
        return switch (Objects.requireNonNull(state, "coverage state must not be null")) {
            case AVAILABLE_DIRECT, AVAILABLE_AGGREGATOR, AVAILABLE_PUBLIC_WEB, AVAILABLE_BROWSER_BRIDGE ->
                    RetailerCoverageStatus.CONNECTED;
            case REQUIRED_UNIMPLEMENTED, DISCOVERY -> RetailerCoverageStatus.DISCOVERY;
            case DEGRADED -> RetailerCoverageStatus.DEGRADED;
            case BLOCKED_EXTERNAL -> RetailerCoverageStatus.BLOCKED;
        };
    }

    private static RetailerProductionAccessStatus productionAccess(ProductionAccessStatus status) {
        return switch (Objects.requireNonNull(status, "production access status must not be null")) {
            case ACCEPTABLE -> RetailerProductionAccessStatus.READY;
            case NOT_ASSESSED, UNRESOLVED -> RetailerProductionAccessStatus.PENDING;
            case BLOCKED -> RetailerProductionAccessStatus.BLOCKED;
        };
    }

    private static String displayName(RetailerId retailerId) {
        return switch (Objects.requireNonNull(retailerId, "retailerId must not be null")) {
            case PYATEROCHKA -> "Пятёрочка";
            case PEREKRESTOK -> "Перекрёсток";
            case CHIZHIK -> "Чижик";
            case MAGNIT -> "Магнит";
            case LENTA -> "Лента";
            case VKUSVILL -> "ВкусВилл";
            case OZON_FRESH -> "Ozon Fresh";
            case SAMOKAT -> "Самокат";
        };
    }
}
