package io.github.trueruslan.zakupgotov.weeklyplanpantryoptimizationpreview;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.trueruslan.zakupgotov.basketoptimization.BasketOptimizer;
import io.github.trueruslan.zakupgotov.optimizationpreview.CheckoutOptimizationPreviewService;
import io.github.trueruslan.zakupgotov.optimizationpreview.NoopCheckoutEconomicsEvidenceSource;
import io.github.trueruslan.zakupgotov.preview.ComparisonPreviewService;
import io.github.trueruslan.zakupgotov.preview.NoopComparisonRuntimeEvidenceSource;
import io.github.trueruslan.zakupgotov.recipe.RecipeId;
import io.github.trueruslan.zakupgotov.recipe.RecipeIngredientId;
import io.github.trueruslan.zakupgotov.recipepreview.RecipeShoppingPreviewIdGenerator;
import io.github.trueruslan.zakupgotov.recipepreview.RecipeShoppingPreviewRequestFactory;
import io.github.trueruslan.zakupgotov.retailer.RetailerRegistry;
import io.github.trueruslan.zakupgotov.retailercheckout.RetailerCheckoutAssessmentService;
import io.github.trueruslan.zakupgotov.shopping.ShoppingListId;
import io.github.trueruslan.zakupgotov.weeklyplan.WeeklyMealOccurrenceId;
import io.github.trueruslan.zakupgotov.weeklyplan.WeeklyPlanId;
import io.github.trueruslan.zakupgotov.weeklyplan.WeeklyPlanShoppingListComposer;
import io.github.trueruslan.zakupgotov.weeklyplanpantrycomparisonpreview.WeeklyPlanPantryComparisonPreviewService;
import io.github.trueruslan.zakupgotov.weeklyplanpantrypreview.WeeklyPlanPantryShoppingPreviewService;
import io.github.trueruslan.zakupgotov.weeklyplanpreview.WeeklyPlanShoppingPreviewIdGenerator;
import io.github.trueruslan.zakupgotov.weeklyplanpreview.WeeklyPlanShoppingPreviewRequestFactory;
import io.github.trueruslan.zakupgotov.weeklyplanpreview.WeeklyPlanShoppingPreviewService;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class WeeklyPlanPantryOptimizationPreviewControllerTest {

    @Test
    void comparedDemandExposesServerOwnedOptimizationState() throws Exception {
        mvc().perform(post("/api/v1/weekly-plan-pantry-optimization-previews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("Москва", "250", "MILLILITER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pantryComparisonPreview.comparisonOutcome").value("COMPARED"))
                .andExpect(jsonPath("$.pantryComparisonPreview.pantryShoppingPreview.remainingShoppingList.items[0].quantity.amount")
                        .value(750))
                .andExpect(jsonPath("$.optimizationPreview.status").value("NO_COMPARABLE_CANDIDATES"))
                .andExpect(jsonPath("$.optimizationPreview.optimalRetailerIds.length()").value(0))
                .andExpect(jsonPath("$.optimizationPreview.retailers.length()").value(8))
                .andExpect(jsonPath("$.optimizationPreview.retailers[0].retailerId").value("pyaterochka"));
    }

    @Test
    void fullPantryCoverageOmitsOptimizationPayload() throws Exception {
        mvc().perform(post("/api/v1/weekly-plan-pantry-optimization-previews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("Москва", "1", "LITER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pantryComparisonPreview.comparisonOutcome").value("NO_REMAINING_DEMAND"))
                .andExpect(jsonPath("$.pantryComparisonPreview.pantryShoppingPreview.remainingShoppingList.items.length()")
                        .value(0))
                .andExpect(content().string(not(containsString("\"optimizationPreview\""))));
    }

    @Test
    void translatesSemanticValidationIntoOptimizationProblemBoundary() throws Exception {
        mvc().perform(post("/api/v1/weekly-plan-pantry-optimization-previews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("   ", "1", "LITER")))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.code").value("INVALID_WEEKLY_PLAN_PANTRY_OPTIMIZATION_PREVIEW"))
                .andExpect(jsonPath("$.errors[0].field").value("locality"))
                .andExpect(jsonPath("$.errors[0].message").value("must not be blank"));
    }

    @Test
    void sanitizesMalformedUnknownAndUnsupportedJson() throws Exception {
        for (var payload : new String[] {
                "{",
                "{\"locality\":\"Москва\",\"weeklyPlan\":{\"occurrences\":[]},\"pantry\":[],\"unknown\":true}",
                body("Москва", "1", "OUNCE")
        }) {
            mvc().perform(post("/api/v1/weekly-plan-pantry-optimization-previews")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(payload))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                    .andExpect(jsonPath("$.code").value("INVALID_WEEKLY_PLAN_PANTRY_OPTIMIZATION_PREVIEW"))
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
        var pantryComparisonService = new WeeklyPlanPantryComparisonPreviewService(
                new WeeklyPlanPantryShoppingPreviewService(weeklyService()),
                new ComparisonPreviewService(
                        RetailerRegistry.initial(),
                        new NoopComparisonRuntimeEvidenceSource()));
        var optimizationService = new CheckoutOptimizationPreviewService(
                new NoopCheckoutEconomicsEvidenceSource(),
                new RetailerCheckoutAssessmentService(),
                new BasketOptimizer());
        var service = new WeeklyPlanPantryOptimizationPreviewService(
                pantryComparisonService,
                optimizationService);
        return MockMvcBuilders.standaloneSetup(new WeeklyPlanPantryOptimizationPreviewController(service))
                .setControllerAdvice(new WeeklyPlanPantryOptimizationPreviewExceptionHandler())
                .build();
    }

    private static WeeklyPlanShoppingPreviewService weeklyService() {
        var weeklyIds = new WeeklyPlanShoppingPreviewIdGenerator() {
            @Override public WeeklyPlanId nextWeeklyPlanId() {
                return new WeeklyPlanId(UUID.fromString("f1000000-0000-0000-0000-000000000001"));
            }
            @Override public WeeklyMealOccurrenceId nextOccurrenceId() {
                return new WeeklyMealOccurrenceId(UUID.fromString("f2000000-0000-0000-0000-000000000001"));
            }
        };
        var recipeIds = new RecipeShoppingPreviewIdGenerator() {
            @Override public RecipeId nextRecipeId() {
                return new RecipeId(UUID.fromString("f3000000-0000-0000-0000-000000000001"));
            }
            @Override public RecipeIngredientId nextIngredientId() {
                return new RecipeIngredientId(UUID.fromString("f4000000-0000-0000-0000-000000000001"));
            }
            @Override public ShoppingListId nextShoppingListId() {
                return new ShoppingListId(UUID.fromString("f5000000-0000-0000-0000-000000000001"));
            }
        };
        return new WeeklyPlanShoppingPreviewService(
                new WeeklyPlanShoppingPreviewRequestFactory(
                        weeklyIds,
                        new RecipeShoppingPreviewRequestFactory(recipeIds)),
                new WeeklyPlanShoppingListComposer());
    }
}
