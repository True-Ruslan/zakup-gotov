package io.github.trueruslan.zakupgotov.optimizationpreview;

import io.github.trueruslan.zakupgotov.basket.BasketEconomics;
import io.github.trueruslan.zakupgotov.basket.BasketFee;
import io.github.trueruslan.zakupgotov.basket.MinimumOrderConstraint;
import io.github.trueruslan.zakupgotov.basketoptimization.BasketOptimizer;
import io.github.trueruslan.zakupgotov.preview.ComparisonPreviewComputation;
import io.github.trueruslan.zakupgotov.retailer.RetailerId;
import io.github.trueruslan.zakupgotov.retailercheckout.RetailerCheckoutAssessmentService;
import io.github.trueruslan.zakupgotov.retailercheckout.RetailerCheckoutEconomicsEvidence;
import java.util.EnumSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class CheckoutOptimizationPreviewService {

    private final CheckoutEconomicsEvidenceSource economicsEvidenceSource;
    private final RetailerCheckoutAssessmentService assessmentService;
    private final BasketOptimizer basketOptimizer;

    public CheckoutOptimizationPreviewService(
            CheckoutEconomicsEvidenceSource economicsEvidenceSource,
            RetailerCheckoutAssessmentService assessmentService,
            BasketOptimizer basketOptimizer) {
        this.economicsEvidenceSource = Objects.requireNonNull(
                economicsEvidenceSource,
                "economicsEvidenceSource must not be null");
        this.assessmentService = Objects.requireNonNull(
                assessmentService,
                "assessmentService must not be null");
        this.basketOptimizer = Objects.requireNonNull(
                basketOptimizer,
                "basketOptimizer must not be null");
    }

    public CheckoutOptimizationPreview create(ComparisonPreviewComputation comparisonComputation) {
        comparisonComputation = Objects.requireNonNull(
                comparisonComputation,
                "comparisonComputation must not be null");
        var comparisonRetailers = comparisonComputation.catalog().retailers();
        if (comparisonRetailers.isEmpty()) {
            throw new IllegalArgumentException("comparison catalog must contain at least one retailer");
        }

        var requestedRetailers = EnumSet.noneOf(RetailerId.class);
        for (var comparison : comparisonRetailers) {
            if (comparison.total().isPresent()) {
                requestedRetailers.add(comparison.retailerId());
            }
        }

        var knownEconomics = loadKnownEconomics(
                comparisonComputation,
                requestedRetailers);
        var results = comparisonRetailers.stream()
                .map(comparison -> assessmentService.assess(
                        comparison,
                        new RetailerCheckoutEconomicsEvidence(
                                comparison.retailerId(),
                                knownEconomics.getOrDefault(
                                        comparison.retailerId(),
                                        unknownEconomics()))))
                .toList();
        var optimization = basketOptimizer.optimize(results);
        var projectedRetailers = results.stream()
                .map(RetailerCheckoutPreview::from)
                .toList();
        var optimalRetailerIds = optimization.optimalCandidates().stream()
                .map(candidate -> candidate.retailerId().canonicalId())
                .toList();

        return new CheckoutOptimizationPreview(
                projectedRetailers,
                optimization.status(),
                optimalRetailerIds,
                optimization.lowestComparableCheckoutTotal(),
                optimization);
    }

    private Map<RetailerId, BasketEconomics> loadKnownEconomics(
            ComparisonPreviewComputation comparisonComputation,
            Set<RetailerId> requestedRetailers) {
        if (requestedRetailers.isEmpty()) {
            return Map.of();
        }
        var immutableRequestedRetailers = Set.copyOf(requestedRetailers);
        var loaded = Objects.requireNonNull(
                economicsEvidenceSource.load(
                        comparisonComputation.input().productLocation(),
                        immutableRequestedRetailers),
                "checkout economics evidence must not be null");
        for (var entry : loaded.entrySet()) {
            var retailerId = Objects.requireNonNull(entry.getKey(), "checkout economics retailer id must not be null");
            Objects.requireNonNull(entry.getValue(), "checkout economics value must not be null");
            if (!immutableRequestedRetailers.contains(retailerId)) {
                throw new IllegalStateException(
                        "checkout economics source returned unrequested retailer: " + retailerId.canonicalId());
            }
        }
        return Map.copyOf(loaded);
    }

    private static BasketEconomics unknownEconomics() {
        return new BasketEconomics(
                BasketFee.unknown(),
                BasketFee.unknown(),
                MinimumOrderConstraint.unknown());
    }
}
