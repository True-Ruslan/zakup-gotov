package io.github.trueruslan.zakupgotov.provider.magnit;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.trueruslan.zakupgotov.shopping.Quantity;
import io.github.trueruslan.zakupgotov.shopping.QuantityUnit;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class MagnitJsonLdPackageQuantityExtractorTest {

    @Test
    void extractsCanonicalWeightFromExactSkuProduct() {
        var result = MagnitJsonLdPackageQuantityExtractor.extract(page(product("""
                {
                  "@context": "https://schema.org/",
                  "@type": "Product",
                  "sku": "3042670099",
                  "name": "Макароны Makfa Виток 450г",
                  "weight": "0.45"
                }
                """)), "3042670099");

        assertThat(result.status()).isEqualTo(MagnitPackageQuantityStatus.FOUND);
        assertThat(result.packageQuantity())
                .contains(new Quantity(new BigDecimal("450"), QuantityUnit.GRAM));
    }

    @Test
    void extractsCanonicalVolumeFromExactAdditionalProperty() {
        var result = MagnitJsonLdPackageQuantityExtractor.extract(page(product("""
                {
                  "@context": "https://schema.org/",
                  "@type": "Product",
                  "sku": "1000273122",
                  "additionalProperty": [
                    {"@type": "PropertyValue", "name": "Объем, л", "value": "0.5"},
                    {"@type": "PropertyValue", "name": "Срок годности, дней", "value": "730"}
                  ]
                }
                """)), "1000273122");

        assertThat(result.status()).isEqualTo(MagnitPackageQuantityStatus.FOUND);
        assertThat(result.packageQuantity())
                .contains(new Quantity(new BigDecimal("500"), QuantityUnit.MILLILITER));
    }

    @Test
    void failsClosedWhenMatchingProductPublishesWeightAndVolume() {
        var result = MagnitJsonLdPackageQuantityExtractor.extract(page(product("""
                {
                  "@type": "Product",
                  "sku": "1000548435",
                  "weight": "1.028",
                  "additionalProperty": [
                    {"name": "Объем, л", "value": "1"}
                  ]
                }
                """)), "1000548435");

        assertThat(result.status()).isEqualTo(MagnitPackageQuantityStatus.AMBIGUOUS_DIMENSIONS);
        assertThat(result.packageQuantity()).isEmpty();
    }

    @Test
    void ignoresForeignSkuAndNonProductNodes() {
        var html = page(
                product("""
                        {"@type":"Product","sku":"foreign","weight":"9"}
                        """),
                product("""
                        {"@type":"Offer","sku":"wanted","weight":"8"}
                        """));

        var result = MagnitJsonLdPackageQuantityExtractor.extract(html, "wanted");

        assertThat(result.status()).isEqualTo(MagnitPackageQuantityStatus.MISSING);
        assertThat(result.packageQuantity()).isEmpty();
    }

    @Test
    void acceptsProductWhenTypeArrayContainsProduct() {
        var result = MagnitJsonLdPackageQuantityExtractor.extract(page(product("""
                {"@type":["Thing","Product"],"sku":"42","weight":0.8}
                """)), "42");

        assertThat(result.status()).isEqualTo(MagnitPackageQuantityStatus.FOUND);
        assertThat(result.packageQuantity())
                .contains(new Quantity(new BigDecimal("800"), QuantityUnit.GRAM));
    }

    @Test
    void deduplicatesEquivalentEvidenceAcrossMatchingProductNodes() {
        var html = page(
                product("""
                        {"@type":"Product","sku":"42","weight":"0.45"}
                        """),
                product("""
                        {"@type":"Product","sku":"42","weight":0.450}
                        """));

        var result = MagnitJsonLdPackageQuantityExtractor.extract(html, "42");

        assertThat(result.status()).isEqualTo(MagnitPackageQuantityStatus.FOUND);
        assertThat(result.packageQuantity())
                .contains(new Quantity(new BigDecimal("450"), QuantityUnit.GRAM));
    }

    @Test
    void failsClosedForConflictingEvidenceAcrossMatchingProductNodes() {
        var html = page(
                product("""
                        {"@type":"Product","sku":"42","weight":"0.45"}
                        """),
                product("""
                        {"@type":"Product","sku":"42","weight":"0.50"}
                        """));

        var result = MagnitJsonLdPackageQuantityExtractor.extract(html, "42");

        assertThat(result.status()).isEqualTo(MagnitPackageQuantityStatus.CONFLICTING_VALUES);
        assertThat(result.packageQuantity()).isEmpty();
    }

    @Test
    void failsClosedForInvalidRecognizedScalarValues() {
        for (var value : new String[] {"0", "-1", "unknown"}) {
            var html = page(product("""
                    {"@type":"Product","sku":"42","weight":"%s"}
                    """.formatted(value)));

            var result = MagnitJsonLdPackageQuantityExtractor.extract(html, "42");

            assertThat(result.status()).as(value).isEqualTo(MagnitPackageQuantityStatus.INVALID_VALUE);
            assertThat(result.packageQuantity()).as(value).isEmpty();
        }
    }

    @Test
    void failsClosedForRecognizedVolumeWithoutScalarValue() {
        var result = MagnitJsonLdPackageQuantityExtractor.extract(page(product("""
                {
                  "@type":"Product",
                  "sku":"42",
                  "additionalProperty":[{"name":"Объем, л","value":{"value":1,"unitText":"л"}}]
                }
                """)), "42");

        assertThat(result.status()).isEqualTo(MagnitPackageQuantityStatus.INVALID_VALUE);
        assertThat(result.packageQuantity()).isEmpty();
    }

    @Test
    void doesNotGuessObjectValuedWeightOrUnprovenFields() {
        var result = MagnitJsonLdPackageQuantityExtractor.extract(page(product("""
                {
                  "@type":"Product",
                  "sku":"42",
                  "name":"Молоко 1л",
                  "description":"упаковка 1000 мл",
                  "url":"https://magnit.ru/product/42-moloko-1l",
                  "weight":{"@type":"QuantitativeValue","value":1,"unitText":"kg"},
                  "volume":"1",
                  "size":"1л",
                  "additionalProperty":[{"name":"Количество в упаковке","value":10}]
                }
                """)), "42");

        assertThat(result.status()).isEqualTo(MagnitPackageQuantityStatus.MISSING);
        assertThat(result.packageQuantity()).isEmpty();
    }

    @Test
    void malformedJsonLdAndOrdinaryScriptsCannotCreateEvidence() {
        var html = """
                <html><body>
                  <script type="application/ld+json">{"@type":"Product","sku":"42","weight":</script>
                  <script type="application/json">{"@type":"Product","sku":"42","weight":"0.45"}</script>
                  <script>window.product={"@type":"Product","sku":"42","weight":"0.45"}</script>
                </body></html>
                """;

        var result = MagnitJsonLdPackageQuantityExtractor.extract(html, "42");

        assertThat(result.status()).isEqualTo(MagnitPackageQuantityStatus.MISSING);
        assertThat(result.packageQuantity()).isEmpty();
    }

    @Test
    void toleratesJsonLdTypeAttributeOrderQuotingAndCase() {
        var html = """
                <html><head>
                  <script nonce='abc' TYPE='Application/LD+JSON' data-x="1">
                    {"@type":"Product","sku":"42","weight":"0.2"}
                  </script>
                </head></html>
                """;

        var result = MagnitJsonLdPackageQuantityExtractor.extract(html, "42");

        assertThat(result.status()).isEqualTo(MagnitPackageQuantityStatus.FOUND);
        assertThat(result.packageQuantity())
                .contains(new Quantity(new BigDecimal("200"), QuantityUnit.GRAM));
    }

    @Test
    void validatesExpectedSkuBoundary() {
        for (var sku : new String[] {"", "   "}) {
            org.assertj.core.api.Assertions.assertThatThrownBy(
                            () -> MagnitJsonLdPackageQuantityExtractor.extract("", sku))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("expectedSku must not be blank");
        }
    }

    private static String product(String json) {
        return "<script type=\"application/ld+json\">" + json + "</script>";
    }

    private static String page(String... scripts) {
        return "<html><head>" + String.join("", scripts) + "</head><body><h1>fixture</h1></body></html>";
    }
}
