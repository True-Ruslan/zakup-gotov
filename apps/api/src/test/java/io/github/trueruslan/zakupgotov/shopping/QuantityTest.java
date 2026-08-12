package io.github.trueruslan.zakupgotov.shopping;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class QuantityTest {

    @Test
    void normalizesMassToGrams() {
        assertThat(new Quantity(new BigDecimal("1.25"), QuantityUnit.KILOGRAM))
                .isEqualTo(new Quantity(new BigDecimal("1250"), QuantityUnit.GRAM));
    }

    @Test
    void normalizesVolumeToMilliliters() {
        assertThat(new Quantity(new BigDecimal("1.5"), QuantityUnit.LITER))
                .isEqualTo(new Quantity(new BigDecimal("1500"), QuantityUnit.MILLILITER));
    }

    @Test
    void preservesCanonicalPieceQuantity() {
        assertThat(new Quantity(new BigDecimal("3"), QuantityUnit.PIECE))
                .isEqualTo(new Quantity(new BigDecimal("3.0"), QuantityUnit.PIECE));
    }

    @Test
    void rejectsNonPositiveAmount() {
        assertThatThrownBy(() -> new Quantity(BigDecimal.ZERO, QuantityUnit.GRAM))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("amount");
        assertThatThrownBy(() -> new Quantity(new BigDecimal("-0.1"), QuantityUnit.MILLILITER))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("amount");
    }

    @Test
    void rejectsMissingUnit() {
        assertThatThrownBy(() -> new Quantity(BigDecimal.ONE, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("unit");
    }
}
