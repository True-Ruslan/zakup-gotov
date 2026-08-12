package io.github.trueruslan.zakupgotov.provider.magnit;

import java.util.Objects;

public record MagnitGeoBoundingBox(MagnitGeoPoint leftTopPoint, MagnitGeoPoint rightBottomPoint) {

    public MagnitGeoBoundingBox {
        leftTopPoint = Objects.requireNonNull(leftTopPoint, "leftTopPoint must not be null");
        rightBottomPoint = Objects.requireNonNull(rightBottomPoint, "rightBottomPoint must not be null");
        if (leftTopPoint.latitude() <= rightBottomPoint.latitude()) {
            throw new IllegalArgumentException("leftTopPoint latitude must be greater than rightBottomPoint latitude");
        }
        if (leftTopPoint.longitude() >= rightBottomPoint.longitude()) {
            throw new IllegalArgumentException("leftTopPoint longitude must be less than rightBottomPoint longitude");
        }
    }
}
