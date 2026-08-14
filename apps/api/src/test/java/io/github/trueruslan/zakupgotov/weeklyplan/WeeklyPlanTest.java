package io.github.trueruslan.zakupgotov.weeklyplan;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.trueruslan.zakupgotov.recipe.Recipe;
import io.github.trueruslan.zakupgotov.recipe.RecipeId;
import io.github.trueruslan.zakupgotov.recipe.RecipeIngredient;
import io.github.trueruslan.zakupgotov.recipe.RecipeIngredientId;
import io.github.trueruslan.zakupgotov.recipe.RecipeServings;
import io.github.trueruslan.zakupgotov.recipe.RecipeTitle;
import io.github.trueruslan.zakupgotov.shopping.Quantity;
import io.github.trueruslan.zakupgotov.shopping.QuantityUnit;
import io.github.trueruslan.zakupgotov.shopping.ShoppingRequirement;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class WeeklyPlanTest {

    private static final WeeklyPlanId PLAN_ID =
            new WeeklyPlanId(UUID.fromString("81000000-0000-0000-0000-000000000001"));
    private static final WeeklyMealOccurrenceId OCCURRENCE_A =
            new WeeklyMealOccurrenceId(UUID.fromString("82000000-0000-0000-0000-000000000001"));
    private static final WeeklyMealOccurrenceId OCCURRENCE_B =
            new WeeklyMealOccurrenceId(UUID.fromString("82000000-0000-0000-0000-000000000002"));

    @Test
    void preservesExplicitOrderAcrossDaysAndAllowsMultipleMealsOnOneDay() {
        var recipeA = recipe("83000000-0000-0000-0000-000000000001");
        var recipeB = recipe("83000000-0000-0000-0000-000000000002");
        var tuesday = new WeeklyMealOccurrence(
                OCCURRENCE_A, WeeklyPlanDay.TUESDAY, recipeA, new RecipeServings(2));
        var monday = new WeeklyMealOccurrence(
                OCCURRENCE_B, WeeklyPlanDay.MONDAY, recipeB, new RecipeServings(4));

        var plan = new WeeklyPlan(PLAN_ID, List.of(tuesday, monday));

        assertThat(plan.id()).isEqualTo(PLAN_ID);
        assertThat(plan.occurrences()).containsExactly(tuesday, monday);

        var sameDayPlan = new WeeklyPlan(
                PLAN_ID,
                List.of(
                        new WeeklyMealOccurrence(
                                OCCURRENCE_A, WeeklyPlanDay.WEDNESDAY, recipeA, new RecipeServings(1)),
                        new WeeklyMealOccurrence(
                                OCCURRENCE_B, WeeklyPlanDay.WEDNESDAY, recipeB, new RecipeServings(1))));
        assertThat(sameDayPlan.occurrences()).hasSize(2);
    }

    @Test
    void allowsSameRecipeMoreThanOnceWhenOccurrenceIdsDiffer() {
        var sharedRecipe = recipe("83000000-0000-0000-0000-000000000011");

        var plan = new WeeklyPlan(
                PLAN_ID,
                List.of(
                        new WeeklyMealOccurrence(
                                OCCURRENCE_A, WeeklyPlanDay.MONDAY, sharedRecipe, new RecipeServings(1)),
                        new WeeklyMealOccurrence(
                                OCCURRENCE_B, WeeklyPlanDay.FRIDAY, sharedRecipe, new RecipeServings(3))));

        assertThat(plan.occurrences()).extracting(occurrence -> occurrence.recipe().id())
                .containsExactly(sharedRecipe.id(), sharedRecipe.id());
    }

    @Test
    void defensivelyCopiesOccurrencesAndExposesImmutableOrder() {
        var mutable = new ArrayList<WeeklyMealOccurrence>();
        mutable.add(new WeeklyMealOccurrence(
                OCCURRENCE_A,
                WeeklyPlanDay.THURSDAY,
                recipe("83000000-0000-0000-0000-000000000021"),
                new RecipeServings(2)));

        var plan = new WeeklyPlan(PLAN_ID, mutable);
        mutable.add(new WeeklyMealOccurrence(
                OCCURRENCE_B,
                WeeklyPlanDay.SATURDAY,
                recipe("83000000-0000-0000-0000-000000000022"),
                new RecipeServings(2)));

        assertThat(plan.occurrences()).hasSize(1);
        assertThatThrownBy(() -> plan.occurrences().clear())
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void rejectsMissingOrEmptyPlanStateAndDuplicateOccurrenceIdentity() {
        var occurrence = new WeeklyMealOccurrence(
                OCCURRENCE_A,
                WeeklyPlanDay.MONDAY,
                recipe("83000000-0000-0000-0000-000000000031"),
                new RecipeServings(1));

        assertThatThrownBy(() -> new WeeklyPlan(null, List.of(occurrence)))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new WeeklyPlan(PLAN_ID, null))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new WeeklyPlan(PLAN_ID, List.of()))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new WeeklyPlan(PLAN_ID, java.util.Arrays.asList(occurrence, null)))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new WeeklyPlan(PLAN_ID, List.of(occurrence, occurrence)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void occurrenceRejectsMissingFieldsAndRecipeServingsRejectsNonPositiveCounts() {
        var recipe = recipe("83000000-0000-0000-0000-000000000041");
        var servings = new RecipeServings(1);

        assertThatThrownBy(() -> new WeeklyMealOccurrence(null, WeeklyPlanDay.MONDAY, recipe, servings))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new WeeklyMealOccurrence(OCCURRENCE_A, null, recipe, servings))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new WeeklyMealOccurrence(OCCURRENCE_A, WeeklyPlanDay.MONDAY, null, servings))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new WeeklyMealOccurrence(OCCURRENCE_A, WeeklyPlanDay.MONDAY, recipe, null))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new RecipeServings(0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new RecipeServings(-1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void identifierValueObjectsRejectNullUuids() {
        assertThatThrownBy(() -> new WeeklyPlanId(null))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new WeeklyMealOccurrenceId(null))
                .isInstanceOf(NullPointerException.class);
    }

    private static Recipe recipe(String id) {
        return new Recipe(
                new RecipeId(UUID.fromString(id)),
                new RecipeTitle("Test recipe"),
                new RecipeServings(2),
                List.of(new RecipeIngredient(
                        new RecipeIngredientId(UUID.randomUUID()),
                        new ShoppingRequirement("Milk"),
                        new Quantity(new BigDecimal("500"), QuantityUnit.MILLILITER))));
    }
}
