package io.github.trueruslan.zakupgotov.recipecomparisonpreview;

import io.github.trueruslan.zakupgotov.preview.ComparisonPreview;
import io.github.trueruslan.zakupgotov.recipepreview.RecipeShoppingPreview;
import java.util.Objects;

public record RecipeComparisonPreview(
        RecipeShoppingPreview recipeShoppingPreview,
        ComparisonPreview comparisonPreview) {

    public RecipeComparisonPreview {
        recipeShoppingPreview = Objects.requireNonNull(
                recipeShoppingPreview,
                "recipeShoppingPreview must not be null");
        comparisonPreview = Objects.requireNonNull(
                comparisonPreview,
                "comparisonPreview must not be null");
    }
}
