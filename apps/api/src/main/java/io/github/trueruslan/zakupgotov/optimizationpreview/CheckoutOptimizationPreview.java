package io.github.trueruslan.zakupgotov.optimizationpreview;

import io.github.trueruslan.zakupgotov.basket.BasketTotal;
import io.github.trueruslan.zakupgotov.basketoptimization.BasketOptimizationStatus;
import io.github.trueruslan.zakupgotov.retailercheckout.RetailerCheckoutComparabilityStatus;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public record CheckoutOptimizationPreview(
        List<RetailerCheckoutPreview> retailers,
        BasketOptimizationStatus status,
        List<String> optimalRetailerIds,
        Optional<BasketTotal> lowestComparableCheckoutTotal) {

    public CheckoutOptimizationPreview {
        retailers = List.copyOf(Objects.requireNonNull(retailers, "retailers must not be null"));
        status = Objects.requireNonNull(status, "status must not be null");
        optimalRetailerIds = List.copyOf(Objects.requireNonNull(
                optimalRetailerIds,
                "optimalRetailerIds must not be null"));
        lowestComparableCheckoutTotal = Objects.requireNonNull(
                lowestComparableCheckoutTotal,
                "lowestComparableCheckoutTotal must not be null");
        if (retailers.isEmpty()) {
            throw new IllegalArgumentException("optimization preview retailers must not be empty");
        }

        var seenRetailers = new HashSet<String>();
        for (var retailer : retailers) {
            if (!seenRetailers.add(retailer.retailerId())) {
                throw new IllegalArgumentException("optimization preview retailers must be unique");
            }
        }

        var expectedOptimalIds = new ArrayList<String>();
        BigDecimal lowestAmount = null;
        String currency = null;
        BasketTotal firstLowestTotal = null;
        for (var retailer : retailers) {
            var assessment = retailer.assessment();
            if (assessment.isEmpty()
                    || assessment.orElseThrow().comparabilityStatus()
                            != RetailerCheckoutComparabilityStatus.COMPARABLE) {
                continue;
            }
            var total = assessment.orElseThrow().comparableCheckoutTotal().orElseThrow();
            if (currency == null) {
                currency = total.currencyCode();
            } else if (!currency.equals(total.currencyCode())) {
                throw new IllegalArgumentException("comparable optimization preview retailers must use one currency");
            }
            if (lowestAmount == null || total.amount().compareTo(lowestAmount) < 0) {
                lowestAmount = total.amount();
                firstLowestTotal = total;
                expectedOptimalIds.clear();
                expectedOptimalIds.add(retailer.retailerId());
            } else if (total.amount().compareTo(lowestAmount) == 0) {
                expectedOptimalIds.add(retailer.retailerId());
            }
        }

        var expectedStatus = switch (expectedOptimalIds.size()) {
            case 0 -> BasketOptimizationStatus.NO_COMPARABLE_CANDIDATES;
            case 1 -> BasketOptimizationStatus.UNIQUE_WINNER;
            default -> BasketOptimizationStatus.TIE;
        };
        if (status != expectedStatus) {
            throw new IllegalArgumentException("optimization preview status must match comparable retailer minima");
        }
        if (!optimalRetailerIds.equals(expectedOptimalIds)) {
            throw new IllegalArgumentException("optimal retailer ids must match comparable retailer minima and order");
        }
        var expectedLowest = firstLowestTotal == null
                ? Optional.<BasketTotal>empty()
                : Optional.of(firstLowestTotal);
        if (!lowestComparableCheckoutTotal.equals(expectedLowest)) {
            throw new IllegalArgumentException("lowest comparable checkout total must match optimal retailer evidence");
        }
    }
}
