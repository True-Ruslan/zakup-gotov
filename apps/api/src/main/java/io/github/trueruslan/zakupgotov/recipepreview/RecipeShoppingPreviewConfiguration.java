package io.github.trueruslan.zakupgotov.recipepreview;

import io.github.trueruslan.zakupgotov.recipe.RecipeShoppingListConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class RecipeShoppingPreviewConfiguration {

    @Bean
    RecipeShoppingPreviewIdGenerator recipeShoppingPreviewIdGenerator() {
        return new UuidRecipeShoppingPreviewIdGenerator();
    }

    @Bean
    RecipeShoppingListConverter recipeShoppingListConverter() {
        return new RecipeShoppingListConverter();
    }

    @Bean
    RecipeShoppingPreviewRequestFactory recipeShoppingPreviewRequestFactory(
            RecipeShoppingPreviewIdGenerator ids) {
        return new RecipeShoppingPreviewRequestFactory(ids);
    }

    @Bean
    RecipeShoppingPreviewService recipeShoppingPreviewService(
            RecipeShoppingPreviewRequestFactory factory,
            RecipeShoppingListConverter converter) {
        return new RecipeShoppingPreviewService(factory, converter);
    }
}
