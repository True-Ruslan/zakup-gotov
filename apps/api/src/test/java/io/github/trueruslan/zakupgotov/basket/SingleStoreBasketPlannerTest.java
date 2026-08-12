package io.github.trueruslan.zakupgotov.basket;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.trueruslan.zakupgotov.matching.MatchScope;
import io.github.trueruslan.zakupgotov.provider.AcquisitionMode;
import io.github.trueruslan.zakupgotov.provider.AvailabilityStatus;
import io.github.trueruslan.zakupgotov.provider.ObservedOffer;
import io.github.trueruslan.zakupgotov.provider.OfferSnapshot;
import io.github.trueruslan.zakupgotov.provider.OfferSnapshotId;
import io.github.trueruslan.zakupgotov.retailer.RetailerId;
import io.github.trueruslan.zakupgotov.shopping.Quantity;
import io.github.trueruslan.zakupgotov.shopping.QuantityUnit;
import io.github.trueruslan.zakupgotov.shopping.ShoppingItem;
import io.github.trueruslan.zakupgotov.shopping.ShoppingItemId;
import io.github.trueruslan.zakupgotov.shopping.ShoppingList;
import io.github.trueruslan.zakupgotov.shopping.ShoppingListId;
import io.github.trueruslan.zakupgotov.shopping.ShoppingRequirement;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class SingleStoreBasketPlannerTest {

    private static final MatchScope SCOPE = new MatchScope(RetailerId.PYATEROCHKA, "store-42");
    private static final Instant OBSERVED_AT = Instant.parse("2026-08-12T09:30:00Z");
    private final SingleStoreBasketPlanner planner = new SingleStoreBasketPlanner();

    @Test
    void buildsCompleteBasketWithWholePackageSelectionStableOrderAndTotal() {
        var milk = item("milk", "Молоко 3,2%", "750", QuantityUnit.GRAM);
        var eggs = item("eggs", "Яйца C1", "7", QuantityUnit.PIECE);
        var list = list(milk, eggs);
        var milkOffer = snapshot("milk-500", "Молоко 3,2%", "89.90", "RUB", AvailabilityStatus.AVAILABLE);
        var eggsOffer = snapshot("eggs-6", "Яйца C1", "110.00", "RUB", AvailabilityStatus.AVAILABLE);
        var packages = packages(
                binding(milkOffer, "500", QuantityUnit.GRAM),
                binding(eggsOffer, "6", QuantityUnit.PIECE));

        var quote = planner.quote(SCOPE, list, List.of(milkOffer, eggsOffer), packages);

        assertThat(quote.scope()).isEqualTo(SCOPE);
        assertThat(quote.shoppingListId()).isEqualTo(list.id());
        assertThat(quote.status()).isEqualTo(BasketQuoteStatus.COMPLETE);
        assertThat(quote.items()).extracting(resolution -> resolution.item().id())
                .containsExactly(milk.id(), eggs.id());
        assertThat(quote.items()).extracting(BasketItemResolution::status)
                .containsExactly(BasketItemResolutionStatus.FULFILLED, BasketItemResolutionStatus.FULFILLED);

        var milkSelection = quote.items().get(0).selection().orElseThrow();
        var eggsSelection = quote.items().get(1).selection().orElseThrow();
        assertThat(milkSelection.packageCount()).isEqualTo(BigInteger.valueOf(2));
        assertThat(milkSelection.providedQuantity()).isEqualTo(quantity("1000", QuantityUnit.GRAM));
        assertThat(eggsSelection.packageCount()).isEqualTo(BigInteger.valueOf(2));
        assertThat(eggsSelection.providedQuantity()).isEqualTo(quantity("12", QuantityUnit.PIECE));

        assertThat(quote.total()).contains(new BasketTotal(new BigDecimal("399.80"), "RUB"));
        assertThatThrownBy(() -> quote.items().clear())
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void keepsUnknownAvailabilityAsUncertainButStillCalculatesSelectedTotal() {
        var milk = item("milk", "Молоко", "1", QuantityUnit.LITER);
        var list = list(milk);
        var offer = snapshot("milk-1l", "Молоко", "99.90", "RUB", AvailabilityStatus.UNKNOWN);
        var packages = packages(binding(offer, "1", QuantityUnit.LITER));

        var quote = planner.quote(SCOPE, list, List.of(offer), packages);

        assertThat(quote.status()).isEqualTo(BasketQuoteStatus.UNCERTAIN);
        assertThat(quote.items()).singleElement()
                .extracting(BasketItemResolution::status)
                .isEqualTo(BasketItemResolutionStatus.AVAILABILITY_UNKNOWN);
        assertThat(quote.items().getFirst().selection()).isPresent();
        assertThat(quote.total()).contains(new BasketTotal(new BigDecimal("99.90"), "RUB"));
    }

    @Test
    void unmatchedAmbiguousUnavailablePackageUnknownAndUnitMismatchStayIncompleteWithoutPartialTotal() {
        var unmatchedItem = item("unmatched", "Сахар", "1", QuantityUnit.KILOGRAM);
        var unmatched = planner.quote(SCOPE, list(unmatchedItem), List.of(), PackageQuantitySet.of(List.of()));
        assertIncomplete(unmatched, BasketItemResolutionStatus.UNMATCHED);

        var ambiguousItem = item("ambiguous", "Молоко", "1", QuantityUnit.LITER);
        var milkA = snapshot("milk-a", "Молоко", "90.00", "RUB", AvailabilityStatus.AVAILABLE);
        var milkB = snapshot("milk-b", "Молоко", "80.00", "RUB", AvailabilityStatus.AVAILABLE);
        var ambiguous = planner.quote(
                SCOPE,
                list(ambiguousItem),
                List.of(milkA, milkB),
                packages(binding(milkA, "1", QuantityUnit.LITER), binding(milkB, "1", QuantityUnit.LITER)));
        assertIncomplete(ambiguous, BasketItemResolutionStatus.AMBIGUOUS);

        var unavailableItem = item("unavailable", "Кефир", "1", QuantityUnit.LITER);
        var unavailableOffer = snapshot("kefir", "Кефир", "89.00", "RUB", AvailabilityStatus.UNAVAILABLE);
        var unavailable = planner.quote(
                SCOPE,
                list(unavailableItem),
                List.of(unavailableOffer),
                PackageQuantitySet.of(List.of()));
        assertIncomplete(unavailable, BasketItemResolutionStatus.UNAVAILABLE);

        var unknownPackageItem = item("unknown-package", "Хлеб", "1", QuantityUnit.PIECE);
        var unknownPackageOffer = snapshot("bread", "Хлеб", "70.00", "RUB", AvailabilityStatus.AVAILABLE);
        var unknownPackage = planner.quote(
                SCOPE,
                list(unknownPackageItem),
                List.of(unknownPackageOffer),
                PackageQuantitySet.of(List.of()));
        assertIncomplete(unknownPackage, BasketItemResolutionStatus.PACKAGE_QUANTITY_UNKNOWN);

        var mismatchItem = item("mismatch", "Яйца", "6", QuantityUnit.PIECE);
        var mismatchOffer = snapshot("eggs", "Яйца", "100.00", "RUB", AvailabilityStatus.AVAILABLE);
        var mismatch = planner.quote(
                SCOPE,
                list(mismatchItem),
                List.of(mismatchOffer),
                packages(binding(mismatchOffer, "500", QuantityUnit.GRAM)));
        assertIncomplete(mismatch, BasketItemResolutionStatus.QUANTITY_UNIT_MISMATCH);
    }

    @Test
    void failsClosedOnMixedCurrenciesWhenEveryItemHasASelection() {
        var milk = item("milk", "Молоко", "1", QuantityUnit.PIECE);
        var bread = item("bread", "Хлеб", "1", QuantityUnit.PIECE);
        var milkOffer = snapshot("milk", "Молоко", "100.00", "RUB", AvailabilityStatus.AVAILABLE);
        var breadOffer = snapshot("bread", "Хлеб", "2.00", "USD", AvailabilityStatus.AVAILABLE);

        assertThatThrownBy(() -> planner.quote(
                        SCOPE,
                        list(milk, bread),
                        List.of(milkOffer, breadOffer),
                        packages(
                                binding(milkOffer, "1", QuantityUnit.PIECE),
                                binding(breadOffer, "1", QuantityUnit.PIECE))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("currency");
    }

    @Test
    void rejectsEmptyListAndMissingInputs() {
        var empty = ShoppingList.create(new ShoppingListId(UUID.fromString("99999999-9999-9999-9999-999999999999")));
        var packages = PackageQuantitySet.of(List.of());

        assertThatThrownBy(() -> planner.quote(SCOPE, empty, List.of(), packages))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("empty");
        assertThatThrownBy(() -> planner.quote(null, empty, List.of(), packages))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("scope");
        assertThatThrownBy(() -> planner.quote(SCOPE, null, List.of(), packages))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("shoppingList");
        assertThatThrownBy(() -> planner.quote(SCOPE, empty, null, packages))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("candidates");
        assertThatThrownBy(() -> planner.quote(SCOPE, empty, List.of(), null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("packageQuantities");
    }

    private static void assertIncomplete(
            SingleStoreBasketQuote quote,
            BasketItemResolutionStatus expectedStatus) {
        assertThat(quote.status()).isEqualTo(BasketQuoteStatus.INCOMPLETE);
        assertThat(quote.total()).isEmpty();
        assertThat(quote.items()).singleElement()
                .extracting(BasketItemResolution::status)
                .isEqualTo(expectedStatus);
        assertThat(quote.items().getFirst().selection()).isEmpty();
    }

    private static ShoppingList list(ShoppingItem... items) {
        var list = ShoppingList.create(new ShoppingListId(
                UUID.nameUUIDFromBytes(String.join("-", java.util.Arrays.stream(items)
                        .map(item -> item.id().value().toString())
                        .toList()).getBytes(StandardCharsets.UTF_8))));
        for (var item : items) {
            list.add(item);
        }
        return list;
    }

    private static ShoppingItem item(
            String idSeed,
            String text,
            String amount,
            QuantityUnit unit) {
        return new ShoppingItem(
                new ShoppingItemId(UUID.nameUUIDFromBytes(idSeed.getBytes(StandardCharsets.UTF_8))),
                new ShoppingRequirement(text),
                quantity(amount, unit));
    }

    private static Quantity quantity(String amount, QuantityUnit unit) {
        return new Quantity(new BigDecimal(amount), unit);
    }

    private static PackageQuantityBinding binding(
            OfferSnapshot snapshot,
            String amount,
            QuantityUnit unit) {
        return new PackageQuantityBinding(snapshot.id(), quantity(amount, unit));
    }

    private static PackageQuantitySet packages(PackageQuantityBinding... bindings) {
        return PackageQuantitySet.of(List.of(bindings));
    }

    private static OfferSnapshot snapshot(
            String sku,
            String productName,
            String price,
            String currency,
            AvailabilityStatus availability) {
        var observed = new ObservedOffer(
                RetailerId.PYATEROCHKA,
                "fixture-provider",
                AcquisitionMode.DIRECT_API,
                "store-42",
                sku,
                productName,
                new BigDecimal(price),
                currency,
                availability,
                OBSERVED_AT,
                "fixture://products/" + sku);
        return OfferSnapshot.observationOnly(
                new OfferSnapshotId(UUID.nameUUIDFromBytes(sku.getBytes(StandardCharsets.UTF_8))),
                observed);
    }
}
