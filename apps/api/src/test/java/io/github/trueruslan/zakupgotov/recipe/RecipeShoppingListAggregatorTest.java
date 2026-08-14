package io.github.trueruslan.zakupgotov.recipe;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.trueruslan.zakupgotov.shopping.Quantity;
import io.github.trueruslan.zakupgotov.shopping.QuantityUnit;
import io.github.trueruslan.zakupgotov.shopping.ShoppingListId;
import io.github.trueruslan.zakupgotov.shopping.ShoppingRequirement;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class RecipeShoppingListAggregatorTest {

    private static final ShoppingListId AGGREGATE_LIST_ID =
            new ShoppingListId(UUID.fromString("70000000-0000-0000-0000-000000000001"));
    private static final RecipeAggregationEntryId ENTRY_A =
            new RecipeAggregationEntryId(UUID.fromString("71000000-0000-0000-0000-000000000001"));
    private static final RecipeAggregationEntryId ENTRY_B =
            new RecipeAggregationEntryId(UUID.fromString("71000000-0000-0000-0000-000000000002"));

    @Test
    void mergesCompatibleCanonicalItemsAcrossRecipeEntriesWithOrderedOccurrenceProvenance() {
        var milkA = ingredient(
                "72000000-0000-0000-0000-000000000001",
                "  Milk  ",
                "0.5",
                QuantityUnit.LITER);
        var milkB = ingredient(
                "72000000-0000-0000-0000-000000000002",
                "Milk",
                "250",
                QuantityUnit.MILLILITER);
        var recipeA = recipe("73000000-0000-0000-0000-000000000001", 2, milkA);
        var recipeB = recipe("73000000-0000-0000-0000-000000000002", 1, milkB);

        var result = new RecipeShoppingListAggregator().aggregate(
                List.of(
                        new RecipeAggregationEntry(ENTRY_A, recipeA, new RecipeServings(4)),
                        new RecipeAggregationEntry(ENTRY_B, recipeB, new RecipeServings(2))),
                AGGREGATE_LIST_ID);

        assertThat(result.shoppingList().id()).isEqualTo(AGGREGATE_LIST_ID);
        assertThat(result.shoppingList().items()).singleElement().satisfies(item -> {
            assertThat(item.requirement()).isEqualTo(new ShoppingRequirement("Milk"));
            assertThat(item.quantity())
                    .isEqualTo(new Quantity(new BigDecimal("1500"), QuantityUnit.MILLILITER));
            assertThat(result.provenance().get(item.id())).containsExactly(
                    new RecipeAggregationIngredientRef(
                            ENTRY_A,
                            new RecipeIngredientRef(recipeA.id(), milkA.id())),
                    new RecipeAggregationIngredientRef(
                            ENTRY_B,
                            new RecipeIngredientRef(recipeB.id(), milkB.id())));
        });
    }

    private static Recipe recipe(String id, int servings, RecipeIngredient... ingredients) {
        return new Recipe(
                new RecipeId(UUID.fromString(id)),
                new RecipeTitle("Test recipe"),
                new RecipeServings(servings),
                List.of(ingredients));
    }

    private static RecipeIngredient ingredient(
            String id,
            String requirement,
            String amount,
            QuantityUnit unit) {
        return new RecipeIngredient(
                new RecipeIngredientId(UUID.fromString(id)),
                new ShoppingRequirement(requirement),
                new Quantity(new BigDecimal(amount), unit));
    }
}
