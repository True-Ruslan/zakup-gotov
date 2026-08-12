package io.github.trueruslan.zakupgotov.preview;

import io.github.trueruslan.zakupgotov.location.ProductLocation;
import io.github.trueruslan.zakupgotov.shopping.ShoppingList;

public interface ComparisonRuntimeEvidenceSource {

    ComparisonRuntimeEvidence load(ShoppingList shoppingList, ProductLocation productLocation);
}
