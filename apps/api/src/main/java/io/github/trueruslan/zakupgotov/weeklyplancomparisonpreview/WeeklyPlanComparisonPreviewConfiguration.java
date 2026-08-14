package io.github.trueruslan.zakupgotov.weeklyplancomparisonpreview;

import io.github.trueruslan.zakupgotov.preview.ComparisonPreviewService;
import io.github.trueruslan.zakupgotov.weeklyplanpreview.WeeklyPlanShoppingPreviewService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class WeeklyPlanComparisonPreviewConfiguration {

    @Bean
    WeeklyPlanComparisonPreviewService weeklyPlanComparisonPreviewService(
            WeeklyPlanShoppingPreviewService weeklyPlanShoppingPreviewService,
            ComparisonPreviewService comparisonPreviewService) {
        return new WeeklyPlanComparisonPreviewService(
                weeklyPlanShoppingPreviewService,
                comparisonPreviewService);
    }
}
