package io.github.trueruslan.zakupgotov.matching;

import io.github.trueruslan.zakupgotov.retailer.RetailerId;
import java.util.Objects;

public record MatchScope(RetailerId retailerId, String fulfillmentContextId) {

    public MatchScope {
        retailerId = Objects.requireNonNull(retailerId, "retailerId must not be null");
        if (fulfillmentContextId == null || fulfillmentContextId.isBlank()) {
            throw new IllegalArgumentException("fulfillmentContextId must not be blank");
        }
    }
}
