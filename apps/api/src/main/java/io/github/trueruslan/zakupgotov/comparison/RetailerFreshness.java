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

        switch (basis) {
            case OBSERVATION_ONLY -> {
                if (providerUpdatedAt.isPresent()) {
                    throw new IllegalArgumentException(
                            "OBSERVATION_ONLY freshness must not contain providerUpdatedAt");
                }
            }
            case PROVIDER_TIMESTAMP -> {
                if (providerUpdatedAt.isEmpty()) {
                    throw new IllegalArgumentException(
                            "PROVIDER_TIMESTAMP freshness requires providerUpdatedAt");
                }
            }
        }

        if (providerUpdatedAt.filter(timestamp -> timestamp.isAfter(observedAt)).isPresent()) {
            throw new IllegalArgumentException("providerUpdatedAt must not be after observedAt");
        }
    }
}
