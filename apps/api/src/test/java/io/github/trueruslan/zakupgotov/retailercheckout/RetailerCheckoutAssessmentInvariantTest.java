package io.github.trueruslan.zakupgotov.retailercheckout;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.trueruslan.zakupgotov.basket.BasketEconomics;
import io.github.trueruslan.zakupgotov.basket.BasketEconomicsCalculator;
import io.github.trueruslan.zakupgotov.basket.BasketFee;
import io.github.trueruslan.zakupgotov.basket.BasketTotal;
import io.github.trueruslan.zakupgotov.basket.MinimumOrderConstraint;
import io.github.trueruslan.zakupgotov.comparison.RetailerComparisonReason;
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

class RetailerCheckoutAssessmentInvariantTest {

    @Test
    void rejectsEconomicsSubtotalThatDoesNotMatchComparisonSubtotal() {
        var comparison = ready(RetailerId.PYATEROCHKA, money("1200.00", "RUB"));
        var economicsAssessment = BasketEconomicsCalculator.assess(
                money("1100.00", "RUB"),
                economics("0", "0", "1000.00", "RUB"));

        assertThatThrownBy(() -> new RetailerCheckoutAssessment(
                        comparison,
                        economicsAssessment,
                        RetailerCheckoutEligibilityStatus.ELIGIBLE,
                        RetailerCheckoutComparabilityStatus.COMPARABLE,
                        economicsAssessment.checkoutTotal()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("subtotal");
    }

    @Test
    void rejectsForgedEligibilityStatus() {
        var comparison = ready(RetailerId.PYATEROCHKA, money("1200.00", "RUB"));
        var economicsAssessment = BasketEconomicsCalculator.assess(
                comparison.total().orElseThrow(),
                economics("100.00", "20.00", "1000.00", "RUB"));

        assertThatThrownBy(() -> new RetailerCheckoutAssessment(
                        comparison,
                        economicsAssessment,
                        RetailerCheckoutEligibilityStatus.UNKNOWN,
                        RetailerCheckoutComparabilityStatus.NOT_COMPARABLE,
                        Optional.empty()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("eligibility");
    }

    @Test
    void rejectsForgedComparabilityStatus() {
        var comparison = ready(RetailerId.PYATEROCHKA, money("1200.00", "RUB"));
        var economicsAssessment = BasketEconomicsCalculator.assess(
                comparison.total().orElseThrow(),
                economics("100.00", "20.00", "1000.00", "RUB"));

        assertThatThrownBy(() -> new RetailerCheckoutAssessment(
                        comparison,
                        economicsAssessment,
                        RetailerCheckoutEligibilityStatus.ELIGIBLE,
                        RetailerCheckoutComparabilityStatus.NOT_COMPARABLE,
                        Optional.empty()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("comparability");
    }

    @Test
    void rejectsComparableTotalThatDiffersFromEconomicsTotal() {
        var comparison = ready(RetailerId.PYATEROCHKA, money("1200.00", "RUB"));
        var economicsAssessment = BasketEconomicsCalculator.assess(
                comparison.total().orElseThrow(),
                economics("100.00", "20.00", "1000.00", "RUB"));

        assertThatThrownBy(() -> new RetailerCheckoutAssessment(
                        comparison,
                        economicsAssessment,
                        RetailerCheckoutEligibilityStatus.ELIGIBLE,
                        RetailerCheckoutComparabilityStatus.COMPARABLE,
                        Optional.of(money("999.00", "RUB"))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("comparable checkout total");
    }

    @Test
    void rejectsAssessmentForIncompleteComparison() {
        var comparison = incomplete();
        var economicsAssessment = BasketEconomicsCalculator.assess(
                money("1200.00", "RUB"),
                economics("100.00", "20.00", "1000.00", "RUB"));

        assertThatThrownBy(() -> new RetailerCheckoutAssessment(
                        comparison,
                        economicsAssessment,
                        RetailerCheckoutEligibilityStatus.ELIGIBLE,
                        RetailerCheckoutComparabilityStatus.COMPARABLE,
                        economicsAssessment.checkoutTotal()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("READY or UNCERTAIN");
    }

    @Test
    void resultRejectsMissingAssessmentForAssessableComparison() {
        var comparison = ready(RetailerId.PYATEROCHKA, money("1200.00", "RUB"));

        assertThatThrownBy(() -> new RetailerCheckoutAssessmentResult(comparison, Optional.empty()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("presence");
    }

    @Test
    void resultRejectsAssessmentFromAnotherComparison() {
        var service = new RetailerCheckoutAssessmentService();
        var pyaterochka = ready(RetailerId.PYATEROCHKA, money("1200.00", "RUB"));
        var perekrestok = ready(RetailerId.PEREKRESTOK, money("1200.00", "RUB"));
        var assessment = service.assess(
                        pyaterochka,
                        economics("100.00", "20.00", "1000.00", "RUB"))
                .assessment()
                .orElseThrow();

        assertThatThrownBy(() -> new RetailerCheckoutAssessmentResult(perekrestok, Optional.of(assessment)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("same retailer comparison");
    }

    @Test
    void m41CurrencyValidationStillFailsFastThroughCompositionService() {
        var comparison = ready(RetailerId.PYATEROCHKA, money("1200.00", "RUB"));
        var mixedCurrencyEconomics = new BasketEconomics(
                BasketFee.known(money("10.00", "USD")),
                BasketFee.known(money("0", "RUB")),
                MinimumOrderConstraint.known(money("1000.00", "RUB")));

        assertThatThrownBy(() -> new RetailerCheckoutAssessmentService().assess(comparison, mixedCurrencyEconomics))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("currency");
    }

    private static BasketEconomics economics(
            String delivery,
            String service,
            String minimum,
            String currency) {
        return new BasketEconomics(
                BasketFee.known(money(delivery, currency)),
                BasketFee.known(money(service, currency)),
                MinimumOrderConstraint.known(money(minimum, currency)));
    }

    private static RetailerComparisonView ready(RetailerId retailerId, BasketTotal subtotal) {
        return new RetailerComparisonView(
                retailerId,
                retailerId == RetailerId.PYATEROCHKA ? "Пятёрочка" : "Перекрёсток",
                RetailerCoverageStatus.CONNECTED,
                RetailerProductionAccessStatus.READY,
                RetailerComparisonStatus.READY,
                List.of(),
                Optional.of(subtotal),
                Optional.of(freshness()));
    }

    private static RetailerComparisonView incomplete() {
        return new RetailerComparisonView(
                RetailerId.PYATEROCHKA,
                "Пятёрочка",
                RetailerCoverageStatus.CONNECTED,
                RetailerProductionAccessStatus.READY,
                RetailerComparisonStatus.INCOMPLETE,
                List.of(RetailerComparisonReason.ITEM_UNMATCHED),
                Optional.empty(),
                Optional.empty());
    }

    private static RetailerFreshness freshness() {
        return new RetailerFreshness(
                RetailerFreshnessBasis.OBSERVATION_ONLY,
                Instant.parse("2026-08-15T12:00:00Z"),
                Optional.empty());
    }

    private static BasketTotal money(String amount, String currency) {
        return new BasketTotal(new BigDecimal(amount), currency);
    }
}
