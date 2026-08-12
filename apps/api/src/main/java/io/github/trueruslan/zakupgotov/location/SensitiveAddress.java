package io.github.trueruslan.zakupgotov.location;

import java.util.Objects;

public final class SensitiveAddress {

    private final String value;

    private SensitiveAddress(String value) {
        this.value = value;
    }

    public static SensitiveAddress of(String raw) {
        Objects.requireNonNull(raw, "address must not be null");
        var normalized = raw.strip();
        if (normalized.isBlank()) {
            throw new IllegalArgumentException("address must not be blank");
        }
        return new SensitiveAddress(normalized);
    }

    public String reveal() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        return this == other || other instanceof SensitiveAddress address && value.equals(address.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return "[REDACTED]";
    }
}
