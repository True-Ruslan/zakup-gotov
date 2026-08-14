package io.github.trueruslan.zakupgotov.weeklyplanpreview;

import io.github.trueruslan.zakupgotov.recipepreview.RecipeShoppingPreviewRequestFactory;
import io.github.trueruslan.zakupgotov.weeklyplan.WeeklyPlanShoppingListComposer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class WeeklyPlanShoppingPreviewConfiguration {

    @Bean
    WeeklyPlanShoppingPreviewIdGenerator weeklyPlanShoppingPreviewIdGenerator() {
        return new RandomWeeklyPlanShoppingPreviewIdGenerator();
    }

    @Bean
    WeeklyPlanShoppingPreviewRequestFactory weeklyPlanShoppingPreviewRequestFactory(
            WeeklyPlanShoppingPreviewIdGenerator ids,
            RecipeShoppingPreviewRequestFactory recipeFactory) {
        return new WeeklyPlanShoppingPreviewRequestFactory(ids, recipeFactory);
    }

    @Bean
    WeeklyPlanShoppingListComposer weeklyPlanShoppingListComposer() {
        return new WeeklyPlanShoppingListComposer();
    }

    @Bean
    WeeklyPlanShoppingPreviewService weeklyPlanShoppingPreviewService(
            WeeklyPlanShoppingPreviewRequestFactory factory,
            WeeklyPlanShoppingListComposer composer) {
        return new WeeklyPlanShoppingPreviewService(factory, composer);
    }
}
