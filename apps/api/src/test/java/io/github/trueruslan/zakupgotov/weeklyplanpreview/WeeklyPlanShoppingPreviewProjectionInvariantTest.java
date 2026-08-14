package io.github.trueruslan.zakupgotov.weeklyplanpreview;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.trueruslan.zakupgotov.recipe.RecipeId;
import io.github.trueruslan.zakupgotov.recipe.RecipeIngredientId;
import io.github.trueruslan.zakupgotov.recipe.RecipeIngredientRef;
import io.github.trueruslan.zakupgotov.recipepreview.RecipeShoppingPreviewIdGenerator;
import io.github.trueruslan.zakupgotov.recipepreview.RecipeShoppingPreviewIngredientRequest;
import io.github.trueruslan.zakupgotov.recipepreview.RecipeShoppingPreviewQuantityRequest;
import io.github.trueruslan.zakupgotov.recipepreview.RecipeShoppingPreviewRequestFactory;
import io.github.trueruslan.zakupgotov.shopping.Quantity;
import io.github.trueruslan.zakupgotov.shopping.QuantityUnit;
import io.github.trueruslan.zakupgotov.shopping.ShoppingItem;
import io.github.trueruslan.zakupgotov.shopping.ShoppingItemId;
import io.github.trueruslan.zakupgotov.shopping.ShoppingList;
import io.github.trueruslan.zakupgotov.shopping.ShoppingListId;
import io.github.trueruslan.zakupgotov.shopping.ShoppingRequirement;
import io.github.trueruslan.zakupgotov.weeklyplan.WeeklyMealOccurrenceId;
import io.github.trueruslan.zakupgotov.weeklyplan.WeeklyPlanDay;
import io.github.trueruslan.zakupgotov.weeklyplan.WeeklyPlanId;
import io.github.trueruslan.zakupgotov.weeklyplan.WeeklyPlanIngredientRef;
import io.github.trueruslan.zakupgotov.weeklyplan.WeeklyPlanShoppingListComposition;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class WeeklyPlanShoppingPreviewProjectionInvariantTest {

    private static final UUID PLAN = uuid("99000000-0000-0000-0000-000000000001");
    private static final UUID OCCURRENCE = uuid("99100000-0000-0000-0000-000000000001");
    private static final UUID RECIPE = uuid("99200000-0000-0000-0000-000000000001");
    private static final UUID INGREDIENT = uuid("99300000-0000-0000-0000-000000000001");
    private static final ShoppingListId LIST = new ShoppingListId(uuid("99400000-0000-0000-0000-000000000001"));
    private static final ShoppingItemId ITEM = new ShoppingItemId(uuid("99500000-0000-0000-0000-000000000001"));

    @Test
    void rejectsFinalShoppingItemWithoutProvenance() {
        var service = service(plan -> new WeeklyPlanShoppingListComposition(
                shoppingList(),
                Map.of()));

        assertThatThrownBy(() -> service.create(request()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("provenance");
    }

    @Test
    void rejectsProvenanceForUnknownOccurrence() {
        var service = service(plan -> composition(new WeeklyPlanIngredientRef(
                new WeeklyMealOccurrenceId(uuid("99100000-0000-0000-0000-000000000099")),
                new RecipeIngredientRef(new RecipeId(RECIPE), new RecipeIngredientId(INGREDIENT)))));

        assertThatThrownBy(() -> service.create(request()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("occurrence");
    }

    @Test
    void rejectsRecipeAndIngredientLineageThatDoesNotResolveInsideOccurrence() {
        var wrongRecipe = service(plan -> composition(new WeeklyPlanIngredientRef(
                new WeeklyMealOccurrenceId(OCCURRENCE),
                new RecipeIngredientRef(
                        new RecipeId(uuid("99200000-0000-0000-0000-000000000099")),
                        new RecipeIngredientId(INGREDIENT)))));
        assertThatThrownBy(() -> wrongRecipe.create(request()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("recipe");

        var wrongIngredient = service(plan -> composition(new WeeklyPlanIngredientRef(
                new WeeklyMealOccurrenceId(OCCURRENCE),
                new RecipeIngredientRef(
                        new RecipeId(RECIPE),
                        new RecipeIngredientId(uuid("99300000-0000-0000-0000-000000000099"))))));
        assertThatThrownBy(() -> wrongIngredient.create(request()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("ingredient");
    }

    private static WeeklyPlanShoppingPreviewService service(WeeklyPlanCompositionBoundary boundary) {
        return new WeeklyPlanShoppingPreviewService(factory(), boundary);
    }

    private static WeeklyPlanShoppingListComposition composition(WeeklyPlanIngredientRef ref) {
        return new WeeklyPlanShoppingListComposition(shoppingList(), Map.of(ITEM, List.of(ref)));
    }

    private static ShoppingList shoppingList() {
        var shoppingList = new ShoppingList(LIST);
        shoppingList.add(new ShoppingItem(
                ITEM,
                new ShoppingRequirement("Milk"),
                new Quantity(new BigDecimal("100"), QuantityUnit.MILLILITER)));
        return shoppingList;
    }

    private static WeeklyPlanShoppingPreviewRequest request() {
        return new WeeklyPlanShoppingPreviewRequest(List.of(new WeeklyPlanShoppingPreviewOccurrenceRequest(
                WeeklyPlanDay.MONDAY,
                1,
                new WeeklyPlanShoppingPreviewRecipeRequest(
                        "Recipe",
                        1,
                        List.of(new RecipeShoppingPreviewIngredientRequest(
                                "Milk",
                                new RecipeShoppingPreviewQuantityRequest(
                                        new BigDecimal("100"),
                                        QuantityUnit.MILLILITER)))))));
    }

    private static WeeklyPlanShoppingPreviewRequestFactory factory() {
        return new WeeklyPlanShoppingPreviewRequestFactory(
                new WeeklyPlanShoppingPreviewIdGenerator() {
                    @Override public WeeklyPlanId nextWeeklyPlanId() { return new WeeklyPlanId(PLAN); }
                    @Override public WeeklyMealOccurrenceId nextOccurrenceId() { return new WeeklyMealOccurrenceId(OCCURRENCE); }
                },
                new RecipeShoppingPreviewRequestFactory(new RecipeShoppingPreviewIdGenerator() {
                    @Override public RecipeId nextRecipeId() { return new RecipeId(RECIPE); }
                    @Override public RecipeIngredientId nextIngredientId() { return new RecipeIngredientId(INGREDIENT); }
                    @Override public ShoppingListId nextShoppingListId() {
                        return new ShoppingListId(uuid("99600000-0000-0000-0000-000000000001"));
                    }
                }));
    }

    private static UUID uuid(String value) {
        return UUID.fromString(value);
    }
}
