package io.github.trueruslan.zakupgotov.recipepreview;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import io.github.trueruslan.zakupgotov.shopping.QuantityUnit;
import java.math.BigDecimal;

public record RecipeShoppingPreviewQuantityRequest(
        BigDecimal amount,
        QuantityUnit unit) {

    @JsonAnySetter
    public void rejectUnknownProperty(String property, Object value) {
        throw new IllegalArgumentException("Unknown recipe shopping preview property");
    }
}
