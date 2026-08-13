package io.github.trueruslan.zakupgotov.recipepreview;

import io.github.trueruslan.zakupgotov.shopping.QuantityUnit;
import java.math.BigDecimal;

public record RecipeShoppingPreviewQuantityRequest(
        BigDecimal amount,
        QuantityUnit unit) {}
