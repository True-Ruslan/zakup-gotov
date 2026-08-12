package io.github.trueruslan.zakupgotov.preview;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.trueruslan.zakupgotov.basket.PackageQuantitySet;
import io.github.trueruslan.zakupgotov.location.ProductLocation;
import io.github.trueruslan.zakupgotov.location.ProductLocationId;
import io.github.trueruslan.zakupgotov.provider.AcquisitionMode;
import io.github.trueruslan.zakupgotov.provider.AvailabilityStatus;
import io.github.trueruslan.zakupgotov.provider.ObservedOffer;
import io.github.trueruslan.zakupgotov.provider.OfferSnapshot;
import io.github.trueruslan.zakupgotov.provider.OfferSnapshotId;
import io.github.trueruslan.zakupgotov.provider.ProviderPathSelection;
import io.github.trueruslan.zakupgotov.provider.ProviderSearchOutcome;
import io.github.trueruslan.zakupgotov.retailer.RetailerId;
import io.github.trueruslan.zakupgotov.shopping.ShoppingList;
import io.github.trueruslan.zakupgotov.shopping.ShoppingListId;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ComparisonRuntimeEvidenceBoundaryTest {

    @Test
    void productionNoopSourceAlwaysReturnsEmptyEvidence() {
        var source = new NoopComparisonRuntimeEvidenceSource();

        var evidence = source.load(
                new ShoppingList(new ShoppingListId(UUID.randomUUID())),
                ProductLocation.localityOnly(new ProductLocationId(UUID.randomUUID()), "Москва"));

        assertThat(evidence.retailers()).isEmpty();
        assertThat(evidence.forRetailer(RetailerId.PYATEROCHKA)).isEmpty();
        assertThat(NoopComparisonRuntimeEvidenceSource.class.getDeclaredFields()).isEmpty();
    }

    @Test
    void preservesRetailerEvidenceOrderAndLookup() {
        var pyaterochka = unavailableEvidence(RetailerId.PYATEROCHKA);
        var magnit = unavailableEvidence(RetailerId.MAGNIT);

        var evidence = ComparisonRuntimeEvidence.of(List.of(pyaterochka, magnit));

        assertThat(evidence.retailers()).containsExactly(pyaterochka, magnit);
        assertThat(evidence.forRetailer(RetailerId.MAGNIT)).contains(magnit);
        assertThat(evidence.forRetailer(RetailerId.SAMOKAT)).isEmpty();
    }

    @Test
    void rejectsDuplicateRetailerEvidence() {
        var first = unavailableEvidence(RetailerId.PYATEROCHKA);
        var second = unavailableEvidence(RetailerId.PYATEROCHKA);

        assertThatThrownBy(() -> ComparisonRuntimeEvidence.of(List.of(first, second)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("duplicate")
                .hasMessageContaining("PYATEROCHKA");
    }

    @Test
    void rejectsProviderOutcomeForAnotherRetailer() {
        var perekrestokOutcome = unavailableOutcome(RetailerId.PEREKRESTOK);

        assertThatThrownBy(() -> new RetailerRuntimeEvidence(
                        RetailerId.PYATEROCHKA,
                        perekrestokOutcome,
                        List.of(),
                        PackageQuantitySet.of(List.of())))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("provider outcome retailer");
    }

    @Test
    void rejectsSnapshotForAnotherRetailer() {
        var outcome = new ProviderSearchOutcome(
                RetailerId.PYATEROCHKA,
                Optional.of(new ProviderPathSelection("fixture-pyaterochka", AcquisitionMode.DIRECT_API)),
                List.of(),
                List.of());
        var perekrestokSnapshot = snapshot(RetailerId.PEREKRESTOK);

        assertThatThrownBy(() -> new RetailerRuntimeEvidence(
                        RetailerId.PYATEROCHKA,
                        outcome,
                        List.of(perekrestokSnapshot),
                        PackageQuantitySet.of(List.of())))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("snapshot retailer");
    }

    @Test
    void unavailableProviderOutcomeCannotCarrySnapshots() {
        assertThatThrownBy(() -> new RetailerRuntimeEvidence(
                        RetailerId.PYATEROCHKA,
                        unavailableOutcome(RetailerId.PYATEROCHKA),
                        List.of(snapshot(RetailerId.PYATEROCHKA)),
                        PackageQuantitySet.of(List.of())))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("unavailable provider outcome");
    }

    private static RetailerRuntimeEvidence unavailableEvidence(RetailerId retailerId) {
        return new RetailerRuntimeEvidence(
                retailerId,
                unavailableOutcome(retailerId),
                List.of(),
                PackageQuantitySet.of(List.of()));
    }

    private static ProviderSearchOutcome unavailableOutcome(RetailerId retailerId) {
        return new ProviderSearchOutcome(retailerId, Optional.empty(), List.of(), List.of());
    }

    private static OfferSnapshot snapshot(RetailerId retailerId) {
        var observedAt = Instant.parse("2026-08-12T10:00:00Z");
        var observation = new ObservedOffer(
                retailerId,
                "fixture-source",
                AcquisitionMode.DIRECT_API,
                "fixture-context",
                "sku-1",
                "Молоко",
                new BigDecimal("100.00"),
                "RUB",
                AvailabilityStatus.AVAILABLE,
                observedAt,
                "fixture://offer/1");
        return OfferSnapshot.observationOnly(new OfferSnapshotId(UUID.randomUUID()), observation);
    }
}
