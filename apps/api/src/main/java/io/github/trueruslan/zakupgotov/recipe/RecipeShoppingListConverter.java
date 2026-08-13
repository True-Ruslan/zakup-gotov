package io.github.trueruslan.zakupgotov.recipe;

import io.github.trueruslan.zakupgotov.shopping.Quantity;
import io.github.trueruslan.zakupgotov.shopping.QuantityUnit;
import io.github.trueruslan.zakupgotov.shopping.ShoppingItem;
import io.github.trueruslan.zakupgotov.shopping.ShoppingItemId;
import io.github.trueruslan.zakupgotov.shopping.ShoppingList;
import io.github.trueruslan.zakupgotov.shopping.ShoppingListId;
import io.github.trueruslan.zakupgotov.shopping.ShoppingRequirement;
import java.math.BigDecimal;
import java.math.MathContext;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public final class RecipeShoppingListConverter {

    public RecipeShoppingListConversion convert(
            Recipe recipe,
            RecipeServings targetServings,
            ShoppingListId shoppingListId) {
        Objects.requireNonNull(recipe, "recipe must not be null");
        Objects.requireNonNull(targetServings, "targetServings must not be null");
        Objects.requireNonNull(shoppingListId, "shoppingListId must not be null");

        var groups = new LinkedHashMap<MergeKey, BigDecimal>();
        for (var ingredient : recipe.ingredients()) {
            var key = new MergeKey(ingredient.requirement(), ingredient.quantity().unit());
            groups.merge(key, ingredient.quantity().amount(), BigDecimal::add);
        }

        var shoppingList = new ShoppingList(shoppingListId);
        var index = 0;
        for (var entry : groups.entrySet()) {
            var key = entry.getKey();
            var scaledAmount = scale(
                    entry.getValue(),
                    targetServings.value(),
                    recipe.baseServings().value());
            var quantity = new Quantity(scaledAmount, key.unit());
            shoppingList.add(new ShoppingItem(
                    temporaryItemId(shoppingListId, index++),
                    key.requirement(),
                    quantity));
        }

        return new RecipeShoppingListConversion(shoppingList, Map.of());
    }

    private static BigDecimal scale(BigDecimal summedBaseAmount, int targetServings, int baseServings) {
        var numerator = summedBaseAmount.multiply(BigDecimal.valueOf(targetServings));
        try {
            return numerator.divide(BigDecimal.valueOf(baseServings));
        } catch (ArithmeticException nonTerminatingDivision) {
            return numerator.divide(BigDecimal.valueOf(baseServings), MathContext.DECIMAL128);
        }
    }

    private static ShoppingItemId temporaryItemId(ShoppingListId shoppingListId, int index) {
        var payload = shoppingListId.value() + ":" + index;
        return new ShoppingItemId(UUID.nameUUIDFromBytes(payload.getBytes(StandardCharsets.UTF_8)));
    }

    private record MergeKey(ShoppingRequirement requirement, QuantityUnit unit) {}
}
