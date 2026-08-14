package io.github.trueruslan.zakupgotov.weeklyplanpreview;

import static org.assertj.core.api.Assertions.assertThat;

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
import io.github.trueruslan.zakupgotov.weeklyplan.WeeklyPlanShoppingListComposer;
import java.math.BigDecimal;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class WeeklyPlanShoppingPreviewServiceTest {

    private static final UUID PLAN_ID = uuid("95000000-0000-0000-0000-000000000001");
    private static final UUID OCCURRENCE_A = uuid("96000000-0000-0000-0000-000000000001");
    private static final UUID OCCURRENCE_B = uuid("96000000-0000-0000-0000-000000000002");
    private static final UUID RECIPE_A = uuid("97000000-0000-0000-0000-000000000001");
    private static final UUID RECIPE_B = uuid("97000000-0000-0000-0000-000000000002");
    private static final UUID INGREDIENT_A = uuid("98000000-0000-0000-0000-000000000001");
    private static final UUID INGREDIENT_B = uuid("98000000-0000-0000-0000-000000000002");

    @Test
    void composesAcceptedWeeklyPlanAndProjectsSelfContainedOccurrenceRecipeProvenance() {
        var factory = new WeeklyPlanShoppingPreviewRequestFactory(
                new WeeklyIds(),
                new RecipeShoppingPreviewRequestFactory(new RecipeIds()));
        var service = new WeeklyPlanShoppingPreviewService(factory, new WeeklyPlanShoppingListComposer());
        var request = new WeeklyPlanShoppingPreviewRequest(List.of(
                occurrence(WeeklyPlanDay.TUESDAY, 4, "Pasta", 2, "Milk", "0.5", QuantityUnit.LITER),
                occurrence(WeeklyPlanDay.MONDAY, 2, "Soup", 1, "Milk", "250", QuantityUnit.MILLILITER)));

        var preview = service.create(request);

        assertThat(preview.weeklyPlan().id()).isEqualTo(PLAN_ID);
        assertThat(preview.weeklyPlan().occurrences()).extracting(WeeklyPlanShoppingPreviewOccurrence::id)
                .containsExactly(OCCURRENCE_A, OCCURRENCE_B);
        assertThat(preview.weeklyPlan().occurrences()).extracting(WeeklyPlanShoppingPreviewOccurrence::day)
                .containsExactly(WeeklyPlanDay.TUESDAY, WeeklyPlanDay.MONDAY);
        assertThat(preview.weeklyPlan().occurrences()).extracting(WeeklyPlanShoppingPreviewOccurrence::targetServings)
                .containsExactly(4, 2);
        assertThat(preview.weeklyPlan().occurrences().get(0).recipe().id()).isEqualTo(RECIPE_A);
        assertThat(preview.weeklyPlan().occurrences().get(1).recipe().id()).isEqualTo(RECIPE_B);
        assertThat(preview.weeklyPlan().occurrences().get(0).recipe().ingredients().getFirst().id())
                .isEqualTo(INGREDIENT_A);
        assertThat(preview.weeklyPlan().occurrences().get(1).recipe().ingredients().getFirst().id())
                .isEqualTo(INGREDIENT_B);

        assertThat(preview.shoppingList().items()).singleElement().satisfies(item -> {
            assertThat(item.requirement()).isEqualTo("Milk");
            assertThat(item.quantity().amount()).isEqualByComparingTo("1500");
            assertThat(item.quantity().unit()).isEqualTo(QuantityUnit.MILLILITER);
            assertThat(item.sources()).containsExactly(
                    new WeeklyPlanShoppingPreviewSource(OCCURRENCE_A, RECIPE_A, INGREDIENT_A),
                    new WeeklyPlanShoppingPreviewSource(OCCURRENCE_B, RECIPE_B, INGREDIENT_B));
        });
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
                new WeeklyPlanShoppingPreviewRecipeRequest(
                        title,
                        baseServings,
                        List.of(new RecipeShoppingPreviewIngredientRequest(
                                requirement,
                                new RecipeShoppingPreviewQuantityRequest(new BigDecimal(amount), unit)))));
    }

    private static UUID uuid(String value) {
        return UUID.fromString(value);
    }

    private static final class WeeklyIds implements WeeklyPlanShoppingPreviewIdGenerator {
        private final ArrayDeque<WeeklyMealOccurrenceId> occurrences = new ArrayDeque<>(List.of(
                new WeeklyMealOccurrenceId(OCCURRENCE_A),
                new WeeklyMealOccurrenceId(OCCURRENCE_B)));

        @Override
        public WeeklyPlanId nextWeeklyPlanId() {
            return new WeeklyPlanId(PLAN_ID);
        }

        @Override
        public WeeklyMealOccurrenceId nextOccurrenceId() {
            return occurrences.removeFirst();
        }
    }

    private static final class RecipeIds implements RecipeShoppingPreviewIdGenerator {
        private final ArrayDeque<RecipeId> recipes = new ArrayDeque<>(List.of(
                new RecipeId(RECIPE_A),
                new RecipeId(RECIPE_B)));
        private final ArrayDeque<RecipeIngredientId> ingredients = new ArrayDeque<>(List.of(
                new RecipeIngredientId(INGREDIENT_A),
                new RecipeIngredientId(INGREDIENT_B)));
        private final ArrayList<ShoppingListId> ignoredLists = new ArrayList<>();

        @Override
        public RecipeId nextRecipeId() {
            return recipes.removeFirst();
        }

        @Override
        public RecipeIngredientId nextIngredientId() {
            return ingredients.removeFirst();
        }

        @Override
        public ShoppingListId nextShoppingListId() {
            var id = new ShoppingListId(UUID.randomUUID());
            ignoredLists.add(id);
            return id;
        }
    }
}
