package io.github.trueruslan.zakupgotov.recipepreview;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.core.SpringVersion;
import org.springframework.web.method.annotation.ExceptionHandlerMethodResolver;

class RecipeShoppingPreviewExceptionHandlerResolverTest {

    @Test
    void resolvesAdviceOnExpectedSpringFrameworkVersion() {
        assertThat(SpringVersion.getVersion()).isEqualTo("7.0.8");
        var resolver = new ExceptionHandlerMethodResolver(RecipeShoppingPreviewExceptionHandler.class);
        assertThat(resolver.hasExceptionMappings()).isTrue();
    }
}
