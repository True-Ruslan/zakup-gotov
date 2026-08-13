package io.github.trueruslan.zakupgotov.recipe;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.trueruslan.zakupgotov.shopping.Quantity;
import io.github.trueruslan.zakupgotov.shopping.QuantityUnit;
import io.github.trueruslan.zakupgotov.shopping.ShoppingRequirement;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class RecipeTest {

    private static final RecipeId RECIPE_ID = new RecipeId(UUID.fromString("5d3fa22c-014f-4b08-b5b3-4f759be8f920"));
    private static final RecipeTitle TITLE = new RecipeTitle("Pasta Carbonara");
    private static final RecipeServings SERVINGS = new RecipeServings(4);

    @Test
    void preservesIngredientOrderAndExposesImmutableSnapshot() {
        var first = ingredient("81d23cd8-cd6f-4692-8a0f-a49e05c779cc", "Pasta", "400", QuantityUnit.GRAM);
        var second = ingredient("11388874-5a42-4863-b5cf-3c210fa70ddd", "Eggs", "4", QuantityUnit.PIECE);
        var source = new ArrayList<>(List.of(first, second));

        var recipe = new Recipe(RECIPE_ID, TITLE, SERVINGS, source);
        source.clear();

        assertThat(recipe.id()).isEqualTo(RECIPE_ID);
        assertThat(recipe.title()).isEqualTo(TITLE);
        assertThat(recipe.baseServings()).isEqualTo(SERVINGS);
        assertThat(recipe.ingredients()).containsExactly(first, second);
        assertThatThrownBy(() -> recipe.ingredients().add(first))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void rejectsEmptyIngredients() {
        assertThatThrownBy(() -> new Recipe(RECIPE_ID, TITLE, SERVINGS, List.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ingredient");
    }

    @Test
    void rejectsDuplicateIngredientIdentity() {
        var id = "81d23cd8-cd6f-4692-8a0f-a49e05c779cc";
        var first = ingredient(id, "Milk", "500", QuantityUnit.MILLILITER);
        var duplicate = ingredient(id, "Milk", "0.5", QuantityUnit.LITER);

        assertThatThrownBy(() -> new Recipe(RECIPE_ID, TITLE, SERVINGS, List.of(first, duplicate)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("duplicate");
    }

    @Test
    void rejectsMissingAggregateFieldsAndNullIngredient() {
        var ingredient = ingredient("81d23cd8-cd6f-4692-8a0f-a49e05c779cc", "Pasta", "400", QuantityUnit.GRAM);

        assertThatThrownBy(() -> new Recipe(null, TITLE, SERVINGS, List.of(ingredient)))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("id");
        assertThatThrownBy(() -> new Recipe(RECIPE_ID, null, SERVINGS, List.of(ingredient)))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("title");
        assertThatThrownBy(() -> new Recipe(RECIPE_ID, TITLE, null, List.of(ingredient)))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("baseServings");
        assertThatThrownBy(() -> new Recipe(RECIPE_ID, TITLE, SERVINGS, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("ingredients");

        var withNull = new ArrayList<RecipeIngredient>();
        withNull.add(ingredient);
        withNull.add(null);
        assertThatThrownBy(() -> new Recipe(RECIPE_ID, TITLE, SERVINGS, withNull))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("ingredient");
    }

    @Test
    void recipeIngredientRejectsMissingFields() {
        var id = new RecipeIngredientId(UUID.fromString("81d23cd8-cd6f-4692-8a0f-a49e05c779cc"));
        var requirement = new ShoppingRequirement("Milk");
        var quantity = new Quantity(new BigDecimal("500"), QuantityUnit.MILLILITER);

        assertThatThrownBy(() -> new RecipeIngredient(null, requirement, quantity))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("id");
        assertThatThrownBy(() -> new RecipeIngredient(id, null, quantity))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("requirement");
        assertThatThrownBy(() -> new RecipeIngredient(id, requirement, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("quantity");
    }

    private static RecipeIngredient ingredient(String id, String requirement, String amount, QuantityUnit unit) {
        return new RecipeIngredient(
                new RecipeIngredientId(UUID.fromString(id)),
                new ShoppingRequirement(requirement),
                new Quantity(new BigDecimal(amount), unit));
    }
}
