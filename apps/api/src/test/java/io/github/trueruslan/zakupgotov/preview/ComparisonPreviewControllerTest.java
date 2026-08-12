package io.github.trueruslan.zakupgotov.preview;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.trueruslan.zakupgotov.database.PostgresIntegrationSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class ComparisonPreviewControllerTest extends PostgresIntegrationSupport {

    @Autowired
    MockMvc mockMvc;

    @Test
    void productionEndpointNormalizesRequestAndNeverFabricatesRuntimeRetailerData() throws Exception {
        var body = """
                {
                  "locality": "  Москва  ",
                  "items": [
                    {
                      "id": "c281d71c-2b27-46ef-a7af-3d624a7447cf",
                      "requirement": "  Молоко  ",
                      "amount": 2,
                      "unit": "LITER"
                    }
                  ]
                }
                """;

        mockMvc.perform(post("/api/v1/comparison-previews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.locality").value("Москва"))
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].id").value("c281d71c-2b27-46ef-a7af-3d624a7447cf"))
                .andExpect(jsonPath("$.items[0].requirement").value("Молоко"))
                .andExpect(jsonPath("$.items[0].quantity.amount").value(2000))
                .andExpect(jsonPath("$.items[0].quantity.unit").value("MILLILITER"))
                .andExpect(jsonPath("$.retailers", hasSize(8)))
                .andExpect(jsonPath("$.retailers[0].id").value("pyaterochka"))
                .andExpect(jsonPath("$.retailers[0].comparisonStatus").value("UNAVAILABLE"))
                .andExpect(jsonPath("$.retailers[0].reasons[0]").value("PRODUCTION_ACCESS_PENDING"))
                .andExpect(jsonPath("$.retailers[0].items", hasSize(0)))
                .andExpect(jsonPath("$.retailers[0].total").doesNotExist())
                .andExpect(jsonPath("$.retailers[0].freshness").doesNotExist())
                .andExpect(jsonPath("$..sku").doesNotExist())
                .andExpect(jsonPath("$..sourceProviderId").doesNotExist())
                .andExpect(jsonPath("$..acquisitionMode").doesNotExist())
                .andExpect(jsonPath("$..sourceReference").doesNotExist())
                .andExpect(jsonPath("$..fulfillmentContextId").doesNotExist());
    }

    @Test
    void invalidQuantityReturnsStableProductSafeProblemDetails() throws Exception {
        var body = """
                {
                  "locality": "Москва",
                  "items": [
                    {
                      "id": "c281d71c-2b27-46ef-a7af-3d624a7447cf",
                      "requirement": "Молоко",
                      "amount": 0,
                      "unit": "LITER"
                    }
                  ]
                }
                """;

        mockMvc.perform(post("/api/v1/comparison-previews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").value("https://zakup-gotov.dev/problems/invalid-comparison-preview"))
                .andExpect(jsonPath("$.title").value("Invalid comparison preview request"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("INVALID_COMPARISON_PREVIEW"))
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].field").value("items[0].amount"))
                .andExpect(jsonPath("$.errors[0].message").value("must be greater than 0"))
                .andExpect(jsonPath("$..sourceProviderId").doesNotExist())
                .andExpect(jsonPath("$..exception").doesNotExist())
                .andExpect(jsonPath("$..trace").doesNotExist());
    }

    @Test
    void malformedJsonReturnsRequestScopedProblemWithoutInternalDetails() throws Exception {
        mockMvc.perform(post("/api/v1/comparison-previews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"locality\":\"Москва\",\"items\":["))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.code").value("INVALID_COMPARISON_PREVIEW"))
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].field").value("$request"))
                .andExpect(jsonPath("$.errors[0].message").value("malformed JSON request"))
                .andExpect(jsonPath("$..exception").doesNotExist())
                .andExpect(jsonPath("$..trace").doesNotExist());
    }
}
