package io.github.trueruslan.zakupgotov.basket;

import java.util.Objects;
import java.util.Optional;

public final class BasketEconomicsCalculator {

    private BasketEconomicsCalculator() {}

    public static BasketEconomicsAssessment assess(
            BasketTotal merchandiseSubtotal,
            BasketEconomics economics) {
        Objects.requireNonNull(merchandiseSubtotal, "merchandiseSubtotal must not be null");
        Objects.requireNonNull(economics, "economics must not be null");

        validateCurrencies(merchandiseSubtotal, economics);
        var minimumOrderStatus = minimumOrderStatus(merchandiseSubtotal, economics.minimumOrder());
        var checkoutTotal = checkoutTotal(merchandiseSubtotal, economics);
        var checkoutTotalStatus = checkoutTotal.isPresent()
                ? CheckoutTotalStatus.KNOWN
                : CheckoutTotalStatus.UNKNOWN;
        return new BasketEconomicsAssessment(
                merchandiseSubtotal,
                economics,
                minimumOrderStatus,
                checkoutTotalStatus,
                checkoutTotal);
    }

    static void validateCurrencies(
            BasketTotal merchandiseSubtotal,
            BasketEconomics economics) {
        validateCurrency(merchandiseSubtotal, economics.deliveryFee().amount());
        validateCurrency(merchandiseSubtotal, economics.serviceFee().amount());
        validateCurrency(merchandiseSubtotal, economics.minimumOrder().threshold());
    }

    static MinimumOrderStatus minimumOrderStatus(
            BasketTotal merchandiseSubtotal,
            MinimumOrderConstraint minimumOrder) {
        if (minimumOrder.status() == BasketEconomicsKnowledgeStatus.UNKNOWN) {
            return MinimumOrderStatus.UNKNOWN;
        }
        return merchandiseSubtotal.amount().compareTo(minimumOrder.threshold().orElseThrow().amount()) >= 0
                ? MinimumOrderStatus.MET
                : MinimumOrderStatus.NOT_MET;
    }

    static Optional<BasketTotal> checkoutTotal(
            BasketTotal merchandiseSubtotal,
            BasketEconomics economics) {
        if (economics.deliveryFee().status() == BasketEconomicsKnowledgeStatus.UNKNOWN
                || economics.serviceFee().status() == BasketEconomicsKnowledgeStatus.UNKNOWN) {
            return Optional.empty();
        }
        var checkoutAmount = merchandiseSubtotal.amount()
                .add(economics.deliveryFee().amount().orElseThrow().amount())
                .add(economics.serviceFee().amount().orElseThrow().amount());
        return Optional.of(new BasketTotal(checkoutAmount, merchandiseSubtotal.currencyCode()));
    }

    private static void validateCurrency(
            BasketTotal merchandiseSubtotal,
            Optional<BasketTotal> component) {
        if (component.isPresent()
                && !merchandiseSubtotal.currencyCode().equals(component.orElseThrow().currencyCode())) {
            throw new IllegalArgumentException("basket economics currency must match merchandise subtotal currency");
        }
    }
}
