package io.github.trueruslan.zakupgotov.basketoptimization;

import io.github.trueruslan.zakupgotov.retailercheckout.RetailerCheckoutAssessmentResult;
import java.util.List;

record BasketOptimizationEvaluation(
        BasketOptimizationStatus status,
        List<RetailerCheckoutAssessmentResult> optimalCandidates) {

    BasketOptimizationEvaluation {
        optimalCandidates = List.copyOf(optimalCandidates);
    }
}
