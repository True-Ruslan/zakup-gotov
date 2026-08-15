package io.github.trueruslan.zakupgotov.retailercheckout;

import io.github.trueruslan.zakupgotov.basket.BasketEconomics;
import io.github.trueruslan.zakupgotov.basket.BasketEconomicsCalculator;
import io.github.trueruslan.zakupgotov.basket.BasketTotal;
import io.github.trueruslan.zakupgotov.basket.CheckoutTotalStatus;
import io.github.trueruslan.zakupgotov.basket.MinimumOrderStatus;
import io.github.trueruslan.zakupgotov.comparison.RetailerComparisonStatus;
import io.github.trueruslan.zakupgotov.comparison.RetailerComparisonView;
import java.util.Objects;
import java.util.Optional;

public final class RetailerCheckoutAssessmentService {

    public RetailerCheckoutAssessmentResult assess(
            RetailerComparisonView comparison,
            RetailerCheckoutEconomicsEvidence economicsEvidence) {
        Objects.requireNonNull(comparison, "comparison must not be null");
        economicsEvidence = Objects.requireNonNull(economicsEvidence, "economicsEvidence must not be null");
        if (economicsEvidence.retailerId() != comparison.retailerId()) {
            throw new IllegalArgumentException("checkout economics retailer must match retailer comparison");
        }
        return assess(comparison, economicsEvidence.economics());
    }

    RetailerCheckoutAssessmentResult assess(
            RetailerComparisonView comparison,
            BasketEconomics economics) {
        Objects.requireNonNull(comparison, "comparison must not be null");
        Objects.requireNonNull(economics, "economics must not be null");

        if (comparison.total().isEmpty()) {
            return new RetailerCheckoutAssessmentResult(comparison, Optional.empty());
        }

        var economicsAssessment = BasketEconomicsCalculator.assess(comparison.total().orElseThrow(), economics);
        var eligibilityStatus = eligibilityStatus(
                comparison.comparisonStatus(), economicsAssessment.minimumOrderStatus());
        var comparabilityStatus = comparabilityStatus(
                comparison.comparisonStatus(), eligibilityStatus, economicsAssessment.checkoutTotalStatus());
        var comparableCheckoutTotal = comparabilityStatus == RetailerCheckoutComparabilityStatus.COMPARABLE
                ? economicsAssessment.checkoutTotal()
                : Optional.<BasketTotal>empty();

        return new RetailerCheckoutAssessmentResult(
                comparison,
                Optional.of(new RetailerCheckoutAssessment(
                        comparison,
                        economicsAssessment,
                        eligibilityStatus,
                        comparabilityStatus,
                        comparableCheckoutTotal)));
    }

    static RetailerCheckoutEligibilityStatus eligibilityStatus(
            RetailerComparisonStatus comparisonStatus,
            MinimumOrderStatus minimumOrderStatus) {
        Objects.requireNonNull(comparisonStatus, "comparisonStatus must not be null");
        Objects.requireNonNull(minimumOrderStatus, "minimumOrderStatus must not be null");

        if (minimumOrderStatus == MinimumOrderStatus.NOT_MET) {
            return RetailerCheckoutEligibilityStatus.INELIGIBLE;
        }
        if (comparisonStatus == RetailerComparisonStatus.UNCERTAIN) {
            return RetailerCheckoutEligibilityStatus.UNKNOWN;
        }
        if (minimumOrderStatus == MinimumOrderStatus.UNKNOWN) {
            return RetailerCheckoutEligibilityStatus.UNKNOWN;
        }
        if (comparisonStatus == RetailerComparisonStatus.READY
                && minimumOrderStatus == MinimumOrderStatus.MET) {
            return RetailerCheckoutEligibilityStatus.ELIGIBLE;
        }
        throw new IllegalArgumentException("eligibility requires assessable READY or UNCERTAIN comparison state");
    }

    static RetailerCheckoutComparabilityStatus comparabilityStatus(
            RetailerComparisonStatus comparisonStatus,
            RetailerCheckoutEligibilityStatus eligibilityStatus,
            CheckoutTotalStatus checkoutTotalStatus) {
        Objects.requireNonNull(comparisonStatus, "comparisonStatus must not be null");
        Objects.requireNonNull(eligibilityStatus, "eligibilityStatus must not be null");
        Objects.requireNonNull(checkoutTotalStatus, "checkoutTotalStatus must not be null");

        return comparisonStatus == RetailerComparisonStatus.READY
                        && eligibilityStatus == RetailerCheckoutEligibilityStatus.ELIGIBLE
                        && checkoutTotalStatus == CheckoutTotalStatus.KNOWN
                ? RetailerCheckoutComparabilityStatus.COMPARABLE
                : RetailerCheckoutComparabilityStatus.NOT_COMPARABLE;
    }
}
