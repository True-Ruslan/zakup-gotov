package io.github.trueruslan.zakupgotov.preview;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.trueruslan.zakupgotov.database.PostgresIntegrationSupport;
import io.github.trueruslan.zakupgotov.retailer.ProductionAccessStatus;
import io.github.trueruslan.zakupgotov.retailer.Retailer;
import io.github.trueruslan.zakupgotov.retailer.RetailerCoverageState;
import io.github.trueruslan.zakupgotov.retailer.RetailerId;
import io.github.trueruslan.zakupgotov.retailer.RetailerRegistry;
import io.github.trueruslan.zakupgotov.retailer.RetailerRegistryEntry;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@Import(ComparisonPreviewDeterministicIntegrationTest.DeterministicPreviewConfiguration.class)
class ComparisonPreviewDeterministicIntegrationTest extends PostgresIntegrationSupport {

    @Autowired
    MockMvc mockMvc;

    @Test
    void publicHttpRequestTraversesTheDeterministicCoreWithMixedRetailerOutcomes() throws Exception {
        var body = """
                {
                  "locality": "Москва",
                  "items": [
                    {
                      "id": "c281d71c-2b27-46ef-a7af-3d624a7447cf",
                      "requirement": "Молоко",
                      "quantity": {"amount": 2, "unit": "LITER"}
                    },
                    {
                      "id": "66d66ee8-521f-48ef-82bd-bc9b850099c2",
                      "requirement": "Яйца",
                      "quantity": {"amount": 10, "unit": "PIECE"}
                    }
                  ]
                }
                """;

        mockMvc.perform(post("/api/v1/comparison-previews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.locality").value("Москва"))
                .andExpect(jsonPath("$.items", hasSize(2)))
                .andExpect(jsonPath("$.retailers", hasSize(8)))
                .andExpect(jsonPath("$.retailers[0].id").value("pyaterochka"))
                .andExpect(jsonPath("$.retailers[0].comparisonStatus").value("READY"))
                .andExpect(jsonPath("$.retailers[0].items", hasSize(2)))
                .andExpect(jsonPath("$.retailers[0].items[0].selection.productName").value("Молоко"))
                .andExpect(jsonPath("$.retailers[0].items[0].selection.packageCount").value(2))
                .andExpect(jsonPath("$.retailers[1].id").value("perekrestok"))
                .andExpect(jsonPath("$.retailers[1].comparisonStatus").value("UNCERTAIN"))
                .andExpect(jsonPath("$.retailers[1].reasons", hasItem("AVAILABILITY_UNKNOWN")))
                .andExpect(jsonPath("$.retailers[2].id").value("chizhik"))
                .andExpect(jsonPath("$.retailers[2].comparisonStatus").value("UNAVAILABLE"))
                .andExpect(jsonPath("$.retailers[2].reasons", hasItem("DATA_NOT_AVAILABLE")))
                .andExpect(jsonPath("$.retailers[3].id").value("magnit"))
                .andExpect(jsonPath("$.retailers[3].comparisonStatus").value("INCOMPLETE"))
                .andExpect(jsonPath("$.retailers[3].reasons", hasItem("PACKAGE_QUANTITY_UNKNOWN")))
                .andExpect(jsonPath("$.retailers[4].reasons", hasItem("ITEM_UNMATCHED")))
                .andExpect(jsonPath("$.retailers[5].reasons", hasItem("ITEM_AMBIGUOUS")))
                .andExpect(jsonPath("$.retailers[6].reasons", hasItem("QUANTITY_UNIT_MISMATCH")))
                .andExpect(jsonPath("$.retailers[7].comparisonStatus").value("UNAVAILABLE"))
                .andExpect(jsonPath("$.retailers[7].reasons", hasItem("SOURCE_UNAVAILABLE")))
                .andExpect(jsonPath("$..sku").doesNotExist())
                .andExpect(jsonPath("$..sourceProviderId").doesNotExist())
                .andExpect(jsonPath("$..acquisitionMode").doesNotExist())
                .andExpect(jsonPath("$..sourceReference").doesNotExist())
                .andExpect(jsonPath("$..fulfillmentContextId").doesNotExist());
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class DeterministicPreviewConfiguration {

        @Bean
        @Primary
        RetailerRegistry deterministicPreviewRetailerRegistry() {
            return RetailerRegistry.of(Arrays.stream(RetailerId.values())
                    .map(id -> new RetailerRegistryEntry(
                            new Retailer(id),
                            RetailerCoverageState.AVAILABLE_DIRECT,
                            ProductionAccessStatus.ACCEPTABLE))
                    .toList());
        }

        @Bean
        @Primary
        ComparisonRuntimeEvidenceSource deterministicComparisonRuntimeEvidenceSource() {
            return new DeterministicComparisonRuntimeEvidenceSource();
        }
    }
}
