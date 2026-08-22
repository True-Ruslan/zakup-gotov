package io.github.trueruslan.zakupgotov.basket;

import java.util.Objects;
import java.util.Optional;

public record BasketEconomicsAssessment(
        BasketTotal merchandiseSubtotal,
        BasketEconomics economics,
        MinimumOrderStatus minimumOrderStatus,
        CheckoutTotalStatus checkoutTotalStatus,
        Optional<BasketTotal> checkoutTotal) {

    public BasketEconomicsAssessment {
        merchandiseSubtotal = Objects.requireNonNull(merchandiseSubtotal, "merchandiseSubtotal must not be null");
        economics = Objects.requireNonNull(economics, "economics must not be null");
        minimumOrderStatus = Objects.requireNonNull(minimumOrderStatus, "minimumOrderStatus must not be null");
        checkoutTotalStatus = Objects.requireNonNull(checkoutTotalStatus, "checkoutTotalStatus must not be null");
        checkoutTotal = Objects.requireNonNull(checkoutTotal, "checkoutTotal must not be null");

        BasketEconomicsCalculator.validateCurrencies(merchandiseSubtotal, economics);
        var expectedMinimumOrderStatus = BasketEconomicsCalculator.minimumOrderStatus(
                merchandiseSubtotal,
                economics.minimumOrder());
        if (minimumOrderStatus != expectedMinimumOrderStatus) {
            throw new IllegalArgumentException("minimum order status must match basket economics");
        }

        var expectedCheckoutTotal = BasketEconomicsCalculator.checkoutTotal(merchandiseSubtotal, economics);
        var expectedCheckoutTotalStatus = expectedCheckoutTotal.isPresent()
                ? CheckoutTotalStatus.KNOWN
                : CheckoutTotalStatus.UNKNOWN;
        if (checkoutTotalStatus != expectedCheckoutTotalStatus) {
            throw new IllegalArgumentException("checkout total status must match basket economics knowledge");
        }
        if (!sameCheckoutTotal(checkoutTotal, expectedCheckoutTotal)) {
            throw new IllegalArgumentException("checkout total amount must match merchandise subtotal and known fees");
        }
    }

    private static boolean sameCheckoutTotal(Optional<BasketTotal> actual, Optional<BasketTotal> expected) {
        if (actual.isEmpty() || expected.isEmpty()) {
            return actual.isEmpty() == expected.isEmpty();
        }
        var actualTotal = actual.get();
        var expectedTotal = expected.get();
        return actualTotal.currencyCode().equals(expectedTotal.currencyCode())
                && actualTotal.amount().compareTo(expectedTotal.amount()) == 0;
    }
}
