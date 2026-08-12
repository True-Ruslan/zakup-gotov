package io.github.trueruslan.zakupgotov.shopping;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ShoppingListTest {

    private static final ShoppingListId LIST_ID = new ShoppingListId(
            UUID.fromString("11111111-1111-1111-1111-111111111111"));
    private static final ShoppingItemId MILK_ID = new ShoppingItemId(
            UUID.fromString("22222222-2222-2222-2222-222222222222"));
    private static final ShoppingItemId BREAD_ID = new ShoppingItemId(
            UUID.fromString("33333333-3333-3333-3333-333333333333"));

    @Test
    void addsRequirementsInStableOrderAndNormalizesOnlyWhitespace() {
        var list = new ShoppingList(LIST_ID);

        list.add(new ShoppingItem(
                MILK_ID,
                new ShoppingRequirement("  Молоко   2.5%  "),
                new Quantity(BigDecimal.ONE, QuantityUnit.LITER)));
        list.add(new ShoppingItem(
                BREAD_ID,
                new ShoppingRequirement("Хлеб бородинский"),
                new Quantity(BigDecimal.ONE, QuantityUnit.PIECE)));

        assertThat(list.id()).isEqualTo(LIST_ID);
        assertThat(list.items())
                .extracting(item -> item.requirement().text())
                .containsExactly("Молоко 2.5%", "Хлеб бородинский");
        assertThat(list.items().getFirst().quantity())
                .isEqualTo(new Quantity(new BigDecimal("1000"), QuantityUnit.MILLILITER));
    }

    @Test
    void replacesExistingItemWithoutChangingItsPosition() {
        var list = populatedList();

        list.replace(new ShoppingItem(
                MILK_ID,
                new ShoppingRequirement("Кефир"),
                new Quantity(new BigDecimal("0.9"), QuantityUnit.LITER)));

        assertThat(list.items())
                .extracting(ShoppingItem::id)
                .containsExactly(MILK_ID, BREAD_ID);
        assertThat(list.items().getFirst().requirement().text()).isEqualTo("Кефир");
        assertThat(list.items().getFirst().quantity())
                .isEqualTo(new Quantity(new BigDecimal("900"), QuantityUnit.MILLILITER));
    }

    @Test
    void removesExistingItem() {
        var list = populatedList();

        list.remove(MILK_ID);

        assertThat(list.items())
                .extracting(ShoppingItem::id)
                .containsExactly(BREAD_ID);
    }

    @Test
    void rejectsDuplicateOrUnknownItemIdentifiers() {
        var list = populatedList();

        assertThatThrownBy(() -> list.add(new ShoppingItem(
                        MILK_ID,
                        new ShoppingRequirement("Молоко другое"),
                        new Quantity(BigDecimal.ONE, QuantityUnit.PIECE))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("duplicate");

        var unknown = new ShoppingItemId(UUID.fromString("44444444-4444-4444-4444-444444444444"));
        assertThatThrownBy(() -> list.replace(new ShoppingItem(
                        unknown,
                        new ShoppingRequirement("Сахар"),
                        new Quantity(BigDecimal.ONE, QuantityUnit.KILOGRAM))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("unknown");
        assertThatThrownBy(() -> list.remove(unknown))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("unknown");
    }

    @Test
    void rejectsBlankRequirementAndExposesImmutableItemView() {
        assertThatThrownBy(() -> new ShoppingRequirement("   \t "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("text");

        var list = populatedList();
        assertThatThrownBy(() -> list.items().clear())
                .isInstanceOf(UnsupportedOperationException.class);
    }

    private static ShoppingList populatedList() {
        var list = new ShoppingList(LIST_ID);
        list.add(new ShoppingItem(
                MILK_ID,
                new ShoppingRequirement("Молоко"),
                new Quantity(BigDecimal.ONE, QuantityUnit.LITER)));
        list.add(new ShoppingItem(
                BREAD_ID,
                new ShoppingRequirement("Хлеб"),
                new Quantity(BigDecimal.ONE, QuantityUnit.PIECE)));
        return list;
    }
}
