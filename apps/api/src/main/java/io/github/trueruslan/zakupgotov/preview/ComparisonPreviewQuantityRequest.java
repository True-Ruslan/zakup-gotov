package io.github.trueruslan.zakupgotov.preview;

import io.github.trueruslan.zakupgotov.shopping.QuantityUnit;
import java.math.BigDecimal;

public record ComparisonPreviewQuantityRequest(
        BigDecimal amount,
        QuantityUnit unit) {}
