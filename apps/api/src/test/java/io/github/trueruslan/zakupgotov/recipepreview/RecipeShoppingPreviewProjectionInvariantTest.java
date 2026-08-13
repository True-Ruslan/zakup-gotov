package io.github.trueruslan.zakupgotov.recipepreview;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.trueruslan.zakupgotov.recipe.Recipe;
import io.github.trueruslan.zakupgotov.recipe.RecipeId;
import io.github.trueruslan.zakupgotov.recipe.RecipeIngredient;
import io.github.trueruslan.zakupgotov.recipe.RecipeIngredientId;
import io.github.trueruslan.zakupgotov.recipe.RecipeIngredientRef;
import io.github.trueruslan.zakupgotov.recipe.RecipeServings;
import io.github.trueruslan.zakupgotov.recipe.RecipeShoppingListConversion;
import io.github.trueruslan.zakupgotov.recipe.RecipeTitle;
import io.github.trueruslan.zakupgotov.shopping.Quantity;
import io.github.trueruslan.zakupgotov.shopping.QuantityUnit;
import io.github.trueruslan.zakupgotov.shopping.ShoppingItem;
import io.github.trueruslan.zakupgotov.shopping.ShoppingItemId;
import io.github.trueruslan.zakupgotov.shopping.ShoppingList;
import io.github.trueruslan.zakupgotov.shopping.ShoppingListId;
import io.github.trueruslan.zakupgotov.shopping.ShoppingRequirement;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class RecipeShoppingPreviewProjectionInvariantTest {
    private static final RecipeId RECIPE_ID = new RecipeId(UUID.fromString("0a8d9ead-258a-4a5f-a151-c61fa18d9e25"));
    private static final RecipeIngredientId INGREDIENT_ID =
            new RecipeIngredientId(UUID.fromString("2e9b5ba9-bcbc-467c-857b-80457ce6680c"));
    private static final ShoppingListId LIST_ID =
            new ShoppingListId(UUID.fromString("96c0a846-13ad-4e07-927d-28e90f36577e"));
    private static final ShoppingItemId ITEM_ID =
            new ShoppingItemId(UUID.fromString("24e8fe8c-f415-4f9f-9fc8-260a31f9b197"));

    @Test
    void rejectsCrossRecipeProvenance() {
        var input = input();
        var ref = new RecipeIngredientRef(
                new RecipeId(UUID.fromString("56a6740f-cd28-4d74-a4f0-3903fb24da4e")),
                INGREDIENT_ID);

        assertThatThrownBy(() -> RecipeShoppingPreviewService.project(input, conversion(LIST_ID, ref)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void rejectsOrphanIngredientProvenance() {
        var input = input();
        var ref = new RecipeIngredientRef(
                RECIPE_ID,
                new RecipeIngredientId(UUID.fromString("b8eca4dd-1ec4-4a92-84e1-6334b0e5d235")));

        assertThatThrownBy(() -> RecipeShoppingPreviewService.project(input, conversion(LIST_ID, ref)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void rejectsUnexpectedShoppingListIdentity() {
        var input = input();
        var otherListId = new ShoppingListId(UUID.fromString("2f33ed07-36a2-4a42-8ce1-91dd1c809961"));
        var ref = new RecipeIngredientRef(RECIPE_ID, INGREDIENT_ID);

        assertThatThrownBy(() -> RecipeShoppingPreviewService.project(input, conversion(otherListId, ref)))
                .isInstanceOf(IllegalStateException.class);
    }

    private static RecipeShoppingPreviewInput input() {
        var ingredient = new RecipeIngredient(
                INGREDIENT_ID,
                new ShoppingRequirement("Milk"),
                new Quantity(BigDecimal.ONE, QuantityUnit.LITER));
        var recipe = new Recipe(RECIPE_ID, new RecipeTitle("Recipe"), new RecipeServings(1), List.of(ingredient));
        return new RecipeShoppingPreviewInput(recipe, new RecipeServings(1), LIST_ID);
    }

    private static RecipeShoppingListConversion conversion(
            ShoppingListId listId,
            RecipeIngredientRef ref) {
        var list = new ShoppingList(listId);
        list.add(new ShoppingItem(
                ITEM_ID,
                new ShoppingRequirement("Milk"),
                new Quantity(new BigDecimal("1000"), QuantityUnit.MILLILITER)));
        return new RecipeShoppingListConversion(list, Map.of(ITEM_ID, List.of(ref)));
    }
}
