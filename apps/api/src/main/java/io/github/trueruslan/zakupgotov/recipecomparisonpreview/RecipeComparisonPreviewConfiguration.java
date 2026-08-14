package io.github.trueruslan.zakupgotov.recipecomparisonpreview;

import io.github.trueruslan.zakupgotov.preview.ComparisonPreviewService;
import io.github.trueruslan.zakupgotov.recipepreview.RecipeShoppingPreviewService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class RecipeComparisonPreviewConfiguration {

    @Bean
    RecipeComparisonPreviewService recipeComparisonPreviewService(
            RecipeShoppingPreviewService recipeShoppingPreviewService,
            ComparisonPreviewService comparisonPreviewService) {
        return new RecipeComparisonPreviewService(
                recipeShoppingPreviewService,
                comparisonPreviewService);
    }
}
