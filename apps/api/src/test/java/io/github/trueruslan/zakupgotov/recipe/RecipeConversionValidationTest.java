package io.github.trueruslan.zakupgotov.recipe;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.trueruslan.zakupgotov.shopping.Quantity;
import io.github.trueruslan.zakupgotov.shopping.QuantityUnit;
import io.github.trueruslan.zakupgotov.shopping.ShoppingListId;
import io.github.trueruslan.zakupgotov.shopping.ShoppingRequirement;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class RecipeConversionValidationTest {

    private static final RecipeId RECIPE_ID = new RecipeId(UUID.fromString("5d3fa22c-014f-4b08-b5b3-4f759be8f920"));
    private static final RecipeIngredientId INGREDIENT_ID = new RecipeIngredientId(UUID.fromString("81d23cd8-cd6f-4692-8a0f-a49e05c779cc"));
    private static final ShoppingListId LIST_ID = new ShoppingListId(UUID.fromString("4ea3a925-1d2a-4246-a970-7a82ffc96402"));

    @Test
    void recipeIngredientRefRequiresBothIdentities() {
        assertThatThrownBy(() -> new RecipeIngredientRef(null, INGREDIENT_ID))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("recipeId");
        assertThatThrownBy(() -> new RecipeIngredientRef(RECIPE_ID, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("ingredientId");
    }

    @Test
    void converterRejectsMissingInputsAndMissingIdDeriver() {
        var converter = new RecipeShoppingListConverter();
        var recipe = recipe();

        assertThatThrownBy(() -> converter.convert(null, new RecipeServings(1), LIST_ID))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("recipe");
        assertThatThrownBy(() -> converter.convert(recipe, null, LIST_ID))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("targetServings");
        assertThatThrownBy(() -> converter.convert(recipe, new RecipeServings(1), null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("shoppingListId");
        assertThatThrownBy(() -> new RecipeShoppingListConverter(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("itemIdDeriver");
    }

    private static Recipe recipe() {
        return new Recipe(
                RECIPE_ID,
                new RecipeTitle("Test recipe"),
                new RecipeServings(1),
                List.of(new RecipeIngredient(
                        INGREDIENT_ID,
                        new ShoppingRequirement("Flour"),
                        new Quantity(new BigDecimal("100"), QuantityUnit.GRAM))));
    }
}
