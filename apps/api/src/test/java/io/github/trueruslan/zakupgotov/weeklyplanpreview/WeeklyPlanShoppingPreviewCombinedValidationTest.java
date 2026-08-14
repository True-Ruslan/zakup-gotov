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
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class WeeklyPlanShoppingPreviewCombinedValidationTest {

    @Test
    void keepsAcceptedRecipeErrorsWhenTargetServingsIsAlsoInvalid() {
        var factory = new WeeklyPlanShoppingPreviewRequestFactory(
                new WeeklyPlanShoppingPreviewIdGenerator() {
                    @Override public WeeklyPlanId nextWeeklyPlanId() {
                        return new WeeklyPlanId(UUID.randomUUID());
                    }
                    @Override public WeeklyMealOccurrenceId nextOccurrenceId() {
                        return new WeeklyMealOccurrenceId(UUID.randomUUID());
                    }
                },
                new RecipeShoppingPreviewRequestFactory(new RecipeShoppingPreviewIdGenerator() {
                    @Override public RecipeId nextRecipeId() { return new RecipeId(UUID.randomUUID()); }
                    @Override public RecipeIngredientId nextIngredientId() {
                        return new RecipeIngredientId(UUID.randomUUID());
                    }
                    @Override public ShoppingListId nextShoppingListId() {
                        return new ShoppingListId(UUID.randomUUID());
                    }
                }));

        var request = new WeeklyPlanShoppingPreviewRequest(List.of(
                new WeeklyPlanShoppingPreviewOccurrenceRequest(
                        WeeklyPlanDay.MONDAY,
                        0,
                        new WeeklyPlanShoppingPreviewRecipeRequest(
                                " ",
                                0,
                                List.of(new RecipeShoppingPreviewIngredientRequest(
                                        " ",
                                        new RecipeShoppingPreviewQuantityRequest(
                                                BigDecimal.ZERO,
                                                QuantityUnit.GRAM)))))));

        assertThatThrownBy(() -> factory.create(request))
                .isInstanceOfSatisfying(InvalidWeeklyPlanShoppingPreviewRequestException.class, exception ->
                        assertThat(exception.errors()).containsExactly(
                                new WeeklyPlanShoppingPreviewValidationError(
                                        "occurrences[0].targetServings", "must be greater than 0"),
                                new WeeklyPlanShoppingPreviewValidationError(
                                        "occurrences[0].recipe.title", "must not be blank"),
                                new WeeklyPlanShoppingPreviewValidationError(
                                        "occurrences[0].recipe.baseServings", "must be greater than 0"),
                                new WeeklyPlanShoppingPreviewValidationError(
                                        "occurrences[0].recipe.ingredients[0].requirement", "must not be blank"),
                                new WeeklyPlanShoppingPreviewValidationError(
                                        "occurrences[0].recipe.ingredients[0].quantity.amount", "must be greater than 0")));
    }
}
