package io.github.trueruslan.zakupgotov.weeklyplan;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.trueruslan.zakupgotov.recipe.Recipe;
import io.github.trueruslan.zakupgotov.recipe.RecipeId;
import io.github.trueruslan.zakupgotov.recipe.RecipeIngredient;
import io.github.trueruslan.zakupgotov.recipe.RecipeIngredientId;
import io.github.trueruslan.zakupgotov.recipe.RecipeIngredientRef;
import io.github.trueruslan.zakupgotov.recipe.RecipeServings;
import io.github.trueruslan.zakupgotov.recipe.RecipeTitle;
import io.github.trueruslan.zakupgotov.shopping.Quantity;
import io.github.trueruslan.zakupgotov.shopping.QuantityUnit;
import io.github.trueruslan.zakupgotov.shopping.ShoppingRequirement;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class WeeklyPlanShoppingListComposerTest {

    private static final WeeklyPlanId PLAN_ID =
            new WeeklyPlanId(UUID.fromString("84000000-0000-0000-0000-000000000001"));
    private static final WeeklyPlanId OTHER_PLAN_ID =
            new WeeklyPlanId(UUID.fromString("84000000-0000-0000-0000-000000000002"));
    private static final WeeklyMealOccurrenceId OCCURRENCE_A =
            new WeeklyMealOccurrenceId(UUID.fromString("85000000-0000-0000-0000-000000000001"));
    private static final WeeklyMealOccurrenceId OCCURRENCE_B =
            new WeeklyMealOccurrenceId(UUID.fromString("85000000-0000-0000-0000-000000000002"));

    private final WeeklyPlanShoppingListComposer composer = new WeeklyPlanShoppingListComposer();

    @Test
    void composesCompatibleWeeklyOccurrencesThroughAcceptedM2Aggregation() {
        var milkA = ingredient(
                "86000000-0000-0000-0000-000000000001",
                "Milk",
                "0.5",
                QuantityUnit.LITER);
        var milkB = ingredient(
                "86000000-0000-0000-0000-000000000002",
                "Milk",
                "250",
                QuantityUnit.MILLILITER);
        var recipeA = recipe("87000000-0000-0000-0000-000000000001", 2, milkA);
        var recipeB = recipe("87000000-0000-0000-0000-000000000002", 1, milkB);
        var plan = new WeeklyPlan(
                PLAN_ID,
                List.of(
                        occurrence(OCCURRENCE_A, WeeklyPlanDay.TUESDAY, recipeA, 4),
                        occurrence(OCCURRENCE_B, WeeklyPlanDay.MONDAY, recipeB, 2)));

        var result = composer.compose(plan);

        assertThat(result.shoppingList().items()).singleElement().satisfies(item -> {
            assertThat(item.requirement()).isEqualTo(new ShoppingRequirement("Milk"));
            assertThat(item.quantity())
                    .isEqualTo(new Quantity(new BigDecimal("1500"), QuantityUnit.MILLILITER));
            assertThat(result.provenance().get(item.id())).containsExactly(
                    new WeeklyPlanIngredientRef(
                            OCCURRENCE_A,
                            new RecipeIngredientRef(recipeA.id(), milkA.id())),
                    new WeeklyPlanIngredientRef(
                            OCCURRENCE_B,
                            new RecipeIngredientRef(recipeB.id(), milkB.id())));
        });
    }

    @Test
    void repeatedRecipeOccurrencesRemainDistinctInPlannerProvenance() {
        var milk = ingredient(
                "86000000-0000-0000-0000-000000000011",
                "Milk",
                "500",
                QuantityUnit.MILLILITER);
        var sharedRecipe = recipe("87000000-0000-0000-0000-000000000011", 1, milk);
        var source = new RecipeIngredientRef(sharedRecipe.id(), milk.id());
        var plan = new WeeklyPlan(
                PLAN_ID,
                List.of(
                        occurrence(OCCURRENCE_A, WeeklyPlanDay.MONDAY, sharedRecipe, 1),
                        occurrence(OCCURRENCE_B, WeeklyPlanDay.FRIDAY, sharedRecipe, 2)));

        var result = composer.compose(plan);
        var item = result.shoppingList().items().getFirst();

        assertThat(item.quantity().amount()).isEqualByComparingTo("1500");
        assertThat(result.provenance().get(item.id())).containsExactly(
                new WeeklyPlanIngredientRef(OCCURRENCE_A, source),
                new WeeklyPlanIngredientRef(OCCURRENCE_B, source));
    }

    @Test
    void preservesExplicitOccurrenceOrderRatherThanSortingByDay() {
        var flour = ingredient(
                "86000000-0000-0000-0000-000000000021",
                "Flour",
                "100",
                QuantityUnit.GRAM);
        var milk = ingredient(
                "86000000-0000-0000-0000-000000000022",
                "Milk",
                "100",
                QuantityUnit.MILLILITER);
        var tuesdayRecipe = recipe("87000000-0000-0000-0000-000000000021", 1, flour);
        var mondayRecipe = recipe("87000000-0000-0000-0000-000000000022", 1, milk);
        var plan = new WeeklyPlan(
                PLAN_ID,
                List.of(
                        occurrence(OCCURRENCE_A, WeeklyPlanDay.TUESDAY, tuesdayRecipe, 1),
                        occurrence(OCCURRENCE_B, WeeklyPlanDay.MONDAY, mondayRecipe, 1)));

        var keys = composer.compose(plan).shoppingList().items().stream()
                .map(item -> item.requirement().text())
                .toList();

        assertThat(keys).containsExactly("Flour", "Milk");
    }

    @Test
    void itemIdentityIsStableAcrossServingAndDayChangesButScopedToWeeklyPlan() {
        var milk = ingredient(
                "86000000-0000-0000-0000-000000000031",
                "Milk",
                "100",
                QuantityUnit.MILLILITER);
        var recipe = recipe("87000000-0000-0000-0000-000000000031", 1, milk);

        var base = composer.compose(new WeeklyPlan(
                PLAN_ID,
                List.of(occurrence(OCCURRENCE_A, WeeklyPlanDay.MONDAY, recipe, 1))));
        var changed = composer.compose(new WeeklyPlan(
                PLAN_ID,
                List.of(occurrence(OCCURRENCE_A, WeeklyPlanDay.SUNDAY, recipe, 3))));
        var otherPlan = composer.compose(new WeeklyPlan(
                OTHER_PLAN_ID,
                List.of(occurrence(OCCURRENCE_A, WeeklyPlanDay.MONDAY, recipe, 1))));

        var baseItem = base.shoppingList().items().getFirst();
        var changedItem = changed.shoppingList().items().getFirst();
        var otherItem = otherPlan.shoppingList().items().getFirst();

        assertThat(changedItem.id()).isEqualTo(baseItem.id());
        assertThat(changedItem.quantity().amount()).isEqualByComparingTo("300");
        assertThat(baseItem.quantity().amount()).isEqualByComparingTo("100");
        assertThat(otherPlan.shoppingList().id()).isNotEqualTo(base.shoppingList().id());
        assertThat(otherItem.id()).isNotEqualTo(baseItem.id());
    }

    @Test
    void reorderingOccurrencesCanChangeOutputOrderWithoutChangingMergeKeyIdentities() {
        var flour = ingredient(
                "86000000-0000-0000-0000-000000000041",
                "Flour",
                "100",
                QuantityUnit.GRAM);
        var milk = ingredient(
                "86000000-0000-0000-0000-000000000042",
                "Milk",
                "100",
                QuantityUnit.MILLILITER);
        var recipeA = recipe("87000000-0000-0000-0000-000000000041", 1, flour);
        var recipeB = recipe("87000000-0000-0000-0000-000000000042", 1, milk);

        var first = composer.compose(new WeeklyPlan(
                PLAN_ID,
                List.of(
                        occurrence(OCCURRENCE_A, WeeklyPlanDay.MONDAY, recipeA, 1),
                        occurrence(OCCURRENCE_B, WeeklyPlanDay.TUESDAY, recipeB, 1))));
        var reversed = composer.compose(new WeeklyPlan(
                PLAN_ID,
                List.of(
                        occurrence(OCCURRENCE_B, WeeklyPlanDay.TUESDAY, recipeB, 1),
                        occurrence(OCCURRENCE_A, WeeklyPlanDay.MONDAY, recipeA, 1))));

        assertThat(first.shoppingList().items()).extracting(item -> item.requirement().text())
                .containsExactly("Flour", "Milk");
        assertThat(reversed.shoppingList().items()).extracting(item -> item.requirement().text())
                .containsExactly("Milk", "Flour");

        var firstIdsByRequirement = first.shoppingList().items().stream()
                .collect(java.util.stream.Collectors.toMap(item -> item.requirement().text(), item -> item.id()));
        var reversedIdsByRequirement = reversed.shoppingList().items().stream()
                .collect(java.util.stream.Collectors.toMap(item -> item.requirement().text(), item -> item.id()));
        assertThat(reversedIdsByRequirement).isEqualTo(firstIdsByRequirement);
    }

    private static WeeklyMealOccurrence occurrence(
            WeeklyMealOccurrenceId id,
            WeeklyPlanDay day,
            Recipe recipe,
            int servings) {
        return new WeeklyMealOccurrence(id, day, recipe, new RecipeServings(servings));
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
