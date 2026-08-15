package io.github.trueruslan.zakupgotov.retailercheckout;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.trueruslan.zakupgotov.basket.BasketEconomics;
import io.github.trueruslan.zakupgotov.basket.BasketFee;
import io.github.trueruslan.zakupgotov.basket.BasketTotal;
import io.github.trueruslan.zakupgotov.basket.MinimumOrderConstraint;
import io.github.trueruslan.zakupgotov.comparison.RetailerComparisonStatus;
import io.github.trueruslan.zakupgotov.comparison.RetailerComparisonView;
import io.github.trueruslan.zakupgotov.comparison.RetailerCoverageStatus;
import io.github.trueruslan.zakupgotov.comparison.RetailerFreshness;
import io.github.trueruslan.zakupgotov.comparison.RetailerFreshnessBasis;
import io.github.trueruslan.zakupgotov.comparison.RetailerProductionAccessStatus;
import io.github.trueruslan.zakupgotov.retailer.RetailerId;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class RetailerCheckoutEconomicsBindingTest {

    private final RetailerCheckoutAssessmentService service = new RetailerCheckoutAssessmentService();

    @Test
    void retailerBoundEconomicsComposesWhenRetailerMatchesComparison() {
        var comparison = ready(RetailerId.PYATEROCHKA);
        var evidence = new RetailerCheckoutEconomicsEvidence(RetailerId.PYATEROCHKA, economics());

        var result = service.assess(comparison, evidence);

        assertThat(result.assessment()).isPresent();
        assertThat(result.assessment().orElseThrow().eligibilityStatus())
                .isEqualTo(RetailerCheckoutEligibilityStatus.ELIGIBLE);
    }

    @Test
    void crossRetailerEconomicsEvidenceFailsClosedBeforeCheckoutArithmetic() {
        var comparison = ready(RetailerId.PYATEROCHKA);
        var evidence = new RetailerCheckoutEconomicsEvidence(RetailerId.PEREKRESTOK, economics());

        assertThatThrownBy(() -> service.assess(comparison, evidence))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("retailer");
    }

    private static RetailerComparisonView ready(RetailerId retailerId) {
        return new RetailerComparisonView(
                retailerId,
                "Retailer",
                RetailerCoverageStatus.CONNECTED,
                RetailerProductionAccessStatus.READY,
                RetailerComparisonStatus.READY,
                List.of(),
                Optional.of(money("1200.00")),
                Optional.of(new RetailerFreshness(
                        RetailerFreshnessBasis.OBSERVATION_ONLY,
                        Instant.parse("2026-08-15T12:00:00Z"),
                        Optional.empty())));
    }

    private static BasketEconomics economics() {
        return new BasketEconomics(
                BasketFee.known(money("100.00")),
                BasketFee.known(money("20.00")),
                MinimumOrderConstraint.known(money("1000.00")));
    }

    private static BasketTotal money(String amount) {
        return new BasketTotal(new BigDecimal(amount), "RUB");
    }
}
