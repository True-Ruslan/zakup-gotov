package io.github.trueruslan.zakupgotov.weeklyplanpantrycomparisonpreview;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.trueruslan.zakupgotov.preview.ComparisonPreview;
import io.github.trueruslan.zakupgotov.preview.ComparisonPreviewRequestedItem;
import io.github.trueruslan.zakupgotov.recipe.RecipeId;
import io.github.trueruslan.zakupgotov.recipe.RecipeIngredientId;
import io.github.trueruslan.zakupgotov.recipepreview.RecipeShoppingPreviewIdGenerator;
import io.github.trueruslan.zakupgotov.recipepreview.RecipeShoppingPreviewRequestFactory;
import io.github.trueruslan.zakupgotov.shopping.Quantity;
import io.github.trueruslan.zakupgotov.shopping.ShoppingListId;
import io.github.trueruslan.zakupgotov.weeklyplan.WeeklyMealOccurrenceId;
import io.github.trueruslan.zakupgotov.weeklyplan.WeeklyPlanId;
import io.github.trueruslan.zakupgotov.weeklyplan.WeeklyPlanShoppingListComposer;
import io.github.trueruslan.zakupgotov.weeklyplanpantrypreview.WeeklyPlanPantryShoppingPreviewService;
import io.github.trueruslan.zakupgotov.weeklyplanpreview.WeeklyPlanShoppingPreviewIdGenerator;
import io.github.trueruslan.zakupgotov.weeklyplanpreview.WeeklyPlanShoppingPreviewRequestFactory;
import io.github.trueruslan.zakupgotov.weeklyplanpreview.WeeklyPlanShoppingPreviewService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class WeeklyPlanPantryComparisonPreviewControllerTest {

    @Test
    void exposesComparedOutcomeForRemainingDemand() throws Exception {
        mvc().perform(post("/api/v1/weekly-plan-pantry-comparison-previews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("Москва", "250", "MILLILITER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comparisonOutcome").value("COMPARED"))
                .andExpect(jsonPath("$.pantryShoppingPreview.originalShoppingList.items[0].quantity.amount").value(1000))
                .andExpect(jsonPath("$.pantryShoppingPreview.pantryAdjustments[0].status").value("PARTIALLY_COVERED"))
                .andExpect(jsonPath("$.pantryShoppingPreview.remainingShoppingList.items[0].quantity.amount").value(750))
                .andExpect(jsonPath("$.comparisonPreview.locality").value("Москва"))
                .andExpect(jsonPath("$.comparisonPreview.items[0].quantity.amount").value(750));
    }

    @Test
    void exposesExplicitZeroDemandOutcomeWithoutComparisonPayload() throws Exception {
        mvc().perform(post("/api/v1/weekly-plan-pantry-comparison-previews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("Москва", "1", "LITER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comparisonOutcome").value("NO_REMAINING_DEMAND"))
                .andExpect(jsonPath("$.pantryShoppingPreview.pantryAdjustments[0].status").value("FULLY_COVERED"))
                .andExpect(jsonPath("$.pantryShoppingPreview.remainingShoppingList.items.length()").value(0))
                .andExpect(content().string(not(containsString("\"comparisonPreview\""))));
    }

    @Test
    void rejectsInvalidLocalityEvenWhenPantryFullyCoversDemand() throws Exception {
        mvc().perform(post("/api/v1/weekly-plan-pantry-comparison-previews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("   ", "1", "LITER")))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.code").value("INVALID_WEEKLY_PLAN_PANTRY_COMPARISON_PREVIEW"))
                .andExpect(jsonPath("$.errors[0].field").value("locality"))
                .andExpect(jsonPath("$.errors[0].message").value("must not be blank"));
    }

    @Test
    void mapsNestedPantryValidationIntoNewSanitizedBoundary() throws Exception {
        mvc().perform(post("/api/v1/weekly-plan-pantry-comparison-previews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("Москва", "0", "MILLILITER")))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.code").value("INVALID_WEEKLY_PLAN_PANTRY_COMPARISON_PREVIEW"))
                .andExpect(jsonPath("$.errors[0].field").value("pantry[0].quantity.amount"));
    }

    @Test
    void sanitizesMalformedJsonUnknownPropertiesAndUnsupportedUnits() throws Exception {
        for (var body : new String[] {
                "{",
                "{\"locality\":\"Москва\",\"weeklyPlan\":{\"occurrences\":[]},\"pantry\":[],\"unknown\":true}",
                body("Москва", "1", "OUNCE")
        }) {
            mvc().perform(post("/api/v1/weekly-plan-pantry-comparison-previews")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                    .andExpect(jsonPath("$.code").value("INVALID_WEEKLY_PLAN_PANTRY_COMPARISON_PREVIEW"))
                    .andExpect(jsonPath("$.errors.length()").value(1))
                    .andExpect(jsonPath("$.errors[0].field").value("$request"))
                    .andExpect(jsonPath("$.errors[0].message").value("malformed JSON request"));
        }
    }

    private static String body(String locality, String pantryAmount, String pantryUnit) {
        return """
                {"locality":"%s","weeklyPlan":{"occurrences":[
                  {"day":"MONDAY","targetServings":2,"recipe":{
                    "title":"Breakfast","baseServings":2,"ingredients":[
                      {"requirement":"Milk","quantity":{"amount":1,"unit":"LITER"}}
                    ]
                  }}
                ]},"pantry":[{"requirement":"Milk","quantity":{"amount":%s,"unit":"%s"}}]}
                """.formatted(locality, pantryAmount, pantryUnit);
    }

    private static org.springframework.test.web.servlet.MockMvc mvc() {
        var pantryService = new WeeklyPlanPantryShoppingPreviewService(weeklyService());
        var service = new WeeklyPlanPantryComparisonPreviewService(pantryService, request -> {
            var items = request.items().stream()
                    .map(item -> new ComparisonPreviewRequestedItem(
                            item.id(),
                            item.requirement(),
                            new Quantity(item.quantity().amount(), item.quantity().unit())))
                    .toList();
            return new ComparisonPreview(request.locality(), items, List.of());
        });
        return MockMvcBuilders.standaloneSetup(new WeeklyPlanPantryComparisonPreviewController(service))
                .setControllerAdvice(new WeeklyPlanPantryComparisonPreviewExceptionHandler())
                .build();
    }

    private static WeeklyPlanShoppingPreviewService weeklyService() {
        var weeklyIds = new WeeklyPlanShoppingPreviewIdGenerator() {
            @Override public WeeklyPlanId nextWeeklyPlanId() {
                return new WeeklyPlanId(UUID.fromString("c1000000-0000-0000-0000-000000000001"));
            }
            @Override public WeeklyMealOccurrenceId nextOccurrenceId() {
                return new WeeklyMealOccurrenceId(UUID.fromString("c2000000-0000-0000-0000-000000000001"));
            }
        };
        var recipeIds = new RecipeShoppingPreviewIdGenerator() {
            @Override public RecipeId nextRecipeId() {
                return new RecipeId(UUID.fromString("c3000000-0000-0000-0000-000000000001"));
            }
            @Override public RecipeIngredientId nextIngredientId() {
                return new RecipeIngredientId(UUID.fromString("c4000000-0000-0000-0000-000000000001"));
            }
            @Override public ShoppingListId nextShoppingListId() {
                return new ShoppingListId(UUID.fromString("c5000000-0000-0000-0000-000000000001"));
            }
        };
        return new WeeklyPlanShoppingPreviewService(
                new WeeklyPlanShoppingPreviewRequestFactory(
                        weeklyIds,
                        new RecipeShoppingPreviewRequestFactory(recipeIds)),
                new WeeklyPlanShoppingListComposer());
    }
}
