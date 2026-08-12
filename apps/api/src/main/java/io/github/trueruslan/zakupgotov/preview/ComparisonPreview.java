package io.github.trueruslan.zakupgotov.preview;

import java.util.List;
import java.util.Objects;

public record ComparisonPreview(
        String locality,
        List<ComparisonPreviewRequestedItem> items,
        List<ComparisonPreviewRetailer> retailers) {

    public ComparisonPreview {
        if (locality == null || locality.isBlank()) {
            throw new IllegalArgumentException("locality must not be blank");
        }
        items = List.copyOf(Objects.requireNonNull(items, "items must not be null"));
        retailers = List.copyOf(Objects.requireNonNull(retailers, "retailers must not be null"));
    }

    public ComparisonPreviewRetailer require(String retailerId) {
        if (retailerId == null || retailerId.isBlank()) {
            throw new IllegalArgumentException("retailerId must not be blank");
        }
        return retailers.stream()
                .filter(retailer -> retailer.id().equals(retailerId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("retailer is not present: " + retailerId));
    }
}
