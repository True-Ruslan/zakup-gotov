package io.github.trueruslan.zakupgotov.provider;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.trueruslan.zakupgotov.location.ProductLocation;
import io.github.trueruslan.zakupgotov.location.SensitiveAddress;
import io.github.trueruslan.zakupgotov.retailer.RetailerId;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ProviderPathLocationBoundaryTest {

    @Test
    void orchestratorRequiresTypedFulfillmentContextSetInsteadOfRawProviderMap() throws Exception {
        var search = ProviderPathOrchestrator.class.getMethod(
                "search",
                RetailerId.class,
                List.class,
                FulfillmentContextSet.class,
                ProductQuery.class);

        assertThat(search.getParameterTypes())
                .contains(FulfillmentContextSet.class)
                .doesNotContain(ProductLocation.class, SensitiveAddress.class);

        assertThat(Arrays.stream(ProviderPathOrchestrator.class.getMethods())
                        .filter(method -> method.getName().equals("search"))
                        .flatMap(method -> Arrays.stream(method.getParameterTypes())))
                .noneMatch(Map.class::isAssignableFrom);
    }
}
