package io.github.trueruslan.zakupgotov.recipecomparisonpreview;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.trueruslan.zakupgotov.preview.ComparisonPreviewService;
import io.github.trueruslan.zakupgotov.preview.NoopComparisonRuntimeEvidenceSource;
import io.github.trueruslan.zakupgotov.recipe.RecipeId;
import io.github.trueruslan.zakupgotov.recipe.RecipeIngredientId;
import io.github.trueruslan.zakupgotov.recipe.RecipeShoppingListConverter;
import io.github.trueruslan.zakupgotov.recipepreview.RecipeShoppingPreviewIdGenerator;
import io.github.trueruslan.zakupgotov.recipepreview.RecipeShoppingPreviewRequestFactory;
import io.github.trueruslan.zakupgotov.recipepreview.RecipeShoppingPreviewService;
import io.github.trueruslan.zakupgotov.retailer.RetailerRegistry;
import io.github.trueruslan.zakupgotov.shopping.ShoppingListId;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class RecipeComparisonPreviewControllerTest {

    private static final UUID RECIPE_ID = UUID.fromString("11000000-0000-0000-0000-000000000001");
    private static final UUID INGREDIENT_ID = UUID.fromString("22000000-0000-0000-0000-000000000001");
    private static final UUID LIST_ID = UUID.fromString("33000000-0000-0000-0000-000000000001");

    @Test
    void exposesComposedPreviewAndPreservesNestedValidationProblems() throws Exception {
        var recipeService = new RecipeShoppingPreviewService(
                new RecipeShoppingPreviewRequestFactory(new FixedIds()),
                new RecipeShoppingListConverter());
        var comparisonService = new ComparisonPreviewService(
                RetailerRegistry.initial(),
                new NoopComparisonRuntimeEvidenceSource());
        var composedService = new RecipeComparisonPreviewService(recipeService, comparisonService);
        var mvc = MockMvcBuilders.standaloneSetup(new RecipeComparisonPreviewController(composedService))
                .setControllerAdvice(new RecipeComparisonPreviewExceptionHandler())
                .build();

        mvc.perform(post("/api/v1/recipe-comparison-previews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"locality":"  Москва  ","recipe":{"title":" Soup ","baseServings":2,"targetServings":4,
                                  "ingredients":[{"requirement":"carrot","quantity":{"amount":0.5,"unit":"KILOGRAM"}}]}}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recipeShoppingPreview.recipe.id").value(RECIPE_ID.toString()))
                .andExpect(jsonPath("$.recipeShoppingPreview.shoppingList.id").value(LIST_ID.toString()))
                .andExpect(jsonPath("$.recipeShoppingPreview.shoppingList.items[0].quantity.amount").value(1000))
                .andExpect(jsonPath("$.comparisonPreview.locality").value("Москва"))
                .andExpect(jsonPath("$.comparisonPreview.items[0].id").exists());

        mvc.perform(post("/api/v1/recipe-comparison-previews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"locality":"Москва","recipe":{"title":" ","baseServings":2,"targetServings":4,"ingredients":[]}}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.code").value("INVALID_RECIPE_SHOPPING_PREVIEW"));

        mvc.perform(post("/api/v1/recipe-comparison-previews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"locality":" ","recipe":{"title":"Soup","baseServings":2,"targetServings":4,
                                  "ingredients":[{"requirement":"carrot","quantity":{"amount":1,"unit":"PIECE"}}]}}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.code").value("INVALID_COMPARISON_PREVIEW"));
    }

    @Test
    void sanitizesMalformedOrUnknownWrapperJson() throws Exception {
        var recipeService = new RecipeShoppingPreviewService(
                new RecipeShoppingPreviewRequestFactory(new FixedIds()),
                new RecipeShoppingListConverter());
        var comparisonService = new ComparisonPreviewService(
                RetailerRegistry.initial(),
                new NoopComparisonRuntimeEvidenceSource());
        var mvc = MockMvcBuilders.standaloneSetup(new RecipeComparisonPreviewController(
                        new RecipeComparisonPreviewService(recipeService, comparisonService)))
                .setControllerAdvice(new RecipeComparisonPreviewExceptionHandler())
                .build();

        mvc.perform(post("/api/v1/recipe-comparison-previews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"locality\":\"Москва\",\"recipe\":{},\"unexpected\":true}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.code").value("INVALID_RECIPE_COMPARISON_PREVIEW"))
                .andExpect(jsonPath("$.errors.length()").value(1))
                .andExpect(jsonPath("$.errors[0].field").value("$request"))
                .andExpect(jsonPath("$.errors[0].message").value("malformed JSON request"));
    }

    private static final class FixedIds implements RecipeShoppingPreviewIdGenerator {
        @Override public RecipeId nextRecipeId() { return new RecipeId(RECIPE_ID); }
        @Override public RecipeIngredientId nextIngredientId() { return new RecipeIngredientId(INGREDIENT_ID); }
        @Override public ShoppingListId nextShoppingListId() { return new ShoppingListId(LIST_ID); }
    }
}
