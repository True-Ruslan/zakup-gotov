package io.github.trueruslan.zakupgotov.retailercheckout;

import io.github.trueruslan.zakupgotov.comparison.RetailerComparisonView;
import io.github.trueruslan.zakupgotov.retailer.RetailerId;
import java.util.Objects;
import java.util.Optional;

public record RetailerCheckoutAssessmentResult(
        RetailerComparisonView comparison,
        Optional<RetailerCheckoutAssessment> assessment) {

    public RetailerCheckoutAssessmentResult {
        comparison = Objects.requireNonNull(comparison, "comparison must not be null");
        assessment = Objects.requireNonNull(assessment, "assessment must not be null");

        var shouldHaveAssessment = comparison.total().isPresent();
        if (assessment.isPresent() != shouldHaveAssessment) {
            throw new IllegalArgumentException("checkout assessment presence must match retailer comparison total availability");
        }
        if (assessment.isPresent() && !assessment.orElseThrow().comparison().equals(comparison)) {
            throw new IllegalArgumentException("checkout assessment must reference the same retailer comparison");
        }
    }

    public RetailerId retailerId() {
        return comparison.retailerId();
    }
}
