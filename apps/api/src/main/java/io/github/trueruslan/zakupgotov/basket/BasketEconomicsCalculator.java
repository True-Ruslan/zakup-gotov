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

        validateCurrency(merchandiseSubtotal, economics.deliveryFee().amount());
        validateCurrency(merchandiseSubtotal, economics.serviceFee().amount());
        validateCurrency(merchandiseSubtotal, economics.minimumOrder().threshold());

        var minimumOrderStatus = minimumOrderStatus(merchandiseSubtotal, economics.minimumOrder());
        if (economics.deliveryFee().status() == BasketEconomicsKnowledgeStatus.UNKNOWN
                || economics.serviceFee().status() == BasketEconomicsKnowledgeStatus.UNKNOWN) {
            return new BasketEconomicsAssessment(
                    merchandiseSubtotal,
                    economics,
                    minimumOrderStatus,
                    CheckoutTotalStatus.UNKNOWN,
                    Optional.empty());
        }

        var checkoutAmount = merchandiseSubtotal.amount()
                .add(economics.deliveryFee().amount().orElseThrow().amount())
                .add(economics.serviceFee().amount().orElseThrow().amount());
        var checkoutTotal = new BasketTotal(checkoutAmount, merchandiseSubtotal.currencyCode());
        return new BasketEconomicsAssessment(
                merchandiseSubtotal,
                economics,
                minimumOrderStatus,
                CheckoutTotalStatus.KNOWN,
                Optional.of(checkoutTotal));
    }

    private static MinimumOrderStatus minimumOrderStatus(
            BasketTotal merchandiseSubtotal,
            MinimumOrderConstraint minimumOrder) {
        if (minimumOrder.status() == BasketEconomicsKnowledgeStatus.UNKNOWN) {
            return MinimumOrderStatus.UNKNOWN;
        }
        return merchandiseSubtotal.amount().compareTo(minimumOrder.threshold().orElseThrow().amount()) >= 0
                ? MinimumOrderStatus.MET
                : MinimumOrderStatus.NOT_MET;
    }

    private static void validateCurrency(
            BasketTotal merchandiseSubtotal,
            Optional<BasketTotal> component) {
        component.ifPresent(amount -> {
            if (!merchandiseSubtotal.currencyCode().equals(amount.currencyCode())) {
                throw new IllegalArgumentException("basket economics currency must match merchandise subtotal currency");
            }
        });
    }
}
