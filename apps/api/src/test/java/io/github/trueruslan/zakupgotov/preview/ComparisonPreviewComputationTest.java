package io.github.trueruslan.zakupgotov.preview;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.trueruslan.zakupgotov.retailer.RetailerRegistry;
import io.github.trueruslan.zakupgotov.shopping.QuantityUnit;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ComparisonPreviewComputationTest {

    @Test
    void computePreservesNormalizedInputDomainCatalogAndExistingPublicProjection() {
        var service = new ComparisonPreviewService(
                RetailerRegistry.initial(),
                new NoopComparisonRuntimeEvidenceSource());
        var itemId = UUID.fromString("2f08f4ab-f643-4eaf-b5a7-736dc8bedef4");
        var request = new ComparisonPreviewRequest(
                "  Москва   ",
                List.of(new ComparisonPreviewItemRequest(
                        itemId,
                        "Молоко",
                        new BigDecimal("2"),
                        QuantityUnit.LITER)));

        var computation = service.compute(request);

        assertThat(computation.input().productLocation().locality()).isEqualTo("Москва");
        assertThat(computation.input().shoppingList().items())
                .extracting(item -> item.id().value())
                .containsExactly(itemId);
        assertThat(computation.preview()).isEqualTo(service.create(request));
        assertThat(computation.catalog().retailers())
                .extracting(view -> view.retailerId().canonicalId())
                .containsExactlyElementsOf(
                        computation.preview().retailers().stream()
                                .map(ComparisonPreviewRetailer::id)
                                .toList());
    }
}
