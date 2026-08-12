package io.github.trueruslan.zakupgotov.basket;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.trueruslan.zakupgotov.provider.AcquisitionMode;
import io.github.trueruslan.zakupgotov.provider.AvailabilityStatus;
import io.github.trueruslan.zakupgotov.provider.ObservedOffer;
import io.github.trueruslan.zakupgotov.provider.OfferSnapshot;
import io.github.trueruslan.zakupgotov.provider.OfferSnapshotId;
import io.github.trueruslan.zakupgotov.retailer.RetailerId;
import io.github.trueruslan.zakupgotov.shopping.Quantity;
import io.github.trueruslan.zakupgotov.shopping.QuantityUnit;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class PackageSelectionCalculatorTest {

    @Test
    void selectsOnePackageForExactRequiredQuantity() {
        var selection = PackageSelectionCalculator.calculate(
                snapshot("sku-500", "99.90"),
                quantity("500", QuantityUnit.GRAM),
                quantity("500", QuantityUnit.GRAM));

        assertThat(selection.packageCount()).isEqualTo(BigInteger.ONE);
        assertThat(selection.providedQuantity()).isEqualTo(quantity("500", QuantityUnit.GRAM));
        assertThat(selection.lineTotal()).isEqualByComparingTo("99.90");
    }

    @Test
    void roundsUpWholePackagesAndCalculatesOversupplyAndLineTotal() {
        var selection = PackageSelectionCalculator.calculate(
                snapshot("sku-500", "89.90"),
                quantity("750", QuantityUnit.GRAM),
                quantity("500", QuantityUnit.GRAM));

        assertThat(selection.packageCount()).isEqualTo(BigInteger.valueOf(2));
        assertThat(selection.providedQuantity()).isEqualTo(quantity("1000", QuantityUnit.GRAM));
        assertThat(selection.lineTotal()).isEqualByComparingTo("179.80");
    }

    @Test
    void usesCanonicalQuantityUnitsForKilogramsAndPieces() {
        var mass = PackageSelectionCalculator.calculate(
                snapshot("sku-400", "75.00"),
                quantity("1", QuantityUnit.KILOGRAM),
                quantity("400", QuantityUnit.GRAM));
        var pieces = PackageSelectionCalculator.calculate(
                snapshot("sku-eggs", "110.00"),
                quantity("7", QuantityUnit.PIECE),
                quantity("6", QuantityUnit.PIECE));

        assertThat(mass.packageCount()).isEqualTo(BigInteger.valueOf(3));
        assertThat(mass.providedQuantity()).isEqualTo(quantity("1200", QuantityUnit.GRAM));
        assertThat(mass.lineTotal()).isEqualByComparingTo("225.00");
        assertThat(pieces.packageCount()).isEqualTo(BigInteger.valueOf(2));
        assertThat(pieces.providedQuantity()).isEqualTo(quantity("12", QuantityUnit.PIECE));
    }

    @Test
    void rejectsIncompatibleUnitsAndMissingInputs() {
        var snapshot = snapshot("sku-1", "99.90");

        assertThatThrownBy(() -> PackageSelectionCalculator.calculate(
                        snapshot,
                        quantity("1", QuantityUnit.PIECE),
                        quantity("500", QuantityUnit.GRAM)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("unit");
        assertThatThrownBy(() -> PackageSelectionCalculator.calculate(null,
                        quantity("1", QuantityUnit.PIECE), quantity("1", QuantityUnit.PIECE)))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("snapshot");
        assertThatThrownBy(() -> PackageSelectionCalculator.calculate(snapshot, null,
                        quantity("1", QuantityUnit.PIECE)))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("requiredQuantity");
        assertThatThrownBy(() -> PackageSelectionCalculator.calculate(snapshot,
                        quantity("1", QuantityUnit.PIECE), null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("packageQuantity");
    }

    private static Quantity quantity(String amount, QuantityUnit unit) {
        return new Quantity(new BigDecimal(amount), unit);
    }

    private static OfferSnapshot snapshot(String sku, String price) {
        var observed = new ObservedOffer(
                RetailerId.PYATEROCHKA,
                "fixture-provider",
                AcquisitionMode.DIRECT_API,
                "store-42",
                sku,
                "Молоко " + sku,
                new BigDecimal(price),
                "RUB",
                AvailabilityStatus.AVAILABLE,
                Instant.parse("2026-08-12T09:20:00Z"),
                "fixture://products/" + sku);
        return OfferSnapshot.observationOnly(
                new OfferSnapshotId(UUID.nameUUIDFromBytes(sku.getBytes(StandardCharsets.UTF_8))),
                observed);
    }
}
