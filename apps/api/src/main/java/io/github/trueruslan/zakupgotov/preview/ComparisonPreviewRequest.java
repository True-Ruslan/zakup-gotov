package io.github.trueruslan.zakupgotov.preview;

import java.util.List;

public record ComparisonPreviewRequest(
        String locality,
        List<ComparisonPreviewItemRequest> items) {}
