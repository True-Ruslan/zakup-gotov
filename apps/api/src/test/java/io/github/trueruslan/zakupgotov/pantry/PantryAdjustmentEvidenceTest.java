package io.github.trueruslan.zakupgotov.pantry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.trueruslan.zakupgotov.shopping.Quantity;
import io.github.trueruslan.zakupgotov.shopping.QuantityUnit;
import io.github.trueruslan.zakupgotov.shopping.ShoppingItemId;
import io.github.trueruslan.zakupgotov.shopping.ShoppingRequirement;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class PantryAdjustmentEvidenceTest {

    @Test
    void unchangedEvidenceHasNoUsedQuantityAndKeepsRequiredAsRemaining() {
        var required = quantity("500", QuantityUnit.GRAM);

        var evidence = new PantryAdjustmentEvidence(
                itemId(1),
                new ShoppingRequirement("Rice"),
                required,
                Optional.empty(),
                Optional.of(required),
                PantryAdjustmentStatus.UNCHANGED);

        assertThat(evidence.pantryUsed()).isEmpty();
        assertThat(evidence.remaining()).contains(required);
        assertThat(evidence.status()).isEqualTo(PantryAdjustmentStatus.UNCHANGED);
    }

    @Test
    void partiallyCoveredEvidenceRequiresUsedPlusRemainingToEqualRequired() {
        var evidence = new PantryAdjustmentEvidence(
                itemId(1),
                new ShoppingRequirement("Rice"),
                quantity("500", QuantityUnit.GRAM),
                Optional.of(quantity("125", QuantityUnit.GRAM)),
                Optional.of(quantity("375", QuantityUnit.GRAM)),
                PantryAdjustmentStatus.PARTIALLY_COVERED);

        assertThat(evidence.pantryUsed()).contains(quantity("125", QuantityUnit.GRAM));
        assertThat(evidence.remaining()).contains(quantity("375", QuantityUnit.GRAM));
    }

    @Test
    void fullyCoveredEvidenceKeepsRequiredAndUsedButHasNoRemainingQuantity() {
        var required = quantity("500", QuantityUnit.GRAM);

        var evidence = new PantryAdjustmentEvidence(
                itemId(1),
                new ShoppingRequirement("Rice"),
                required,
                Optional.of(required),
                Optional.empty(),
                PantryAdjustmentStatus.FULLY_COVERED);

        assertThat(evidence.status()).isEqualTo(PantryAdjustmentStatus.FULLY_COVERED);
        assertThat(evidence.pantryUsed()).contains(required);
        assertThat(evidence.remaining()).isEmpty();
    }

    @Test
    void rejectsUnchangedEvidenceWithPantryUsage() {
        var required = quantity("500", QuantityUnit.GRAM);

        assertThatThrownBy(() -> new PantryAdjustmentEvidence(
                        itemId(1),
                        new ShoppingRequirement("Rice"),
                        required,
                        Optional.of(quantity("100", QuantityUnit.GRAM)),
                        Optional.of(required),
                        PantryAdjustmentStatus.UNCHANGED))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("unchanged");
    }

    @Test
    void rejectsPartialEvidenceWithoutBothPositiveParts() {
        assertThatThrownBy(() -> new PantryAdjustmentEvidence(
                        itemId(1),
                        new ShoppingRequirement("Rice"),
                        quantity("500", QuantityUnit.GRAM),
                        Optional.of(quantity("100", QuantityUnit.GRAM)),
                        Optional.empty(),
                        PantryAdjustmentStatus.PARTIALLY_COVERED))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("partial");
    }

    @Test
    void rejectsPartialEvidenceWhenArithmeticDoesNotBalance() {
        assertThatThrownBy(() -> new PantryAdjustmentEvidence(
                        itemId(1),
                        new ShoppingRequirement("Rice"),
                        quantity("500", QuantityUnit.GRAM),
                        Optional.of(quantity("100", QuantityUnit.GRAM)),
                        Optional.of(quantity("350", QuantityUnit.GRAM)),
                        PantryAdjustmentStatus.PARTIALLY_COVERED))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("required");
    }

    @Test
    void rejectsEvidenceWithIncompatibleUnits() {
        assertThatThrownBy(() -> new PantryAdjustmentEvidence(
                        itemId(1),
                        new ShoppingRequirement("Milk"),
                        quantity("500", QuantityUnit.MILLILITER),
                        Optional.of(quantity("100", QuantityUnit.GRAM)),
                        Optional.of(quantity("400", QuantityUnit.MILLILITER)),
                        PantryAdjustmentStatus.PARTIALLY_COVERED))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("unit");
    }

    @Test
    void rejectsFullEvidenceWithRemainingQuantity() {
        var required = quantity("500", QuantityUnit.GRAM);

        assertThatThrownBy(() -> new PantryAdjustmentEvidence(
                        itemId(1),
                        new ShoppingRequirement("Rice"),
                        required,
                        Optional.of(required),
                        Optional.of(quantity("1", QuantityUnit.GRAM)),
                        PantryAdjustmentStatus.FULLY_COVERED))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("full");
    }

    private static Quantity quantity(String amount, QuantityUnit unit) {
        return new Quantity(new BigDecimal(amount), unit);
    }

    private static ShoppingItemId itemId(int seed) {
        return new ShoppingItemId(new UUID(0L, seed));
    }
}
