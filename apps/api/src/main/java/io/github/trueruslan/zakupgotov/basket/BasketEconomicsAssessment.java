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
        if (checkoutTotalStatus == CheckoutTotalStatus.KNOWN && checkoutTotal.isEmpty()) {
            throw new IllegalArgumentException("KNOWN checkout total requires an amount");
        }
        if (checkoutTotalStatus == CheckoutTotalStatus.UNKNOWN && checkoutTotal.isPresent()) {
            throw new IllegalArgumentException("UNKNOWN checkout total must not carry an amount");
        }
        checkoutTotal.ifPresent(total -> {
            if (!merchandiseSubtotal.currencyCode().equals(total.currencyCode())) {
                throw new IllegalArgumentException("checkout total currency must match merchandise subtotal currency");
            }
        });
    }
}
