package io.github.trueruslan.zakupgotov.provider;

import io.github.trueruslan.zakupgotov.location.ProductLocationId;
import java.util.Objects;

public record FulfillmentContextBinding(
        ProductLocationId productLocationId,
        LocationContext context,
        FulfillmentContextSelectionMode mode) {

    public FulfillmentContextBinding {
        productLocationId = Objects.requireNonNull(productLocationId, "productLocationId must not be null");
        context = Objects.requireNonNull(context, "context must not be null");
        mode = Objects.requireNonNull(mode, "mode must not be null");
    }
}
