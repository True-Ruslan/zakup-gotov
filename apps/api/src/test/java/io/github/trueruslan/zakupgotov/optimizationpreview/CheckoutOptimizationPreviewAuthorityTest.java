package io.github.trueruslan.zakupgotov.optimizationpreview;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.trueruslan.zakupgotov.basket.BasketEconomics;
import io.github.trueruslan.zakupgotov.basket.BasketFee;
import io.github.trueruslan.zakupgotov.basket.BasketTotal;
import io.github.trueruslan.zakupgotov.basket.MinimumOrderConstraint;
import io.github.trueruslan.zakupgotov.basketoptimization.BasketOptimizationStatus;
import io.github.trueruslan.zakupgotov.basketoptimization.BasketOptimizer;
import io.github.trueruslan.zakupgotov.comparison.RetailerComparisonStatus;
import io.github.trueruslan.zakupgotov.comparison.RetailerComparisonView;
import io.github.trueruslan.zakupgotov.comparison.RetailerCoverageStatus;
import io.github.trueruslan.zakupgotov.comparison.RetailerFreshness;
import io.github.trueruslan.zakupgotov.comparison.RetailerFreshnessBasis;
import io.github.trueruslan.zakupgotov.comparison.RetailerProductionAccessStatus;
import io.github.trueruslan.zakupgotov.retailer.RetailerId;
import io.github.trueruslan.zakupgotov.retailercheckout.RetailerCheckoutAssessmentService;
import io.github.trueruslan.zakupgotov.retailercheckout.RetailerCheckoutEconomicsEvidence;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class CheckoutOptimizationPreviewAuthorityTest {

    @Test
    void projectionCannotContradictAcceptedOptimizerResult() {
        var assessmentService = new RetailerCheckoutAssessmentService();
        var result = assessmentService.assess(
                readyComparison(),
                new RetailerCheckoutEconomicsEvidence(
                        RetailerId.PYATEROCHKA,
                        new BasketEconomics(
                                BasketFee.known(total("0")),
                                BasketFee.known(total("0")),
                                MinimumOrderConstraint.known(total("0")))));
        var optimization = new BasketOptimizer().optimize(List.of(result));
        var projectedRetailers = List.of(RetailerCheckoutPreview.from(result));

        assertThatThrownBy(() -> new CheckoutOptimizationPreview(
                        projectedRetailers,
                        BasketOptimizationStatus.NO_COMPARABLE_CANDIDATES,
                        List.of(),
                        Optional.empty(),
                        optimization))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("accepted optimizer result");
    }

    private static RetailerComparisonView readyComparison() {
        return new RetailerComparisonView(
                RetailerId.PYATEROCHKA,
                "Пятёрочка",
                RetailerCoverageStatus.CONNECTED,
                RetailerProductionAccessStatus.READY,
                RetailerComparisonStatus.READY,
                List.of(),
                Optional.of(total("1000")),
                Optional.of(new RetailerFreshness(
                        RetailerFreshnessBasis.OBSERVATION_ONLY,
                        Instant.parse("2026-08-15T10:00:00Z"),
                        Optional.empty())));
    }

    private static BasketTotal total(String amount) {
        return new BasketTotal(new BigDecimal(amount), "RUB");
    }
}
