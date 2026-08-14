package io.github.trueruslan.zakupgotov.recipe;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.trueruslan.zakupgotov.shopping.Quantity;
import io.github.trueruslan.zakupgotov.shopping.QuantityUnit;
import io.github.trueruslan.zakupgotov.shopping.ShoppingItemId;
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

    @Test
    void preservesFirstOccurrenceOrderAndKeepsCaseAndUnitDifferencesSeparate() {
        var recipeA = recipe(
                "73000000-0000-0000-0000-000000000011",
                1,
                ingredient("72000000-0000-0000-0000-000000000011", "Milk", "100", QuantityUnit.MILLILITER),
                ingredient("72000000-0000-0000-0000-000000000012", "Flour", "100", QuantityUnit.GRAM));
        var recipeB = recipe(
                "73000000-0000-0000-0000-000000000012",
                1,
                ingredient("72000000-0000-0000-0000-000000000013", "Milk", "200", QuantityUnit.MILLILITER),
                ingredient("72000000-0000-0000-0000-000000000014", "milk", "50", QuantityUnit.MILLILITER),
                ingredient("72000000-0000-0000-0000-000000000015", "Eggs", "2", QuantityUnit.PIECE),
                ingredient("72000000-0000-0000-0000-000000000016", "Eggs", "100", QuantityUnit.GRAM));

        var result = new RecipeShoppingListAggregator().aggregate(
                List.of(
                        new RecipeAggregationEntry(ENTRY_A, recipeA, new RecipeServings(1)),
                        new RecipeAggregationEntry(ENTRY_B, recipeB, new RecipeServings(1))),
                AGGREGATE_LIST_ID);

        var keys = result.shoppingList().items().stream()
                .map(item -> item.requirement().text() + ":" + item.quantity().unit())
                .toList();
        assertThat(keys).containsExactly(
                "Milk:MILLILITER",
                "Flour:GRAM",
                "milk:MILLILITER",
                "Eggs:PIECE",
                "Eggs:GRAM");
        assertThat(result.shoppingList().items().getFirst().quantity().amount())
                .isEqualByComparingTo("300");
    }

    @Test
    void allowsSameRecipeToAppearTwiceWhenOccurrenceIdsDiffer() {
        var milk = ingredient(
                "72000000-0000-0000-0000-000000000021",
                "Milk",
                "500",
                QuantityUnit.MILLILITER);
        var sharedRecipe = recipe("73000000-0000-0000-0000-000000000021", 1, milk);
        var sourceRef = new RecipeIngredientRef(sharedRecipe.id(), milk.id());

        var result = new RecipeShoppingListAggregator().aggregate(
                List.of(
                        new RecipeAggregationEntry(ENTRY_A, sharedRecipe, new RecipeServings(1)),
                        new RecipeAggregationEntry(ENTRY_B, sharedRecipe, new RecipeServings(2))),
                AGGREGATE_LIST_ID);

        var item = result.shoppingList().items().getFirst();
        assertThat(item.quantity().amount()).isEqualByComparingTo("1500");
        assertThat(result.provenance().get(item.id())).containsExactly(
                new RecipeAggregationIngredientRef(ENTRY_A, sourceRef),
                new RecipeAggregationIngredientRef(ENTRY_B, sourceRef));
    }

    @Test
    void rejectsEmptyAggregationEntries() {
        assertThatThrownBy(() -> new RecipeShoppingListAggregator().aggregate(List.of(), AGGREGATE_LIST_ID))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("entries must not be empty");
    }

    @Test
    void rejectsDuplicateAggregationEntryIdentity() {
        var recipeA = recipe(
                "73000000-0000-0000-0000-000000000031",
                1,
                ingredient("72000000-0000-0000-0000-000000000031", "Milk", "100", QuantityUnit.MILLILITER));
        var recipeB = recipe(
                "73000000-0000-0000-0000-000000000032",
                1,
                ingredient("72000000-0000-0000-0000-000000000032", "Flour", "100", QuantityUnit.GRAM));

        assertThatThrownBy(() -> new RecipeShoppingListAggregator().aggregate(
                        List.of(
                                new RecipeAggregationEntry(ENTRY_A, recipeA, new RecipeServings(1)),
                                new RecipeAggregationEntry(ENTRY_A, recipeB, new RecipeServings(1))),
                        AGGREGATE_LIST_ID))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("duplicate aggregation entry id");
    }

    @Test
    void finalItemIdentityIsStableAcrossAmountsAndServingsButScopedToAggregateList() {
        var ingredientA = ingredient(
                "72000000-0000-0000-0000-000000000041",
                "Milk",
                "100",
                QuantityUnit.MILLILITER);
        var ingredientB = ingredient(
                "72000000-0000-0000-0000-000000000041",
                "Milk",
                "500",
                QuantityUnit.MILLILITER);
        var recipeA = recipe("73000000-0000-0000-0000-000000000041", 1, ingredientA);
        var recipeB = recipe("73000000-0000-0000-0000-000000000041", 1, ingredientB);
        var aggregator = new RecipeShoppingListAggregator();

        var baseId = aggregator.aggregate(
                        List.of(new RecipeAggregationEntry(ENTRY_A, recipeA, new RecipeServings(1))),
                        AGGREGATE_LIST_ID)
                .shoppingList().items().getFirst().id();
        var changedAmountAndServingsId = aggregator.aggregate(
                        List.of(new RecipeAggregationEntry(ENTRY_A, recipeB, new RecipeServings(3))),
                        AGGREGATE_LIST_ID)
                .shoppingList().items().getFirst().id();
        var otherListId = aggregator.aggregate(
                        List.of(new RecipeAggregationEntry(ENTRY_A, recipeA, new RecipeServings(1))),
                        new ShoppingListId(UUID.fromString("70000000-0000-0000-0000-000000000002")))
                .shoppingList().items().getFirst().id();

        assertThat(changedAmountAndServingsId).isEqualTo(baseId);
        assertThat(otherListId).isNotEqualTo(baseId);
    }

    @Test
    void failsClosedWhenDifferentAggregateKeysDeriveSameItemIdentity() {
        var fixedId = new ShoppingItemId(UUID.fromString("74000000-0000-0000-0000-000000000001"));
        var aggregator = new RecipeShoppingListAggregator(
                new RecipeShoppingListConverter(),
                (listId, requirement, unit) -> fixedId);
        var recipe = recipe(
                "73000000-0000-0000-0000-000000000051",
                1,
                ingredient("72000000-0000-0000-0000-000000000051", "Milk", "100", QuantityUnit.MILLILITER),
                ingredient("72000000-0000-0000-0000-000000000052", "Flour", "100", QuantityUnit.GRAM));

        assertThatThrownBy(() -> aggregator.aggregate(
                        List.of(new RecipeAggregationEntry(ENTRY_A, recipe, new RecipeServings(1))),
                        AGGREGATE_LIST_ID))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("generated shopping item id collision");
    }

    @Test
    void exposesAggregateProvenanceAsDeeplyImmutableOutput() {
        var milk = ingredient(
                "72000000-0000-0000-0000-000000000061",
                "Milk",
                "100",
                QuantityUnit.MILLILITER);
        var recipe = recipe("73000000-0000-0000-0000-000000000061", 1, milk);
        var result = new RecipeShoppingListAggregator().aggregate(
                List.of(new RecipeAggregationEntry(ENTRY_A, recipe, new RecipeServings(1))),
                AGGREGATE_LIST_ID);
        var itemId = result.shoppingList().items().getFirst().id();

        assertThatThrownBy(() -> result.provenance().clear())
                .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> result.provenance().get(itemId).add(
                        new RecipeAggregationIngredientRef(
                                ENTRY_B,
                                new RecipeIngredientRef(recipe.id(), milk.id()))))
                .isInstanceOf(UnsupportedOperationException.class);
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
