package io.github.trueruslan.zakupgotov.weeklyplan;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.trueruslan.zakupgotov.recipe.Recipe;
import io.github.trueruslan.zakupgotov.recipe.RecipeAggregationEntryId;
import io.github.trueruslan.zakupgotov.recipe.RecipeAggregationIngredientRef;
import io.github.trueruslan.zakupgotov.recipe.RecipeId;
import io.github.trueruslan.zakupgotov.recipe.RecipeIngredient;
import io.github.trueruslan.zakupgotov.recipe.RecipeIngredientId;
import io.github.trueruslan.zakupgotov.recipe.RecipeIngredientRef;
import io.github.trueruslan.zakupgotov.recipe.RecipeServings;
import io.github.trueruslan.zakupgotov.recipe.RecipeShoppingListAggregation;
import io.github.trueruslan.zakupgotov.recipe.RecipeTitle;
import io.github.trueruslan.zakupgotov.shopping.Quantity;
import io.github.trueruslan.zakupgotov.shopping.QuantityUnit;
import io.github.trueruslan.zakupgotov.shopping.ShoppingItem;
import io.github.trueruslan.zakupgotov.shopping.ShoppingItemId;
import io.github.trueruslan.zakupgotov.shopping.ShoppingList;
import io.github.trueruslan.zakupgotov.shopping.ShoppingListId;
import io.github.trueruslan.zakupgotov.shopping.ShoppingRequirement;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class WeeklyPlanShoppingListComposerHardeningTest {

    private static final WeeklyPlanId PLAN_ID =
            new WeeklyPlanId(UUID.fromString("88000000-0000-0000-0000-000000000001"));
    private static final WeeklyMealOccurrenceId OCCURRENCE_A =
            new WeeklyMealOccurrenceId(UUID.fromString("89000000-0000-0000-0000-000000000001"));
    private static final WeeklyMealOccurrenceId OCCURRENCE_B =
            new WeeklyMealOccurrenceId(UUID.fromString("89000000-0000-0000-0000-000000000002"));
    private static final ShoppingListId LIST_ID =
            new ShoppingListId(UUID.fromString("8a000000-0000-0000-0000-000000000001"));
    private static final ShoppingItemId ITEM_A =
            new ShoppingItemId(UUID.fromString("8b000000-0000-0000-0000-000000000001"));
    private static final ShoppingItemId ITEM_B =
            new ShoppingItemId(UUID.fromString("8b000000-0000-0000-0000-000000000002"));

    @Test
    void rejectsFinalShoppingItemWithoutProvenance() {
        var composer = composerReturning((entries, listId) ->
                new RecipeShoppingListAggregation(listWith(ITEM_A), Map.of()));

        assertThatThrownBy(() -> composer.compose(singleOccurrencePlan()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void rejectsOrphanProvenanceKey() {
        var composer = composerReturning((entries, listId) -> {
            var ref = validRef(entries.getFirst().id());
            return new RecipeShoppingListAggregation(listWith(ITEM_A), Map.of(ITEM_B, List.of(ref)));
        });

        assertThatThrownBy(() -> composer.compose(singleOccurrencePlan()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void rejectsEmptyAggregateProvenanceList() {
        var composer = composerReturning((entries, listId) ->
                new RecipeShoppingListAggregation(listWith(ITEM_A), Map.of(ITEM_A, List.of())));

        assertThatThrownBy(() -> composer.compose(singleOccurrencePlan()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void rejectsUnknownInternalAggregationEntryInProvenance() {
        var unknown = new RecipeAggregationEntryId(
                UUID.fromString("8c000000-0000-0000-0000-000000000099"));
        var composer = composerReturning((entries, listId) -> new RecipeShoppingListAggregation(
                listWith(ITEM_A), Map.of(ITEM_A, List.of(validRef(unknown)))));

        assertThatThrownBy(() -> composer.compose(singleOccurrencePlan()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void rejectsGeneratedInternalAggregationIdCollision() {
        var collision = new RecipeAggregationEntryId(
                UUID.fromString("8c000000-0000-0000-0000-000000000001"));
        var identity = new WeeklyPlanIdentityDeriver() {
            @Override
            public ShoppingListId shoppingListId(WeeklyPlanId planId) {
                return LIST_ID;
            }

            @Override
            public RecipeAggregationEntryId aggregationEntryId(
                    WeeklyPlanId planId, WeeklyMealOccurrenceId occurrenceId) {
                return collision;
            }
        };
        RecipeAggregationBoundary unused = (entries, listId) -> {
            throw new AssertionError("aggregation must not be invoked after identity collision");
        };
        var composer = new WeeklyPlanShoppingListComposer(unused, identity);

        assertThatThrownBy(() -> composer.compose(twoOccurrencePlan()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void rejectsNullDerivedShoppingListIdAndNullPlan() {
        var identity = new WeeklyPlanIdentityDeriver() {
            @Override
            public ShoppingListId shoppingListId(WeeklyPlanId planId) {
                return null;
            }

            @Override
            public RecipeAggregationEntryId aggregationEntryId(
                    WeeklyPlanId planId, WeeklyMealOccurrenceId occurrenceId) {
                throw new AssertionError("entry id must not be requested");
            }
        };
        var composer = new WeeklyPlanShoppingListComposer(
                (entries, listId) -> { throw new AssertionError("must not aggregate"); }, identity);

        assertThatThrownBy(() -> composer.compose(singleOccurrencePlan()))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new WeeklyPlanShoppingListComposer().compose(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void plannerProvenanceIsDeeplyImmutableAndReturnsExactAggregateShoppingList() {
        var capturedList = listWith(ITEM_A);
        RecipeAggregationBoundary boundary = (entries, listId) -> new RecipeShoppingListAggregation(
                capturedList,
                Map.of(ITEM_A, List.of(validRef(entries.getFirst().id()))));
        var composer = new WeeklyPlanShoppingListComposer(boundary, fixedIdentity());

        var result = composer.compose(singleOccurrencePlan());

        assertThat(result.shoppingList()).isSameAs(capturedList);
        assertThatThrownBy(() -> result.provenance().clear())
                .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> result.provenance().get(ITEM_A).clear())
                .isInstanceOf(UnsupportedOperationException.class);
    }

    private static WeeklyPlanShoppingListComposer composerReturning(RecipeAggregationBoundary boundary) {
        return new WeeklyPlanShoppingListComposer(boundary, fixedIdentity());
    }

    private static WeeklyPlanIdentityDeriver fixedIdentity() {
        return new WeeklyPlanIdentityDeriver() {
            @Override
            public ShoppingListId shoppingListId(WeeklyPlanId planId) {
                return LIST_ID;
            }

            @Override
            public RecipeAggregationEntryId aggregationEntryId(
                    WeeklyPlanId planId, WeeklyMealOccurrenceId occurrenceId) {
                var suffix = occurrenceId.equals(OCCURRENCE_A) ? 1 : 2;
                return new RecipeAggregationEntryId(UUID.fromString(
                        "8c000000-0000-0000-0000-%012d".formatted(suffix)));
            }
        };
    }

    private static RecipeAggregationIngredientRef validRef(RecipeAggregationEntryId entryId) {
        var recipe = recipe("8d000000-0000-0000-0000-000000000001");
        return new RecipeAggregationIngredientRef(
                entryId,
                new RecipeIngredientRef(recipe.id(), recipe.ingredients().getFirst().id()));
    }

    private static ShoppingList listWith(ShoppingItemId itemId) {
        var list = new ShoppingList(LIST_ID);
        list.add(new ShoppingItem(
                itemId,
                new ShoppingRequirement("Milk"),
                new Quantity(new BigDecimal("100"), QuantityUnit.MILLILITER)));
        return list;
    }

    private static WeeklyPlan singleOccurrencePlan() {
        return new WeeklyPlan(
                PLAN_ID,
                List.of(new WeeklyMealOccurrence(
                        OCCURRENCE_A,
                        WeeklyPlanDay.MONDAY,
                        recipe("8d000000-0000-0000-0000-000000000011"),
                        new RecipeServings(1))));
    }

    private static WeeklyPlan twoOccurrencePlan() {
        var recipe = recipe("8d000000-0000-0000-0000-000000000021");
        return new WeeklyPlan(
                PLAN_ID,
                List.of(
                        new WeeklyMealOccurrence(
                                OCCURRENCE_A, WeeklyPlanDay.MONDAY, recipe, new RecipeServings(1)),
                        new WeeklyMealOccurrence(
                                OCCURRENCE_B, WeeklyPlanDay.TUESDAY, recipe, new RecipeServings(1))));
    }

    private static Recipe recipe(String id) {
        return new Recipe(
                new RecipeId(UUID.fromString(id)),
                new RecipeTitle("Test recipe"),
                new RecipeServings(1),
                List.of(new RecipeIngredient(
                        new RecipeIngredientId(UUID.fromString("8e000000-0000-0000-0000-000000000001")),
                        new ShoppingRequirement("Milk"),
                        new Quantity(new BigDecimal("100"), QuantityUnit.MILLILITER))));
    }
}
