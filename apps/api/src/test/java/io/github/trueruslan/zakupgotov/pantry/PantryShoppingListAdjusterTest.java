package io.github.trueruslan.zakupgotov.pantry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.trueruslan.zakupgotov.shopping.Quantity;
import io.github.trueruslan.zakupgotov.shopping.QuantityUnit;
import io.github.trueruslan.zakupgotov.shopping.ShoppingItem;
import io.github.trueruslan.zakupgotov.shopping.ShoppingItemId;
import io.github.trueruslan.zakupgotov.shopping.ShoppingList;
import io.github.trueruslan.zakupgotov.shopping.ShoppingListId;
import io.github.trueruslan.zakupgotov.shopping.ShoppingRequirement;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class PantryShoppingListAdjusterTest {

    private final PantryShoppingListAdjuster adjuster = new PantryShoppingListAdjuster();

    @Test
    void partialCoverageReducesQuantityAndPreservesIdentityOrderAndSource() {
        var source = shoppingList(
                item(1, "Rice", "1000", QuantityUnit.GRAM),
                item(2, "Milk", "1000", QuantityUnit.MILLILITER));

        var result = adjuster.adjust(source, List.of(
                pantry("Rice", "250", QuantityUnit.GRAM)));

        assertThat(result.remainingShoppingList().id()).isEqualTo(source.id());
        assertThat(result.remainingShoppingList().items())
                .extracting(ShoppingItem::id)
                .containsExactly(itemId(1), itemId(2));
        assertThat(result.remainingShoppingList().items().getFirst().requirement())
                .isEqualTo(new ShoppingRequirement("Rice"));
        assertThat(result.remainingShoppingList().items().getFirst().quantity())
                .isEqualTo(quantity("750", QuantityUnit.GRAM));
        assertThat(result.remainingShoppingList().items().get(1).quantity())
                .isEqualTo(quantity("1000", QuantityUnit.MILLILITER));

        assertThat(result.evidence())
                .extracting(PantryAdjustmentEvidence::status)
                .containsExactly(
                        PantryAdjustmentStatus.PARTIALLY_COVERED,
                        PantryAdjustmentStatus.UNCHANGED);
        assertThat(result.evidence().getFirst().pantryUsed())
                .contains(quantity("250", QuantityUnit.GRAM));
        assertThat(result.evidence().getFirst().remaining())
                .contains(quantity("750", QuantityUnit.GRAM));

        assertThat(source.items().getFirst().quantity())
                .isEqualTo(quantity("1000", QuantityUnit.GRAM));
    }

    @Test
    void unmatchedPantryLeavesItemUnchangedWithUnchangedEvidence() {
        var source = shoppingList(item(1, "Rice", "500", QuantityUnit.GRAM));

        var result = adjuster.adjust(source, List.of(
                pantry("Pasta", "500", QuantityUnit.GRAM)));

        assertThat(result.remainingShoppingList().items()).containsExactly(source.items().getFirst());
        assertThat(result.evidence()).singleElement().satisfies(evidence -> {
            assertThat(evidence.itemId()).isEqualTo(itemId(1));
            assertThat(evidence.status()).isEqualTo(PantryAdjustmentStatus.UNCHANGED);
            assertThat(evidence.pantryUsed()).isEmpty();
            assertThat(evidence.remaining()).contains(quantity("500", QuantityUnit.GRAM));
        });
    }

    @Test
    void fullCoverageRemovesItemButKeepsFullAuditEvidence() {
        var source = shoppingList(
                item(1, "Rice", "500", QuantityUnit.GRAM),
                item(2, "Milk", "1000", QuantityUnit.MILLILITER));

        var result = adjuster.adjust(source, List.of(
                pantry("Rice", "500", QuantityUnit.GRAM)));

        assertThat(result.remainingShoppingList().items())
                .extracting(ShoppingItem::id)
                .containsExactly(itemId(2));
        assertThat(result.evidence()).hasSize(2);
        assertThat(result.evidence().getFirst().itemId()).isEqualTo(itemId(1));
        assertThat(result.evidence().getFirst().status())
                .isEqualTo(PantryAdjustmentStatus.FULLY_COVERED);
        assertThat(result.evidence().getFirst().pantryUsed())
                .contains(quantity("500", QuantityUnit.GRAM));
        assertThat(result.evidence().getFirst().remaining()).isEmpty();
    }

    @Test
    void kilogramsCoverGramRequirementsThroughAcceptedQuantityCanonicalization() {
        var source = shoppingList(item(1, "Rice", "1000", QuantityUnit.GRAM));

        var result = adjuster.adjust(source, List.of(
                pantry("Rice", "0.25", QuantityUnit.KILOGRAM)));

        assertThat(result.remainingShoppingList().items().getFirst().quantity())
                .isEqualTo(quantity("750", QuantityUnit.GRAM));
        assertThat(result.evidence().getFirst().pantryUsed())
                .contains(quantity("250", QuantityUnit.GRAM));
    }

    @Test
    void litersCoverMilliliterRequirementsThroughAcceptedQuantityCanonicalization() {
        var source = shoppingList(item(1, "Milk", "1500", QuantityUnit.MILLILITER));

        var result = adjuster.adjust(source, List.of(
                pantry("Milk", "0.5", QuantityUnit.LITER)));

        assertThat(result.remainingShoppingList().items().getFirst().quantity())
                .isEqualTo(quantity("1000", QuantityUnit.MILLILITER));
        assertThat(result.evidence().getFirst().pantryUsed())
                .contains(quantity("500", QuantityUnit.MILLILITER));
    }

    @Test
    void incompatibleQuantityDimensionsDoNotMatch() {
        var source = shoppingList(item(1, "Eggs", "6", QuantityUnit.PIECE));

        var result = adjuster.adjust(source, List.of(
                pantry("Eggs", "600", QuantityUnit.GRAM)));

        assertThat(result.remainingShoppingList().items()).containsExactly(source.items().getFirst());
        assertThat(result.evidence().getFirst().status())
                .isEqualTo(PantryAdjustmentStatus.UNCHANGED);
    }

    @Test
    void duplicatePantryRowsAggregateByExactRequirementAndCanonicalUnit() {
        var source = shoppingList(item(1, "Rice", "500", QuantityUnit.GRAM));

        var result = adjuster.adjust(source, List.of(
                pantry("Rice", "100", QuantityUnit.GRAM),
                pantry("Rice", "0.25", QuantityUnit.KILOGRAM)));

        assertThat(result.remainingShoppingList().items().getFirst().quantity())
                .isEqualTo(quantity("150", QuantityUnit.GRAM));
        assertThat(result.evidence().getFirst().pantryUsed())
                .contains(quantity("350", QuantityUnit.GRAM));
    }

    @Test
    void sharedPantryStockIsConsumedOnceAcrossDuplicateSourceKeysInSourceOrder() {
        var source = shoppingList(
                item(1, "Rice", "300", QuantityUnit.GRAM),
                item(2, "Rice", "300", QuantityUnit.GRAM));

        var result = adjuster.adjust(source, List.of(
                pantry("Rice", "400", QuantityUnit.GRAM)));

        assertThat(result.remainingShoppingList().items())
                .extracting(ShoppingItem::id)
                .containsExactly(itemId(2));
        assertThat(result.remainingShoppingList().items().getFirst().quantity())
                .isEqualTo(quantity("200", QuantityUnit.GRAM));
        assertThat(result.evidence())
                .extracting(PantryAdjustmentEvidence::itemId)
                .containsExactly(itemId(1), itemId(2));
        assertThat(result.evidence())
                .extracting(PantryAdjustmentEvidence::status)
                .containsExactly(
                        PantryAdjustmentStatus.FULLY_COVERED,
                        PantryAdjustmentStatus.PARTIALLY_COVERED);
        assertThat(result.evidence().get(1).pantryUsed())
                .contains(quantity("100", QuantityUnit.GRAM));
    }

    @Test
    void excessPantryNeverCreatesZeroOrNegativeShoppingQuantity() {
        var source = shoppingList(item(1, "Rice", "100", QuantityUnit.GRAM));

        var result = adjuster.adjust(source, List.of(
                pantry("Rice", "1000", QuantityUnit.GRAM)));

        assertThat(result.remainingShoppingList().items()).isEmpty();
        assertThat(result.evidence()).singleElement().satisfies(evidence -> {
            assertThat(evidence.status()).isEqualTo(PantryAdjustmentStatus.FULLY_COVERED);
            assertThat(evidence.pantryUsed()).contains(quantity("100", QuantityUnit.GRAM));
            assertThat(evidence.remaining()).isEmpty();
        });
    }

    @Test
    void exactRequirementMatchingRemainsCaseSensitive() {
        var source = shoppingList(item(1, "Rice", "500", QuantityUnit.GRAM));

        var result = adjuster.adjust(source, List.of(
                pantry("rice", "500", QuantityUnit.GRAM)));

        assertThat(result.remainingShoppingList().items()).containsExactly(source.items().getFirst());
        assertThat(result.evidence().getFirst().status())
                .isEqualTo(PantryAdjustmentStatus.UNCHANGED);
    }

    @Test
    void doesNotMutateCallerOwnedPantryCollection() {
        var source = shoppingList(item(1, "Rice", "500", QuantityUnit.GRAM));
        var pantryRows = new ArrayList<>(List.of(
                pantry("Rice", "100", QuantityUnit.GRAM),
                pantry("Rice", "50", QuantityUnit.GRAM)));
        var snapshot = List.copyOf(pantryRows);

        adjuster.adjust(source, pantryRows);

        assertThat(pantryRows).containsExactlyElementsOf(snapshot);
    }

    @Test
    void rejectsNullInputsAndNullPantryRows() {
        var source = shoppingList(item(1, "Rice", "500", QuantityUnit.GRAM));

        assertThatThrownBy(() -> adjuster.adjust(null, List.of()))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> adjuster.adjust(source, null))
                .isInstanceOf(NullPointerException.class);

        var pantryRows = new ArrayList<PantryItem>();
        pantryRows.add(null);
        assertThatThrownBy(() -> adjuster.adjust(source, pantryRows))
                .isInstanceOf(NullPointerException.class);
    }

    private static ShoppingList shoppingList(ShoppingItem... items) {
        var list = new ShoppingList(new ShoppingListId(new UUID(1L, 1L)));
        for (var item : items) {
            list.add(item);
        }
        return list;
    }

    private static ShoppingItem item(
            int seed,
            String requirement,
            String amount,
            QuantityUnit unit) {
        return new ShoppingItem(
                itemId(seed),
                new ShoppingRequirement(requirement),
                quantity(amount, unit));
    }

    private static PantryItem pantry(String requirement, String amount, QuantityUnit unit) {
        return new PantryItem(new ShoppingRequirement(requirement), quantity(amount, unit));
    }

    private static Quantity quantity(String amount, QuantityUnit unit) {
        return new Quantity(new BigDecimal(amount), unit);
    }

    private static ShoppingItemId itemId(int seed) {
        return new ShoppingItemId(new UUID(0L, seed));
    }
}
