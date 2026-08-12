package io.github.trueruslan.zakupgotov.provider.magnit;

import java.util.List;
import java.util.Objects;

public record MagnitStoreSearchRequest(Filters filters) {

    private static final List<String> STORE_TYPES =
            List.of("MM", "GM", "DG", "MO", "ME", "MC", "DARKSTORE", "MM_MINI", "ZARYAD");

    public MagnitStoreSearchRequest {
        filters = Objects.requireNonNull(filters, "filters must not be null");
    }

    public static MagnitStoreSearchRequest forBoundingBox(MagnitGeoBoundingBox boundingBox) {
        var box = Objects.requireNonNull(boundingBox, "boundingBox must not be null");
        return new MagnitStoreSearchRequest(new Filters(
                new GeoFilter("box", box.leftTopPoint(), box.rightBottomPoint()),
                STORE_TYPES));
    }

    public static List<String> storeTypes() {
        return STORE_TYPES;
    }

    public record Filters(GeoFilter geo, List<String> storeTypeListV2) {
        public Filters {
            geo = Objects.requireNonNull(geo, "geo must not be null");
            storeTypeListV2 = List.copyOf(Objects.requireNonNull(storeTypeListV2, "storeTypeListV2 must not be null"));
        }
    }

    public record GeoFilter(String typeName, MagnitGeoPoint leftTopPoint, MagnitGeoPoint rightBottomPoint) {
        public GeoFilter {
            if (!"box".equals(typeName)) {
                throw new IllegalArgumentException("typeName must be box");
            }
            leftTopPoint = Objects.requireNonNull(leftTopPoint, "leftTopPoint must not be null");
            rightBottomPoint = Objects.requireNonNull(rightBottomPoint, "rightBottomPoint must not be null");
        }
    }
}
