package io.github.trueruslan.zakupgotov.weeklyplanpantrypreview;

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
import io.github.trueruslan.zakupgotov.weeklyplanpreview.WeeklyPlanShoppingPreviewIdGenerator;
import io.github.trueruslan.zakupgotov.weeklyplanpreview.WeeklyPlanShoppingPreviewRequestFactory;
import io.github.trueruslan.zakupgotov.weeklyplanpreview.WeeklyPlanShoppingPreviewService;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class WeeklyPlanPantryShoppingPreviewControllerTest {

    @Test
    void exposesOriginalPantryEvidenceAndRemainingShoppingList() throws Exception {
        var mvc = mvc();

        mvc.perform(post("/api/v1/weekly-plan-pantry-shopping-previews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validBody("250", "MILLILITER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.weeklyPlan.occurrences[0].day").value("MONDAY"))
                .andExpect(jsonPath("$.originalShoppingList.items[0].requirement").value("Milk"))
                .andExpect(jsonPath("$.originalShoppingList.items[0].quantity.amount").value(1000))
                .andExpect(jsonPath("$.pantryAdjustments[0].status").value("PARTIALLY_COVERED"))
                .andExpect(jsonPath("$.pantryAdjustments[0].pantryUsed.amount").value(250))
                .andExpect(jsonPath("$.pantryAdjustments[0].remaining.amount").value(750))
                .andExpect(jsonPath("$.remainingShoppingList.items[0].quantity.amount").value(750))
                .andExpect(jsonPath("$.remainingShoppingList.items[0].sources[0].occurrenceId")
                        .value("a2000000-0000-0000-0000-000000000001"));

        mvc.perform(post("/api/v1/weekly-plan-pantry-shopping-previews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validBody("1", "LITER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pantryAdjustments[0].status").value("FULLY_COVERED"))
                .andExpect(jsonPath("$.pantryAdjustments[0].remaining").doesNotExist())
                .andExpect(jsonPath("$.remainingShoppingList.items.length()").value(0));
    }

    @Test
    void returnsSanitizedProductProblemForPantryAndNestedWeeklyValidation() throws Exception {
        var mvc = mvc();

        mvc.perform(post("/api/v1/weekly-plan-pantry-shopping-previews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validBody("0", "MILLILITER")))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.code").value("INVALID_WEEKLY_PLAN_PANTRY_SHOPPING_PREVIEW"))
                .andExpect(jsonPath("$.errors[0].field").value("pantry[0].quantity.amount"));

        mvc.perform(post("/api/v1/weekly-plan-pantry-shopping-previews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"weeklyPlan\":{\"occurrences\":[]},\"pantry\":[]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_WEEKLY_PLAN_PANTRY_SHOPPING_PREVIEW"))
                .andExpect(jsonPath("$.errors[0].field").value("weeklyPlan.occurrences"));
    }

    @Test
    void sanitizesMalformedJsonUnknownFieldsAndUnsupportedUnits() throws Exception {
        var mvc = mvc();
        for (var body : new String[] {
                "{",
                "{\"weeklyPlan\":{\"occurrences\":[]},\"pantry\":[],\"unknown\":true}",
                validBody("1", "OUNCE")
        }) {
            mvc.perform(post("/api/v1/weekly-plan-pantry-shopping-previews")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                    .andExpect(jsonPath("$.code").value("INVALID_WEEKLY_PLAN_PANTRY_SHOPPING_PREVIEW"))
                    .andExpect(jsonPath("$.errors.length()").value(1))
                    .andExpect(jsonPath("$.errors[0].field").value("$request"))
                    .andExpect(jsonPath("$.errors[0].message").value("malformed JSON request"));
        }
    }

    private static String validBody(String pantryAmount, String pantryUnit) {
        return """
                {"weeklyPlan":{"occurrences":[
                  {"day":"MONDAY","targetServings":2,"recipe":{
                    "title":"Breakfast","baseServings":2,"ingredients":[
                      {"requirement":"Milk","quantity":{"amount":1,"unit":"LITER"}}
                    ]
                  }}
                ]},"pantry":[{"requirement":"Milk","quantity":{"amount":%s,"unit":"%s"}}]}
                """.formatted(pantryAmount, pantryUnit);
    }

    private static org.springframework.test.web.servlet.MockMvc mvc() {
        var service = new WeeklyPlanPantryShoppingPreviewService(weeklyService());
        return MockMvcBuilders.standaloneSetup(new WeeklyPlanPantryShoppingPreviewController(service))
                .setControllerAdvice(new WeeklyPlanPantryShoppingPreviewExceptionHandler())
                .build();
    }

    private static WeeklyPlanShoppingPreviewService weeklyService() {
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
