package io.github.trueruslan.zakupgotov.weeklyplancomparisonpreview;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.trueruslan.zakupgotov.preview.ComparisonPreviewService;
import io.github.trueruslan.zakupgotov.preview.NoopComparisonRuntimeEvidenceSource;
import io.github.trueruslan.zakupgotov.recipe.RecipeId;
import io.github.trueruslan.zakupgotov.recipe.RecipeIngredientId;
import io.github.trueruslan.zakupgotov.recipepreview.RecipeShoppingPreviewIdGenerator;
import io.github.trueruslan.zakupgotov.recipepreview.RecipeShoppingPreviewRequestFactory;
import io.github.trueruslan.zakupgotov.retailer.RetailerRegistry;
import io.github.trueruslan.zakupgotov.shopping.ShoppingListId;
import io.github.trueruslan.zakupgotov.weeklyplan.WeeklyMealOccurrenceId;
import io.github.trueruslan.zakupgotov.weeklyplan.WeeklyPlanId;
import io.github.trueruslan.zakupgotov.weeklyplan.WeeklyPlanShoppingListComposer;
import io.github.trueruslan.zakupgotov.weeklyplanpreview.WeeklyPlanShoppingPreviewIdGenerator;
import io.github.trueruslan.zakupgotov.weeklyplanpreview.WeeklyPlanShoppingPreviewRequestFactory;
import io.github.trueruslan.zakupgotov.weeklyplanpreview.WeeklyPlanShoppingPreviewService;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class WeeklyPlanComparisonPreviewControllerTest {

    private static final UUID PLAN_ID = UUID.fromString("b1000000-0000-0000-0000-000000000001");
    private static final UUID OCCURRENCE_ID = UUID.fromString("b2000000-0000-0000-0000-000000000001");
    private static final UUID RECIPE_ID = UUID.fromString("b3000000-0000-0000-0000-000000000001");
    private static final UUID INGREDIENT_ID = UUID.fromString("b4000000-0000-0000-0000-000000000001");
    private static final UUID RECIPE_LIST_ID = UUID.fromString("b5000000-0000-0000-0000-000000000001");

    @Test
    void exposesComposedPreviewAndPreservesAcceptedSemanticProblems() throws Exception {
        var mvc = mvc();

        mvc.perform(post("/api/v1/weekly-plan-comparison-previews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequest("  Москва  ")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.weeklyPlanShoppingPreview.weeklyPlan.id").value(PLAN_ID.toString()))
                .andExpect(jsonPath("$.weeklyPlanShoppingPreview.weeklyPlan.occurrences[0].id")
                        .value(OCCURRENCE_ID.toString()))
                .andExpect(jsonPath("$.weeklyPlanShoppingPreview.weeklyPlan.occurrences[0].day").value("TUESDAY"))
                .andExpect(jsonPath("$.weeklyPlanShoppingPreview.weeklyPlan.occurrences[0].recipe.id")
                        .value(RECIPE_ID.toString()))
                .andExpect(jsonPath("$.weeklyPlanShoppingPreview.shoppingList.items[0].requirement").value("Milk"))
                .andExpect(jsonPath("$.weeklyPlanShoppingPreview.shoppingList.items[0].quantity.amount").value(1000))
                .andExpect(jsonPath("$.weeklyPlanShoppingPreview.shoppingList.items[0].quantity.unit")
                        .value("MILLILITER"))
                .andExpect(jsonPath("$.weeklyPlanShoppingPreview.shoppingList.items[0].sources[0].occurrenceId")
                        .value(OCCURRENCE_ID.toString()))
                .andExpect(jsonPath("$.weeklyPlanShoppingPreview.shoppingList.items[0].sources[0].recipeId")
                        .value(RECIPE_ID.toString()))
                .andExpect(jsonPath("$.weeklyPlanShoppingPreview.shoppingList.items[0].sources[0].recipeIngredientId")
                        .value(INGREDIENT_ID.toString()))
                .andExpect(jsonPath("$.comparisonPreview.locality").value("Москва"))
                .andExpect(jsonPath("$.comparisonPreview.items[0].requirement").value("Milk"))
                .andExpect(jsonPath("$.comparisonPreview.items[0].quantity.amount").value(1000))
                .andExpect(jsonPath("$.comparisonPreview.items[0].quantity.unit").value("MILLILITER"));

        mvc.perform(post("/api/v1/weekly-plan-comparison-previews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"locality\":\"Москва\",\"weeklyPlan\":{\"occurrences\":[]}}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.code").value("INVALID_WEEKLY_PLAN_SHOPPING_PREVIEW"))
                .andExpect(jsonPath("$.errors[0].field").value("occurrences"));

        mvc.perform(post("/api/v1/weekly-plan-comparison-previews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequest(" ")))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.code").value("INVALID_COMPARISON_PREVIEW"));
    }

    @Test
    void ownsBindingFailuresAcrossEntireComposedJsonAndSanitizesThem() throws Exception {
        var mvc = mvc();
        var unreadableBodies = new String[] {
                "{",
                "",
                "null",
                validRequestWithExtra("\"unexpected\":true"),
                "{\"locality\":\"Москва\",\"weeklyPlan\":{\"occurrences\":[],\"unknown\":true}}",
                requestWithOccurrence("\"day\":\"TUESDAY\",\"targetServings\":4,\"unknown\":true," + validRecipe()),
                requestWithOccurrence("\"day\":\"TUESDAY\",\"targetServings\":4,\"recipe\":{" +
                        "\"title\":\"Pasta\",\"baseServings\":2,\"unknown\":true," + validIngredients() + "}"),
                requestWithOccurrence("\"day\":\"TUESDAY\",\"targetServings\":4,\"recipe\":{" +
                        "\"title\":\"Pasta\",\"baseServings\":2,\"ingredients\":[{" +
                        "\"requirement\":\"Milk\",\"unknown\":true,\"quantity\":{\"amount\":0.5,\"unit\":\"LITER\"}}]}}"),
                requestWithOccurrence("\"day\":\"TUESDAY\",\"targetServings\":4,\"recipe\":{" +
                        "\"title\":\"Pasta\",\"baseServings\":2,\"ingredients\":[{" +
                        "\"requirement\":\"Milk\",\"quantity\":{\"amount\":0.5,\"unit\":\"LITER\",\"unknown\":true}}]}}"),
                requestWithOccurrence("\"day\":\"HOLIDAY\",\"targetServings\":4," + validRecipe()),
                requestWithOccurrence("\"day\":\"TUESDAY\",\"targetServings\":4.5," + validRecipe()),
                requestWithOccurrence("\"day\":\"TUESDAY\",\"targetServings\":4,\"recipe\":{" +
                        "\"title\":\"Pasta\",\"baseServings\":2.5," + validIngredients() + "}"),
                requestWithOccurrence("\"day\":\"TUESDAY\",\"targetServings\":4,\"recipe\":{" +
                        "\"title\":\"Pasta\",\"baseServings\":2,\"ingredients\":[{" +
                        "\"requirement\":\"Milk\",\"quantity\":{\"amount\":0.5,\"unit\":\"OUNCE\"}}]}}")
        };

        for (var body : unreadableBodies) {
            mvc.perform(post("/api/v1/weekly-plan-comparison-previews")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                    .andExpect(jsonPath("$.type")
                            .value("https://zakup-gotov.dev/problems/invalid-weekly-plan-comparison-preview"))
                    .andExpect(jsonPath("$.title").value("Invalid weekly plan comparison preview request"))
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.code").value("INVALID_WEEKLY_PLAN_COMPARISON_PREVIEW"))
                    .andExpect(jsonPath("$.errors.length()").value(1))
                    .andExpect(jsonPath("$.errors[0].field").value("$request"))
                    .andExpect(jsonPath("$.errors[0].message").value("malformed JSON request"))
                    .andExpect(content().string(not(containsString("tools.jackson"))))
                    .andExpect(content().string(not(containsString("HttpMessageNotReadableException"))))
                    .andExpect(content().string(not(containsString("IllegalArgumentException"))));
        }
    }

    private static MockMvc mvc() {
        var weeklyIds = new WeeklyPlanShoppingPreviewIdGenerator() {
            @Override public WeeklyPlanId nextWeeklyPlanId() {
                return new WeeklyPlanId(PLAN_ID);
            }
            @Override public WeeklyMealOccurrenceId nextOccurrenceId() {
                return new WeeklyMealOccurrenceId(OCCURRENCE_ID);
            }
        };
        var recipeIds = new RecipeShoppingPreviewIdGenerator() {
            @Override public RecipeId nextRecipeId() {
                return new RecipeId(RECIPE_ID);
            }
            @Override public RecipeIngredientId nextIngredientId() {
                return new RecipeIngredientId(INGREDIENT_ID);
            }
            @Override public ShoppingListId nextShoppingListId() {
                return new ShoppingListId(RECIPE_LIST_ID);
            }
        };
        var weeklyPlanService = new WeeklyPlanShoppingPreviewService(
                new WeeklyPlanShoppingPreviewRequestFactory(
                        weeklyIds,
                        new RecipeShoppingPreviewRequestFactory(recipeIds)),
                new WeeklyPlanShoppingListComposer());
        var comparisonService = new ComparisonPreviewService(
                RetailerRegistry.initial(),
                new NoopComparisonRuntimeEvidenceSource());
        var service = new WeeklyPlanComparisonPreviewService(weeklyPlanService, comparisonService);
        return MockMvcBuilders.standaloneSetup(new WeeklyPlanComparisonPreviewController(service))
                .setControllerAdvice(new WeeklyPlanComparisonPreviewExceptionHandler())
                .build();
    }

    private static String validRequest(String locality) {
        return "{\"locality\":\"" + locality + "\",\"weeklyPlan\":{\"occurrences\":[{" +
                "\"day\":\"TUESDAY\",\"targetServings\":4," + validRecipe() + "}]}}";
    }

    private static String validRequestWithExtra(String extra) {
        return "{\"locality\":\"Москва\",\"weeklyPlan\":{\"occurrences\":[{" +
                "\"day\":\"TUESDAY\",\"targetServings\":4," + validRecipe() + "}]}," + extra + "}";
    }

    private static String requestWithOccurrence(String occurrenceFields) {
        return "{\"locality\":\"Москва\",\"weeklyPlan\":{\"occurrences\":[{" + occurrenceFields + "}]}}";
    }

    private static String validRecipe() {
        return "\"recipe\":{\"title\":\"Pasta\",\"baseServings\":2," + validIngredients() + "}";
    }

    private static String validIngredients() {
        return "\"ingredients\":[{\"requirement\":\"Milk\",\"quantity\":{\"amount\":0.5,\"unit\":\"LITER\"}}]";
    }
}
