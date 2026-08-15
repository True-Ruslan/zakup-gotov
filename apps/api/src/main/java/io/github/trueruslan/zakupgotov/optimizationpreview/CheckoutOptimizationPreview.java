package io.github.trueruslan.zakupgotov.optimizationpreview;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.github.trueruslan.zakupgotov.basket.BasketTotal;
import io.github.trueruslan.zakupgotov.basketoptimization.BasketOptimizationResult;
import io.github.trueruslan.zakupgotov.basketoptimization.BasketOptimizationStatus;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public record CheckoutOptimizationPreview(
        List<RetailerCheckoutPreview> retailers,
        BasketOptimizationStatus status,
        List<String> optimalRetailerIds,
        Optional<BasketTotal> lowestComparableCheckoutTotal,
        @JsonIgnore BasketOptimizationResult acceptedOptimizerResult) {

    public CheckoutOptimizationPreview {
        retailers = List.copyOf(Objects.requireNonNull(retailers, "retailers must not be null"));
        status = Objects.requireNonNull(status, "status must not be null");
        optimalRetailerIds = List.copyOf(Objects.requireNonNull(
                optimalRetailerIds,
                "optimalRetailerIds must not be null"));
        lowestComparableCheckoutTotal = Objects.requireNonNull(
                lowestComparableCheckoutTotal,
                "lowestComparableCheckoutTotal must not be null");
        acceptedOptimizerResult = Objects.requireNonNull(
                acceptedOptimizerResult,
                "acceptedOptimizerResult must not be null");
        if (retailers.isEmpty()) {
            throw new IllegalArgumentException("optimization preview retailers must not be empty");
        }

        var seenRetailers = new HashSet<String>();
        for (var retailer : retailers) {
            if (!seenRetailers.add(retailer.retailerId())) {
                throw new IllegalArgumentException("optimization preview retailers must be unique");
            }
        }

        var expectedRetailers = acceptedOptimizerResult.candidates().stream()
                .map(RetailerCheckoutPreview::from)
                .toList();
        if (!retailers.equals(expectedRetailers)) {
            throw new IllegalArgumentException("optimization preview retailers must match accepted optimizer result");
        }
        if (status != acceptedOptimizerResult.status()) {
            throw new IllegalArgumentException("optimization preview status must match accepted optimizer result");
        }
        var expectedOptimalRetailerIds = acceptedOptimizerResult.optimalCandidates().stream()
                .map(candidate -> candidate.retailerId().canonicalId())
                .toList();
        if (!optimalRetailerIds.equals(expectedOptimalRetailerIds)) {
            throw new IllegalArgumentException("optimal retailer ids must match accepted optimizer result");
        }
        if (!lowestComparableCheckoutTotal.equals(acceptedOptimizerResult.lowestComparableCheckoutTotal())) {
            throw new IllegalArgumentException("lowest comparable checkout total must match accepted optimizer result");
        }
    }
}
