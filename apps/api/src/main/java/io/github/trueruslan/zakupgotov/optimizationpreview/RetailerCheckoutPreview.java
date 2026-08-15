package io.github.trueruslan.zakupgotov.optimizationpreview;

import io.github.trueruslan.zakupgotov.retailercheckout.RetailerCheckoutAssessmentResult;
import java.util.Objects;
import java.util.Optional;

public record RetailerCheckoutPreview(
        String retailerId,
        Optional<RetailerCheckoutAssessmentPreview> assessment) {

    public RetailerCheckoutPreview {
        if (retailerId == null || retailerId.isBlank()) {
            throw new IllegalArgumentException("retailerId must not be blank");
        }
        assessment = Objects.requireNonNull(assessment, "assessment must not be null");
    }

    public static RetailerCheckoutPreview from(RetailerCheckoutAssessmentResult result) {
        result = Objects.requireNonNull(result, "result must not be null");
        return new RetailerCheckoutPreview(
                result.retailerId().canonicalId(),
                result.assessment().map(RetailerCheckoutAssessmentPreview::from));
    }
}
