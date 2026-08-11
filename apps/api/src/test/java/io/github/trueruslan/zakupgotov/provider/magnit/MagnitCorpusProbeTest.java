package io.github.trueruslan.zakupgotov.provider.magnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class MagnitCorpusProbeTest {

    @Test
    void fixedCorpusContainsExactlyTheApprovedTwentyRequirements() {
        var corpus = MagnitCorpusProbe.fixedCorpus();

        assertThat(corpus).hasSize(20);
        assertThat(corpus.stream().map(MagnitCorpusProbe.CorpusItem::requirement).toList())
                .containsExactly(
                        "milk",
                        "eggs",
                        "bread",
                        "bananas",
                        "potatoes",
                        "onions",
                        "tomatoes",
                        "cucumbers",
                        "chicken",
                        "beef/mince",
                        "rice",
                        "buckwheat",
                        "pasta",
                        "sunflower oil",
                        "butter",
                        "cheese",
                        "kefir",
                        "sugar",
                        "salt",
                        "tea");
        assertThat(corpus)
                .allSatisfy(item -> {
                    assertThat(item.sku()).isNotBlank();
                    assertThat(item.productSlug()).startsWith(item.sku() + "-");
                });
        assertThat(corpus.stream().map(MagnitCorpusProbe.CorpusItem::sku).toList())
                .doesNotHaveDuplicates();
    }

    @Test
    void parsesCurrentAndRegularPromoPriceBoundToExpectedSku() throws Exception {
        var observation = MagnitCorpusProbe.parseProductPage(fixture("promo-available.html"), "9072651501");

        assertThat(observation.skuEvidence()).isTrue();
        assertThat(observation.currentPrice()).contains(new BigDecimal("123.45"));
        assertThat(observation.regularPrice()).contains(new BigDecimal("150.00"));
        assertThat(observation.promo()).isTrue();
        assertThat(observation.availability()).isEqualTo(MagnitCorpusProbe.Availability.AVAILABLE);
    }

    @Test
    void fallsBackToSkuBoundEmbeddedCurrentPriceWhenRenderedScopeHasNoPrice() throws Exception {
        var observation = MagnitCorpusProbe.parseProductPage(fixture("embedded-current-price.html"), "9072651501");

        assertThat(observation.skuEvidence()).isTrue();
        assertThat(observation.currentPrice()).contains(new BigDecimal("123.45"));
        assertThat(observation.regularPrice()).isEmpty();
        assertThat(observation.promo()).isFalse();
        assertThat(observation.availability()).isEqualTo(MagnitCorpusProbe.Availability.AVAILABLE);
    }

    @Test
    void treatsExplicitProductUnavailabilityAsUnavailable() throws Exception {
        var observation = MagnitCorpusProbe.parseProductPage(fixture("regular-unavailable.html"), "1000135280");

        assertThat(observation.currentPrice()).contains(new BigDecimal("111.11"));
        assertThat(observation.regularPrice()).isEmpty();
        assertThat(observation.promo()).isFalse();
        assertThat(observation.availability()).isEqualTo(MagnitCorpusProbe.Availability.UNAVAILABLE);
    }

    @Test
    void keepsAvailabilityUnknownWhenNoExplicitStockSemanticExists() throws Exception {
        var observation = MagnitCorpusProbe.parseProductPage(fixture("regular-unknown.html"), "3152910003");

        assertThat(observation.currentPrice()).contains(new BigDecimal("88.88"));
        assertThat(observation.availability()).isEqualTo(MagnitCorpusProbe.Availability.UNKNOWN);
    }

    @Test
    void refusesPriceAndAvailabilityWhenExpectedSkuIsMissing() throws Exception {
        var observation = MagnitCorpusProbe.parseProductPage(fixture("promo-available.html"), "1111111111");

        assertThat(observation.skuEvidence()).isFalse();
        assertThat(observation.currentPrice()).isEmpty();
        assertThat(observation.regularPrice()).isEmpty();
        assertThat(observation.availability()).isEqualTo(MagnitCorpusProbe.Availability.UNKNOWN);
    }

    @Test
    void evidenceLineIncludesOnlyApprovedFailedRequirementNames() {
        var result = new MagnitCorpusProbe.CorpusResult(
                20, 40, 19, 19, 19, 19, 19, 6, 0, List.of("tea", "beef/mince"));

        assertThat(result.toEvidenceLine())
                .endsWith("failed_count=2 failed_requirements=tea,beef/mince");
    }

    @Test
    void livePhaseBCorpusRunsOnlyWhenExplicitlyEnabled() throws Exception {
        assumeTrue(Boolean.getBoolean("zakup.live.magnit.corpus"));

        var result = MagnitCorpusProbe.create().runFixedCorpus("139147", "773577");
        System.out.println(result.toEvidenceLine());

        assertThat(result.totalRequirements()).isEqualTo(20);
        assertThat(result.totalRequests()).isEqualTo(40);
        assertThat(result.firstHttp2xx()).isBetween(0, 20);
        assertThat(result.secondHttp2xx()).isBetween(0, 20);
        assertThat(result.firstUsable()).isBetween(0, 20);
        assertThat(result.secondUsable()).isBetween(0, 20);
        assertThat(result.stableIdentity()).isBetween(0, 20);
        assertThat(result.knownAvailability()).isBetween(0, 40);
        assertThat(result.promoObservations()).isBetween(0, 40);

        var approvedRequirements = Set.copyOf(MagnitCorpusProbe.fixedCorpus().stream()
                .map(MagnitCorpusProbe.CorpusItem::requirement)
                .toList());
        assertThat(result.failedRequirements()).allSatisfy(requirement -> assertThat(approvedRequirements).contains(requirement));
    }

    private String fixture(String name) throws Exception {
        try (var input = getClass().getResourceAsStream("/provider/magnit/" + name)) {
            assertThat(input).as("fixture %s", name).isNotNull();
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
