package io.github.trueruslan.zakupgotov.comparison;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

public record RetailerFreshness(
        RetailerFreshnessBasis basis,
        Instant observedAt,
        Optional<Instant> providerUpdatedAt) {

    public RetailerFreshness {
        basis = Objects.requireNonNull(basis, "basis must not be null");
        observedAt = Objects.requireNonNull(observedAt, "observedAt must not be null");
        providerUpdatedAt = Objects.requireNonNull(providerUpdatedAt, "providerUpdatedAt must not be null");
    }
}
