package io.github.trueruslan.zakupgotov.recipe;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.trueruslan.zakupgotov.shopping.Quantity;
import io.github.trueruslan.zakupgotov.shopping.QuantityUnit;
import io.github.trueruslan.zakupgotov.shopping.ShoppingListId;
import io.github.trueruslan.zakupgotov.shopping.ShoppingRequirement;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class RecipeShoppingListConverterTest {

    private static final RecipeId RECIPE_ID = new RecipeId(UUID.fromString("5d3fa22c-014f-4b08-b5b3-4f759be8f920"));
    private static final ShoppingListId LIST_ID = new ShoppingListId(UUID.fromString("4ea3a925-1d2a-4246-a970-7a82ffc96402"));
    private final RecipeShoppingListConverter converter = new RecipeShoppingListConverter();

    @Test
    void scalesOneIngredientIntoShoppingList() {
        var recipe = recipe(4, ingredient("81d23cd8-cd6f-4692-8a0f-a49e05c779cc", "Flour", "400", QuantityUnit.GRAM));

        var result = converter.convert(recipe, new RecipeServings(2), LIST_ID);

        assertThat(result.shoppingList().id()).isEqualTo(LIST_ID);
        assertThat(result.shoppingList().items()).singleElement().satisfies(item -> {
            assertThat(item.requirement()).isEqualTo(new ShoppingRequirement("Flour"));
            assertThat(item.quantity()).isEqualTo(new Quantity(new BigDecimal("200"), QuantityUnit.GRAM));
        });
    }

    @Test
    void reusesShoppingQuantityCanonicalizationForMassAndVolume() {
        var recipe = recipe(
                2,
                ingredient("81d23cd8-cd6f-4692-8a0f-a49e05c779cc", "Flour", "0.5", QuantityUnit.KILOGRAM),
                ingredient("11388874-5a42-4863-b5cf-3c210fa70ddd", "Milk", "0.5", QuantityUnit.LITER));

        var result = converter.convert(recipe, new RecipeServings(4), LIST_ID);

        assertThat(result.shoppingList().items())
                .extracting(item -> item.quantity())
                .containsExactly(
                        new Quantity(new BigDecimal("1000"), QuantityUnit.GRAM),
                        new Quantity(new BigDecimal("1000"), QuantityUnit.MILLILITER));
    }

    @Test
    void mergesExactNormalizedRequirementWithSameCanonicalUnitBeforeScaling() {
        var recipe = recipe(
                4,
                ingredient("81d23cd8-cd6f-4692-8a0f-a49e05c779cc", "  Milk  ", "500", QuantityUnit.MILLILITER),
                ingredient("11388874-5a42-4863-b5cf-3c210fa70ddd", "Milk", "0.5", QuantityUnit.LITER));

        var result = converter.convert(recipe, new RecipeServings(2), LIST_ID);

        assertThat(result.shoppingList().items()).singleElement().satisfies(item -> {
            assertThat(item.requirement()).isEqualTo(new ShoppingRequirement("Milk"));
            assertThat(item.quantity()).isEqualTo(new Quantity(new BigDecimal("500"), QuantityUnit.MILLILITER));
        });
    }

    @Test
    void doesNotMergeCaseDifferencesSynonymsOrPhysicalDimensionMismatches() {
        var recipe = recipe(
                1,
                ingredient("81d23cd8-cd6f-4692-8a0f-a49e05c779cc", "Milk", "500", QuantityUnit.MILLILITER),
                ingredient("11388874-5a42-4863-b5cf-3c210fa70ddd", "milk", "500", QuantityUnit.MILLILITER),
                ingredient("e982cbf4-9a04-4dca-b16e-cbb0bad92023", "tomatoes", "300", QuantityUnit.GRAM),
                ingredient("d592f53c-c663-4d8b-8cab-00f8168d4b5a", "tomato", "300", QuantityUnit.GRAM),
                ingredient("22531761-b7c8-4526-b431-e6f99500fcc4", "Eggs", "600", QuantityUnit.GRAM),
                ingredient("f6a89b9f-5068-419a-b69c-d25d82bb86bc", "Eggs", "10", QuantityUnit.PIECE));

        var result = converter.convert(recipe, new RecipeServings(1), LIST_ID);

        assertThat(result.shoppingList().items()).hasSize(6);
    }

    @Test
    void preservesFirstMergeGroupOccurrenceOrder() {
        var recipe = recipe(
                1,
                ingredient("81d23cd8-cd6f-4692-8a0f-a49e05c779cc", "Milk", "250", QuantityUnit.MILLILITER),
                ingredient("11388874-5a42-4863-b5cf-3c210fa70ddd", "Flour", "200", QuantityUnit.GRAM),
                ingredient("e982cbf4-9a04-4dca-b16e-cbb0bad92023", "Milk", "250", QuantityUnit.MILLILITER),
                ingredient("d592f53c-c663-4d8b-8cab-00f8168d4b5a", "Eggs", "2", QuantityUnit.PIECE));

        var result = converter.convert(recipe, new RecipeServings(1), LIST_ID);

        assertThat(result.shoppingList().items())
                .extracting(item -> item.requirement().text())
                .containsExactly("Milk", "Flour", "Eggs");
    }

    @Test
    void usesDeterministicDecimal128ForNonTerminatingScaleRatio() {
        var recipe = recipe(3, ingredient("81d23cd8-cd6f-4692-8a0f-a49e05c779cc", "Spice", "100", QuantityUnit.GRAM));

        var first = converter.convert(recipe, new RecipeServings(1), LIST_ID);
        var second = converter.convert(recipe, new RecipeServings(1), LIST_ID);

        assertThat(first.shoppingList().items().getFirst().quantity())
                .isEqualTo(second.shoppingList().items().getFirst().quantity());
        assertThat(first.shoppingList().items().getFirst().quantity().amount())
                .isEqualByComparingTo(new BigDecimal("33.33333333333333333333333333333333"));
    }

    @Test
    void derivesItemIdentityFromListRequirementAndCanonicalUnitRatherThanPositionOrAmount() {
        var flourKg = recipe(2, ingredient("81d23cd8-cd6f-4692-8a0f-a49e05c779cc", "Flour", "0.5", QuantityUnit.KILOGRAM));
        var flourG = recipe(4, ingredient("11388874-5a42-4863-b5cf-3c210fa70ddd", "Flour", "500", QuantityUnit.GRAM));
        var milk = recipe(2, ingredient("e982cbf4-9a04-4dca-b16e-cbb0bad92023", "Milk", "500", QuantityUnit.MILLILITER));

        var flourAtBase = converter.convert(flourKg, new RecipeServings(2), LIST_ID).shoppingList().items().getFirst().id();
        var flourAtDifferentServings = converter.convert(flourKg, new RecipeServings(4), LIST_ID).shoppingList().items().getFirst().id();
        var flourCanonicalRepresentation = converter.convert(flourG, new RecipeServings(4), LIST_ID).shoppingList().items().getFirst().id();
        var milkId = converter.convert(milk, new RecipeServings(2), LIST_ID).shoppingList().items().getFirst().id();
        var otherListId = converter.convert(
                        flourKg,
                        new RecipeServings(2),
                        new ShoppingListId(UUID.fromString("b0d7c4a7-e9b2-446e-b01e-d12fd5d81f6a")))
                .shoppingList().items().getFirst().id();

        assertThat(flourAtDifferentServings).isEqualTo(flourAtBase);
        assertThat(flourCanonicalRepresentation).isEqualTo(flourAtBase);
        assertThat(milkId).isNotEqualTo(flourAtBase);
        assertThat(otherListId).isNotEqualTo(flourAtBase);
    }

    @Test
    void preservesAcceptedLiteralShoppingItemIdentityFixture() {
        var recipe = recipe(1, ingredient("81d23cd8-cd6f-4692-8a0f-a49e05c779cc", "Flour", "500", QuantityUnit.GRAM));

        var itemId = converter.convert(recipe, new RecipeServings(1), LIST_ID)
                .shoppingList().items().getFirst().id();

        assertThat(itemId.value())
                .isEqualTo(UUID.fromString("3d737f10-a263-39b3-b90a-fe7868c035b9"));
    }

    @Test
    void recordsCompleteOrderedProvenanceForMergedAndNonMergedIngredients() {
        var milkFirst = ingredient("81d23cd8-cd6f-4692-8a0f-a49e05c779cc", "Milk", "500", QuantityUnit.MILLILITER);
        var flour = ingredient("11388874-5a42-4863-b5cf-3c210fa70ddd", "Flour", "200", QuantityUnit.GRAM);
        var milkSecond = ingredient("e982cbf4-9a04-4dca-b16e-cbb0bad92023", "Milk", "0.5", QuantityUnit.LITER);
        var recipe = recipe(1, milkFirst, flour, milkSecond);

        var result = converter.convert(recipe, new RecipeServings(1), LIST_ID);
        var milkItem = result.shoppingList().items().get(0);
        var flourItem = result.shoppingList().items().get(1);

        assertThat(result.provenance()).hasSize(2);
        assertThat(result.provenance().get(milkItem.id())).containsExactly(
                new RecipeIngredientRef(RECIPE_ID, milkFirst.id()),
                new RecipeIngredientRef(RECIPE_ID, milkSecond.id()));
        assertThat(result.provenance().get(flourItem.id())).containsExactly(
                new RecipeIngredientRef(RECIPE_ID, flour.id()));
    }

    @Test
    void exposesProvenanceAsDeeplyImmutableOutput() {
        var ingredient = ingredient("81d23cd8-cd6f-4692-8a0f-a49e05c779cc", "Flour", "200", QuantityUnit.GRAM);
        var result = converter.convert(recipe(1, ingredient), new RecipeServings(1), LIST_ID);
        var itemId = result.shoppingList().items().getFirst().id();

        assertThatThrownBy(() -> result.provenance().clear())
                .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> result.provenance().get(itemId).add(new RecipeIngredientRef(RECIPE_ID, ingredient.id())))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    private static Recipe recipe(int servings, RecipeIngredient... ingredients) {
        return new Recipe(RECIPE_ID, new RecipeTitle("Test recipe"), new RecipeServings(servings), List.of(ingredients));
    }

    private static RecipeIngredient ingredient(String id, String requirement, String amount, QuantityUnit unit) {
        return new RecipeIngredient(
                new RecipeIngredientId(UUID.fromString(id)),
                new ShoppingRequirement(requirement),
                new Quantity(new BigDecimal(amount), unit));
    }
}
