package io.github.trueruslan.zakupgotov.optimizationpreview;

import io.github.trueruslan.zakupgotov.basket.BasketEconomics;
import io.github.trueruslan.zakupgotov.location.ProductLocation;
import io.github.trueruslan.zakupgotov.retailer.RetailerId;
import java.util.Map;
import java.util.Set;

@FunctionalInterface
public interface CheckoutEconomicsEvidenceSource {

    Map<RetailerId, BasketEconomics> load(
            ProductLocation location,
            Set<RetailerId> requestedRetailers);
}
