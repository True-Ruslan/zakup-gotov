package io.github.trueruslan.zakupgotov.preview;

import io.github.trueruslan.zakupgotov.location.ProductLocation;
import io.github.trueruslan.zakupgotov.retailer.RetailerId;
import io.github.trueruslan.zakupgotov.shopping.ShoppingList;
import java.util.Objects;
import java.util.Set;

public final class NoopComparisonRuntimeEvidenceSource implements ComparisonRuntimeEvidenceSource {

    @Override
    public ComparisonRuntimeEvidence load(
            ShoppingList shoppingList,
            ProductLocation productLocation,
            Set<RetailerId> requestedRetailers) {
        Objects.requireNonNull(shoppingList, "shoppingList must not be null");
        Objects.requireNonNull(productLocation, "productLocation must not be null");
        Objects.requireNonNull(requestedRetailers, "requestedRetailers must not be null");
        return ComparisonRuntimeEvidence.empty();
    }
}
