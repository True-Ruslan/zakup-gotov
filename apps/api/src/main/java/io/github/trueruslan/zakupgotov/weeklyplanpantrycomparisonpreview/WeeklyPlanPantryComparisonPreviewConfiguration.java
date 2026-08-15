package io.github.trueruslan.zakupgotov.weeklyplanpantrycomparisonpreview;

import io.github.trueruslan.zakupgotov.preview.ComparisonPreviewService;
import io.github.trueruslan.zakupgotov.weeklyplanpantrypreview.WeeklyPlanPantryShoppingPreviewService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class WeeklyPlanPantryComparisonPreviewConfiguration {

    @Bean
    WeeklyPlanPantryComparisonPreviewService weeklyPlanPantryComparisonPreviewService(
            WeeklyPlanPantryShoppingPreviewService weeklyPlanPantryShoppingPreviewService,
            ComparisonPreviewService comparisonPreviewService) {
        return new WeeklyPlanPantryComparisonPreviewService(
                weeklyPlanPantryShoppingPreviewService,
                comparisonPreviewService);
    }
}
