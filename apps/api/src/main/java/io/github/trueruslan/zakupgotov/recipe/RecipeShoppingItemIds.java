package io.github.trueruslan.zakupgotov.recipe;

import io.github.trueruslan.zakupgotov.shopping.QuantityUnit;
import io.github.trueruslan.zakupgotov.shopping.ShoppingItemId;
import io.github.trueruslan.zakupgotov.shopping.ShoppingListId;
import io.github.trueruslan.zakupgotov.shopping.ShoppingRequirement;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

final class RecipeShoppingItemIds {
    private RecipeShoppingItemIds() {}

    static ShoppingItemId derive(
            ShoppingListId shoppingListId,
            ShoppingRequirement requirement,
            QuantityUnit unit) {
        var payload = shoppingListId.value()
                + "\n"
                + requirement.text()
                + "\n"
                + unit.name();
        return new ShoppingItemId(UUID.nameUUIDFromBytes(payload.getBytes(StandardCharsets.UTF_8)));
    }
}
