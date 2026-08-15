package io.github.trueruslan.zakupgotov.basket;

import java.util.Objects;

public record BasketEconomics(
        BasketFee deliveryFee,
        BasketFee serviceFee,
        MinimumOrderConstraint minimumOrder) {

    public BasketEconomics {
        deliveryFee = Objects.requireNonNull(deliveryFee, "deliveryFee must not be null");
        serviceFee = Objects.requireNonNull(serviceFee, "serviceFee must not be null");
        minimumOrder = Objects.requireNonNull(minimumOrder, "minimumOrder must not be null");
    }
}
