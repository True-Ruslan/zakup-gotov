package io.github.trueruslan.zakupgotov.basketoptimization;

import io.github.trueruslan.zakupgotov.retailercheckout.RetailerCheckoutAssessmentResult;
import java.util.List;

public final class BasketOptimizer {

    public BasketOptimizationResult optimize(List<RetailerCheckoutAssessmentResult> candidates) {
        var evaluation = BasketOptimizationRules.evaluate(candidates);
        return new BasketOptimizationResult(candidates, evaluation.status(), evaluation.optimalCandidates());
    }
}
