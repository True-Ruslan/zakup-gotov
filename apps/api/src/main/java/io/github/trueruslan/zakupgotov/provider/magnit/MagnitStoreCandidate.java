package io.github.trueruslan.zakupgotov.provider.magnit;

import java.util.Objects;

public record MagnitStoreCandidate(String shopCode, MagnitGeoPoint coordinates) {

    public MagnitStoreCandidate {
        if (shopCode == null || shopCode.isBlank()) {
            throw new IllegalArgumentException("shopCode must not be blank");
        }
        shopCode = shopCode.trim();
        coordinates = Objects.requireNonNull(coordinates, "coordinates must not be null");
    }
}
