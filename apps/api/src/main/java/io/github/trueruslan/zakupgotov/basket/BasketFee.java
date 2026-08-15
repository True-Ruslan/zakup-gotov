package io.github.trueruslan.zakupgotov.basket;

import java.util.Objects;
import java.util.Optional;

public record BasketFee(
        BasketEconomicsKnowledgeStatus status,
        Optional<BasketTotal> amount) {

    public BasketFee {
        status = Objects.requireNonNull(status, "status must not be null");
        amount = Objects.requireNonNull(amount, "amount must not be null");
        if (status == BasketEconomicsKnowledgeStatus.KNOWN && amount.isEmpty()) {
            throw new IllegalArgumentException("KNOWN fee requires an amount");
        }
        if (status == BasketEconomicsKnowledgeStatus.UNKNOWN && amount.isPresent()) {
            throw new IllegalArgumentException("UNKNOWN fee must not carry an amount");
        }
    }

    public static BasketFee known(BasketTotal amount) {
        return new BasketFee(
                BasketEconomicsKnowledgeStatus.KNOWN,
                Optional.of(Objects.requireNonNull(amount, "amount must not be null")));
    }

    public static BasketFee unknown() {
        return new BasketFee(BasketEconomicsKnowledgeStatus.UNKNOWN, Optional.empty());
    }
}
