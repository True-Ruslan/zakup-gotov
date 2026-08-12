package io.github.trueruslan.zakupgotov.comparison;

import io.github.trueruslan.zakupgotov.basket.SingleStoreBasketQuote;
import io.github.trueruslan.zakupgotov.provider.ProviderSearchOutcome;
import java.util.Optional;

public record RetailerComparisonEvidence(
        ProviderSearchOutcome providerOutcome,
        Optional<SingleStoreBasketQuote> basketQuote) {
}
