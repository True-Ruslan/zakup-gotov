package io.github.trueruslan.zakupgotov.preview;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.github.trueruslan.zakupgotov.basket.BasketTotal;
import io.github.trueruslan.zakupgotov.comparison.RetailerComparisonReason;
import io.github.trueruslan.zakupgotov.comparison.RetailerComparisonStatus;
import io.github.trueruslan.zakupgotov.comparison.RetailerCoverageStatus;
import io.github.trueruslan.zakupgotov.comparison.RetailerFreshness;
import io.github.trueruslan.zakupgotov.comparison.RetailerProductionAccessStatus;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@JsonInclude(JsonInclude.Include.NON_ABSENT)
public record ComparisonPreviewRetailer(
        String id,
        String displayName,
        RetailerCoverageStatus coverage,
        RetailerProductionAccessStatus productionAccess,
        RetailerComparisonStatus comparisonStatus,
        List<RetailerComparisonReason> reasons,
        Optional<BasketTotal> total,
        Optional<RetailerFreshness> freshness,
        List<ComparisonPreviewItem> items) {

    public ComparisonPreviewRetailer {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("id must not be blank");
        }
        if (displayName == null || displayName.isBlank()) {
            throw new IllegalArgumentException("displayName must not be blank");
        }
        coverage = Objects.requireNonNull(coverage, "coverage must not be null");
        productionAccess = Objects.requireNonNull(productionAccess, "productionAccess must not be null");
        comparisonStatus = Objects.requireNonNull(comparisonStatus, "comparisonStatus must not be null");
        reasons = List.copyOf(Objects.requireNonNull(reasons, "reasons must not be null"));
        total = Objects.requireNonNull(total, "total must not be null");
        freshness = Objects.requireNonNull(freshness, "freshness must not be null");
        items = List.copyOf(Objects.requireNonNull(items, "items must not be null"));
        if (comparisonStatus == RetailerComparisonStatus.UNAVAILABLE && !items.isEmpty()) {
            throw new IllegalArgumentException("UNAVAILABLE retailer must not expose item-level comparison details");
        }
    }
}
