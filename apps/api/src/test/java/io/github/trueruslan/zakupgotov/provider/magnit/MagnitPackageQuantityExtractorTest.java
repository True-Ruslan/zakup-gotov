package io.github.trueruslan.zakupgotov.provider.magnit;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.trueruslan.zakupgotov.shopping.Quantity;
import io.github.trueruslan.zakupgotov.shopping.QuantityUnit;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class MagnitPackageQuantityExtractorTest {

    @Test
    void extractsCanonicalWeightFromExactCharacteristicsField() {
        var result = MagnitPackageQuantityExtractor.extract(page(
                "Макароны Makfa Виток 450г",
                "<div>Бренд Makfa</div><div>Вес, кг 0,45</div><div>Артикул 3042670099</div>"));

        assertThat(result.status()).isEqualTo(MagnitPackageQuantityStatus.FOUND);
        assertThat(result.packageQuantity())
                .contains(new Quantity(new BigDecimal("450"), QuantityUnit.GRAM));
    }

    @Test
    void extractsCanonicalVolumeFromExactCharacteristicsField() {
        var result = MagnitPackageQuantityExtractor.extract(page(
                "Вода питьевая 1.5л",
                "<div>Производитель Тандер</div><div>Объем, л 1.5</div><div>Артикул 1000279107</div>"));

        assertThat(result.status()).isEqualTo(MagnitPackageQuantityStatus.FOUND);
        assertThat(result.packageQuantity())
                .contains(new Quantity(new BigDecimal("1500"), QuantityUnit.MILLILITER));
    }

    @Test
    void deduplicatesRepeatedEquivalentCharacteristicValues() {
        var result = MagnitPackageQuantityExtractor.extract(page(
                "Макароны 450г",
                "<div>Вес, кг 0.45</div><span>Вес, кг: 0,45</span><div>Артикул 1</div>"));

        assertThat(result.status()).isEqualTo(MagnitPackageQuantityStatus.FOUND);
        assertThat(result.packageQuantity())
                .contains(new Quantity(new BigDecimal("450"), QuantityUnit.GRAM));
    }

    @Test
    void failsClosedWhenWeightAndVolumeAreBothPublished() {
        var result = MagnitPackageQuantityExtractor.extract(page(
                "Молоко 1л",
                "<div>Объем, л 1</div><div>Вес, кг 1.028</div><div>Артикул 1000548435</div>"));

        assertThat(result.status()).isEqualTo(MagnitPackageQuantityStatus.AMBIGUOUS_DIMENSIONS);
        assertThat(result.packageQuantity()).isEmpty();
    }

    @Test
    void failsClosedWhenOneDimensionHasConflictingValues() {
        var result = MagnitPackageQuantityExtractor.extract(page(
                "Макароны",
                "<div>Вес, кг 0.45</div><div>Вес, кг 0.50</div><div>Артикул 1</div>"));

        assertThat(result.status()).isEqualTo(MagnitPackageQuantityStatus.CONFLICTING_VALUES);
        assertThat(result.packageQuantity()).isEmpty();
    }

    @Test
    void failsClosedWhenSupportedCharacteristicValueIsInvalid() {
        for (var value : new String[] {"0", "-1", "неизвестно"}) {
            var result = MagnitPackageQuantityExtractor.extract(page(
                    "Товар",
                    "<div>Вес, кг " + value + "</div><div>Артикул 1</div>"));

            assertThat(result.status()).as(value).isEqualTo(MagnitPackageQuantityStatus.INVALID_VALUE);
            assertThat(result.packageQuantity()).as(value).isEmpty();
        }
    }

    @Test
    void ignoresPackageLookingTitleAndTextOutsideCharacteristics() {
        var result = MagnitPackageQuantityExtractor.extract("""
                <html><body>
                  <h1>Макароны Makfa 450г</h1>
                  <div>Объем, л: 9</div>
                  <h2>Описание</h2><p>Упаковка 0,45 кг.</p>
                  <h2>Характеристики</h2>
                  <div>Бренд Makfa</div><div>Артикул 3042670099</div>
                  <h2>Условия хранения</h2>
                </body></html>
                """);

        assertThat(result.status()).isEqualTo(MagnitPackageQuantityStatus.MISSING);
        assertThat(result.packageQuantity()).isEmpty();
    }

    @Test
    void countOnlySelectorIsNotAcceptedByVersionOne() {
        var result = MagnitPackageQuantityExtractor.extract("""
                <html><body>
                  <h1>Яйца 10шт</h1>
                  <div>Количество в упаковке: 10</div>
                  <h2>Характеристики</h2>
                  <div>Бренд Лето</div><div>Артикул 1000246228</div>
                  <h2>Условия хранения</h2>
                </body></html>
                """);

        assertThat(result.status()).isEqualTo(MagnitPackageQuantityStatus.MISSING);
        assertThat(result.packageQuantity()).isEmpty();
    }

    @Test
    void scriptOrStyleTextCannotCreateCharacteristicEvidence() {
        var result = MagnitPackageQuantityExtractor.extract("""
                <html><body>
                  <script>document.write('Характеристики Вес, кг 0.45');</script>
                  <style>.x::after { content: 'Характеристики Объем, л 1'; }</style>
                  <h1>Макароны 450г</h1>
                </body></html>
                """);

        assertThat(result.status()).isEqualTo(MagnitPackageQuantityStatus.MISSING);
        assertThat(result.packageQuantity()).isEmpty();
    }

    private static String page(String title, String characteristics) {
        return """
                <html><body>
                  <h1>%s</h1>
                  <div>Объем, л: 99</div>
                  <h2>Характеристики</h2>
                  %s
                  <h2>Условия хранения</h2>
                  <div>Срок годности, дней 730</div>
                </body></html>
                """.formatted(title, characteristics);
    }
}
