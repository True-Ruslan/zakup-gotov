package io.github.trueruslan.zakupgotov.retailercheckout;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.trueruslan.zakupgotov.basket.BasketEconomics;
import io.github.trueruslan.zakupgotov.basket.BasketEconomicsKnowledgeStatus;
import io.github.trueruslan.zakupgotov.basket.BasketFee;
import io.github.trueruslan.zakupgotov.basket.BasketTotal;
import io.github.trueruslan.zakupgotov.basket.CheckoutTotalStatus;
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

class RetailerCheckoutAssessmentServiceTest {

    private final RetailerCheckoutAssessmentService service = new RetailerCheckoutAssessmentService();

    @Test
    void readyKnownEconomicsAndMetMinimumIsEligibleAndComparable() {
        var subtotal = money("1200.00");
        var result = service.assess(ready(subtotal), economics("149.00", "39.00", "1000.00"));

        var assessment = result.assessment().orElseThrow();
        assertThat(assessment.eligibilityStatus()).isEqualTo(RetailerCheckoutEligibilityStatus.ELIGIBLE);
        assertThat(assessment.economicsAssessment().checkoutTotalStatus()).isEqualTo(CheckoutTotalStatus.KNOWN);
        assertThat(assessment.economicsAssessment().checkoutTotal()).contains(money("1388.00"));
        assertThat(assessment.comparabilityStatus()).isEqualTo(RetailerCheckoutComparabilityStatus.COMPARABLE);
        assertThat(assessment.comparableCheckoutTotal()).contains(money("1388.00"));
    }

    @Test
    void readyKnownEconomicsAndUnmetMinimumIsIneligibleButArithmeticTotalRemainsInspectable() {
        var subtotal = money("950.00");
        var result = service.assess(ready(subtotal), economics("100.00", "50.00", "1000.00"));

        var assessment = result.assessment().orElseThrow();
        assertThat(assessment.eligibilityStatus()).isEqualTo(RetailerCheckoutEligibilityStatus.INELIGIBLE);
        assertThat(assessment.economicsAssessment().checkoutTotal()).contains(money("1100.00"));
        assertThat(assessment.comparabilityStatus()).isEqualTo(RetailerCheckoutComparabilityStatus.NOT_COMPARABLE);
        assertThat(assessment.comparableCheckoutTotal()).isEmpty();
    }

    @Test
    void readyUnknownMinimumHasUnknownEligibilityAndIsNotComparable() {
        var subtotal = money("1200.00");
        var economics = new BasketEconomics(
                BasketFee.known(money("100.00")),
                BasketFee.known(money("20.00")),
                MinimumOrderConstraint.unknown());

        var assessment = service.assess(ready(subtotal), economics).assessment().orElseThrow();

        assertThat(assessment.eligibilityStatus()).isEqualTo(RetailerCheckoutEligibilityStatus.UNKNOWN);
        assertThat(assessment.economicsAssessment().checkoutTotal()).contains(money("1320.00"));
        assertThat(assessment.comparabilityStatus()).isEqualTo(RetailerCheckoutComparabilityStatus.NOT_COMPARABLE);
        assertThat(assessment.comparableCheckoutTotal()).isEmpty();
    }

    @Test
    void readyMetMinimumWithUnknownMaterialFeeCanBeEligibleButNotComparable() {
        var subtotal = money("1200.00");
        var economics = new BasketEconomics(
                BasketFee.unknown(),
                BasketFee.known(money("20.00")),
                MinimumOrderConstraint.known(money("1000.00")));

        var assessment = service.assess(ready(subtotal), economics).assessment().orElseThrow();

        assertThat(assessment.eligibilityStatus()).isEqualTo(RetailerCheckoutEligibilityStatus.ELIGIBLE);
        assertThat(assessment.economicsAssessment().checkoutTotalStatus()).isEqualTo(CheckoutTotalStatus.UNKNOWN);
        assertThat(assessment.economicsAssessment().checkoutTotal()).isEmpty();
        assertThat(assessment.comparabilityStatus()).isEqualTo(RetailerCheckoutComparabilityStatus.NOT_COMPARABLE);
        assertThat(assessment.comparableCheckoutTotal()).isEmpty();
    }

    @Test
    void uncertainBasketIsNeverUpgradedToEligibleOrComparable() {
        var subtotal = money("1200.00");
        var assessment = service.assess(uncertain(subtotal), economics("100.00", "20.00", "1000.00"))
                .assessment()
                .orElseThrow();

        assertThat(assessment.eligibilityStatus()).isEqualTo(RetailerCheckoutEligibilityStatus.UNKNOWN);
        assertThat(assessment.economicsAssessment().checkoutTotal()).contains(money("1320.00"));
        assertThat(assessment.comparabilityStatus()).isEqualTo(RetailerCheckoutComparabilityStatus.NOT_COMPARABLE);
        assertThat(assessment.comparableCheckoutTotal()).isEmpty();
    }

    @Test
    void knownUnmetMinimumStillProvesIneligibilityForUncertainBasket() {
        var subtotal = money("900.00");
        var assessment = service.assess(uncertain(subtotal), economics("100.00", "20.00", "1000.00"))
                .assessment()
                .orElseThrow();

        assertThat(assessment.eligibilityStatus()).isEqualTo(RetailerCheckoutEligibilityStatus.INELIGIBLE);
        assertThat(assessment.comparabilityStatus()).isEqualTo(RetailerCheckoutComparabilityStatus.NOT_COMPARABLE);
    }

    @Test
    void incompleteAndUnavailableComparisonsDoNotFabricateCheckoutAssessment() {
        var economics = economics("100.00", "20.00", "1000.00");

        assertThat(service.assess(incomplete(), economics).assessment()).isEmpty();
        assertThat(service.assess(unavailable(), economics).assessment()).isEmpty();
    }

    @Test
    void knownZeroFeesRemainKnownAndComparableWhenMinimumIsMet() {
        var subtotal = money("499.90");
        var economics = new BasketEconomics(
                BasketFee.known(money("0")),
                BasketFee.known(money("0.00")),
                MinimumOrderConstraint.known(money("0")));

        var assessment = service.assess(ready(subtotal), economics).assessment().orElseThrow();

        assertThat(assessment.economicsAssessment().economics().deliveryFee().status())
                .isEqualTo(BasketEconomicsKnowledgeStatus.KNOWN);
        assertThat(assessment.economicsAssessment().economics().serviceFee().status())
                .isEqualTo(BasketEconomicsKnowledgeStatus.KNOWN);
        assertThat(assessment.economicsAssessment().checkoutTotal()).contains(subtotal);
        assertThat(assessment.comparableCheckoutTotal()).contains(subtotal);
    }

    private static BasketEconomics economics(String delivery, String service, String minimum) {
        return new BasketEconomics(
                BasketFee.known(money(delivery)),
                BasketFee.known(money(service)),
                MinimumOrderConstraint.known(money(minimum)));
    }

    private static RetailerComparisonView ready(BasketTotal subtotal) {
        return new RetailerComparisonView(
                RetailerId.PYATEROCHKA,
                "Пятёрочка",
                RetailerCoverageStatus.CONNECTED,
                RetailerProductionAccessStatus.READY,
                RetailerComparisonStatus.READY,
                List.of(),
                Optional.of(subtotal),
                Optional.of(freshness()));
    }

    private static RetailerComparisonView uncertain(BasketTotal subtotal) {
        return new RetailerComparisonView(
                RetailerId.PYATEROCHKA,
                "Пятёрочка",
                RetailerCoverageStatus.CONNECTED,
                RetailerProductionAccessStatus.READY,
                RetailerComparisonStatus.UNCERTAIN,
                List.of(RetailerComparisonReason.AVAILABILITY_UNKNOWN),
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

    private static RetailerComparisonView unavailable() {
        return new RetailerComparisonView(
                RetailerId.PYATEROCHKA,
                "Пятёрочка",
                RetailerCoverageStatus.DISCOVERY,
                RetailerProductionAccessStatus.PENDING,
                RetailerComparisonStatus.UNAVAILABLE,
                List.of(RetailerComparisonReason.COVERAGE_DISCOVERY),
                Optional.empty(),
                Optional.empty());
    }

    private static RetailerFreshness freshness() {
        return new RetailerFreshness(
                RetailerFreshnessBasis.OBSERVATION_ONLY,
                Instant.parse("2026-08-15T12:00:00Z"),
                Optional.empty());
    }

    private static BasketTotal money(String amount) {
        return new BasketTotal(new BigDecimal(amount), "RUB");
    }
}
