package io.github.trueruslan.zakupgotov.weeklyplancomparisonpreview;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.trueruslan.zakupgotov.preview.ComparisonPreview;
import io.github.trueruslan.zakupgotov.preview.ComparisonPreviewRequestedItem;
import io.github.trueruslan.zakupgotov.preview.ComparisonPreviewService;
import io.github.trueruslan.zakupgotov.preview.NoopComparisonRuntimeEvidenceSource;
import io.github.trueruslan.zakupgotov.recipe.RecipeId;
import io.github.trueruslan.zakupgotov.recipe.RecipeIngredientId;
import io.github.trueruslan.zakupgotov.recipepreview.RecipeShoppingPreviewIdGenerator;
import io.github.trueruslan.zakupgotov.recipepreview.RecipeShoppingPreviewIngredientRequest;
import io.github.trueruslan.zakupgotov.recipepreview.RecipeShoppingPreviewQuantityRequest;
import io.github.trueruslan.zakupgotov.recipepreview.RecipeShoppingPreviewRequestFactory;
import io.github.trueruslan.zakupgotov.retailer.RetailerRegistry;
import io.github.trueruslan.zakupgotov.shopping.Quantity;
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
import io.github.trueruslan.zakupgotov.weeklyplanpreview.WeeklyPlanShoppingPreviewSource;
import java.math.BigDecimal;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class WeeklyPlanComparisonPreviewServiceTest {

    private static final UUID PLAN_ID = uuid("a5000000-0000-0000-0000-000000000001");
    private static final UUID OCCURRENCE_A = uuid("a6000000-0000-0000-0000-000000000001");
    private static final UUID OCCURRENCE_B = uuid("a6000000-0000-0000-0000-000000000002");
    private static final UUID RECIPE_A = uuid("a7000000-0000-0000-0000-000000000001");
    private static final UUID RECIPE_B = uuid("a7000000-0000-0000-0000-000000000002");
    private static final UUID INGREDIENT_A = uuid("a8000000-0000-0000-0000-000000000001");
    private static final UUID INGREDIENT_B = uuid("a8000000-0000-0000-0000-000000000002");

    @Test
    void preservesGeneratedWeeklyShoppingIdentityOrderQuantityAndProvenanceIntoComparison() {
        var result = createResult();

        assertThat(result.weeklyPlanShoppingPreview().weeklyPlan().id()).isEqualTo(PLAN_ID);
        assertThat(result.weeklyPlanShoppingPreview().weeklyPlan().occurrences())
                .extracting(occurrence -> occurrence.id())
                .containsExactly(OCCURRENCE_A, OCCURRENCE_B);
        assertThat(result.comparisonPreview().locality()).isEqualTo("Москва");
        assertThat(result.weeklyPlanShoppingPreview().shoppingList().items()).hasSize(1);
        assertThat(result.comparisonPreview().items()).hasSize(1);

        var generated = result.weeklyPlanShoppingPreview().shoppingList().items().getFirst();
        var compared = result.comparisonPreview().items().getFirst();
        assertThat(compared.id()).isEqualTo(generated.id());
        assertThat(compared.requirement()).isEqualTo(generated.requirement()).isEqualTo("Milk");
        assertThat(compared.quantity()).isEqualTo(generated.quantity());
        assertThat(compared.quantity().amount()).isEqualByComparingTo("1500");
        assertThat(compared.quantity().unit()).isEqualTo(QuantityUnit.MILLILITER);
        assertThat(generated.sources()).containsExactly(
                new WeeklyPlanShoppingPreviewSource(OCCURRENCE_A, RECIPE_A, INGREDIENT_A),
                new WeeklyPlanShoppingPreviewSource(OCCURRENCE_B, RECIPE_B, INGREDIENT_B));
    }

    @Test
    void failsClosedWhenComparisonProjectionDriftsFromGeneratedWeeklyShoppingItems() {
        var result = createResult();
        var compared = result.comparisonPreview().items().getFirst();
        var retailers = result.comparisonPreview().retailers();

        assertThatThrownBy(() -> WeeklyPlanComparisonPreviewService.verifyComposition(
                        result.weeklyPlanShoppingPreview(),
                        new ComparisonPreview("Москва", List.of(), retailers)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("comparison item cardinality drift");

        assertThatThrownBy(() -> WeeklyPlanComparisonPreviewService.verifyComposition(
                        result.weeklyPlanShoppingPreview(),
                        new ComparisonPreview(
                                "Москва",
                                List.of(new ComparisonPreviewRequestedItem(
                                        uuid("a9000000-0000-0000-0000-000000000001"),
                                        compared.requirement(),
                                        compared.quantity())),
                                retailers)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("comparison item identity/order drift");

        assertThatThrownBy(() -> WeeklyPlanComparisonPreviewService.verifyComposition(
                        result.weeklyPlanShoppingPreview(),
                        new ComparisonPreview(
                                "Москва",
                                List.of(new ComparisonPreviewRequestedItem(
                                        compared.id(),
                                        "different requirement",
                                        compared.quantity())),
                                retailers)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("comparison item requirement drift");

        assertThatThrownBy(() -> WeeklyPlanComparisonPreviewService.verifyComposition(
                        result.weeklyPlanShoppingPreview(),
                        new ComparisonPreview(
                                "Москва",
                                List.of(new ComparisonPreviewRequestedItem(
                                        compared.id(),
                                        compared.requirement(),
                                        new Quantity(new BigDecimal("999"), QuantityUnit.MILLILITER))),
                                retailers)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("comparison item quantity drift");
    }

    private static WeeklyPlanComparisonPreview createResult() {
        var weeklyPlanService = new WeeklyPlanShoppingPreviewService(
                new WeeklyPlanShoppingPreviewRequestFactory(
                        new WeeklyIds(),
                        new RecipeShoppingPreviewRequestFactory(new RecipeIds())),
                new WeeklyPlanShoppingListComposer());
        var comparisonService = new ComparisonPreviewService(
                RetailerRegistry.initial(),
                new NoopComparisonRuntimeEvidenceSource());
        var service = new WeeklyPlanComparisonPreviewService(weeklyPlanService, comparisonService);

        return service.create(new WeeklyPlanComparisonPreviewRequest(
                "  Москва  ",
                new WeeklyPlanShoppingPreviewRequest(List.of(
                        occurrence(WeeklyPlanDay.TUESDAY, 4, "Pasta", 2, "Milk", "0.5", QuantityUnit.LITER),
                        occurrence(WeeklyPlanDay.MONDAY, 2, "Soup", 1, "Milk", "250", QuantityUnit.MILLILITER)))));
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
        private int planCalls;

        @Override
        public WeeklyPlanId nextWeeklyPlanId() {
            if (++planCalls > 1) {
                throw new AssertionError("WeeklyPlan shopping preview must be created exactly once");
            }
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
