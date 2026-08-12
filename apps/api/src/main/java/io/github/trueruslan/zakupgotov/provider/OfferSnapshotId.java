package io.github.trueruslan.zakupgotov.provider;

import java.util.Objects;
import java.util.UUID;

public record OfferSnapshotId(UUID value) {

    public OfferSnapshotId {
        value = Objects.requireNonNull(value, "value must not be null");
    }
}
