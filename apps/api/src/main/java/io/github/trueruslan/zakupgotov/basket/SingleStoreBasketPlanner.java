package io.github.trueruslan.zakupgotov.basket;

import io.github.trueruslan.zakupgotov.matching.DeterministicProductMatcher;
import io.github.trueruslan.zakupgotov.matching.MatchScope;
import io.github.trueruslan.zakupgotov.matching.ProductMatchStatus;
import io.github.trueruslan.zakupgotov.provider.AvailabilityStatus;
import io.github.trueruslan.zakupgotov.provider.OfferSnapshot;
import io.github.trueruslan.zakupgotov.shopping.ShoppingItem;
import io.github.trueruslan.zakupgotov.shopping.ShoppingList;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class SingleStoreBasketPlanner {

    private final DeterministicProductMatcher matcher = new DeterministicProductMatcher();

    public SingleStoreBasketQuote quote(
            MatchScope scope,
            ShoppingList shoppingList,
            List<OfferSnapshot> candidates,
            PackageQuantitySet packageQuantities) {
        Objects.requireNonNull(scope, "scope must not be null");
        Objects.requireNonNull(shoppingList, "shoppingList must not be null");
        Objects.requireNonNull(candidates, "candidates must not be null");
        Objects.requireNonNull(packageQuantities, "packageQuantities must not be null");
        if (shoppingList.items().isEmpty()) {
            throw new IllegalArgumentException("shoppingList must not be empty");
        }

        var resolutions = new ArrayList<BasketItemResolution>();
        for (var item : shoppingList.items()) {
            resolutions.add(resolve(scope, item, candidates, packageQuantities));
        }

        validateSelectedCurrencies(resolutions);

        var hasIncomplete = resolutions.stream().anyMatch(SingleStoreBasketPlanner::isIncomplete);
        if (hasIncomplete) {
            return new SingleStoreBasketQuote(
                    scope,
                    shoppingList.id(),
                    BasketQuoteStatus.INCOMPLETE,
                    resolutions,
                    Optional.empty());
        }

        var total = total(resolutions);
        var hasUnknownAvailability = resolutions.stream()
                .anyMatch(resolution -> resolution.status() == BasketItemResolutionStatus.AVAILABILITY_UNKNOWN);
        return new SingleStoreBasketQuote(
                scope,
                shoppingList.id(),
                hasUnknownAvailability ? BasketQuoteStatus.UNCERTAIN : BasketQuoteStatus.COMPLETE,
                resolutions,
                Optional.of(total));
    }

    private BasketItemResolution resolve(
            MatchScope scope,
            ShoppingItem item,
            List<OfferSnapshot> candidates,
            PackageQuantitySet packageQuantities) {
        var match = matcher.match(scope, item.requirement(), candidates);
        if (match.status() == ProductMatchStatus.UNMATCHED) {
            return new BasketItemResolution(
                    item, match, BasketItemResolutionStatus.UNMATCHED, Optional.empty());
        }
        if (match.status() == ProductMatchStatus.AMBIGUOUS) {
            return new BasketItemResolution(
                    item, match, BasketItemResolutionStatus.AMBIGUOUS, Optional.empty());
        }

        var snapshot = match.candidates().getFirst();
        switch (snapshot.availability()) {
            case UNAVAILABLE -> {
                return new BasketItemResolution(
                        item, match, BasketItemResolutionStatus.UNAVAILABLE, Optional.empty());
            }
            case AVAILABLE, UNKNOWN -> {
                // Resolution continues below; the exhaustive switch guards against a future
                // AvailabilityStatus value silently falling through as available.
            }
        }

        var packageQuantity = packageQuantities.quantityFor(snapshot.id());
        if (packageQuantity.isEmpty()) {
            return new BasketItemResolution(
                    item, match, BasketItemResolutionStatus.PACKAGE_QUANTITY_UNKNOWN, Optional.empty());
        }
        if (item.quantity().unit() != packageQuantity.orElseThrow().unit()) {
            return new BasketItemResolution(
                    item, match, BasketItemResolutionStatus.QUANTITY_UNIT_MISMATCH, Optional.empty());
        }

        var selection = PackageSelectionCalculator.calculate(
                snapshot,
                item.quantity(),
                packageQuantity.orElseThrow());
        var status = switch (snapshot.availability()) {
            case UNKNOWN -> BasketItemResolutionStatus.AVAILABILITY_UNKNOWN;
            case AVAILABLE -> BasketItemResolutionStatus.FULFILLED;
            case UNAVAILABLE -> throw new IllegalStateException("UNAVAILABLE is handled above");
        };
        return new BasketItemResolution(item, match, status, Optional.of(selection));
    }

    private static boolean isIncomplete(BasketItemResolution resolution) {
        return resolution.status() != BasketItemResolutionStatus.FULFILLED
                && resolution.status() != BasketItemResolutionStatus.AVAILABILITY_UNKNOWN;
    }

    private static void validateSelectedCurrencies(List<BasketItemResolution> resolutions) {
        String currency = null;
        for (var resolution : resolutions) {
            if (resolution.selection().isEmpty()) {
                continue;
            }
            var selectedCurrency = resolution.selection().orElseThrow().snapshot().currencyCode();
            if (currency == null) {
                currency = selectedCurrency;
            } else if (!currency.equals(selectedCurrency)) {
                throw new IllegalArgumentException("selected basket lines must use the same currency");
            }
        }
    }

    private static BasketTotal total(List<BasketItemResolution> resolutions) {
        BigDecimal amount = BigDecimal.ZERO;
        String currency = null;
        for (var resolution : resolutions) {
            var selection = resolution.selection().orElseThrow();
            amount = amount.add(selection.lineTotal());
            if (currency == null) {
                currency = selection.snapshot().currencyCode();
            }
        }
        return new BasketTotal(amount, Objects.requireNonNull(currency, "basket currency must not be null"));
    }
}
