package io.github.trueruslan.zakupgotov.preview;

import io.github.trueruslan.zakupgotov.location.ProductLocation;
import io.github.trueruslan.zakupgotov.shopping.ShoppingList;
import java.util.Objects;

public record ComparisonPreviewInput(
        ShoppingList shoppingList,
        ProductLocation productLocation) {

    public ComparisonPreviewInput {
        shoppingList = Objects.requireNonNull(shoppingList, "shoppingList must not be null");
        productLocation = Objects.requireNonNull(productLocation, "productLocation must not be null");
    }
}
