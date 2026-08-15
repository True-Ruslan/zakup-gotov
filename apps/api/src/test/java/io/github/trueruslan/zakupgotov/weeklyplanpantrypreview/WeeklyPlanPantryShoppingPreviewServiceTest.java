package io.github.trueruslan.zakupgotov.weeklyplanpantrypreview;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.trueruslan.zakupgotov.pantry.PantryAdjustmentStatus;
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

class WeeklyPlanPantryShoppingPreviewServiceTest {

    @Test
    void composesAcceptedWeeklyPreviewWithPartialAndFullPantryEvidence() {
        var service = new WeeklyPlanPantryShoppingPreviewService(weeklyService());

        var result = service.create(new WeeklyPlanPantryShoppingPreviewRequest(
                weeklyRequest(),
                List.of(
                        pantry("Milk", "250", QuantityUnit.MILLILITER),
                        pantry("Eggs", "6", QuantityUnit.PIECE))));

        assertThat(result.originalShoppingList().items()).hasSize(2);
        assertThat(result.pantryAdjustments()).hasSize(2);
        assertThat(result.remainingShoppingList().items()).hasSize(1);

        var originalMilk = result.originalShoppingList().items().getFirst();
        var milkEvidence = result.pantryAdjustments().getFirst();
        var remainingMilk = result.remainingShoppingList().items().getFirst();

        assertThat(milkEvidence.itemId()).isEqualTo(originalMilk.id());
        assertThat(milkEvidence.requirement()).isEqualTo("Milk");
        assertThat(milkEvidence.required().amount()).isEqualByComparingTo("1000");
        assertThat(milkEvidence.pantryUsed().amount()).isEqualByComparingTo("250");
        assertThat(milkEvidence.remaining().amount()).isEqualByComparingTo("750");
        assertThat(milkEvidence.status()).isEqualTo(PantryAdjustmentStatus.PARTIALLY_COVERED);

        assertThat(remainingMilk.id()).isEqualTo(originalMilk.id());
        assertThat(remainingMilk.requirement()).isEqualTo(originalMilk.requirement());
        assertThat(remainingMilk.quantity().amount()).isEqualByComparingTo("750");
        assertThat(remainingMilk.sources()).isEqualTo(originalMilk.sources());

        var originalEggs = result.originalShoppingList().items().get(1);
        var eggsEvidence = result.pantryAdjustments().get(1);
        assertThat(eggsEvidence.itemId()).isEqualTo(originalEggs.id());
        assertThat(eggsEvidence.status()).isEqualTo(PantryAdjustmentStatus.FULLY_COVERED);
        assertThat(eggsEvidence.pantryUsed().amount()).isEqualByComparingTo("6");
        assertThat(eggsEvidence.remaining()).isNull();
    }

    @Test
    void representsCompletePantryCoverageAsEmptyRemainingListWithoutLosingEvidence() {
        var service = new WeeklyPlanPantryShoppingPreviewService(weeklyService());

        var result = service.create(new WeeklyPlanPantryShoppingPreviewRequest(
                weeklyRequest(),
                List.of(
                        pantry("Milk", "1", QuantityUnit.LITER),
                        pantry("Eggs", "6", QuantityUnit.PIECE))));

        assertThat(result.originalShoppingList().items()).hasSize(2);
        assertThat(result.pantryAdjustments())
                .extracting(WeeklyPlanPantryAdjustmentEvidence::status)
                .containsExactly(
                        PantryAdjustmentStatus.FULLY_COVERED,
                        PantryAdjustmentStatus.FULLY_COVERED);
        assertThat(result.remainingShoppingList().items()).isEmpty();
        assertThat(result.remainingShoppingList().id()).isEqualTo(result.originalShoppingList().id());
    }

    @Test
    void emptyPantryKeepsEveryCanonicalItemIdentityOrderQuantityAndSource() {
        var service = new WeeklyPlanPantryShoppingPreviewService(weeklyService());

        var result = service.create(new WeeklyPlanPantryShoppingPreviewRequest(weeklyRequest(), List.of()));

        assertThat(result.pantryAdjustments())
                .extracting(WeeklyPlanPantryAdjustmentEvidence::status)
                .containsOnly(PantryAdjustmentStatus.UNCHANGED);
        assertThat(result.remainingShoppingList().items()).isEqualTo(result.originalShoppingList().items());
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
                return new WeeklyPlanId(UUID.fromString("a1000000-0000-0000-0000-000000000001"));
            }

            @Override
            public WeeklyMealOccurrenceId nextOccurrenceId() {
                return new WeeklyMealOccurrenceId(UUID.fromString("a2000000-0000-0000-0000-000000000001"));
            }
        };
        var recipeIds = new RecipeShoppingPreviewIdGenerator() {
            private int ingredientSequence;

            @Override
            public RecipeId nextRecipeId() {
                return new RecipeId(UUID.fromString("a3000000-0000-0000-0000-000000000001"));
            }

            @Override
            public RecipeIngredientId nextIngredientId() {
                ingredientSequence++;
                return new RecipeIngredientId(UUID.fromString(
                        "a4000000-0000-0000-0000-%012d".formatted(ingredientSequence)));
            }

            @Override
            public ShoppingListId nextShoppingListId() {
                return new ShoppingListId(UUID.fromString("a5000000-0000-0000-0000-000000000001"));
            }
        };
        return new WeeklyPlanShoppingPreviewService(
                new WeeklyPlanShoppingPreviewRequestFactory(
                        weeklyIds,
                        new RecipeShoppingPreviewRequestFactory(recipeIds)),
                new WeeklyPlanShoppingListComposer());
    }
}
