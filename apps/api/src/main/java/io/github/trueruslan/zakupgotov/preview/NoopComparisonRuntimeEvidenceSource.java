package io.github.trueruslan.zakupgotov.preview;

import io.github.trueruslan.zakupgotov.location.ProductLocation;
import io.github.trueruslan.zakupgotov.shopping.ShoppingList;
import java.util.Objects;

public final class NoopComparisonRuntimeEvidenceSource implements ComparisonRuntimeEvidenceSource {

    @Override
    public ComparisonRuntimeEvidence load(ShoppingList shoppingList, ProductLocation productLocation) {
        Objects.requireNonNull(shoppingList, "shoppingList must not be null");
        Objects.requireNonNull(productLocation, "productLocation must not be null");
        return ComparisonRuntimeEvidence.empty();
    }
}
