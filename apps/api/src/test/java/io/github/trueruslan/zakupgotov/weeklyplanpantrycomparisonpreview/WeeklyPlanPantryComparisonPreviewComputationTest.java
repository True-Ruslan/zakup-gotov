package io.github.trueruslan.zakupgotov.weeklyplanpantrycomparisonpreview;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.trueruslan.zakupgotov.preview.ComparisonPreviewService;
import io.github.trueruslan.zakupgotov.preview.NoopComparisonRuntimeEvidenceSource;
import io.github.trueruslan.zakupgotov.recipe.RecipeId;
import io.github.trueruslan.zakupgotov.recipe.RecipeIngredientId;
import io.github.trueruslan.zakupgotov.recipepreview.RecipeShoppingPreviewIdGenerator;
import io.github.trueruslan.zakupgotov.recipepreview.RecipeShoppingPreviewIngredientRequest;
import io.github.trueruslan.zakupgotov.recipepreview.RecipeShoppingPreviewQuantityRequest;
import io.github.trueruslan.zakupgotov.recipepreview.RecipeShoppingPreviewRequestFactory;
import io.github.trueruslan.zakupgotov.retailer.RetailerRegistry;
import io.github.trueruslan.zakupgotov.shopping.QuantityUnit;
import io.github.trueruslan.zakupgotov.shopping.ShoppingListId;
import io.github.trueruslan.zakupgotov.weeklyplan.WeeklyMealOccurrenceId;
import io.github.trueruslan.zakupgotov.weeklyplan.WeeklyPlanDay;
import io.github.trueruslan.zakupgotov.weeklyplan.WeeklyPlanId;
import io.github.trueruslan.zakupgotov.weeklyplan.WeeklyPlanShoppingListComposer;
import io.github.trueruslan.zakupgotov.weeklyplanpantrypreview.WeeklyPlanPantryItemRequest;
import io.github.trueruslan.zakupgotov.weeklyplanpantrypreview.WeeklyPlanPantryQuantityRequest;
import io.github.trueruslan.zakupgotov.weeklyplanpantrypreview.WeeklyPlanPantryShoppingPreviewService;
import io.github.trueruslan.zakupgotov.weeklyplanpreview.WeeklyPlanShoppingPreviewIdGenerator;
import io.github.trueruslan.zakupgotov.weeklyplanpreview.WeeklyPlanShoppingPreviewOccurrenceRequest;
import io.github.trueruslan.zakupgotov.weeklyplanpreview.WeeklyPlanShoppingPreviewRecipeRequest;
import io.github.trueruslan.zakupgotov.weeklyplanpreview.WeeklyPlanShoppingPreviewRequest;
import io.github.trueruslan.zakupgotov.weeklyplanpreview.WeeklyPlanShoppingPreviewRequestFactory;
import io.github.trueruslan.zakupgotov.weeklyplanpreview.WeeklyPlanShoppingPreviewService;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class WeeklyPlanPantryComparisonPreviewComputationTest {

    @Test
    void fullPantryCoverageHasNoDetailedComparisonComputation() {
        var service = service();

        var computation = service.compute(request(List.of(
                pantry("Milk", "1", QuantityUnit.LITER),
                pantry("Eggs", "6", QuantityUnit.PIECE))));

        assertThat(computation.preview().comparisonOutcome())
                .isEqualTo(WeeklyPlanPantryComparisonOutcome.NO_REMAINING_DEMAND);
        assertThat(computation.preview().comparisonPreview()).isNull();
        assertThat(computation.comparisonComputation()).isEmpty();
        assertThat(computation.preview()).isEqualTo(service.create(request(List.of(
                pantry("Milk", "1", QuantityUnit.LITER),
                pantry("Eggs", "6", QuantityUnit.PIECE)))));
    }

    @Test
    void comparedDemandCarriesExactDetailedComparisonBehindAcceptedProjection() {
        var service = service();

        var computation = service.compute(request(List.of(
                pantry("Eggs", "6", QuantityUnit.PIECE))));

        assertThat(computation.preview().comparisonOutcome())
                .isEqualTo(WeeklyPlanPantryComparisonOutcome.COMPARED);
        assertThat(computation.comparisonComputation()).isPresent();
        assertThat(computation.comparisonComputation().orElseThrow().preview())
                .isEqualTo(computation.preview().comparisonPreview());
        assertThat(computation.comparisonComputation().orElseThrow().input().productLocation().locality())
                .isEqualTo("Москва");
    }

    private static WeeklyPlanPantryComparisonPreviewService service() {
        return new WeeklyPlanPantryComparisonPreviewService(
                new WeeklyPlanPantryShoppingPreviewService(weeklyService()),
                new ComparisonPreviewService(
                        RetailerRegistry.initial(),
                        new NoopComparisonRuntimeEvidenceSource()));
    }

    private static WeeklyPlanPantryComparisonPreviewRequest request(List<WeeklyPlanPantryItemRequest> pantry) {
        return new WeeklyPlanPantryComparisonPreviewRequest("Москва", weeklyRequest(), pantry);
    }

    private static WeeklyPlanPantryItemRequest pantry(String requirement, String amount, QuantityUnit unit) {
        return new WeeklyPlanPantryItemRequest(
                requirement,
                new WeeklyPlanPantryQuantityRequest(new BigDecimal(amount), unit));
    }

    private static WeeklyPlanShoppingPreviewRequest weeklyRequest() {
        return new WeeklyPlanShoppingPreviewRequest(List.of(
                new WeeklyPlanShoppingPreviewOccurrenceRequest(
                        WeeklyPlanDay.MONDAY,
                        2,
                        new WeeklyPlanShoppingPreviewRecipeRequest(
                                "Breakfast",
                                2,
                                List.of(
                                        new RecipeShoppingPreviewIngredientRequest(
                                                "Milk",
                                                new RecipeShoppingPreviewQuantityRequest(
                                                        BigDecimal.ONE,
                                                        QuantityUnit.LITER)),
                                        new RecipeShoppingPreviewIngredientRequest(
                                                "Eggs",
                                                new RecipeShoppingPreviewQuantityRequest(
                                                        new BigDecimal("6"),
                                                        QuantityUnit.PIECE)))))));
    }

    private static WeeklyPlanShoppingPreviewService weeklyService() {
        var weeklyIds = new WeeklyPlanShoppingPreviewIdGenerator() {
            @Override
            public WeeklyPlanId nextWeeklyPlanId() {
                return new WeeklyPlanId(UUID.fromString("c1000000-0000-0000-0000-000000000001"));
            }

            @Override
            public WeeklyMealOccurrenceId nextOccurrenceId() {
                return new WeeklyMealOccurrenceId(UUID.fromString("c2000000-0000-0000-0000-000000000001"));
            }
        };
        var recipeIds = new RecipeShoppingPreviewIdGenerator() {
            private int ingredientSequence;

            @Override
            public RecipeId nextRecipeId() {
                return new RecipeId(UUID.fromString("c3000000-0000-0000-0000-000000000001"));
            }

            @Override
            public RecipeIngredientId nextIngredientId() {
                ingredientSequence++;
                return new RecipeIngredientId(UUID.fromString(
                        "c4000000-0000-0000-0000-%012d".formatted(ingredientSequence)));
            }

            @Override
            public ShoppingListId nextShoppingListId() {
                return new ShoppingListId(UUID.fromString("c5000000-0000-0000-0000-000000000001"));
            }
        };
        return new WeeklyPlanShoppingPreviewService(
                new WeeklyPlanShoppingPreviewRequestFactory(
                        weeklyIds,
                        new RecipeShoppingPreviewRequestFactory(recipeIds)),
                new WeeklyPlanShoppingListComposer());
    }
}
