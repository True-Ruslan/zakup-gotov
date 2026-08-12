package io.github.trueruslan.zakupgotov.preview;

import io.github.trueruslan.zakupgotov.retailer.RetailerId;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class ComparisonRuntimeEvidence {

    private final List<RetailerRuntimeEvidence> retailers;
    private final Map<RetailerId, RetailerRuntimeEvidence> retailersById;

    private ComparisonRuntimeEvidence(List<RetailerRuntimeEvidence> retailers) {
        var input = Objects.requireNonNull(retailers, "retailers must not be null");
        var byId = new EnumMap<RetailerId, RetailerRuntimeEvidence>(RetailerId.class);
        for (var retailer : input) {
            Objects.requireNonNull(retailer, "retailer evidence must not be null");
            if (byId.putIfAbsent(retailer.retailerId(), retailer) != null) {
                throw new IllegalArgumentException("duplicate retailer evidence: " + retailer.retailerId());
            }
        }
        this.retailers = List.copyOf(input);
        this.retailersById = Map.copyOf(byId);
    }

    public static ComparisonRuntimeEvidence empty() {
        return new ComparisonRuntimeEvidence(List.of());
    }

    public static ComparisonRuntimeEvidence of(List<RetailerRuntimeEvidence> retailers) {
        return new ComparisonRuntimeEvidence(retailers);
    }

    public List<RetailerRuntimeEvidence> retailers() {
        return retailers;
    }

    public Optional<RetailerRuntimeEvidence> forRetailer(RetailerId retailerId) {
        return Optional.ofNullable(retailersById.get(Objects.requireNonNull(retailerId, "retailerId must not be null")));
    }
}
