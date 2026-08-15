package io.github.trueruslan.zakupgotov.preview;

import io.github.trueruslan.zakupgotov.comparison.RetailerComparisonCatalog;
import java.util.Objects;

public record ComparisonPreviewComputation(
        ComparisonPreviewInput input,
        ComparisonPreview preview,
        RetailerComparisonCatalog catalog) {

    public ComparisonPreviewComputation {
        input = Objects.requireNonNull(input, "input must not be null");
        preview = Objects.requireNonNull(preview, "preview must not be null");
        catalog = Objects.requireNonNull(catalog, "catalog must not be null");

        var previewRetailers = preview.retailers();
        var catalogRetailers = catalog.retailers();
        if (previewRetailers.size() != catalogRetailers.size()) {
            throw new IllegalArgumentException("preview retailer cardinality must match comparison catalog");
        }
        for (var index = 0; index < previewRetailers.size(); index++) {
            var previewRetailer = previewRetailers.get(index);
            var catalogRetailer = catalogRetailers.get(index);
            if (!previewRetailer.id().equals(catalogRetailer.retailerId().canonicalId())) {
                throw new IllegalArgumentException("preview retailer identity/order must match comparison catalog");
            }
        }
    }
}
