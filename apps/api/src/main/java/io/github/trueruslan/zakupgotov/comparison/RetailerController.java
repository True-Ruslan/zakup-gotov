package io.github.trueruslan.zakupgotov.comparison;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.github.trueruslan.zakupgotov.basket.BasketTotal;
import io.github.trueruslan.zakupgotov.retailer.RetailerRegistry;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public final class RetailerController {

    private final RetailerComparisonReadModelAssembler assembler = new RetailerComparisonReadModelAssembler();
    private final RetailerRegistry registry = RetailerRegistry.initial();

    @GetMapping("/retailers")
    public RetailerReadinessResponse retailers() {
        var catalog = assembler.assemble(registry, Map.of());
        return new RetailerReadinessResponse(catalog.retailers().stream()
                .map(RetailerReadinessItem::from)
                .toList());
    }

    public record RetailerReadinessResponse(List<RetailerReadinessItem> retailers) {
        public RetailerReadinessResponse {
            retailers = List.copyOf(Objects.requireNonNull(retailers, "retailers must not be null"));
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record RetailerReadinessItem(
            String id,
            String displayName,
            RetailerCoverageStatus coverage,
            RetailerProductionAccessStatus productionAccess,
            RetailerComparisonStatus comparisonStatus,
            List<RetailerComparisonReason> reasons,
            BasketTotalResponse total,
            RetailerFreshnessResponse freshness) {

        private static RetailerReadinessItem from(RetailerComparisonView view) {
            return new RetailerReadinessItem(
                    view.retailerId().canonicalId(),
                    view.displayName(),
                    view.coverage(),
                    view.productionAccess(),
                    view.comparisonStatus(),
                    view.reasons(),
                    view.total().map(BasketTotalResponse::from).orElse(null),
                    view.freshness().map(RetailerFreshnessResponse::from).orElse(null));
        }

        public RetailerReadinessItem {
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
        }
    }

    public record BasketTotalResponse(BigDecimal amount, String currencyCode) {
        private static BasketTotalResponse from(BasketTotal total) {
            return new BasketTotalResponse(total.amount(), total.currencyCode());
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record RetailerFreshnessResponse(
            RetailerFreshnessBasis basis,
            Instant observedAt,
            Instant providerUpdatedAt) {
        private static RetailerFreshnessResponse from(RetailerFreshness freshness) {
            return new RetailerFreshnessResponse(
                    freshness.basis(),
                    freshness.observedAt(),
                    freshness.providerUpdatedAt().orElse(null));
        }
    }
}
