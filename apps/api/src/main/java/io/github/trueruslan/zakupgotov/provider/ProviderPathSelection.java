package io.github.trueruslan.zakupgotov.provider;

import java.util.Objects;

public record ProviderPathSelection(String sourceProviderId, AcquisitionMode sourceMode) {

    public ProviderPathSelection {
        if (sourceProviderId == null || sourceProviderId.isBlank()) {
            throw new IllegalArgumentException("sourceProviderId must not be blank");
        }
        sourceMode = Objects.requireNonNull(sourceMode, "sourceMode must not be null");
    }
}
