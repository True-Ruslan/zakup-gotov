package io.github.trueruslan.zakupgotov.basket;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.trueruslan.zakupgotov.provider.OfferSnapshotId;
import io.github.trueruslan.zakupgotov.shopping.Quantity;
import io.github.trueruslan.zakupgotov.shopping.QuantityUnit;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class PackageQuantitySetTest {

    private static final OfferSnapshotId MILK = new OfferSnapshotId(
            UUID.fromString("11111111-1111-1111-1111-111111111111"));
    private static final OfferSnapshotId WATER = new OfferSnapshotId(
            UUID.fromString("22222222-2222-2222-2222-222222222222"));
    private static final OfferSnapshotId UNKNOWN = new OfferSnapshotId(
            UUID.fromString("33333333-3333-3333-3333-333333333333"));

    @Test
    void preservesKnownPackageEvidenceInStableOrderAndCanonicalUnits() {
        var milk = new PackageQuantityBinding(
                MILK,
                new Quantity(new BigDecimal("0.5"), QuantityUnit.KILOGRAM));
        var water = new PackageQuantityBinding(
                WATER,
                new Quantity(new BigDecimal("1.5"), QuantityUnit.LITER));

        var set = PackageQuantitySet.of(List.of(milk, water));

        assertThat(set.bindings()).containsExactly(milk, water);
        assertThat(set.quantityFor(MILK))
                .contains(new Quantity(new BigDecimal("500"), QuantityUnit.GRAM));
        assertThat(set.quantityFor(WATER))
                .contains(new Quantity(new BigDecimal("1500"), QuantityUnit.MILLILITER));
        assertThat(set.quantityFor(UNKNOWN)).isEmpty();
    }

    @Test
    void rejectsDuplicateSnapshotEvidence() {
        var first = new PackageQuantityBinding(
                MILK,
                new Quantity(new BigDecimal("500"), QuantityUnit.GRAM));
        var second = new PackageQuantityBinding(
                MILK,
                new Quantity(new BigDecimal("900"), QuantityUnit.MILLILITER));

        assertThatThrownBy(() -> PackageQuantitySet.of(List.of(first, second)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("duplicate")
                .hasMessageContaining(MILK.value().toString());
    }

    @Test
    void exposesImmutableBindingSnapshot() {
        var set = PackageQuantitySet.of(List.of(new PackageQuantityBinding(
                MILK,
                new Quantity(new BigDecimal("500"), QuantityUnit.GRAM))));

        assertThatThrownBy(() -> set.bindings().clear())
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void rejectsMissingBindingPartsAndNullLookups() {
        assertThatThrownBy(() -> new PackageQuantityBinding(
                        null,
                        new Quantity(new BigDecimal("500"), QuantityUnit.GRAM)))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("snapshotId");
        assertThatThrownBy(() -> new PackageQuantityBinding(MILK, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("packageQuantity");
        assertThatThrownBy(() -> PackageQuantitySet.of(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("bindings");
        assertThatThrownBy(() -> PackageQuantitySet.of(java.util.Arrays.asList((PackageQuantityBinding) null)))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("binding");
        assertThatThrownBy(() -> PackageQuantitySet.of(List.of()).quantityFor(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("snapshotId");
    }
}
