package io.github.trueruslan.zakupgotov.optimizationpreview;

import io.github.trueruslan.zakupgotov.basket.BasketFee;
import io.github.trueruslan.zakupgotov.basket.BasketTotal;
import io.github.trueruslan.zakupgotov.basket.CheckoutTotalStatus;
import io.github.trueruslan.zakupgotov.basket.MinimumOrderConstraint;
import io.github.trueruslan.zakupgotov.basket.MinimumOrderStatus;
import io.github.trueruslan.zakupgotov.retailercheckout.RetailerCheckoutAssessment;
import io.github.trueruslan.zakupgotov.retailercheckout.RetailerCheckoutComparabilityStatus;
import io.github.trueruslan.zakupgotov.retailercheckout.RetailerCheckoutEligibilityStatus;
import java.util.Objects;
import java.util.Optional;

public record RetailerCheckoutAssessmentPreview(
        BasketTotal merchandiseSubtotal,
        BasketFee deliveryFee,
        BasketFee serviceFee,
        MinimumOrderConstraint minimumOrder,
        MinimumOrderStatus minimumOrderStatus,
        CheckoutTotalStatus checkoutTotalStatus,
        Optional<BasketTotal> checkoutTotal,
        RetailerCheckoutEligibilityStatus eligibilityStatus,
        RetailerCheckoutComparabilityStatus comparabilityStatus,
        Optional<BasketTotal> comparableCheckoutTotal) {

    public RetailerCheckoutAssessmentPreview {
        merchandiseSubtotal = Objects.requireNonNull(merchandiseSubtotal, "merchandiseSubtotal must not be null");
        deliveryFee = Objects.requireNonNull(deliveryFee, "deliveryFee must not be null");
        serviceFee = Objects.requireNonNull(serviceFee, "serviceFee must not be null");
        minimumOrder = Objects.requireNonNull(minimumOrder, "minimumOrder must not be null");
        minimumOrderStatus = Objects.requireNonNull(minimumOrderStatus, "minimumOrderStatus must not be null");
        checkoutTotalStatus = Objects.requireNonNull(checkoutTotalStatus, "checkoutTotalStatus must not be null");
        checkoutTotal = Objects.requireNonNull(checkoutTotal, "checkoutTotal must not be null");
        eligibilityStatus = Objects.requireNonNull(eligibilityStatus, "eligibilityStatus must not be null");
        comparabilityStatus = Objects.requireNonNull(comparabilityStatus, "comparabilityStatus must not be null");
        comparableCheckoutTotal = Objects.requireNonNull(
                comparableCheckoutTotal,
                "comparableCheckoutTotal must not be null");

        if (checkoutTotalStatus == CheckoutTotalStatus.KNOWN && checkoutTotal.isEmpty()) {
            throw new IllegalArgumentException("KNOWN checkout total requires amount");
        }
        if (checkoutTotalStatus == CheckoutTotalStatus.UNKNOWN && checkoutTotal.isPresent()) {
            throw new IllegalArgumentException("UNKNOWN checkout total must not carry amount");
        }
        if (comparabilityStatus == RetailerCheckoutComparabilityStatus.COMPARABLE
                && comparableCheckoutTotal.isEmpty()) {
            throw new IllegalArgumentException("COMPARABLE checkout requires comparable total");
        }
        if (comparabilityStatus == RetailerCheckoutComparabilityStatus.NOT_COMPARABLE
                && comparableCheckoutTotal.isPresent()) {
            throw new IllegalArgumentException("NOT_COMPARABLE checkout must not carry comparable total");
        }
    }

    public static RetailerCheckoutAssessmentPreview from(RetailerCheckoutAssessment assessment) {
        assessment = Objects.requireNonNull(assessment, "assessment must not be null");
        var economics = assessment.economicsAssessment();
        return new RetailerCheckoutAssessmentPreview(
                economics.merchandiseSubtotal(),
                economics.economics().deliveryFee(),
                economics.economics().serviceFee(),
                economics.economics().minimumOrder(),
                economics.minimumOrderStatus(),
                economics.checkoutTotalStatus(),
                economics.checkoutTotal(),
                assessment.eligibilityStatus(),
                assessment.comparabilityStatus(),
                assessment.comparableCheckoutTotal());
    }
}
