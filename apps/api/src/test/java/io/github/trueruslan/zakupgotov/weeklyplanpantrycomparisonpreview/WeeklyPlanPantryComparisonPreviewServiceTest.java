package io.github.trueruslan.zakupgotov.weeklyplanpantrycomparisonpreview;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.trueruslan.zakupgotov.preview.ComparisonPreview;
import io.github.trueruslan.zakupgotov.preview.ComparisonPreviewRequest;
import io.github.trueruslan.zakupgotov.preview.ComparisonPreviewRequestedItem;
import io.github.trueruslan.zakupgotov.recipe.RecipeId;
import io.github.trueruslan.zakupgotov.recipe.RecipeIngredientId;
import io.github.trueruslan.zakupgotov.recipepreview.RecipeShoppingPreviewIdGenerator;
import io.github.trueruslan.zakupgotov.recipepreview.RecipeShoppingPreviewIngredientRequest;
import io.github.trueruslan.zakupgotov.recipepreview.RecipeShoppingPreviewQuantityRequest;
import io.github.trueruslan.zakupgotov.recipepreview.RecipeShoppingPreviewRequestFactory;
import io.github.trueruslan.zakupgotov.shopping.Quantity;
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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import org.junit.jupiter.api.Test;

class WeeklyPlanPantryComparisonPreviewServiceTest {

    @Test
    void comparesOnlyDemandRemainingAfterAcceptedPantryAdjustment() {
        var calls = new AtomicInteger();
        var service = service(request -> {
            calls.incrementAndGet();
            return echoComparison(request);
        });

        var result = service.create(request(
                "  Москва  ",
                List.of(
                        pantry("Milk", "250", QuantityUnit.MILLILITER),
                        pantry("Eggs", "6", QuantityUnit.PIECE))));

        assertThat(calls).hasValue(1);
        assertThat(result.comparisonOutcome()).isEqualTo(WeeklyPlanPantryComparisonOutcome.COMPARED);
        assertThat(result.pantryShoppingPreview().originalShoppingList().items()).hasSize(2);
        assertThat(result.pantryShoppingPreview().remainingShoppingList().items()).hasSize(1);
        assertThat(result.comparisonPreview()).isNotNull();
        assertThat(result.comparisonPreview().locality()).isEqualTo("Москва");
        assertThat(result.comparisonPreview().items()).hasSize(1);

        var remaining = result.pantryShoppingPreview().remainingShoppingList().items().getFirst();
        var compared = result.comparisonPreview().items().getFirst();
        assertThat(compared.id()).isEqualTo(remaining.id());
        assertThat(compared.requirement()).isEqualTo(remaining.requirement()).isEqualTo("Milk");
        assertThat(compared.quantity()).isEqualTo(remaining.quantity());
        assertThat(compared.quantity().amount()).isEqualByComparingTo("750");
        assertThat(compared.quantity().unit()).isEqualTo(QuantityUnit.MILLILITER);
    }

    @Test
    void fullPantryCoverageReturnsExplicitZeroDemandOutcomeWithoutInvokingComparison() {
        var calls = new AtomicInteger();
        var service = service(request -> {
            calls.incrementAndGet();
            throw new AssertionError("comparison must not be invoked for zero remaining demand");
        });

        var result = service.create(request(
                "Москва",
                List.of(
                        pantry("Milk", "1", QuantityUnit.LITER),
                        pantry("Eggs", "6", QuantityUnit.PIECE))));

        assertThat(calls).hasValue(0);
        assertThat(result.pantryShoppingPreview().remainingShoppingList().items()).isEmpty();
        assertThat(result.comparisonOutcome()).isEqualTo(WeeklyPlanPantryComparisonOutcome.NO_REMAINING_DEMAND);
        assertThat(result.comparisonPreview()).isNull();
        assertThat(result.pantryShoppingPreview().pantryAdjustments()).hasSize(2);
    }

    @Test
    void emptyPantryComparesEveryAcceptedWeeklyShoppingItemInOrder() {
        var service = service(WeeklyPlanPantryComparisonPreviewServiceTest::echoComparison);

        var result = service.create(request("Москва", List.of()));

        assertThat(result.comparisonOutcome()).isEqualTo(WeeklyPlanPantryComparisonOutcome.COMPARED);
        assertThat(result.comparisonPreview().items())
                .extracting(ComparisonPreviewRequestedItem::id)
                .containsExactlyElementsOf(result.pantryShoppingPreview().remainingShoppingList().items().stream()
                        .map(item -> item.id())
                        .toList());
        assertThat(result.comparisonPreview().items()).hasSize(2);
    }

    @Test
    void invalidLocalityIsRejectedEvenWhenPantryWouldFullyCoverDemand() {
        var service = service(request -> {
            throw new AssertionError("comparison must not be invoked");
        });

        assertThatThrownBy(() -> service.create(request(
                        "   ",
                        List.of(
                                pantry("Milk", "1", QuantityUnit.LITER),
                                pantry("Eggs", "6", QuantityUnit.PIECE)))))
                .isInstanceOf(InvalidWeeklyPlanPantryComparisonPreviewRequestException.class)
                .satisfies(error -> assertThat(((InvalidWeeklyPlanPantryComparisonPreviewRequestException) error)
                                .errors())
                        .containsExactly(new WeeklyPlanPantryComparisonPreviewValidationError(
                                "locality",
                                "must not be blank")));
    }

    @Test
    void failsClosedWhenComparisonProjectionDriftsFromRemainingDemand() {
        var service = service(request -> {
            var valid = echoComparison(request);
            return new ComparisonPreview(valid.locality(), List.of(), valid.retailers());
        });

        assertThatThrownBy(() -> service.create(request(
                        "Москва",
                        List.of(pantry("Eggs", "6", QuantityUnit.PIECE)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("comparison item cardinality drift");
    }

    private static WeeklyPlanPantryComparisonPreviewService service(
            Function<ComparisonPreviewRequest, ComparisonPreview> comparisonCreator) {
        var pantryPreviewService = new WeeklyPlanPantryShoppingPreviewService(weeklyService());
        return new WeeklyPlanPantryComparisonPreviewService(pantryPreviewService, comparisonCreator);
    }

    private static ComparisonPreview echoComparison(ComparisonPreviewRequest request) {
        var items = request.items().stream()
                .map(item -> new ComparisonPreviewRequestedItem(
                        item.id(),
                        item.requirement(),
                        new Quantity(item.quantity().amount(), item.quantity().unit())))
                .toList();
        return new ComparisonPreview(
                request.locality().strip().replaceAll("\\s+", " "),
                items,
                List.of());
    }

    private static WeeklyPlanPantryComparisonPreviewRequest request(
            String locality,
            List<WeeklyPlanPantryItemRequest> pantry) {
        return new WeeklyPlanPantryComparisonPreviewRequest(locality, weeklyRequest(), pantry);
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
                return new WeeklyPlanId(UUID.fromString("b1000000-0000-0000-0000-000000000001"));
            }

            @Override
            public WeeklyMealOccurrenceId nextOccurrenceId() {
                return new WeeklyMealOccurrenceId(UUID.fromString("b2000000-0000-0000-0000-000000000001"));
            }
        };
        var recipeIds = new RecipeShoppingPreviewIdGenerator() {
            private int ingredientSequence;

            @Override
            public RecipeId nextRecipeId() {
                return new RecipeId(UUID.fromString("b3000000-0000-0000-0000-000000000001"));
            }

            @Override
            public RecipeIngredientId nextIngredientId() {
                ingredientSequence++;
                return new RecipeIngredientId(UUID.fromString(
                        "b4000000-0000-0000-0000-%012d".formatted(ingredientSequence)));
            }

            @Override
            public ShoppingListId nextShoppingListId() {
                return new ShoppingListId(UUID.fromString("b5000000-0000-0000-0000-000000000001"));
            }
        };
        return new WeeklyPlanShoppingPreviewService(
                new WeeklyPlanShoppingPreviewRequestFactory(
                        weeklyIds,
                        new RecipeShoppingPreviewRequestFactory(recipeIds)),
                new WeeklyPlanShoppingListComposer());
    }
}
