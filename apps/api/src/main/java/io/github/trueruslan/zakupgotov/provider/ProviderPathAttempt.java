package io.github.trueruslan.zakupgotov.provider;

import java.util.Objects;

public record ProviderPathAttempt(
        String sourceProviderId,
        AcquisitionMode sourceMode,
        ProviderPathAttemptStatus status) {

    public ProviderPathAttempt {
        if (sourceProviderId == null || sourceProviderId.isBlank()) {
            throw new IllegalArgumentException("sourceProviderId must not be blank");
        }
        sourceMode = Objects.requireNonNull(sourceMode, "sourceMode must not be null");
        status = Objects.requireNonNull(status, "status must not be null");
    }
}
