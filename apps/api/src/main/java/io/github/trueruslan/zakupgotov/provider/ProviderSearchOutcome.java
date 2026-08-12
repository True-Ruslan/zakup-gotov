package io.github.trueruslan.zakupgotov.provider;

import io.github.trueruslan.zakupgotov.retailer.RetailerId;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public record ProviderSearchOutcome(
        RetailerId retailerId,
        Optional<ProviderPathSelection> selectedPath,
        List<ObservedOffer> offers,
        List<ProviderPathAttempt> attempts) {

    public ProviderSearchOutcome {
        retailerId = Objects.requireNonNull(retailerId, "retailerId must not be null");
        selectedPath = Objects.requireNonNull(selectedPath, "selectedPath must not be null");
        offers = List.copyOf(Objects.requireNonNull(offers, "offers must not be null"));
        attempts = List.copyOf(Objects.requireNonNull(attempts, "attempts must not be null"));
    }

    public boolean succeeded() {
        return selectedPath.isPresent();
    }

    static ProviderSearchOutcome success(
            RetailerId retailerId,
            ProviderPathSelection selectedPath,
            List<ObservedOffer> offers,
            List<ProviderPathAttempt> attempts) {
        return new ProviderSearchOutcome(retailerId, Optional.of(selectedPath), offers, attempts);
    }

    static ProviderSearchOutcome unavailable(RetailerId retailerId, List<ProviderPathAttempt> attempts) {
        return new ProviderSearchOutcome(retailerId, Optional.empty(), List.of(), attempts);
    }
}
