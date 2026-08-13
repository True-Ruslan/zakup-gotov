package io.github.trueruslan.zakupgotov.recipepreview;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.trueruslan.zakupgotov.recipe.RecipeId;
import io.github.trueruslan.zakupgotov.recipe.RecipeIngredientId;
import io.github.trueruslan.zakupgotov.recipe.RecipeShoppingListConverter;
import io.github.trueruslan.zakupgotov.shopping.Quantity;
import io.github.trueruslan.zakupgotov.shopping.QuantityUnit;
import io.github.trueruslan.zakupgotov.shopping.ShoppingListId;
import java.math.BigDecimal;
import java.util.ArrayDeque;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class RecipeShoppingPreviewServiceTest {
    private static final UUID RECIPE_ID = UUID.fromString("0a8d9ead-258a-4a5f-a151-c61fa18d9e25");
    private static final UUID LIST_ID = UUID.fromString("96c0a846-13ad-4e07-927d-28e90f36577e");
    private static final UUID MILK_1 = UUID.fromString("2e9b5ba9-bcbc-467c-857b-80457ce6680c");
    private static final UUID FLOUR = UUID.fromString("bc4a3f07-f78f-4d00-845a-a1219585060b");
    private static final UUID MILK_2 = UUID.fromString("6740cb8d-8705-49d9-9388-a91e97c0e41b");
    private static final UUID LOWER_MILK = UUID.fromString("5342044b-9114-4c24-a239-5851432fc585");
    private static final UUID EGGS = UUID.fromString("4cd8fe14-5dc4-4e31-b4e6-190765067ce1");

    @Test
    void projectsCanonicalRecipeAndScaledMergedShoppingListWithResolvableProvenance() {
        var service = service(MILK_1, FLOUR, MILK_2, LOWER_MILK, EGGS);

        var preview = service.create(new RecipeShoppingPreviewRequest(
                "  Pancakes  ",
                2,
                4,
                List.of(
                        ingredient("Milk", "0.5", QuantityUnit.LITER),
                        ingredient("Flour", "0.5", QuantityUnit.KILOGRAM),
                        ingredient("Milk", "250", QuantityUnit.MILLILITER),
                        ingredient("milk", "100", QuantityUnit.MILLILITER),
                        ingredient("Eggs", "0.5", QuantityUnit.PIECE))));

        assertThat(preview.recipe().id()).isEqualTo(RECIPE_ID);
        assertThat(preview.recipe().title()).isEqualTo("Pancakes");
        assertThat(preview.recipe().baseServings()).isEqualTo(2);
        assertThat(preview.recipe().targetServings()).isEqualTo(4);
        assertThat(preview.recipe().ingredients())
                .extracting(RecipeShoppingPreviewRecipeIngredient::id)
                .containsExactly(MILK_1, FLOUR, MILK_2, LOWER_MILK, EGGS);
        assertThat(preview.recipe().ingredients())
                .extracting(RecipeShoppingPreviewRecipeIngredient::quantity)
                .containsExactly(
                        quantity("500", QuantityUnit.MILLILITER),
                        quantity("500", QuantityUnit.GRAM),
                        quantity("250", QuantityUnit.MILLILITER),
                        quantity("100", QuantityUnit.MILLILITER),
                        quantity("0.5", QuantityUnit.PIECE));

        assertThat(preview.shoppingList().id()).isEqualTo(LIST_ID);
        assertThat(preview.shoppingList().items())
                .extracting(RecipeShoppingPreviewShoppingItem::requirement)
                .containsExactly("Milk", "Flour", "milk", "Eggs");
        assertThat(preview.shoppingList().items())
                .extracting(RecipeShoppingPreviewShoppingItem::quantity)
                .containsExactly(
                        quantity("1500", QuantityUnit.MILLILITER),
                        quantity("1000", QuantityUnit.GRAM),
                        quantity("200", QuantityUnit.MILLILITER),
                        quantity("1", QuantityUnit.PIECE));
        assertThat(preview.shoppingList().items().get(0).sourceIngredientIds())
                .containsExactly(MILK_1, MILK_2);
        assertThat(preview.shoppingList().items().get(1).sourceIngredientIds()).containsExactly(FLOUR);
        assertThat(preview.shoppingList().items().get(2).sourceIngredientIds()).containsExactly(LOWER_MILK);
        assertThat(preview.shoppingList().items().get(3).sourceIngredientIds()).containsExactly(EGGS);

        var sourceIds = preview.recipe().ingredients().stream()
                .map(RecipeShoppingPreviewRecipeIngredient::id)
                .collect(java.util.stream.Collectors.toSet());
        assertThat(preview.shoppingList().items())
                .allSatisfy(item -> {
                    assertThat(item.sourceIngredientIds()).isNotEmpty();
                    assertThat(item.sourceIngredientIds()).allMatch(sourceIds::contains);
                });
    }

    @Test
    void preservesDecimal128ScalingAndFractionalPiecesWithoutHiddenRounding() {
        var spice = UUID.fromString("e642ea96-84e5-4f21-978f-0cb46f644ccf");
        var egg = UUID.fromString("c2b030f3-a9ae-4502-80e9-4c7deaf9cd52");
        var service = service(spice, egg);

        var preview = service.create(new RecipeShoppingPreviewRequest(
                "Recipe", 3, 1,
                List.of(
                        ingredient("Spice", "100", QuantityUnit.GRAM),
                        ingredient("Egg", "0.5", QuantityUnit.PIECE))));

        assertThat(preview.shoppingList().items().get(0).quantity().amount())
                .isEqualByComparingTo(new BigDecimal("33.33333333333333333333333333333333"));
        assertThat(preview.shoppingList().items().get(1).quantity().amount())
                .isEqualByComparingTo(new BigDecimal("0.1666666666666666666666666666666667"));
        assertThat(preview.shoppingList().items().get(1).quantity().unit()).isEqualTo(QuantityUnit.PIECE);
    }

    @Test
    void exposesNestedProjectionListsAsImmutable() {
        var service = service(MILK_1);
        var preview = service.create(new RecipeShoppingPreviewRequest(
                "Recipe", 1, 1, List.of(ingredient("Milk", "1", QuantityUnit.LITER))));

        assertThatThrownBy(() -> preview.recipe().ingredients().clear())
                .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> preview.shoppingList().items().clear())
                .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> preview.shoppingList().items().getFirst().sourceIngredientIds().clear())
                .isInstanceOf(UnsupportedOperationException.class);
    }

    private static RecipeShoppingPreviewService service(UUID... ingredientIds) {
        var ids = new FixedIds(List.of(ingredientIds));
        var factory = new RecipeShoppingPreviewRequestFactory(ids);
        return new RecipeShoppingPreviewService(factory, new RecipeShoppingListConverter());
    }

    private static RecipeShoppingPreviewIngredientRequest ingredient(
            String requirement, String amount, QuantityUnit unit) {
        return new RecipeShoppingPreviewIngredientRequest(
                requirement,
                new RecipeShoppingPreviewQuantityRequest(new BigDecimal(amount), unit));
    }

    private static Quantity quantity(String amount, QuantityUnit unit) {
        return new Quantity(new BigDecimal(amount), unit);
    }

    private static final class FixedIds implements RecipeShoppingPreviewIdGenerator {
        private final ArrayDeque<UUID> ingredientIds;

        private FixedIds(List<UUID> ingredientIds) {
            this.ingredientIds = new ArrayDeque<>(ingredientIds);
        }

        @Override public RecipeId nextRecipeId() { return new RecipeId(RECIPE_ID); }
        @Override public RecipeIngredientId nextIngredientId() { return new RecipeIngredientId(ingredientIds.removeFirst()); }
        @Override public ShoppingListId nextShoppingListId() { return new ShoppingListId(LIST_ID); }
    }
}
