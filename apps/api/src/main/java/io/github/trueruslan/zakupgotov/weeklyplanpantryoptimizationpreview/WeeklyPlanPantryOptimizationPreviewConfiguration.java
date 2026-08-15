package io.github.trueruslan.zakupgotov.weeklyplanpantryoptimizationpreview;

import io.github.trueruslan.zakupgotov.basketoptimization.BasketOptimizer;
import io.github.trueruslan.zakupgotov.optimizationpreview.CheckoutEconomicsEvidenceSource;
import io.github.trueruslan.zakupgotov.optimizationpreview.CheckoutOptimizationPreviewService;
import io.github.trueruslan.zakupgotov.optimizationpreview.NoopCheckoutEconomicsEvidenceSource;
import io.github.trueruslan.zakupgotov.retailercheckout.RetailerCheckoutAssessmentService;
import io.github.trueruslan.zakupgotov.weeklyplanpantrycomparisonpreview.WeeklyPlanPantryComparisonPreviewService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class WeeklyPlanPantryOptimizationPreviewConfiguration {

    @Bean
    CheckoutEconomicsEvidenceSource checkoutEconomicsEvidenceSource() {
        return new NoopCheckoutEconomicsEvidenceSource();
    }

    @Bean
    RetailerCheckoutAssessmentService retailerCheckoutAssessmentService() {
        return new RetailerCheckoutAssessmentService();
    }

    @Bean
    BasketOptimizer basketOptimizer() {
        return new BasketOptimizer();
    }

    @Bean
    CheckoutOptimizationPreviewService checkoutOptimizationPreviewService(
            CheckoutEconomicsEvidenceSource checkoutEconomicsEvidenceSource,
            RetailerCheckoutAssessmentService retailerCheckoutAssessmentService,
            BasketOptimizer basketOptimizer) {
        return new CheckoutOptimizationPreviewService(
                checkoutEconomicsEvidenceSource,
                retailerCheckoutAssessmentService,
                basketOptimizer);
    }

    @Bean
    WeeklyPlanPantryOptimizationPreviewService weeklyPlanPantryOptimizationPreviewService(
            WeeklyPlanPantryComparisonPreviewService weeklyPlanPantryComparisonPreviewService,
            CheckoutOptimizationPreviewService checkoutOptimizationPreviewService) {
        return new WeeklyPlanPantryOptimizationPreviewService(
                weeklyPlanPantryComparisonPreviewService,
                checkoutOptimizationPreviewService);
    }
}
