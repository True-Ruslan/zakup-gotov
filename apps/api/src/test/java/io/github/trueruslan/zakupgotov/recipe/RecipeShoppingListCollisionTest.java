package io.github.trueruslan.zakupgotov.recipe;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.trueruslan.zakupgotov.shopping.Quantity;
import io.github.trueruslan.zakupgotov.shopping.QuantityUnit;
import io.github.trueruslan.zakupgotov.shopping.ShoppingItemId;
import io.github.trueruslan.zakupgotov.shopping.ShoppingListId;
import io.github.trueruslan.zakupgotov.shopping.ShoppingRequirement;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class RecipeShoppingListCollisionTest {

    @Test
    void failsClosedWhenDifferentMergeKeysResolveToSameGeneratedItemId() {
        var fixedId = new ShoppingItemId(UUID.fromString("26537fbd-8177-4b99-bb94-9315cf2a1065"));
        RecipeShoppingItemIdDeriver collidingDeriver = (listId, requirement, unit) -> fixedId;
        var converter = new RecipeShoppingListConverter(collidingDeriver);
        var recipe = new Recipe(
                new RecipeId(UUID.fromString("5d3fa22c-014f-4b08-b5b3-4f759be8f920")),
                new RecipeTitle("Collision recipe"),
                new RecipeServings(1),
                List.of(
                        ingredient("81d23cd8-cd6f-4692-8a0f-a49e05c779cc", "Milk", "500", QuantityUnit.MILLILITER),
                        ingredient("11388874-5a42-4863-b5cf-3c210fa70ddd", "Flour", "200", QuantityUnit.GRAM)));

        assertThatThrownBy(() -> converter.convert(
                        recipe,
                        new RecipeServings(1),
                        new ShoppingListId(UUID.fromString("4ea3a925-1d2a-4246-a970-7a82ffc96402"))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("collision");
    }

    private static RecipeIngredient ingredient(String id, String requirement, String amount, QuantityUnit unit) {
        return new RecipeIngredient(
                new RecipeIngredientId(UUID.fromString(id)),
                new ShoppingRequirement(requirement),
                new Quantity(new BigDecimal(amount), unit));
    }
}
