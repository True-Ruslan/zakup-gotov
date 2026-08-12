package io.github.trueruslan.zakupgotov.comparison;

import io.github.trueruslan.zakupgotov.basket.SingleStoreBasketQuote;
import io.github.trueruslan.zakupgotov.provider.ProviderSearchOutcome;
import java.util.Objects;
import java.util.Optional;

public record RetailerComparisonEvidence(
        ProviderSearchOutcome providerOutcome,
        Optional<SingleStoreBasketQuote> basketQuote) {

    public RetailerComparisonEvidence {
        providerOutcome = Objects.requireNonNull(providerOutcome, "providerOutcome must not be null");
        basketQuote = Objects.requireNonNull(basketQuote, "basketQuote must not be null");

        if (providerOutcome.succeeded() && basketQuote.isEmpty()) {
            throw new IllegalArgumentException("successful provider outcome requires a basket quote");
        }
        if (!providerOutcome.succeeded() && basketQuote.isPresent()) {
            throw new IllegalArgumentException("unavailable provider outcome must not carry a basket quote");
        }
        if (basketQuote.isPresent()
                && basketQuote.orElseThrow().scope().retailerId() != providerOutcome.retailerId()) {
            throw new IllegalArgumentException("basket quote retailer must match provider outcome retailer");
        }
    }
}
