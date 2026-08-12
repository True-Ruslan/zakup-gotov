package io.github.trueruslan.zakupgotov.preview;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.trueruslan.zakupgotov.shopping.Quantity;
import io.github.trueruslan.zakupgotov.shopping.QuantityUnit;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ComparisonPreviewRequestFactoryTest {

    private static final UUID MILK_ID = UUID.fromString("c281d71c-2b27-46ef-a7af-3d624a7447cf");

    @Test
    void normalizesLocalityRequirementAndQuantityThroughExistingDomainTypes() {
        var request = new ComparisonPreviewRequest(
                "  Москва  ",
                List.of(new ComparisonPreviewItemRequest(
                        MILK_ID,
                        "  Молоко   3,2%  ",
                        new BigDecimal("2"),
                        QuantityUnit.LITER)));

        var input = ComparisonPreviewRequestFactory.create(request);

        assertThat(input.productLocation().locality()).isEqualTo("Москва");
        assertThat(input.productLocation().address()).isEmpty();
        assertThat(input.shoppingList().items()).hasSize(1);
        var item = input.shoppingList().items().getFirst();
        assertThat(item.id().value()).isEqualTo(MILK_ID);
        assertThat(item.requirement().text()).isEqualTo("Молоко 3,2%");
        assertThat(item.quantity()).isEqualTo(new Quantity(new BigDecimal("2000"), QuantityUnit.MILLILITER));
    }

    @Test
    void rejectsBlankAndOverlongLocalityAfterNormalization() {
        assertThatThrownBy(() -> ComparisonPreviewRequestFactory.create(request("   ", item(MILK_ID))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("locality");
        assertThatThrownBy(() -> ComparisonPreviewRequestFactory.create(request("я".repeat(161), item(MILK_ID))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("160");
    }

    @Test
    void rejectsEmptyAndOversizedItemLists() {
        assertThatThrownBy(() -> ComparisonPreviewRequestFactory.create(new ComparisonPreviewRequest("Москва", List.of())))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("items");

        var items = new ArrayList<ComparisonPreviewItemRequest>();
        for (var index = 0; index < 101; index++) {
            items.add(item(UUID.nameUUIDFromBytes(("item-" + index).getBytes())));
        }
        assertThatThrownBy(() -> ComparisonPreviewRequestFactory.create(new ComparisonPreviewRequest("Москва", items)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("100");
    }

    @Test
    void rejectsDuplicateClientItemIds() {
        assertThatThrownBy(() -> ComparisonPreviewRequestFactory.create(new ComparisonPreviewRequest(
                        "Москва",
                        List.of(item(MILK_ID), item(MILK_ID)))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("duplicate");
    }

    @Test
    void rejectsBlankAndOverlongRequirements() {
        assertThatThrownBy(() -> ComparisonPreviewRequestFactory.create(request(
                        "Москва",
                        new ComparisonPreviewItemRequest(MILK_ID, "   ", BigDecimal.ONE, QuantityUnit.PIECE))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("requirement");
        assertThatThrownBy(() -> ComparisonPreviewRequestFactory.create(request(
                        "Москва",
                        new ComparisonPreviewItemRequest(MILK_ID, "м".repeat(241), BigDecimal.ONE, QuantityUnit.PIECE))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("240");
    }

    @Test
    void rejectsNullZeroAndNegativeQuantityValues() {
        assertThatThrownBy(() -> ComparisonPreviewRequestFactory.create(request(
                        "Москва",
                        new ComparisonPreviewItemRequest(MILK_ID, "Молоко", null, QuantityUnit.LITER))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("amount");
        assertThatThrownBy(() -> ComparisonPreviewRequestFactory.create(request(
                        "Москва",
                        new ComparisonPreviewItemRequest(MILK_ID, "Молоко", BigDecimal.ZERO, QuantityUnit.LITER))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("amount");
        assertThatThrownBy(() -> ComparisonPreviewRequestFactory.create(request(
                        "Москва",
                        new ComparisonPreviewItemRequest(MILK_ID, "Молоко", BigDecimal.ONE.negate(), QuantityUnit.LITER))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("amount");
        assertThatThrownBy(() -> ComparisonPreviewRequestFactory.create(request(
                        "Москва",
                        new ComparisonPreviewItemRequest(MILK_ID, "Молоко", BigDecimal.ONE, null))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("unit");
    }

    @Test
    void publicRequestTypesExposeNoAddressOrProviderIdentifiers() {
        assertThat(List.of(ComparisonPreviewRequest.class.getRecordComponents()))
                .extracting(component -> component.getName())
                .containsExactly("locality", "items");
        assertThat(List.of(ComparisonPreviewItemRequest.class.getRecordComponents()))
                .extracting(component -> component.getName())
                .containsExactly("id", "requirement", "amount", "unit");
    }

    private static ComparisonPreviewRequest request(String locality, ComparisonPreviewItemRequest item) {
        return new ComparisonPreviewRequest(locality, List.of(item));
    }

    private static ComparisonPreviewItemRequest item(UUID id) {
        return new ComparisonPreviewItemRequest(id, "Молоко", BigDecimal.ONE, QuantityUnit.LITER);
    }
}
