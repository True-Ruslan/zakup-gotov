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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.UUID;

public final class RecipeShoppingListConverter {

    private final RecipeShoppingItemIdDeriver itemIdDeriver;

    public RecipeShoppingListConverter() {
        this(RecipeShoppingListConverter::deriveDefaultItemId);
    }

    RecipeShoppingListConverter(RecipeShoppingItemIdDeriver itemIdDeriver) {
        this.itemIdDeriver = Objects.requireNonNull(itemIdDeriver, "itemIdDeriver must not be null");
    }

    public RecipeShoppingListConversion convert(
            Recipe recipe,
            RecipeServings targetServings,
            ShoppingListId shoppingListId) {
        Objects.requireNonNull(recipe, "recipe must not be null");
        Objects.requireNonNull(targetServings, "targetServings must not be null");
        Objects.requireNonNull(shoppingListId, "shoppingListId must not be null");

        var groups = new LinkedHashMap<MergeKey, GroupAccumulator>();
        for (var ingredient : recipe.ingredients()) {
            var key = new MergeKey(ingredient.requirement(), ingredient.quantity().unit());
            groups.computeIfAbsent(key, ignored -> new GroupAccumulator())
                    .add(ingredient.quantity().amount(), new RecipeIngredientRef(recipe.id(), ingredient.id()));
        }

        var shoppingList = new ShoppingList(shoppingListId);
        var provenance = new LinkedHashMap<ShoppingItemId, java.util.List<RecipeIngredientRef>>();
        var itemKeys = new LinkedHashMap<ShoppingItemId, MergeKey>();
        for (var entry : groups.entrySet()) {
            var key = entry.getKey();
            var accumulator = entry.getValue();
            var scaledAmount = scale(
                    accumulator.totalAmount(),
                    targetServings.value(),
                    recipe.baseServings().value());
            var quantity = new Quantity(scaledAmount, key.unit());
            var itemId = Objects.requireNonNull(
                    itemIdDeriver.derive(shoppingListId, key.requirement(), key.unit()),
                    "derived item id must not be null");
            var previousKey = itemKeys.putIfAbsent(itemId, key);
            if (previousKey != null && !previousKey.equals(key)) {
                throw new IllegalStateException("generated shopping item id collision");
            }
            shoppingList.add(new ShoppingItem(itemId, key.requirement(), quantity));
            provenance.put(itemId, accumulator.refs());
        }

        return new RecipeShoppingListConversion(shoppingList, provenance);
    }

    private static BigDecimal scale(BigDecimal summedBaseAmount, int targetServings, int baseServings) {
        var numerator = summedBaseAmount.multiply(BigDecimal.valueOf(targetServings));
        try {
            return numerator.divide(BigDecimal.valueOf(baseServings));
        } catch (ArithmeticException nonTerminatingDivision) {
            return numerator.divide(BigDecimal.valueOf(baseServings), MathContext.DECIMAL128);
        }
    }

    private static ShoppingItemId deriveDefaultItemId(
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

    private record MergeKey(ShoppingRequirement requirement, QuantityUnit unit) {}

    private static final class GroupAccumulator {
        private BigDecimal totalAmount = BigDecimal.ZERO;
        private final ArrayList<RecipeIngredientRef> refs = new ArrayList<>();

        void add(BigDecimal amount, RecipeIngredientRef ref) {
            totalAmount = totalAmount.add(amount);
            refs.add(ref);
        }

        BigDecimal totalAmount() {
            return totalAmount;
        }

        java.util.List<RecipeIngredientRef> refs() {
            return refs;
        }
    }
}
