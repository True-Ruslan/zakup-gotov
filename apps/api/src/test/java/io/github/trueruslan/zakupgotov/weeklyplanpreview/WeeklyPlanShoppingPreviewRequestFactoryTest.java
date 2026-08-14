package io.github.trueruslan.zakupgotov.weeklyplanpreview;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.trueruslan.zakupgotov.recipe.RecipeId;
import io.github.trueruslan.zakupgotov.recipe.RecipeIngredientId;
import io.github.trueruslan.zakupgotov.recipepreview.RecipeShoppingPreviewIdGenerator;
import io.github.trueruslan.zakupgotov.recipepreview.RecipeShoppingPreviewIngredientRequest;
import io.github.trueruslan.zakupgotov.recipepreview.RecipeShoppingPreviewQuantityRequest;
import io.github.trueruslan.zakupgotov.recipepreview.RecipeShoppingPreviewRequestFactory;
import io.github.trueruslan.zakupgotov.shopping.QuantityUnit;
import io.github.trueruslan.zakupgotov.shopping.ShoppingListId;
import io.github.trueruslan.zakupgotov.weeklyplan.WeeklyMealOccurrenceId;
import io.github.trueruslan.zakupgotov.weeklyplan.WeeklyPlanDay;
import io.github.trueruslan.zakupgotov.weeklyplan.WeeklyPlanId;
import java.math.BigDecimal;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class WeeklyPlanShoppingPreviewRequestFactoryTest {

    private static final WeeklyPlanId PLAN_ID =
            new WeeklyPlanId(UUID.fromString("91000000-0000-0000-0000-000000000001"));
    private static final WeeklyMealOccurrenceId OCCURRENCE_A =
            new WeeklyMealOccurrenceId(UUID.fromString("92000000-0000-0000-0000-000000000001"));
    private static final WeeklyMealOccurrenceId OCCURRENCE_B =
            new WeeklyMealOccurrenceId(UUID.fromString("92000000-0000-0000-0000-000000000002"));

    @Test
    void buildsOrderedWeeklyPlanAndDelegatesRecipeNormalization() {
        var weeklyIds = new QueuedWeeklyIds(PLAN_ID, OCCURRENCE_A, OCCURRENCE_B);
        var recipeIds = new QueuedRecipeIds(
                List.of(
                        uuid("93000000-0000-0000-0000-000000000001"),
                        uuid("93000000-0000-0000-0000-000000000002")),
                List.of(
                        uuid("94000000-0000-0000-0000-000000000001"),
                        uuid("94000000-0000-0000-0000-000000000002")));
        var factory = new WeeklyPlanShoppingPreviewRequestFactory(
                weeklyIds,
                new RecipeShoppingPreviewRequestFactory(recipeIds));

        var input = factory.create(new WeeklyPlanShoppingPreviewRequest(List.of(
                occurrence(WeeklyPlanDay.TUESDAY, 4, "  Pasta  ", 2, "  Milk  ", "0.5", QuantityUnit.LITER),
                occurrence(WeeklyPlanDay.MONDAY, 2, "Soup", 1, "Salt", "5", QuantityUnit.GRAM))));

        assertThat(input.weeklyPlan().id()).isEqualTo(PLAN_ID);
        assertThat(input.weeklyPlan().occurrences()).extracting(occurrence -> occurrence.id())
                .containsExactly(OCCURRENCE_A, OCCURRENCE_B);
        assertThat(input.weeklyPlan().occurrences()).extracting(occurrence -> occurrence.day())
                .containsExactly(WeeklyPlanDay.TUESDAY, WeeklyPlanDay.MONDAY);
        assertThat(input.weeklyPlan().occurrences()).extracting(occurrence -> occurrence.targetServings().value())
                .containsExactly(4, 2);
        assertThat(input.weeklyPlan().occurrences().getFirst().recipe().title().value()).isEqualTo("Pasta");
        assertThat(input.weeklyPlan().occurrences().getFirst().recipe().ingredients().getFirst().quantity().amount())
                .isEqualByComparingTo("500");
        assertThat(input.weeklyPlan().occurrences().getFirst().recipe().ingredients().getFirst().quantity().unit())
                .isEqualTo(QuantityUnit.MILLILITER);
        assertThat(recipeIds.calls).containsExactly(
                "recipe", "ingredient", "list",
                "recipe", "ingredient", "list");
    }

    @Test
    void repeatedRecipePayloadStillGetsDistinctOccurrenceAndRecipeIdentity() {
        var weeklyIds = new QueuedWeeklyIds(PLAN_ID, OCCURRENCE_A, OCCURRENCE_B);
        var recipeIds = new QueuedRecipeIds(
                List.of(
                        uuid("93000000-0000-0000-0000-000000000011"),
                        uuid("93000000-0000-0000-0000-000000000012")),
                List.of(
                        uuid("94000000-0000-0000-0000-000000000011"),
                        uuid("94000000-0000-0000-0000-000000000012")));
        var factory = new WeeklyPlanShoppingPreviewRequestFactory(
                weeklyIds,
                new RecipeShoppingPreviewRequestFactory(recipeIds));
        var sameRecipe = recipe("Pasta", 2, "Milk", "100", QuantityUnit.MILLILITER);

        var input = factory.create(new WeeklyPlanShoppingPreviewRequest(List.of(
                new WeeklyPlanShoppingPreviewOccurrenceRequest(WeeklyPlanDay.MONDAY, 2, sameRecipe),
                new WeeklyPlanShoppingPreviewOccurrenceRequest(WeeklyPlanDay.FRIDAY, 3, sameRecipe))));

        var first = input.weeklyPlan().occurrences().get(0);
        var second = input.weeklyPlan().occurrences().get(1);
        assertThat(first.id()).isNotEqualTo(second.id());
        assertThat(first.recipe().id()).isNotEqualTo(second.recipe().id());
    }

    @Test
    void rejectsNullEmptyAndMoreThanThirtyFiveOccurrences() {
        var factory = factory();

        assertThatThrownBy(() -> factory.create(null))
                .isInstanceOfSatisfying(InvalidWeeklyPlanShoppingPreviewRequestException.class, exception ->
                        assertThat(exception.errors()).containsExactly(error("$request", "must not be null")));
        assertThatThrownBy(() -> factory.create(new WeeklyPlanShoppingPreviewRequest(null)))
                .isInstanceOfSatisfying(InvalidWeeklyPlanShoppingPreviewRequestException.class, exception ->
                        assertThat(exception.errors()).containsExactly(error("occurrences", "must not be null")));
        assertThatThrownBy(() -> factory.create(new WeeklyPlanShoppingPreviewRequest(List.of())))
                .isInstanceOfSatisfying(InvalidWeeklyPlanShoppingPreviewRequestException.class, exception ->
                        assertThat(exception.errors()).containsExactly(error("occurrences", "must contain at least one occurrence")));

        var valid = occurrence(WeeklyPlanDay.MONDAY, 1, "Recipe", 1, "Item", "1", QuantityUnit.PIECE);
        assertThatThrownBy(() -> factory.create(new WeeklyPlanShoppingPreviewRequest(Collections.nCopies(36, valid))))
                .isInstanceOfSatisfying(InvalidWeeklyPlanShoppingPreviewRequestException.class, exception ->
                        assertThat(exception.errors()).containsExactly(error("occurrences", "must not exceed 35 occurrences")));
    }

    @Test
    void prefixesPlannerAndNestedRecipeErrorsInOccurrenceOrder() {
        var occurrences = new ArrayList<WeeklyPlanShoppingPreviewOccurrenceRequest>();
        occurrences.add(null);
        occurrences.add(new WeeklyPlanShoppingPreviewOccurrenceRequest(null, 0, null));
        occurrences.add(new WeeklyPlanShoppingPreviewOccurrenceRequest(
                WeeklyPlanDay.WEDNESDAY,
                2,
                new WeeklyPlanShoppingPreviewRecipeRequest(
                        " ",
                        -1,
                        List.of(new RecipeShoppingPreviewIngredientRequest(
                                " ",
                                new RecipeShoppingPreviewQuantityRequest(BigDecimal.ZERO, null))))));

        assertThatThrownBy(() -> factory().create(new WeeklyPlanShoppingPreviewRequest(occurrences)))
                .isInstanceOfSatisfying(InvalidWeeklyPlanShoppingPreviewRequestException.class, exception ->
                        assertThat(exception.errors()).containsExactly(
                                error("occurrences[0]", "must not be null"),
                                error("occurrences[1].day", "must not be null"),
                                error("occurrences[1].targetServings", "must be greater than 0"),
                                error("occurrences[1].recipe", "must not be null"),
                                error("occurrences[2].recipe.title", "must not be blank"),
                                error("occurrences[2].recipe.baseServings", "must be greater than 0"),
                                error("occurrences[2].recipe.ingredients[0].requirement", "must not be blank"),
                                error("occurrences[2].recipe.ingredients[0].quantity.amount", "must be greater than 0"),
                                error("occurrences[2].recipe.ingredients[0].quantity.unit", "must not be null")));
    }

    private static WeeklyPlanShoppingPreviewRequestFactory factory() {
        return new WeeklyPlanShoppingPreviewRequestFactory(
                new QueuedWeeklyIds(PLAN_ID, OCCURRENCE_A, OCCURRENCE_B),
                new RecipeShoppingPreviewRequestFactory(new QueuedRecipeIds(
                        List.of(uuid("93000000-0000-0000-0000-000000000021")),
                        List.of(uuid("94000000-0000-0000-0000-000000000021")))));
    }

    private static WeeklyPlanShoppingPreviewOccurrenceRequest occurrence(
            WeeklyPlanDay day,
            int targetServings,
            String title,
            int baseServings,
            String requirement,
            String amount,
            QuantityUnit unit) {
        return new WeeklyPlanShoppingPreviewOccurrenceRequest(
                day,
                targetServings,
                recipe(title, baseServings, requirement, amount, unit));
    }

    private static WeeklyPlanShoppingPreviewRecipeRequest recipe(
            String title,
            int baseServings,
            String requirement,
            String amount,
            QuantityUnit unit) {
        return new WeeklyPlanShoppingPreviewRecipeRequest(
                title,
                baseServings,
                List.of(new RecipeShoppingPreviewIngredientRequest(
                        requirement,
                        new RecipeShoppingPreviewQuantityRequest(new BigDecimal(amount), unit))));
    }

    private static WeeklyPlanShoppingPreviewValidationError error(String field, String message) {
        return new WeeklyPlanShoppingPreviewValidationError(field, message);
    }

    private static UUID uuid(String value) {
        return UUID.fromString(value);
    }

    private static final class QueuedWeeklyIds implements WeeklyPlanShoppingPreviewIdGenerator {
        private final WeeklyPlanId planId;
        private final ArrayDeque<WeeklyMealOccurrenceId> occurrenceIds;

        private QueuedWeeklyIds(WeeklyPlanId planId, WeeklyMealOccurrenceId... occurrenceIds) {
            this.planId = planId;
            this.occurrenceIds = new ArrayDeque<>(List.of(occurrenceIds));
        }

        @Override
        public WeeklyPlanId nextWeeklyPlanId() {
            return planId;
        }

        @Override
        public WeeklyMealOccurrenceId nextOccurrenceId() {
            return occurrenceIds.removeFirst();
        }
    }

    private static final class QueuedRecipeIds implements RecipeShoppingPreviewIdGenerator {
        private final ArrayDeque<UUID> recipeIds;
        private final ArrayDeque<UUID> ingredientIds;
        private final ArrayList<String> calls = new ArrayList<>();

        private QueuedRecipeIds(List<UUID> recipeIds, List<UUID> ingredientIds) {
            this.recipeIds = new ArrayDeque<>(recipeIds);
            this.ingredientIds = new ArrayDeque<>(ingredientIds);
        }

        @Override
        public RecipeId nextRecipeId() {
            calls.add("recipe");
            return new RecipeId(recipeIds.removeFirst());
        }

        @Override
        public RecipeIngredientId nextIngredientId() {
            calls.add("ingredient");
            return new RecipeIngredientId(ingredientIds.removeFirst());
        }

        @Override
        public ShoppingListId nextShoppingListId() {
            calls.add("list");
            return new ShoppingListId(UUID.randomUUID());
        }
    }
}
