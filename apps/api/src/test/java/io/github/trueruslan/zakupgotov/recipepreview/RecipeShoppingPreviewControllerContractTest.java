package io.github.trueruslan.zakupgotov.recipepreview;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.trueruslan.zakupgotov.recipe.RecipeId;
import io.github.trueruslan.zakupgotov.recipe.RecipeIngredientId;
import io.github.trueruslan.zakupgotov.recipe.RecipeShoppingListConverter;
import io.github.trueruslan.zakupgotov.shopping.ShoppingListId;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class RecipeShoppingPreviewControllerContractTest {
    private static final UUID RECIPE_ID = UUID.fromString("0a8d9ead-258a-4a5f-a151-c61fa18d9e25");
    private static final UUID INGREDIENT_ID = UUID.fromString("2e9b5ba9-bcbc-467c-857b-80457ce6680c");
    private static final UUID LIST_ID = UUID.fromString("96c0a846-13ad-4e07-927d-28e90f36577e");

    @Test
    void exposesCanonicalPreviewAndSemanticProblem() throws Exception {
        var service = new RecipeShoppingPreviewService(
                new RecipeShoppingPreviewRequestFactory(new FixedIds()),
                new RecipeShoppingListConverter());
        var mvc = MockMvcBuilders.standaloneSetup(new RecipeShoppingPreviewController(service))
                .setControllerAdvice(new RecipeShoppingPreviewExceptionHandler())
                .build();

        mvc.perform(post("/api/v1/recipe-shopping-previews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":" Soup ","baseServings":2,"targetServings":4,"ingredients":[
                                  {"requirement":"carrot","quantity":{"amount":0.5,"unit":"KILOGRAM"}}]}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recipe.id").value(RECIPE_ID.toString()))
                .andExpect(jsonPath("$.recipe.title").value("Soup"))
                .andExpect(jsonPath("$.recipe.ingredients[0].quantity.amount").value(500))
                .andExpect(jsonPath("$.shoppingList.id").value(LIST_ID.toString()))
                .andExpect(jsonPath("$.shoppingList.items[0].quantity.amount").value(1000))
                .andExpect(jsonPath("$.shoppingList.items[0].sourceIngredientIds[0]")
                        .value(INGREDIENT_ID.toString()));

        mvc.perform(post("/api/v1/recipe-shopping-previews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""{"title":" ","baseServings":0,"targetServings":-1,"ingredients":[]}"""))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.code").value("INVALID_RECIPE_SHOPPING_PREVIEW"))
                .andExpect(jsonPath("$.errors.length()").value(4))
                .andExpect(jsonPath("$.errors[0].field").value("title"))
                .andExpect(jsonPath("$.errors[3].field").value("ingredients"));
    }

    private static final class FixedIds implements RecipeShoppingPreviewIdGenerator {
        @Override public RecipeId nextRecipeId() { return new RecipeId(RECIPE_ID); }
        @Override public RecipeIngredientId nextIngredientId() { return new RecipeIngredientId(INGREDIENT_ID); }
        @Override public ShoppingListId nextShoppingListId() { return new ShoppingListId(LIST_ID); }
    }
}
