package io.github.trueruslan.zakupgotov.comparison;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.trueruslan.zakupgotov.basket.BasketQuoteStatus;
import io.github.trueruslan.zakupgotov.basket.BasketTotal;
import io.github.trueruslan.zakupgotov.basket.PackageQuantityBinding;
import io.github.trueruslan.zakupgotov.basket.PackageQuantitySet;
import io.github.trueruslan.zakupgotov.basket.SingleStoreBasketPlanner;
import io.github.trueruslan.zakupgotov.matching.MatchScope;
import io.github.trueruslan.zakupgotov.provider.AcquisitionMode;
import io.github.trueruslan.zakupgotov.provider.AvailabilityStatus;
import io.github.trueruslan.zakupgotov.provider.ObservedOffer;
import io.github.trueruslan.zakupgotov.provider.OfferSnapshot;
import io.github.trueruslan.zakupgotov.provider.OfferSnapshotId;
import io.github.trueruslan.zakupgotov.provider.ProviderPathAttempt;
import io.github.trueruslan.zakupgotov.provider.ProviderPathAttemptStatus;
import io.github.trueruslan.zakupgotov.provider.ProviderPathSelection;
import io.github.trueruslan.zakupgotov.provider.ProviderSearchOutcome;
import io.github.trueruslan.zakupgotov.retailer.ProductionAccessStatus;
import io.github.trueruslan.zakupgotov.retailer.Retailer;
import io.github.trueruslan.zakupgotov.retailer.RetailerCoverageState;
import io.github.trueruslan.zakupgotov.retailer.RetailerId;
import io.github.trueruslan.zakupgotov.retailer.RetailerRegistryEntry;
import io.github.trueruslan.zakupgotov.shopping.Quantity;
import io.github.trueruslan.zakupgotov.shopping.QuantityUnit;
import io.github.trueruslan.zakupgotov.shopping.ShoppingItem;
import io.github.trueruslan.zakupgotov.shopping.ShoppingItemId;
import io.github.trueruslan.zakupgotov.shopping.ShoppingList;
import io.github.trueruslan.zakupgotov.shopping.ShoppingListId;
import io.github.trueruslan.zakupgotov.shopping.ShoppingRequirement;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class RetailerComparisonEvidenceMappingTest {

    private static final String PROVIDER = "fixture-provider";
    private static final String CONTEXT = "store-42";
    private final RetailerComparisonReadModelAssembler assembler = new RetailerComparisonReadModelAssembler();
    private final SingleStoreBasketPlanner planner = new SingleStoreBasketPlanner();

    @Test
    void mapsProviderPathFailureToSourceUnavailableWithoutLeakingAttemptDetails() {
        var outcome = new ProviderSearchOutcome(
                RetailerId.PYATEROCHKA,
                Optional.empty(),
                List.of(),
                List.of(new ProviderPathAttempt(
                        PROVIDER,
                        AcquisitionMode.DIRECT_API,
                        ProviderPathAttemptStatus.FAILED)));
        var evidence = new RetailerComparisonEvidence(outcome, Optional.empty());

        var view = view(evidence);

        assertThat(view.comparisonStatus()).isEqualTo(RetailerComparisonStatus.UNAVAILABLE);
        assertThat(view.reasons()).containsExactly(RetailerComparisonReason.SOURCE_UNAVAILABLE);
        assertThat(view.total()).isEmpty();
        assertThat(view.freshness()).isEmpty();
    }

    @Test
    void mapsCompleteQuoteToReadyTotalAndOldestProviderFreshness() {
        var milk = observed(
                "milk",
                "Молоко",
                "100.00",
                AvailabilityStatus.AVAILABLE,
                "2026-08-12T11:00:00Z");
        var bread = observed(
                "bread",
                "Хлеб",
                "70.00",
                AvailabilityStatus.AVAILABLE,
                "2026-08-12T11:10:00Z");
        var milkSnapshot = snapshotWithProviderTime(milk, "2026-08-12T10:50:00Z");
        var breadSnapshot = snapshotWithProviderTime(bread, "2026-08-12T11:05:00Z");
        var quote = quote(
                List.of(item("milk-item", "Молоко"), item("bread-item", "Хлеб")),
                List.of(milkSnapshot, breadSnapshot),
                packages(milkSnapshot, breadSnapshot));
        assertThat(quote.status()).isEqualTo(BasketQuoteStatus.COMPLETE);

        var view = view(successEvidence(List.of(milk, bread), quote));

        assertThat(view.comparisonStatus()).isEqualTo(RetailerComparisonStatus.READY);
        assertThat(view.reasons()).isEmpty();
        assertThat(view.total()).contains(new BasketTotal(new BigDecimal("170.00"), "RUB"));
        assertThat(view.freshness()).contains(new RetailerFreshness(
                RetailerFreshnessBasis.PROVIDER_TIMESTAMP,
                Instant.parse("2026-08-12T11:00:00Z"),
                Optional.of(Instant.parse("2026-08-12T10:50:00Z"))));
    }

    @Test
    void fallsBackToObservationOnlyWhenAnySelectedLineLacksProviderTimestamp() {
        var milk = observed(
                "milk-observation-only",
                "Молоко",
                "100.00",
                AvailabilityStatus.AVAILABLE,
                "2026-08-12T11:00:00Z");
        var bread = observed(
                "bread-provider-time",
                "Хлеб",
                "70.00",
                AvailabilityStatus.AVAILABLE,
                "2026-08-12T11:10:00Z");
        var milkSnapshot = OfferSnapshot.observationOnly(snapshotId(milk.sku()), milk);
        var breadSnapshot = snapshotWithProviderTime(bread, "2026-08-12T11:05:00Z");
        var quote = quote(
                List.of(item("milk-item-observation", "Молоко"), item("bread-item-provider", "Хлеб")),
                List.of(milkSnapshot, breadSnapshot),
                packages(milkSnapshot, breadSnapshot));

        var view = view(successEvidence(List.of(milk, bread), quote));

        assertThat(view.freshness()).contains(new RetailerFreshness(
                RetailerFreshnessBasis.OBSERVATION_ONLY,
                Instant.parse("2026-08-12T11:00:00Z"),
                Optional.empty()));
    }

    @Test
    void mapsUnknownAvailabilityToUncertainWithoutPromotingItToReady() {
        var milk = observed(
                "milk-unknown",
                "Молоко",
                "99.90",
                AvailabilityStatus.UNKNOWN,
                "2026-08-12T11:20:00Z");
        var snapshot = OfferSnapshot.observationOnly(snapshotId(milk.sku()), milk);
        var quote = quote(
                List.of(item("milk-unknown-item", "Молоко")),
                List.of(snapshot),
                packages(snapshot));
        assertThat(quote.status()).isEqualTo(BasketQuoteStatus.UNCERTAIN);

        var view = view(successEvidence(List.of(milk), quote));

        assertThat(view.comparisonStatus()).isEqualTo(RetailerComparisonStatus.UNCERTAIN);
        assertThat(view.reasons()).containsExactly(RetailerComparisonReason.AVAILABILITY_UNKNOWN);
        assertThat(view.total()).contains(new BasketTotal(new BigDecimal("99.90"), "RUB"));
        assertThat(view.freshness()).contains(new RetailerFreshness(
                RetailerFreshnessBasis.OBSERVATION_ONLY,
                Instant.parse("2026-08-12T11:20:00Z"),
                Optional.empty()));
    }

    @Test
    void mapsIncompleteItemsToStableDeduplicatedReasonsWithoutTotalOrFreshness() {
        var milkA = observed("milk-a", "Молоко", "90.00", AvailabilityStatus.AVAILABLE, "2026-08-12T11:30:00Z");
        var milkB = observed("milk-b", "Молоко", "80.00", AvailabilityStatus.AVAILABLE, "2026-08-12T11:30:00Z");
        var bread = observed("bread-package-unknown", "Хлеб", "70.00", AvailabilityStatus.AVAILABLE, "2026-08-12T11:30:00Z");
        var milkASnapshot = OfferSnapshot.observationOnly(snapshotId(milkA.sku()), milkA);
        var milkBSnapshot = OfferSnapshot.observationOnly(snapshotId(milkB.sku()), milkB);
        var breadSnapshot = OfferSnapshot.observationOnly(snapshotId(bread.sku()), bread);
        var list = List.of(
                item("sugar-1", "Сахар"),
                item("milk-ambiguous", "Молоко"),
                item("bread-package", "Хлеб"),
                item("sugar-2", "Сахар другой"));
        var quote = quote(
                list,
                List.of(milkASnapshot, milkBSnapshot, breadSnapshot),
                PackageQuantitySet.of(List.of(
                        packageBinding(milkASnapshot),
                        packageBinding(milkBSnapshot))));
        assertThat(quote.status()).isEqualTo(BasketQuoteStatus.INCOMPLETE);

        var view = view(successEvidence(List.of(milkA, milkB, bread), quote));

        assertThat(view.comparisonStatus()).isEqualTo(RetailerComparisonStatus.INCOMPLETE);
        assertThat(view.reasons()).containsExactly(
                RetailerComparisonReason.ITEM_UNMATCHED,
                RetailerComparisonReason.ITEM_AMBIGUOUS,
                RetailerComparisonReason.PACKAGE_QUANTITY_UNKNOWN);
        assertThat(view.total()).isEmpty();
        assertThat(view.freshness()).isEmpty();
    }

    @Test
    void rejectsCrossRetailerAndStructurallyImpossibleRuntimeEvidence() {
        var foreignOutcome = new ProviderSearchOutcome(
                RetailerId.MAGNIT,
                Optional.empty(),
                List.of(),
                List.of());

        assertThatThrownBy(() -> assembler.assembleEntries(
                        List.of(readyEntry()),
                        Map.of(RetailerId.PYATEROCHKA, new RetailerComparisonEvidence(foreignOutcome, Optional.empty()))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("retailer");

        var successfulOutcome = new ProviderSearchOutcome(
                RetailerId.PYATEROCHKA,
                Optional.of(new ProviderPathSelection(PROVIDER, AcquisitionMode.DIRECT_API)),
                List.of(),
                List.of(new ProviderPathAttempt(
                        PROVIDER,
                        AcquisitionMode.DIRECT_API,
                        ProviderPathAttemptStatus.SUCCESS)));

        assertThatThrownBy(() -> assembler.assembleEntries(
                        List.of(readyEntry()),
                        Map.of(RetailerId.PYATEROCHKA, new RetailerComparisonEvidence(successfulOutcome, Optional.empty()))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("basket");
    }

    private RetailerComparisonView view(RetailerComparisonEvidence evidence) {
        return assembler.assembleEntries(
                        List.of(readyEntry()),
                        Map.of(RetailerId.PYATEROCHKA, evidence))
                .require(RetailerId.PYATEROCHKA);
    }

    private static RetailerRegistryEntry readyEntry() {
        return new RetailerRegistryEntry(
                new Retailer(RetailerId.PYATEROCHKA),
                RetailerCoverageState.AVAILABLE_DIRECT,
                ProductionAccessStatus.ACCEPTABLE);
    }

    private static RetailerComparisonEvidence successEvidence(
            List<ObservedOffer> offers,
            io.github.trueruslan.zakupgotov.basket.SingleStoreBasketQuote quote) {
        return new RetailerComparisonEvidence(
                new ProviderSearchOutcome(
                        RetailerId.PYATEROCHKA,
                        Optional.of(new ProviderPathSelection(PROVIDER, AcquisitionMode.DIRECT_API)),
                        offers,
                        List.of(new ProviderPathAttempt(
                                PROVIDER,
                                AcquisitionMode.DIRECT_API,
                                ProviderPathAttemptStatus.SUCCESS))),
                Optional.of(quote));
    }

    private io.github.trueruslan.zakupgotov.basket.SingleStoreBasketQuote quote(
            List<ShoppingItem> items,
            List<OfferSnapshot> snapshots,
            PackageQuantitySet packages) {
        var list = new ShoppingList(new ShoppingListId(UUID.nameUUIDFromBytes(
                items.stream().map(item -> item.id().value().toString()).reduce("", String::concat)
                        .getBytes(StandardCharsets.UTF_8))));
        items.forEach(list::add);
        return planner.quote(
                new MatchScope(RetailerId.PYATEROCHKA, CONTEXT),
                list,
                snapshots,
                packages);
    }

    private static ShoppingItem item(String seed, String requirement) {
        return new ShoppingItem(
                new ShoppingItemId(UUID.nameUUIDFromBytes(seed.getBytes(StandardCharsets.UTF_8))),
                new ShoppingRequirement(requirement),
                new Quantity(BigDecimal.ONE, QuantityUnit.PIECE));
    }

    private static ObservedOffer observed(
            String sku,
            String productName,
            String price,
            AvailabilityStatus availability,
            String observedAt) {
        return new ObservedOffer(
                RetailerId.PYATEROCHKA,
                PROVIDER,
                AcquisitionMode.DIRECT_API,
                CONTEXT,
                sku,
                productName,
                new BigDecimal(price),
                "RUB",
                availability,
                Instant.parse(observedAt),
                "fixture://products/" + sku);
    }

    private static OfferSnapshot snapshotWithProviderTime(ObservedOffer offer, String providerUpdatedAt) {
        return OfferSnapshot.withProviderUpdatedAt(
                snapshotId(offer.sku()),
                offer,
                Instant.parse(providerUpdatedAt));
    }

    private static OfferSnapshotId snapshotId(String sku) {
        return new OfferSnapshotId(UUID.nameUUIDFromBytes(sku.getBytes(StandardCharsets.UTF_8)));
    }

    private static PackageQuantitySet packages(OfferSnapshot... snapshots) {
        var bindings = new ArrayList<PackageQuantityBinding>();
        for (var snapshot : snapshots) {
            bindings.add(packageBinding(snapshot));
        }
        return PackageQuantitySet.of(bindings);
    }

    private static PackageQuantityBinding packageBinding(OfferSnapshot snapshot) {
        return new PackageQuantityBinding(
                snapshot.id(),
                new Quantity(BigDecimal.ONE, QuantityUnit.PIECE));
    }
}
