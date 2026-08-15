package io.github.trueruslan.zakupgotov.basket;

import java.util.Objects;
import java.util.Optional;

public record MinimumOrderConstraint(
        BasketEconomicsKnowledgeStatus status,
        Optional<BasketTotal> threshold) {

    public MinimumOrderConstraint {
        status = Objects.requireNonNull(status, "status must not be null");
        threshold = Objects.requireNonNull(threshold, "threshold must not be null");
        if (status == BasketEconomicsKnowledgeStatus.KNOWN && threshold.isEmpty()) {
            throw new IllegalArgumentException("KNOWN minimum order requires a threshold");
        }
        if (status == BasketEconomicsKnowledgeStatus.UNKNOWN && threshold.isPresent()) {
            throw new IllegalArgumentException("UNKNOWN minimum order must not carry a threshold");
        }
    }

    public static MinimumOrderConstraint known(BasketTotal threshold) {
        return new MinimumOrderConstraint(
                BasketEconomicsKnowledgeStatus.KNOWN,
                Optional.of(Objects.requireNonNull(threshold, "threshold must not be null")));
    }

    public static MinimumOrderConstraint unknown() {
        return new MinimumOrderConstraint(BasketEconomicsKnowledgeStatus.UNKNOWN, Optional.empty());
    }
}
