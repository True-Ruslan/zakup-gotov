package io.github.trueruslan.zakupgotov.provider.magnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.math.BigDecimal;
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
    void parsesCurrentAndRegularPromoPriceBoundToExpectedSku() {
        var html = """
                <html><body>
                  <aside>Другой товар 9 ₽ Нет в наличии</aside>
                  <h1>Бананы 1кг</h1>
                  <div>Финальная цена</div>
                  <div>159,99 ₽</div>
                  <div>199,99 ₽</div>
                  <div>-20%</div>
                  <button>Добавить в корзину</button>
                  <section>Характеристики Артикул 9072651501</section>
                  <footer>999 ₽</footer>
                </body></html>
                """;

        var observation = MagnitCorpusProbe.parseProductPage(html, "9072651501");

        assertThat(observation.skuEvidence()).isTrue();
        assertThat(observation.currentPrice()).contains(new BigDecimal("159.99"));
        assertThat(observation.regularPrice()).contains(new BigDecimal("199.99"));
        assertThat(observation.promo()).isTrue();
        assertThat(observation.availability()).isEqualTo(MagnitCorpusProbe.Availability.AVAILABLE);
    }

    @Test
    void treatsExplicitProductUnavailabilityAsUnavailable() {
        var html = """
                <h1>Яйцо куриное С1 15шт</h1>
                <div>Нет в наличии</div>
                <div>159.99 ₽</div>
                <section>Характеристики Артикул 1000135280</section>
                """;

        var observation = MagnitCorpusProbe.parseProductPage(html, "1000135280");

        assertThat(observation.currentPrice()).contains(new BigDecimal("159.99"));
        assertThat(observation.regularPrice()).isEmpty();
        assertThat(observation.promo()).isFalse();
        assertThat(observation.availability()).isEqualTo(MagnitCorpusProbe.Availability.UNAVAILABLE);
    }

    @Test
    void keepsAvailabilityUnknownWhenNoExplicitStockSemanticExists() {
        var html = """
                <h1>Рис длиннозерный 800г</h1>
                <div>62.59 ₽</div>
                <section>Характеристики Артикул 3152910005</section>
                """;

        var observation = MagnitCorpusProbe.parseProductPage(html, "3152910005");

        assertThat(observation.currentPrice()).contains(new BigDecimal("62.59"));
        assertThat(observation.availability()).isEqualTo(MagnitCorpusProbe.Availability.UNKNOWN);
    }

    @Test
    void refusesPriceAndAvailabilityWhenExpectedSkuIsMissing() {
        var html = """
                <h1>Другой товар</h1>
                <div>Финальная цена</div>
                <div>10 ₽</div>
                <div>20 ₽</div>
                <button>Добавить в корзину</button>
                <section>Артикул 1111111111</section>
                """;

        var observation = MagnitCorpusProbe.parseProductPage(html, "9072651501");

        assertThat(observation.skuEvidence()).isFalse();
        assertThat(observation.currentPrice()).isEmpty();
        assertThat(observation.regularPrice()).isEmpty();
        assertThat(observation.availability()).isEqualTo(MagnitCorpusProbe.Availability.UNKNOWN);
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
        assertThat(result.failedRequirements())
                .allSatisfy(requirement -> assertThat(Set.copyOf(
                                MagnitCorpusProbe.fixedCorpus().stream()
                                        .map(MagnitCorpusProbe.CorpusItem::requirement)
                                        .toList()))
                        .contains(requirement));
    }
}
