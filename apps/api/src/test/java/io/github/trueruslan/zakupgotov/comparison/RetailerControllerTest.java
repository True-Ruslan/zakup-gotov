package io.github.trueruslan.zakupgotov.comparison;

import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
class RetailerControllerTest extends PostgresIntegrationSupport {

    @Autowired
    MockMvc mockMvc;

    @Test
    void returnsAllCanonicalRetailersWithProductSafeReadinessState() throws Exception {
        mockMvc.perform(get("/api/v1/retailers"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", aMapWithSize(1)))
                .andExpect(jsonPath("$.retailers", hasSize(8)))
                .andExpect(jsonPath("$.retailers[0].id").value("pyaterochka"))
                .andExpect(jsonPath("$.retailers[0].displayName").value("Пятёрочка"))
                .andExpect(jsonPath("$.retailers[0].coverage").value("CONNECTED"))
                .andExpect(jsonPath("$.retailers[0].productionAccess").value("PENDING"))
                .andExpect(jsonPath("$.retailers[0].comparisonStatus").value("UNAVAILABLE"))
                .andExpect(jsonPath("$.retailers[0].reasons", hasSize(1)))
                .andExpect(jsonPath("$.retailers[0].reasons[0]").value("PRODUCTION_ACCESS_PENDING"))
                .andExpect(jsonPath("$.retailers[0].total").doesNotExist())
                .andExpect(jsonPath("$.retailers[0].freshness").doesNotExist())
                .andExpect(jsonPath("$.retailers[1].id").value("perekrestok"))
                .andExpect(jsonPath("$.retailers[2].id").value("chizhik"))
                .andExpect(jsonPath("$.retailers[2].coverage").value("DISCOVERY"))
                .andExpect(jsonPath("$.retailers[2].reasons[0]").value("COVERAGE_DISCOVERY"))
                .andExpect(jsonPath("$.retailers[3].id").value("magnit"))
                .andExpect(jsonPath("$.retailers[3].coverage").value("CONNECTED"))
                .andExpect(jsonPath("$.retailers[3].productionAccess").value("PENDING"))
                .andExpect(jsonPath("$.retailers[4].id").value("lenta"))
                .andExpect(jsonPath("$.retailers[5].id").value("vkusvill"))
                .andExpect(jsonPath("$.retailers[6].id").value("ozon-fresh"))
                .andExpect(jsonPath("$.retailers[7].id").value("samokat"))
                .andExpect(jsonPath("$..sourceProviderId").doesNotExist())
                .andExpect(jsonPath("$..sourceMode").doesNotExist())
                .andExpect(jsonPath("$..sourceReference").doesNotExist());
    }
}
