package io.github.trueruslan.zakupgotov.provider.magnit;

import io.github.trueruslan.zakupgotov.location.ProductLocationId;
import io.github.trueruslan.zakupgotov.provider.FulfillmentContextBinding;
import io.github.trueruslan.zakupgotov.provider.FulfillmentContextSelectionMode;
import io.github.trueruslan.zakupgotov.provider.LocationContext;
import java.util.Objects;
import java.util.Optional;

public final class MagnitFulfillmentContextBindings {

    public static final String SOURCE_PROVIDER_ID = "magnit-public-page";

    private MagnitFulfillmentContextBindings() {}

    public static Optional<FulfillmentContextBinding> autoResolved(
            ProductLocationId productLocationId,
            String locality,
            MagnitStoreResolution resolution) {
        var result = Objects.requireNonNull(resolution, "resolution must not be null");
        if (result.status() != MagnitStoreResolutionStatus.RESOLVED) {
            return Optional.empty();
        }
        return result.candidate().map(candidate -> binding(
                productLocationId,
                locality,
                candidate,
                FulfillmentContextSelectionMode.RESOLVED));
    }

    public static FulfillmentContextBinding manual(
            ProductLocationId productLocationId,
            String locality,
            MagnitStoreCandidate candidate) {
        return binding(
                productLocationId,
                locality,
                Objects.requireNonNull(candidate, "candidate must not be null"),
                FulfillmentContextSelectionMode.MANUAL);
    }

    private static FulfillmentContextBinding binding(
            ProductLocationId productLocationId,
            String locality,
            MagnitStoreCandidate candidate,
            FulfillmentContextSelectionMode mode) {
        var context = new LocationContext(SOURCE_PROVIDER_ID, candidate.shopCode(), locality);
        return new FulfillmentContextBinding(
                Objects.requireNonNull(productLocationId, "productLocationId must not be null"),
                context,
                mode);
    }
}
