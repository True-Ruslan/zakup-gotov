package io.github.trueruslan.zakupgotov.recipepreview;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.trueruslan.zakupgotov.preview.ComparisonPreviewExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.core.SpringVersion;
import org.springframework.web.method.annotation.ExceptionHandlerMethodResolver;

class RecipeShoppingPreviewExceptionHandlerResolverTest {

    @Test
    void resolvesExistingComparisonAdviceOnExpectedSpringFrameworkVersion() {
        assertThat(SpringVersion.getVersion()).isEqualTo("7.0.8");
        var resolver = new ExceptionHandlerMethodResolver(ComparisonPreviewExceptionHandler.class);
        assertThat(resolver.hasExceptionMappings()).isTrue();
    }

    @Test
    void resolvesRecipeAdviceOnExpectedSpringFrameworkVersion() {
        assertThat(SpringVersion.getVersion()).isEqualTo("7.0.8");
        var resolver = new ExceptionHandlerMethodResolver(RecipeShoppingPreviewExceptionHandler.class);
        assertThat(resolver.hasExceptionMappings()).isTrue();
    }
}
