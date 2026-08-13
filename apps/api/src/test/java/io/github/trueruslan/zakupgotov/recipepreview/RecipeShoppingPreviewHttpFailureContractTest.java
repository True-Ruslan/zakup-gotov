package io.github.trueruslan.zakupgotov.recipepreview;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class RecipeShoppingPreviewHttpFailureContractTest {
    private static final String PATH = "/api/v1/recipe-shopping-previews";
    private static final String VALID = """
            {"title":"Soup","baseServings":2,"targetServings":4,"ingredients":[
              {"requirement":"carrot","quantity":{"amount":0.5,"unit":"KILOGRAM"}}]}
            """;

    @Test
    void sanitizesUnreadableBodies() throws Exception {
        var mvc = mvc(new FixedIds());
        assertMalformed(mvc, "{\"title\":");
        assertMalformed(mvc, VALID.replace("KILOGRAM", "STONE"));
        assertMalformed(mvc, VALID.replace("\"baseServings\":2", "\"baseServings\":1.5"));
    }

    @Test
    void rejectsUnknownPropertiesAtEveryRequestLevel() throws Exception {
        var mvc = mvc(new FixedIds());
        assertMalformed(mvc, VALID.replace("\"title\":\"Soup\"", "\"title\":\"Soup\",\"unexpectedRoot\":true"));
        assertMalformed(mvc, VALID.replace("\"requirement\":\"carrot\"", "\"requirement\":\"carrot\",\"unexpectedIngredient\":true"));
        assertMalformed(mvc, VALID.replace("\"amount\":0.5", "\"amount\":0.5,\"unexpectedQuantity\":true"));
    }

    @Test
    void internalFailureIsNotConvertedToPublicBadRequest() {
        var mvc = mvc(new FailingIds());
        assertThatThrownBy(() -> mvc.perform(post(PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID)))
                .hasRootCauseInstanceOf(IllegalStateException.class);
    }

    private static void assertMalformed(MockMvc mvc, String body) throws Exception {
        var result = mvc.perform(post(PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").value(InvalidRecipeShoppingPreviewProblem.TYPE))
                .andExpect(jsonPath("$.title").value(InvalidRecipeShoppingPreviewProblem.TITLE))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value(InvalidRecipeShoppingPreviewProblem.CODE))
                .andExpect(jsonPath("$.errors.length()").value(1))
                .andExpect(jsonPath("$.errors[0].field").value("$request"))
                .andExpect(jsonPath("$.errors[0].message").value("malformed JSON request"))
                .andReturn();

        assertThat(result.getResponse().getContentAsString())
                .doesNotContain("tools.jackson", "java.lang", "stackTrace");
    }

    private static MockMvc mvc(RecipeShoppingPreviewIdGenerator ids) {
        var service = new RecipeShoppingPreviewService(
                new RecipeShoppingPreviewRequestFactory(ids),
                new RecipeShoppingListConverter());
        return MockMvcBuilders.standaloneSetup(new RecipeShoppingPreviewController(service))
                .setControllerAdvice(new RecipeShoppingPreviewExceptionHandler())
                .build();
    }

    private static final class FixedIds implements RecipeShoppingPreviewIdGenerator {
        @Override public RecipeId nextRecipeId() { return new RecipeId(UUID.fromString("0a8d9ead-258a-4a5f-a151-c61fa18d9e25")); }
        @Override public RecipeIngredientId nextIngredientId() { return new RecipeIngredientId(UUID.fromString("2e9b5ba9-bcbc-467c-857b-80457ce6680c")); }
        @Override public ShoppingListId nextShoppingListId() { return new ShoppingListId(UUID.fromString("96c0a846-13ad-4e07-927d-28e90f36577e")); }
    }

    private static final class FailingIds implements RecipeShoppingPreviewIdGenerator {
        @Override public RecipeId nextRecipeId() { throw new IllegalStateException("internal id generator failure"); }
        @Override public RecipeIngredientId nextIngredientId() { throw new AssertionError(); }
        @Override public ShoppingListId nextShoppingListId() { throw new AssertionError(); }
    }
}
