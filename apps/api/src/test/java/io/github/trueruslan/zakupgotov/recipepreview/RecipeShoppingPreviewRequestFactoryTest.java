package io.github.trueruslan.zakupgotov.recipepreview;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.trueruslan.zakupgotov.recipe.RecipeId;
import io.github.trueruslan.zakupgotov.recipe.RecipeIngredientId;
import io.github.trueruslan.zakupgotov.shopping.Quantity;
import io.github.trueruslan.zakupgotov.shopping.QuantityUnit;
import io.github.trueruslan.zakupgotov.shopping.ShoppingListId;
import java.math.BigDecimal;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class RecipeShoppingPreviewRequestFactoryTest {

    private static final UUID RECIPE_ID = UUID.fromString("fc715c4b-17a0-4f69-8cb0-5cf8c4e44893");
    private static final UUID INGREDIENT_ID = UUID.fromString("dc7cd921-d07c-4c91-9079-3c9c1a947759");
    private static final UUID LIST_ID = UUID.fromString("ed46a060-72f2-464b-b5c4-bb98d96db8d8");

    @Test
    void normalizesCanonicalizesAndAllocatesServerOwnedIds() {
        var ids = new QueuedIds();
        var factory = new RecipeShoppingPreviewRequestFactory(ids);

        var input = factory.create(new RecipeShoppingPreviewRequest(
                "  Курица   с овощами  ", 2, 4,
                List.of(new RecipeShoppingPreviewIngredientRequest(
                        "  морковь  ",
                        new RecipeShoppingPreviewQuantityRequest(new BigDecimal("0.3"), QuantityUnit.KILOGRAM)))));

        assertThat(input.recipe().id()).isEqualTo(new RecipeId(RECIPE_ID));
        assertThat(input.recipe().title().value()).isEqualTo("Курица с овощами");
        assertThat(input.recipe().ingredients().getFirst().id()).isEqualTo(new RecipeIngredientId(INGREDIENT_ID));
        assertThat(input.recipe().ingredients().getFirst().quantity())
                .isEqualTo(new Quantity(new BigDecimal("300"), QuantityUnit.GRAM));
        assertThat(input.targetServings().value()).isEqualTo(4);
        assertThat(input.shoppingListId()).isEqualTo(new ShoppingListId(LIST_ID));
        assertThat(ids.calls).containsExactly("recipe", "ingredient", "list");
    }

    @Test
    void accumulatesTopLevelErrorsBeforeAllocatingIds() {
        var ids = new QueuedIds();
        var factory = new RecipeShoppingPreviewRequestFactory(ids);

        assertThatThrownBy(() -> factory.create(new RecipeShoppingPreviewRequest(" ", 0, -1, List.of())))
                .isInstanceOfSatisfying(InvalidRecipeShoppingPreviewRequestException.class, exception ->
                        assertThat(exception.errors()).containsExactly(
                                new RecipeShoppingPreviewValidationError("title", "must not be blank"),
                                new RecipeShoppingPreviewValidationError("baseServings", "must be greater than 0"),
                                new RecipeShoppingPreviewValidationError("targetServings", "must be greater than 0"),
                                new RecipeShoppingPreviewValidationError("ingredients", "must contain at least one ingredient")));
        assertThat(ids.calls).isEmpty();
    }

    @Test
    void rejectsNullRequestWithoutAllocatingIds() {
        var ids = new QueuedIds();
        var factory = new RecipeShoppingPreviewRequestFactory(ids);

        assertThatThrownBy(() -> factory.create(null))
                .isInstanceOfSatisfying(InvalidRecipeShoppingPreviewRequestException.class, exception ->
                        assertThat(exception.errors()).containsExactly(
                                new RecipeShoppingPreviewValidationError("$request", "must not be null")));
        assertThat(ids.calls).isEmpty();
    }

    private static final class QueuedIds implements RecipeShoppingPreviewIdGenerator {
        private final ArrayDeque<UUID> ingredientIds = new ArrayDeque<>(List.of(INGREDIENT_ID));
        private final ArrayList<String> calls = new ArrayList<>();

        @Override
        public RecipeId nextRecipeId() {
            calls.add("recipe");
            return new RecipeId(RECIPE_ID);
        }

        @Override
        public RecipeIngredientId nextIngredientId() {
            calls.add("ingredient");
            return new RecipeIngredientId(ingredientIds.removeFirst());
        }

        @Override
        public ShoppingListId nextShoppingListId() {
            calls.add("list");
            return new ShoppingListId(LIST_ID);
        }
    }
}
