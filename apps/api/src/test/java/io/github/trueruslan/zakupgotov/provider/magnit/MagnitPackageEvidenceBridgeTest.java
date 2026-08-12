package io.github.trueruslan.zakupgotov.provider.magnit;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.trueruslan.zakupgotov.provider.AcquisitionMode;
import io.github.trueruslan.zakupgotov.provider.AvailabilityStatus;
import io.github.trueruslan.zakupgotov.provider.ObservedOffer;
import io.github.trueruslan.zakupgotov.provider.OfferSnapshot;
import io.github.trueruslan.zakupgotov.provider.OfferSnapshotId;
import io.github.trueruslan.zakupgotov.retailer.RetailerId;
import io.github.trueruslan.zakupgotov.shopping.Quantity;
import io.github.trueruslan.zakupgotov.shopping.QuantityUnit;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class MagnitPackageEvidenceBridgeTest {

    @Test
    void foundCharacteristicCanEnterTheAcceptedProviderSnapshotEvidencePath() {
        var extraction = MagnitPackageQuantityExtractor.extract("""
                <h1>Макароны Makfa 450г</h1>
                <h2>Характеристики</h2>
                <div>Вес, кг 0.45</div>
                <div>Артикул 3042670099</div>
                <h2>Условия хранения</h2>
                """);
        var observed = observedOffer(extraction);
        var snapshot = OfferSnapshot.observationOnly(
                new OfferSnapshotId(UUID.fromString("82828282-8282-8282-8282-828282828282")),
                observed);

        var expected = new Quantity(new BigDecimal("450"), QuantityUnit.GRAM);
        assertThat(observed.packageQuantity()).contains(expected);
        assertThat(snapshot.packageQuantity()).contains(expected);
    }

    @Test
    void ambiguousMagnitCharacteristicsRemainPackageUnknownDownstream() {
        var extraction = MagnitPackageQuantityExtractor.extract("""
                <h1>Молоко 1л</h1>
                <h2>Характеристики</h2>
                <div>Объем, л 1</div>
                <div>Вес, кг 1.028</div>
                <div>Артикул 1000548435</div>
                <h2>Условия хранения</h2>
                """);

        assertThat(extraction.status()).isEqualTo(MagnitPackageQuantityStatus.AMBIGUOUS_DIMENSIONS);
        assertThat(observedOffer(extraction).packageQuantity()).isEmpty();
    }

    private static ObservedOffer observedOffer(MagnitPackageQuantityExtraction extraction) {
        return new ObservedOffer(
                RetailerId.MAGNIT,
                "magnit-public-page",
                AcquisitionMode.PUBLIC_WEB,
                "shop-683800",
                "3042670099",
                "Макароны Makfa 450г",
                new BigDecimal("79.99"),
                "RUB",
                AvailabilityStatus.UNKNOWN,
                Instant.parse("2026-08-12T16:40:00Z"),
                "https://magnit.ru/product/3042670099-makarony_makfa_vitki_450g?shopCode=683800&shopType=1",
                extraction.packageQuantity());
    }
}
