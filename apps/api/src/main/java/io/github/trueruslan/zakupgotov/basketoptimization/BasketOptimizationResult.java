package io.github.trueruslan.zakupgotov.basketoptimization;

import io.github.trueruslan.zakupgotov.basket.BasketTotal;
import io.github.trueruslan.zakupgotov.retailercheckout.RetailerCheckoutAssessmentResult;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public record BasketOptimizationResult(
        List<RetailerCheckoutAssessmentResult> candidates,
        BasketOptimizationStatus status,
        List<RetailerCheckoutAssessmentResult> optimalCandidates) {

    public BasketOptimizationResult {
        candidates = List.copyOf(Objects.requireNonNull(candidates, "candidates must not be null"));
        status = Objects.requireNonNull(status, "status must not be null");
        optimalCandidates = List.copyOf(Objects.requireNonNull(optimalCandidates, "optimalCandidates must not be null"));

        var expected = BasketOptimizationRules.evaluate(candidates);
        if (status != expected.status()) {
            throw new IllegalArgumentException("optimization status must match deterministic candidate evaluation");
        }
        if (!optimalCandidates.equals(expected.optimalCandidates())) {
            throw new IllegalArgumentException("optimal candidates must exactly match deterministic minimum set and order");
        }
    }

    public Optional<BasketTotal> lowestComparableCheckoutTotal() {
        if (optimalCandidates.isEmpty()) {
            return Optional.empty();
        }
        return optimalCandidates.getFirst().assessment().orElseThrow().comparableCheckoutTotal();
    }
}
