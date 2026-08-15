package io.github.trueruslan.zakupgotov.retailercheckout;

import io.github.trueruslan.zakupgotov.basket.BasketEconomicsAssessment;
import io.github.trueruslan.zakupgotov.basket.BasketTotal;
import io.github.trueruslan.zakupgotov.comparison.RetailerComparisonStatus;
import io.github.trueruslan.zakupgotov.comparison.RetailerComparisonView;
import java.util.Objects;
import java.util.Optional;

public record RetailerCheckoutAssessment(
        RetailerComparisonView comparison,
        BasketEconomicsAssessment economicsAssessment,
        RetailerCheckoutEligibilityStatus eligibilityStatus,
        RetailerCheckoutComparabilityStatus comparabilityStatus,
        Optional<BasketTotal> comparableCheckoutTotal) {

    public RetailerCheckoutAssessment {
        comparison = Objects.requireNonNull(comparison, "comparison must not be null");
        economicsAssessment = Objects.requireNonNull(economicsAssessment, "economicsAssessment must not be null");
        eligibilityStatus = Objects.requireNonNull(eligibilityStatus, "eligibilityStatus must not be null");
        comparabilityStatus = Objects.requireNonNull(comparabilityStatus, "comparabilityStatus must not be null");
        comparableCheckoutTotal = Objects.requireNonNull(comparableCheckoutTotal, "comparableCheckoutTotal must not be null");

        if (comparison.comparisonStatus() != RetailerComparisonStatus.READY
                && comparison.comparisonStatus() != RetailerComparisonStatus.UNCERTAIN) {
            throw new IllegalArgumentException("checkout assessment requires READY or UNCERTAIN comparison");
        }

        var comparisonSubtotal = comparison.total().orElseThrow(() ->
                new IllegalArgumentException("checkout assessment requires merchandise subtotal"));
        if (!economicsAssessment.merchandiseSubtotal().equals(comparisonSubtotal)) {
            throw new IllegalArgumentException("economics merchandise subtotal must match retailer comparison total");
        }

        var expectedEligibility = RetailerCheckoutAssessmentService.eligibilityStatus(
                comparison.comparisonStatus(), economicsAssessment.minimumOrderStatus());
        if (eligibilityStatus != expectedEligibility) {
            throw new IllegalArgumentException("eligibility status must match comparison and minimum-order state");
        }

        var expectedComparability = RetailerCheckoutAssessmentService.comparabilityStatus(
                comparison.comparisonStatus(), eligibilityStatus, economicsAssessment.checkoutTotalStatus());
        if (comparabilityStatus != expectedComparability) {
            throw new IllegalArgumentException("comparability status must match comparison, eligibility and checkout-total state");
        }

        var expectedComparableTotal = comparabilityStatus == RetailerCheckoutComparabilityStatus.COMPARABLE
                ? economicsAssessment.checkoutTotal()
                : Optional.<BasketTotal>empty();
        if (!comparableCheckoutTotal.equals(expectedComparableTotal)) {
            throw new IllegalArgumentException("comparable checkout total must match comparability and economics total");
        }
    }
}
