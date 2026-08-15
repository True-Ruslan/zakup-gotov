package io.github.trueruslan.zakupgotov.weeklyplanpantrypreview;

import io.github.trueruslan.zakupgotov.weeklyplanpreview.WeeklyPlanShoppingPreviewService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class WeeklyPlanPantryShoppingPreviewConfiguration {

    @Bean
    WeeklyPlanPantryShoppingPreviewService weeklyPlanPantryShoppingPreviewService(
            WeeklyPlanShoppingPreviewService weeklyPlanShoppingPreviewService) {
        return new WeeklyPlanPantryShoppingPreviewService(weeklyPlanShoppingPreviewService);
    }
}
