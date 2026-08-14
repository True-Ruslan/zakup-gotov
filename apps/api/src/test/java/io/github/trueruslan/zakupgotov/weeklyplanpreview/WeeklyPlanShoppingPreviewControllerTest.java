package io.github.trueruslan.zakupgotov.weeklyplanpreview;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.trueruslan.zakupgotov.recipe.RecipeId;
import io.github.trueruslan.zakupgotov.recipe.RecipeIngredientId;
import io.github.trueruslan.zakupgotov.recipepreview.RecipeShoppingPreviewIdGenerator;
import io.github.trueruslan.zakupgotov.recipepreview.RecipeShoppingPreviewRequestFactory;
import io.github.trueruslan.zakupgotov.shopping.ShoppingListId;
import io.github.trueruslan.zakupgotov.weeklyplan.WeeklyMealOccurrenceId;
import io.github.trueruslan.zakupgotov.weeklyplan.WeeklyPlanId;
import io.github.trueruslan.zakupgotov.weeklyplan.WeeklyPlanShoppingListComposer;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class WeeklyPlanShoppingPreviewControllerTest {

    @Test
    void exposesCanonicalWeeklyPreviewAndSemanticProblem() throws Exception {
        var service = service();
        var mvc = MockMvcBuilders.standaloneSetup(new WeeklyPlanShoppingPreviewController(service))
                .setControllerAdvice(new WeeklyPlanShoppingPreviewExceptionHandler())
                .build();

        mvc.perform(post("/api/v1/weekly-plan-shopping-previews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"occurrences":[
                                  {"day":"TUESDAY","targetServings":4,"recipe":{
                                    "title":" Pasta ","baseServings":2,"ingredients":[
                                      {"requirement":"Milk","quantity":{"amount":0.5,"unit":"LITER"}}
                                    ]
                                  }}
                                ]}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.weeklyPlan.id").value("a1000000-0000-0000-0000-000000000001"))
                .andExpect(jsonPath("$.weeklyPlan.occurrences[0].id").value("a2000000-0000-0000-0000-000000000001"))
                .andExpect(jsonPath("$.weeklyPlan.occurrences[0].day").value("TUESDAY"))
                .andExpect(jsonPath("$.weeklyPlan.occurrences[0].recipe.title").value("Pasta"))
                .andExpect(jsonPath("$.shoppingList.items[0].quantity.amount").value(1000))
                .andExpect(jsonPath("$.shoppingList.items[0].quantity.unit").value("MILLILITER"))
                .andExpect(jsonPath("$.shoppingList.items[0].sources[0].occurrenceId")
                        .value("a2000000-0000-0000-0000-000000000001"))
                .andExpect(jsonPath("$.shoppingList.items[0].sources[0].recipeId")
                        .value("a3000000-0000-0000-0000-000000000001"))
                .andExpect(jsonPath("$.shoppingList.items[0].sources[0].recipeIngredientId")
                        .value("a4000000-0000-0000-0000-000000000001"));

        mvc.perform(post("/api/v1/weekly-plan-shopping-previews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"occurrences\":[]}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.code").value("INVALID_WEEKLY_PLAN_SHOPPING_PREVIEW"))
                .andExpect(jsonPath("$.errors[0].field").value("occurrences"));
    }

    @Test
    void sanitizesUnreadableJsonUnknownFieldsEnumsAndFractionalServings() throws Exception {
        var mvc = MockMvcBuilders.standaloneSetup(new WeeklyPlanShoppingPreviewController(service()))
                .setControllerAdvice(new WeeklyPlanShoppingPreviewExceptionHandler())
                .build();

        for (var body : new String[] {
                "{",
                "{\"occurrences\":[],\"unknown\":true}",
                validPrefix() + "\"day\":\"HOLIDAY\",\"targetServings\":1," + validRecipe() + validSuffix(),
                validPrefix() + "\"day\":\"MONDAY\",\"targetServings\":1.5," + validRecipe() + validSuffix(),
                validPrefix() + "\"day\":\"MONDAY\",\"targetServings\":1,\"recipe\":{\"title\":\"R\",\"baseServings\":1,\"ingredients\":[{\"requirement\":\"Milk\",\"quantity\":{\"amount\":1,\"unit\":\"OUNCE\"}}]}}]}"
        }) {
            mvc.perform(post("/api/v1/weekly-plan-shopping-previews")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                    .andExpect(jsonPath("$.code").value("INVALID_WEEKLY_PLAN_SHOPPING_PREVIEW"))
                    .andExpect(jsonPath("$.errors.length()").value(1))
                    .andExpect(jsonPath("$.errors[0].field").value("$request"))
                    .andExpect(jsonPath("$.errors[0].message").value("malformed JSON request"));
        }
    }

    private static String validPrefix() {
        return "{\"occurrences\":[{";
    }

    private static String validRecipe() {
        return "\"recipe\":{\"title\":\"R\",\"baseServings\":1,\"ingredients\":[{\"requirement\":\"Milk\",\"quantity\":{\"amount\":1,\"unit\":\"PIECE\"}}]}";
    }

    private static String validSuffix() {
        return "}]}";
    }

    private static WeeklyPlanShoppingPreviewService service() {
        var weeklyIds = new WeeklyPlanShoppingPreviewIdGenerator() {
            @Override public WeeklyPlanId nextWeeklyPlanId() {
                return new WeeklyPlanId(UUID.fromString("a1000000-0000-0000-0000-000000000001"));
            }
            @Override public WeeklyMealOccurrenceId nextOccurrenceId() {
                return new WeeklyMealOccurrenceId(UUID.fromString("a2000000-0000-0000-0000-000000000001"));
            }
        };
        var recipeIds = new RecipeShoppingPreviewIdGenerator() {
            @Override public RecipeId nextRecipeId() {
                return new RecipeId(UUID.fromString("a3000000-0000-0000-0000-000000000001"));
            }
            @Override public RecipeIngredientId nextIngredientId() {
                return new RecipeIngredientId(UUID.fromString("a4000000-0000-0000-0000-000000000001"));
            }
            @Override public ShoppingListId nextShoppingListId() {
                return new ShoppingListId(UUID.fromString("a5000000-0000-0000-0000-000000000001"));
            }
        };
        return new WeeklyPlanShoppingPreviewService(
                new WeeklyPlanShoppingPreviewRequestFactory(
                        weeklyIds,
                        new RecipeShoppingPreviewRequestFactory(recipeIds)),
                new WeeklyPlanShoppingListComposer());
    }
}
