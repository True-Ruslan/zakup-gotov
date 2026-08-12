package io.github.trueruslan.zakupgotov.provider.magnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.trueruslan.zakupgotov.shopping.Quantity;
import io.github.trueruslan.zakupgotov.shopping.QuantityUnit;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class MagnitCorpusPackageEvidenceTest {

    @Test
    void identityValidPageCarriesAcceptedPackageExtraction() {
        var observation = MagnitCorpusProbe.parseProductPage("""
                <html><body>
                  <h1>Макароны Makfa 450г 79,99 ₽ Артикул 3042670099</h1>
                  <h2>Характеристики</h2>
                  <div>Вес, кг 0.45</div>
                  <div>Артикул 3042670099</div>
                  <h2>Условия хранения</h2>
                </body></html>
                """, "3042670099");

        assertThat(observation.skuEvidence()).isTrue();
        assertThat(observation.packageExtraction().status()).isEqualTo(MagnitPackageQuantityStatus.FOUND);
        assertThat(observation.packageExtraction().packageQuantity())
                .contains(new Quantity(new BigDecimal("450"), QuantityUnit.GRAM));
    }

    @Test
    void missingExpectedSkuCarriesNoAttributablePackageEvidence() {
        var observation = MagnitCorpusProbe.parseProductPage("""
                <html><body>
                  <h1>Другой товар 79,99 ₽ Артикул 1111111111</h1>
                  <h2>Характеристики</h2><div>Вес, кг 0.45</div>
                </body></html>
                """, "3042670099");

        assertThat(observation.skuEvidence()).isFalse();
        assertThat(observation.packageExtraction().status()).isEqualTo(MagnitPackageQuantityStatus.MISSING);
        assertThat(observation.packageExtraction().packageQuantity()).isEmpty();
    }

    @Test
    void summarizesEveryExtractionStatusWithoutInventingAQualityThreshold() {
        var summary = MagnitCorpusProbe.PackageEvidenceSummary.summarize(List.of(
                found("0.45", QuantityUnit.KILOGRAM),
                MagnitPackageQuantityExtraction.empty(MagnitPackageQuantityStatus.MISSING),
                MagnitPackageQuantityExtraction.empty(MagnitPackageQuantityStatus.AMBIGUOUS_DIMENSIONS),
                MagnitPackageQuantityExtraction.empty(MagnitPackageQuantityStatus.CONFLICTING_VALUES),
                MagnitPackageQuantityExtraction.empty(MagnitPackageQuantityStatus.INVALID_VALUE),
                found("1.5", QuantityUnit.LITER)));

        assertThat(summary.packageEvidencePages()).isEqualTo(6);
        assertThat(summary.found()).isEqualTo(2);
        assertThat(summary.missing()).isEqualTo(1);
        assertThat(summary.ambiguousDimensions()).isEqualTo(1);
        assertThat(summary.conflictingValues()).isEqualTo(1);
        assertThat(summary.invalidValues()).isEqualTo(1);
        assertThat(summary.classifiedPages()).isEqualTo(summary.packageEvidencePages());
    }

    @Test
    void summaryRejectsCountsThatDoNotEqualEligiblePageCount() {
        assertThatThrownBy(() -> new MagnitCorpusProbe.PackageEvidenceSummary(6, 2, 1, 1, 0, 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("package evidence counts")
                .hasMessageContaining("eligible pages");
    }

    private static MagnitPackageQuantityExtraction found(String amount, QuantityUnit unit) {
        return MagnitPackageQuantityExtraction.found(new Quantity(new BigDecimal(amount), unit));
    }
}
