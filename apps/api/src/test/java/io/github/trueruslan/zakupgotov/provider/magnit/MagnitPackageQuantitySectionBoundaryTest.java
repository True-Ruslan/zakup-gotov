package io.github.trueruslan.zakupgotov.provider.magnit;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class MagnitPackageQuantitySectionBoundaryTest {

    @Test
    void ignoresSupportedLabelsAfterStorageSectionStarts() {
        var result = MagnitPackageQuantityExtractor.extract("""
                <h1>Товар 450г</h1>
                <h2>Характеристики</h2>
                <div>Бренд Тест</div>
                <h2>Условия хранения</h2>
                <div>Вес, кг 0.45</div>
                """);

        assertThat(result.status()).isEqualTo(MagnitPackageQuantityStatus.MISSING);
        assertThat(result.packageQuantity()).isEmpty();
    }

    @Test
    void usesEarliestFollowingKnownSectionMarker() {
        var result = MagnitPackageQuantityExtractor.extract("""
                <h2>Характеристики</h2>
                <div>Бренд Тест</div>
                <h2>Документы</h2>
                <div>Объем, л 1.5</div>
                <h2>Условия хранения</h2>
                """);

        assertThat(result.status()).isEqualTo(MagnitPackageQuantityStatus.MISSING);
        assertThat(result.packageQuantity()).isEmpty();
    }

    @Test
    void ignoresDeferredCountEvenWhenItAppearsInsideCharacteristics() {
        var result = MagnitPackageQuantityExtractor.extract("""
                <h2>Характеристики</h2>
                <div>Количество в упаковке 10</div>
                <div>Артикул 1000246228</div>
                <h2>Условия хранения</h2>
                """);

        assertThat(result.status()).isEqualTo(MagnitPackageQuantityStatus.MISSING);
        assertThat(result.packageQuantity()).isEmpty();
    }
}
