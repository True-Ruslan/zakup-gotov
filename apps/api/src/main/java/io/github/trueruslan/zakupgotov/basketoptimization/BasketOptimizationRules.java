package io.github.trueruslan.zakupgotov.basketoptimization;

import io.github.trueruslan.zakupgotov.retailer.RetailerId;
import io.github.trueruslan.zakupgotov.retailercheckout.RetailerCheckoutAssessmentResult;
import io.github.trueruslan.zakupgotov.retailercheckout.RetailerCheckoutComparabilityStatus;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

final class BasketOptimizationRules {

    private BasketOptimizationRules() {}

    static BasketOptimizationEvaluation evaluate(List<RetailerCheckoutAssessmentResult> candidates) {
        Objects.requireNonNull(candidates, "candidates must not be null");
        if (candidates.isEmpty()) {
            throw new IllegalArgumentException("optimizer candidates must not be empty");
        }

        var snapshot = List.copyOf(candidates);
        validateUniqueRetailers(snapshot);

        String comparableCurrency = null;
        BigDecimal lowestAmount = null;
        var optimalCandidates = new ArrayList<RetailerCheckoutAssessmentResult>();

        for (var candidate : snapshot) {
            var assessment = candidate.assessment();
            if (assessment.isEmpty()
                    || assessment.orElseThrow().comparabilityStatus()
                            != RetailerCheckoutComparabilityStatus.COMPARABLE) {
                continue;
            }

            var total = assessment.orElseThrow().comparableCheckoutTotal().orElseThrow(() ->
                    new IllegalArgumentException("COMPARABLE checkout assessment requires comparable total"));
            if (comparableCurrency == null) {
                comparableCurrency = total.currencyCode();
            } else if (!comparableCurrency.equals(total.currencyCode())) {
                throw new IllegalArgumentException("comparable optimizer candidates must use one currency");
            }

            if (lowestAmount == null) {
                lowestAmount = total.amount();
                optimalCandidates.add(candidate);
                continue;
            }

            var comparison = total.amount().compareTo(lowestAmount);
            if (comparison < 0) {
                lowestAmount = total.amount();
                optimalCandidates.clear();
                optimalCandidates.add(candidate);
            } else if (comparison == 0) {
                optimalCandidates.add(candidate);
            }
        }

        var status = switch (optimalCandidates.size()) {
            case 0 -> BasketOptimizationStatus.NO_COMPARABLE_CANDIDATES;
            case 1 -> BasketOptimizationStatus.UNIQUE_WINNER;
            default -> BasketOptimizationStatus.TIE;
        };
        return new BasketOptimizationEvaluation(status, optimalCandidates);
    }

    private static void validateUniqueRetailers(List<RetailerCheckoutAssessmentResult> candidates) {
        Set<RetailerId> retailers = new HashSet<>();
        for (var candidate : candidates) {
            Objects.requireNonNull(candidate, "optimizer candidate must not be null");
            var retailerId = candidate.retailerId();
            if (!retailers.add(retailerId)) {
                throw new IllegalArgumentException("optimizer candidates must contain unique retailer ids");
            }
        }
    }
}
