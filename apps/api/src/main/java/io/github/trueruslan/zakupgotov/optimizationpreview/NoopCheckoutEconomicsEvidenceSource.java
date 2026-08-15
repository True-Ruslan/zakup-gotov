package io.github.trueruslan.zakupgotov.optimizationpreview;

import io.github.trueruslan.zakupgotov.basket.BasketEconomics;
import io.github.trueruslan.zakupgotov.location.ProductLocation;
import io.github.trueruslan.zakupgotov.retailer.RetailerId;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class NoopCheckoutEconomicsEvidenceSource implements CheckoutEconomicsEvidenceSource {

    @Override
    public Map<RetailerId, BasketEconomics> load(
            ProductLocation location,
            Set<RetailerId> requestedRetailers) {
        Objects.requireNonNull(location, "location must not be null");
        Objects.requireNonNull(requestedRetailers, "requestedRetailers must not be null");
        return Map.of();
    }
}
