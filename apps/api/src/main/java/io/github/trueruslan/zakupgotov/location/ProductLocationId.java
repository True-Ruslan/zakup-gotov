package io.github.trueruslan.zakupgotov.location;

import java.util.Objects;
import java.util.UUID;

public record ProductLocationId(UUID value) {

    public ProductLocationId {
        value = Objects.requireNonNull(value, "value must not be null");
    }
}
