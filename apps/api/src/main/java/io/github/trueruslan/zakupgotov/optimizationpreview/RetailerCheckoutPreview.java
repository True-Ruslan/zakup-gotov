package io.github.trueruslan.zakupgotov.optimizationpreview;

import io.github.trueruslan.zakupgotov.retailer.RetailerId;
import io.github.trueruslan.zakupgotov.retailercheckout.RetailerCheckoutAssessmentResult;
import java.util.Objects;
import java.util.Optional;

public record RetailerCheckoutPreview(
        RetailerId retailerId,
        Optional<RetailerCheckoutAssessmentPreview> assessment) {

    public RetailerCheckoutPreview {
        retailerId = Objects.requireNonNull(retailerId, "retailerId must not be null");
        assessment = Objects.requireNonNull(assessment, "assessment must not be null");
    }

    public static RetailerCheckoutPreview from(RetailerCheckoutAssessmentResult result) {
        result = Objects.requireNonNull(result, "result must not be null");
        return new RetailerCheckoutPreview(
                result.retailerId(),
                result.assessment().map(RetailerCheckoutAssessmentPreview::from));
    }
}
