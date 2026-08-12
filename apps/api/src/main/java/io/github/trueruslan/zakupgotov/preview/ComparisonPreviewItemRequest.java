package io.github.trueruslan.zakupgotov.preview;

import io.github.trueruslan.zakupgotov.shopping.QuantityUnit;
import java.math.BigDecimal;
import java.util.UUID;

public record ComparisonPreviewItemRequest(
        UUID id,
        String requirement,
        BigDecimal amount,
        QuantityUnit unit) {}
