package io.github.trueruslan.zakupgotov.recipecomparisonpreview;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.trueruslan.zakupgotov.preview.ComparisonPreview;
import io.github.trueruslan.zakupgotov.preview.ComparisonPreviewRequestedItem;
import io.github.trueruslan.zakupgotov.preview.ComparisonPreviewService;
import io.github.trueruslan.zakupgotov.preview.NoopComparisonRuntimeEvidenceSource;
import io.github.trueruslan.zakupgotov.recipe.RecipeId;
import io.github.trueruslan.zakupgotov.recipe.RecipeIngredientId;
import io.github.trueruslan.zakupgotov.recipe.RecipeShoppingListConverter;
import io.github.trueruslan.zakupgotov.recipepreview.RecipeShoppingPreviewIdGenerator;
import io.github.trueruslan.zakupgotov.recipepreview.RecipeShoppingPreviewIngredientRequest;
import io.github.trueruslan.zakupgotov.recipepreview.RecipeShoppingPreviewQuantityRequest;
import io.github.trueruslan.zakupgotov.recipepreview.RecipeShoppingPreviewRequest;
import io.github.trueruslan.zakupgotov.recipepreview.RecipeShoppingPreviewRequestFactory;
import io.github.trueruslan.zakupgotov.recipepreview.RecipeShoppingPreviewService;
import io.github.trueruslan.zakupgotov.retailer.RetailerRegistry;
import io.github.trueruslan.zakupgotov.shopping.Quantity;
import io.github.trueruslan.zakupgotov.shopping.QuantityUnit;
import io.github.trueruslan.zakupgotov.shopping.ShoppingListId;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class RecipeComparisonPreviewServiceTest {

    private static final UUID RECIPE_ID = UUID.fromString("10000000-0000-0000-0000-000000000001");
    private static final UUID INGREDIENT_ID = UUID.fromString("20000000-0000-0000-0000-000000000001");
    private static final UUID SHOPPING_LIST_ID = UUID.fromString("30000000-0000-0000-0000-000000000001");

    @Test
    void preservesGeneratedShoppingItemIdentityAndCanonicalQuantityIntoComparison() {
        var result = createResult();

        assertThat(result.recipeShoppingPreview().recipe().id()).isEqualTo(RECIPE_ID);
        assertThat(result.comparisonPreview().locality()).isEqualTo("Москва");
        assertThat(result.recipeShoppingPreview().shoppingList().items()).hasSize(1);
        assertThat(result.comparisonPreview().items()).hasSize(1);

        var generated = result.recipeShoppingPreview().shoppingList().items().getFirst();
        var compared = result.comparisonPreview().items().getFirst();
        assertThat(compared.id()).isEqualTo(generated.id());
        assertThat(compared.requirement()).isEqualTo(generated.requirement()).isEqualTo("курица");
        assertThat(compared.quantity()).isEqualTo(generated.quantity());
        assertThat(compared.quantity().amount()).isEqualByComparingTo("1000");
        assertThat(compared.quantity().unit()).isEqualTo(QuantityUnit.GRAM);
        assertThat(generated.sourceIngredientIds()).containsExactly(INGREDIENT_ID);
    }

    @Test
    void failsClosedWhenComparisonProjectionDriftsFromGeneratedShoppingItems() {
        var result = createResult();
        var compared = result.comparisonPreview().items().getFirst();
        var retailers = result.comparisonPreview().retailers();

        assertThatThrownBy(() -> RecipeComparisonPreviewService.verifyComposition(
                        result.recipeShoppingPreview(),
                        new ComparisonPreview("Москва", List.of(), retailers)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("comparison item cardinality drift");

        assertThatThrownBy(() -> RecipeComparisonPreviewService.verifyComposition(
                        result.recipeShoppingPreview(),
                        new ComparisonPreview(
                                "Москва",
                                List.of(new ComparisonPreviewRequestedItem(
                                        UUID.fromString("40000000-0000-0000-0000-000000000001"),
                                        compared.requirement(),
                                        compared.quantity())),
                                retailers)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("comparison item identity/order drift");

        assertThatThrownBy(() -> RecipeComparisonPreviewService.verifyComposition(
                        result.recipeShoppingPreview(),
                        new ComparisonPreview(
                                "Москва",
                                List.of(new ComparisonPreviewRequestedItem(
                                        compared.id(),
                                        "другая потребность",
                                        compared.quantity())),
                                retailers)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("comparison item requirement drift");

        assertThatThrownBy(() -> RecipeComparisonPreviewService.verifyComposition(
                        result.recipeShoppingPreview(),
                        new ComparisonPreview(
                                "Москва",
                                List.of(new ComparisonPreviewRequestedItem(
                                        compared.id(),
                                        compared.requirement(),
                                        new Quantity(new BigDecimal("999"), QuantityUnit.GRAM))),
                                retailers)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("comparison item quantity drift");
    }

    private static RecipeComparisonPreview createResult() {
        var recipeService = new RecipeShoppingPreviewService(
                new RecipeShoppingPreviewRequestFactory(new FixedIds()),
                new RecipeShoppingListConverter());
        var comparisonService = new ComparisonPreviewService(
                RetailerRegistry.initial(),
                new NoopComparisonRuntimeEvidenceSource());
        var service = new RecipeComparisonPreviewService(recipeService, comparisonService);

        return service.create(new RecipeComparisonPreviewRequest(
                "  Москва  ",
                new RecipeShoppingPreviewRequest(
                        "  Курица с овощами  ",
                        2,
                        4,
                        List.of(new RecipeShoppingPreviewIngredientRequest(
                                "  курица  ",
                                new RecipeShoppingPreviewQuantityRequest(
                                        new BigDecimal("0.5"),
                                        QuantityUnit.KILOGRAM))))));
    }

    private static final class FixedIds implements RecipeShoppingPreviewIdGenerator {
        @Override
        public RecipeId nextRecipeId() {
            return new RecipeId(RECIPE_ID);
        }

        @Override
        public RecipeIngredientId nextIngredientId() {
            return new RecipeIngredientId(INGREDIENT_ID);
        }

        @Override
        public ShoppingListId nextShoppingListId() {
            return new ShoppingListId(SHOPPING_LIST_ID);
        }
    }
}
