package io.github.trueruslan.zakupgotov.basketoptimization;

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
import io.github.trueruslan.zakupgotov.retailercheckout.RetailerCheckoutAssessmentResult;
import io.github.trueruslan.zakupgotov.retailercheckout.RetailerCheckoutAssessmentService;
import io.github.trueruslan.zakupgotov.retailercheckout.RetailerCheckoutEconomicsEvidence;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class BasketOptimizationInvariantTest {

    @Test
    void optimizerRejectsEmptyCandidateSet() {
        assertThatThrownBy(() -> new BasketOptimizer().optimize(List.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must not be empty");
    }

    @Test
    void optimizerRejectsNullCandidate() {
        var candidates = new ArrayList<RetailerCheckoutAssessmentResult>();
        candidates.add(null);

        assertThatThrownBy(() -> new BasketOptimizer().optimize(candidates))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void optimizerRejectsDuplicateRetailerIds() {
        var first = comparable(RetailerId.PYATEROCHKA, "1000.00", "RUB");
        var second = comparable(RetailerId.PYATEROCHKA, "900.00", "RUB");

        assertThatThrownBy(() -> new BasketOptimizer().optimize(List.of(first, second)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("unique retailer ids");
    }

    @Test
    void resultRejectsForgedStatus() {
        var winner = comparable(RetailerId.PYATEROCHKA, "900.00", "RUB");
        var other = comparable(RetailerId.PEREKRESTOK, "1000.00", "RUB");

        assertThatThrownBy(() -> new BasketOptimizationResult(
                        List.of(winner, other),
                        BasketOptimizationStatus.TIE,
                        List.of(winner)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("status");
    }

    @Test
    void resultRejectsMissingCandidateFromTrueTie() {
        var first = comparable(RetailerId.PYATEROCHKA, "900.0", "RUB");
        var second = comparable(RetailerId.PEREKRESTOK, "900.00", "RUB");

        assertThatThrownBy(() -> new BasketOptimizationResult(
                        List.of(first, second),
                        BasketOptimizationStatus.TIE,
                        List.of(first)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("optimal candidates");
    }

    @Test
    void resultRejectsExtraNonMinimumCandidate() {
        var winner = comparable(RetailerId.PYATEROCHKA, "900.00", "RUB");
        var higher = comparable(RetailerId.PEREKRESTOK, "1000.00", "RUB");

        assertThatThrownBy(() -> new BasketOptimizationResult(
                        List.of(winner, higher),
                        BasketOptimizationStatus.UNIQUE_WINNER,
                        List.of(winner, higher)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("optimal candidates");
    }

    @Test
    void resultRejectsNonComparableCandidateAsWinner() {
        var ineligible = ineligible(RetailerId.PYATEROCHKA, "200.00", "500.00", "RUB");
        var comparable = comparable(RetailerId.PEREKRESTOK, "900.00", "RUB");

        assertThatThrownBy(() -> new BasketOptimizationResult(
                        List.of(ineligible, comparable),
                        BasketOptimizationStatus.UNIQUE_WINNER,
                        List.of(ineligible)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("optimal candidates");
    }

    @Test
    void resultRejectsReorderedTieWinners() {
        var first = comparable(RetailerId.PYATEROCHKA, "900.00", "RUB");
        var second = comparable(RetailerId.PEREKRESTOK, "900.0", "RUB");

        assertThatThrownBy(() -> new BasketOptimizationResult(
                        List.of(first, second),
                        BasketOptimizationStatus.TIE,
                        List.of(second, first)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("optimal candidates");
    }

    @Test
    void resultRejectsMixedComparableCurrencies() {
        var rub = comparable(RetailerId.PYATEROCHKA, "900.00", "RUB");
        var usd = comparable(RetailerId.PEREKRESTOK, "10.00", "USD");

        assertThatThrownBy(() -> new BasketOptimizationResult(
                        List.of(rub, usd),
                        BasketOptimizationStatus.TIE,
                        List.of(rub, usd)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("currency");
    }

    @Test
    void resultDefensivelyCopiesCandidateLists() {
        var first = comparable(RetailerId.PYATEROCHKA, "900.00", "RUB");
        var second = comparable(RetailerId.PEREKRESTOK, "1000.00", "RUB");
        var source = new ArrayList<>(List.of(first, second));

        var result = new BasketOptimizer().optimize(source);
        source.clear();

        assertThat(result.candidates()).containsExactly(first, second);
        assertThat(result.optimalCandidates()).containsExactly(first);
    }

    private static RetailerCheckoutAssessmentResult comparable(
            RetailerId retailerId,
            String amount,
            String currency) {
        return assess(retailerId, amount, "0", currency);
    }

    private static RetailerCheckoutAssessmentResult ineligible(
            RetailerId retailerId,
            String amount,
            String minimum,
            String currency) {
        return assess(retailerId, amount, minimum, currency);
    }

    private static RetailerCheckoutAssessmentResult assess(
            RetailerId retailerId,
            String amount,
            String minimum,
            String currency) {
        var subtotal = money(amount, currency);
        var comparison = new RetailerComparisonView(
                retailerId,
                retailerId.name(),
                RetailerCoverageStatus.CONNECTED,
                RetailerProductionAccessStatus.READY,
                RetailerComparisonStatus.READY,
                List.of(),
                Optional.of(subtotal),
                Optional.of(new RetailerFreshness(
                        RetailerFreshnessBasis.OBSERVATION_ONLY,
                        Instant.parse("2026-08-15T12:00:00Z"),
                        Optional.empty())));
        var economics = new BasketEconomics(
                BasketFee.known(money("0", currency)),
                BasketFee.known(money("0", currency)),
                MinimumOrderConstraint.known(money(minimum, currency)));
        return new RetailerCheckoutAssessmentService().assess(
                comparison,
                new RetailerCheckoutEconomicsEvidence(retailerId, economics));
    }

    private static BasketTotal money(String amount, String currency) {
        return new BasketTotal(new BigDecimal(amount), currency);
    }
}
