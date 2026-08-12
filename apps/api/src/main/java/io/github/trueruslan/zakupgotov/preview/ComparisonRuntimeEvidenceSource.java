package io.github.trueruslan.zakupgotov.preview;

import io.github.trueruslan.zakupgotov.location.ProductLocation;
import io.github.trueruslan.zakupgotov.retailer.RetailerId;
import io.github.trueruslan.zakupgotov.shopping.ShoppingList;
import java.util.Set;

@FunctionalInterface
public interface ComparisonRuntimeEvidenceSource {

    ComparisonRuntimeEvidence load(
            ShoppingList shoppingList,
            ProductLocation productLocation,
            Set<RetailerId> requestedRetailers);
}
