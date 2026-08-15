package io.github.trueruslan.zakupgotov.basketoptimization;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.trueruslan.zakupgotov.basket.BasketEconomics;
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
import io.github.trueruslan.zakupgotov.retailercheckout.RetailerCheckoutAssessmentResult;
import io.github.trueruslan.zakupgotov.retailercheckout.RetailerCheckoutAssessmentService;
import io.github.trueruslan.zakupgotov.retailercheckout.RetailerCheckoutEconomicsEvidence;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class BasketOptimizerTest {

    private final BasketOptimizer optimizer = new BasketOptimizer();

    @Test
    void selectsUniqueLowestComparableCandidateWithoutSortingOriginalCandidates() {
        var expensive = comparable(RetailerId.PYATEROCHKA, "1200.00", "2026-08-15T12:00:00Z");
        var winner = comparable(RetailerId.PEREKRESTOK, "950.00", "2026-08-15T11:00:00Z");
        var middle = comparable(RetailerId.MAGNIT, "1100.00", "2026-08-15T13:00:00Z");
        var input = List.of(expensive, winner, middle);

        var result = optimizer.optimize(input);

        assertThat(result.status()).isEqualTo(BasketOptimizationStatus.UNIQUE_WINNER);
        assertThat(result.candidates()).containsExactlyElementsOf(input);
        assertThat(result.optimalCandidates()).containsExactly(winner);
        assertThat(result.lowestComparableCheckoutTotal()).contains(money("950.00", "RUB"));
    }

    @Test
    void numericEqualMinimumsAreExplicitTieEvenWhenDecimalScaleDiffers() {
        var first = comparable(RetailerId.PYATEROCHKA, "1000.0", "2026-08-15T12:00:00Z");
        var second = comparable(RetailerId.PEREKRESTOK, "1000.00", "2026-08-15T13:00:00Z");
        var higher = comparable(RetailerId.MAGNIT, "1001.00", "2026-08-15T14:00:00Z");

        var result = optimizer.optimize(List.of(first, second, higher));

        assertThat(result.status()).isEqualTo(BasketOptimizationStatus.TIE);
        assertThat(result.optimalCandidates()).containsExactly(first, second);
        assertThat(result.lowestComparableCheckoutTotal()).contains(money("1000.0", "RUB"));
    }

    @Test
    void nonEmptyVisibleSetWithNoComparableCandidatesIsExplicit() {
        var ineligible = ineligible(RetailerId.PYATEROCHKA, "500.00", "1000.00", "RUB");
        var uncertain = uncertain(RetailerId.PEREKRESTOK, "450.00", "RUB", "2026-08-15T12:00:00Z");
        var incomplete = incomplete(RetailerId.MAGNIT);

        var result = optimizer.optimize(List.of(ineligible, uncertain, incomplete));

        assertThat(result.status()).isEqualTo(BasketOptimizationStatus.NO_COMPARABLE_CANDIDATES);
        assertThat(result.optimalCandidates()).isEmpty();
        assertThat(result.lowestComparableCheckoutTotal()).isEmpty();
    }

    @Test
    void cheaperIneligibleCandidateNeverBeatsComparableCandidate() {
        var ineligible = ineligible(RetailerId.PYATEROCHKA, "300.00", "1000.00", "RUB");
        var comparable = comparable(RetailerId.PEREKRESTOK, "700.00", "2026-08-15T12:00:00Z");

        var result = optimizer.optimize(List.of(ineligible, comparable));

        assertThat(result.status()).isEqualTo(BasketOptimizationStatus.UNIQUE_WINNER);
        assertThat(result.optimalCandidates()).containsExactly(comparable);
    }

    @Test
    void cheaperUncertainArithmeticTotalNeverBeatsComparableCandidate() {
        var uncertain = uncertain(RetailerId.PYATEROCHKA, "250.00", "RUB", "2026-08-15T14:00:00Z");
        var comparable = comparable(RetailerId.PEREKRESTOK, "700.00", "2026-08-15T10:00:00Z");

        var result = optimizer.optimize(List.of(uncertain, comparable));

        assertThat(result.status()).isEqualTo(BasketOptimizationStatus.UNIQUE_WINNER);
        assertThat(result.optimalCandidates()).containsExactly(comparable);
    }

    @Test
    void mixedCurrenciesAmongComparableCandidatesFailClosed() {
        var rub = comparable(RetailerId.PYATEROCHKA, "700.00", "RUB", "2026-08-15T12:00:00Z");
        var usd = comparable(RetailerId.PEREKRESTOK, "10.00", "USD", "2026-08-15T12:00:00Z");

        assertThatThrownBy(() -> optimizer.optimize(List.of(rub, usd)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("currency");
    }

    @Test
    void differentCurrencyNonComparableCandidateDoesNotPoisonValidWinner() {
        var usdIneligible = ineligible(RetailerId.PYATEROCHKA, "10.00", "100.00", "USD");
        var rubWinner = comparable(RetailerId.PEREKRESTOK, "700.00", "RUB", "2026-08-15T12:00:00Z");

        var result = optimizer.optimize(List.of(usdIneligible, rubWinner));

        assertThat(result.status()).isEqualTo(BasketOptimizationStatus.UNIQUE_WINNER);
        assertThat(result.optimalCandidates()).containsExactly(rubWinner);
    }

    @Test
    void freshnessDifferencesNeverBreakExactMonetaryTie() {
        var older = comparable(RetailerId.PYATEROCHKA, "800.00", "2026-08-15T08:00:00Z");
        var newer = comparable(RetailerId.PEREKRESTOK, "800.00", "2026-08-15T18:00:00Z");

        var result = optimizer.optimize(List.of(older, newer));

        assertThat(result.status()).isEqualTo(BasketOptimizationStatus.TIE);
        assertThat(result.optimalCandidates()).containsExactly(older, newer);
    }

    private static RetailerCheckoutAssessmentResult comparable(
            RetailerId retailerId,
            String amount,
            String observedAt) {
        return comparable(retailerId, amount, "RUB", observedAt);
    }

    private static RetailerCheckoutAssessmentResult comparable(
            RetailerId retailerId,
            String amount,
            String currency,
            String observedAt) {
        var subtotal = money(amount, currency);
        return new RetailerCheckoutAssessmentService().assess(
                ready(retailerId, subtotal, observedAt),
                new RetailerCheckoutEconomicsEvidence(
                        retailerId,
                        new BasketEconomics(
                                BasketFee.known(money("0", currency)),
                                BasketFee.known(money("0", currency)),
                                MinimumOrderConstraint.known(money("0", currency)))));
    }

    private static RetailerCheckoutAssessmentResult ineligible(
            RetailerId retailerId,
            String amount,
            String minimum,
            String currency) {
        var subtotal = money(amount, currency);
        return new RetailerCheckoutAssessmentService().assess(
                ready(retailerId, subtotal, "2026-08-15T12:00:00Z"),
                new RetailerCheckoutEconomicsEvidence(
                        retailerId,
                        new BasketEconomics(
                                BasketFee.known(money("0", currency)),
                                BasketFee.known(money("0", currency)),
                                MinimumOrderConstraint.known(money(minimum, currency)))));
    }

    private static RetailerCheckoutAssessmentResult uncertain(
            RetailerId retailerId,
            String amount,
            String currency,
            String observedAt) {
        var subtotal = money(amount, currency);
        return new RetailerCheckoutAssessmentService().assess(
                new RetailerComparisonView(
                        retailerId,
                        displayName(retailerId),
                        RetailerCoverageStatus.CONNECTED,
                        RetailerProductionAccessStatus.READY,
                        RetailerComparisonStatus.UNCERTAIN,
                        List.of(RetailerComparisonReason.AVAILABILITY_UNKNOWN),
                        Optional.of(subtotal),
                        Optional.of(freshness(observedAt))),
                new RetailerCheckoutEconomicsEvidence(
                        retailerId,
                        new BasketEconomics(
                                BasketFee.known(money("0", currency)),
                                BasketFee.known(money("0", currency)),
                                MinimumOrderConstraint.known(money("0", currency)))));
    }

    private static RetailerCheckoutAssessmentResult incomplete(RetailerId retailerId) {
        return new RetailerCheckoutAssessmentResult(
                new RetailerComparisonView(
                        retailerId,
                        displayName(retailerId),
                        RetailerCoverageStatus.CONNECTED,
                        RetailerProductionAccessStatus.READY,
                        RetailerComparisonStatus.INCOMPLETE,
                        List.of(RetailerComparisonReason.ITEM_UNMATCHED),
                        Optional.empty(),
                        Optional.empty()),
                Optional.empty());
    }

    private static RetailerComparisonView ready(RetailerId retailerId, BasketTotal subtotal, String observedAt) {
        return new RetailerComparisonView(
                retailerId,
                displayName(retailerId),
                RetailerCoverageStatus.CONNECTED,
                RetailerProductionAccessStatus.READY,
                RetailerComparisonStatus.READY,
                List.of(),
                Optional.of(subtotal),
                Optional.of(freshness(observedAt)));
    }

    private static RetailerFreshness freshness(String observedAt) {
        return new RetailerFreshness(
                RetailerFreshnessBasis.OBSERVATION_ONLY,
                Instant.parse(observedAt),
                Optional.empty());
    }

    private static String displayName(RetailerId retailerId) {
        return retailerId.name();
    }

    private static BasketTotal money(String amount, String currency) {
        return new BasketTotal(new BigDecimal(amount), currency);
    }
}
