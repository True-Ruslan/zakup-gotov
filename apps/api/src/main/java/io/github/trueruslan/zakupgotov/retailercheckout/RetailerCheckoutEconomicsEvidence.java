package io.github.trueruslan.zakupgotov.retailercheckout;

import io.github.trueruslan.zakupgotov.basket.BasketEconomics;
import io.github.trueruslan.zakupgotov.retailer.RetailerId;
import java.util.Objects;

public record RetailerCheckoutEconomicsEvidence(
        RetailerId retailerId,
        BasketEconomics economics) {

    public RetailerCheckoutEconomicsEvidence {
        retailerId = Objects.requireNonNull(retailerId, "retailerId must not be null");
        economics = Objects.requireNonNull(economics, "economics must not be null");
    }
}
