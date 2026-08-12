package io.github.trueruslan.zakupgotov.provider;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

public final class FreshnessEvidence {

    private final Instant observedAt;
    private final Instant providerUpdatedAt;
    private final FreshnessBasis basis;

    private FreshnessEvidence(
            Instant observedAt,
            Instant providerUpdatedAt,
            FreshnessBasis basis) {
        this.observedAt = Objects.requireNonNull(observedAt, "observedAt must not be null");
        this.providerUpdatedAt = providerUpdatedAt;
        this.basis = Objects.requireNonNull(basis, "basis must not be null");

        if (providerUpdatedAt != null && providerUpdatedAt.isAfter(observedAt)) {
            throw new IllegalArgumentException("providerUpdatedAt must not be after observedAt");
        }
    }

    public static FreshnessEvidence observationOnly(Instant observedAt) {
        return new FreshnessEvidence(observedAt, null, FreshnessBasis.OBSERVATION_ONLY);
    }

    public static FreshnessEvidence providerUpdatedAt(
            Instant observedAt,
            Instant providerUpdatedAt) {
        return new FreshnessEvidence(
                observedAt,
                Objects.requireNonNull(providerUpdatedAt, "providerUpdatedAt must not be null"),
                FreshnessBasis.PROVIDER_UPDATED_AT);
    }

    public Instant observedAt() {
        return observedAt;
    }

    public Optional<Instant> providerUpdatedAt() {
        return Optional.ofNullable(providerUpdatedAt);
    }

    public FreshnessBasis basis() {
        return basis;
    }

    @Override
    public boolean equals(Object other) {
        return this == other
                || other instanceof FreshnessEvidence freshness
                        && observedAt.equals(freshness.observedAt)
                        && Objects.equals(providerUpdatedAt, freshness.providerUpdatedAt)
                        && basis == freshness.basis;
    }

    @Override
    public int hashCode() {
        return Objects.hash(observedAt, providerUpdatedAt, basis);
    }

    @Override
    public String toString() {
        return "FreshnessEvidence[observedAt=" + observedAt
                + ", providerUpdatedAt=" + providerUpdatedAt()
                + ", basis=" + basis + "]";
    }
}
