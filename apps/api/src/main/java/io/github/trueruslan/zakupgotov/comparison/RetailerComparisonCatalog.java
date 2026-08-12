package io.github.trueruslan.zakupgotov.comparison;

import io.github.trueruslan.zakupgotov.retailer.RetailerId;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class RetailerComparisonCatalog {

    private final List<RetailerComparisonView> retailers;
    private final Map<RetailerId, RetailerComparisonView> retailersById;

    public RetailerComparisonCatalog(List<RetailerComparisonView> retailers) {
        this.retailers = List.copyOf(Objects.requireNonNull(retailers, "retailers must not be null"));
        var byId = new EnumMap<RetailerId, RetailerComparisonView>(RetailerId.class);
        for (var retailer : this.retailers) {
            var previous = byId.put(retailer.retailerId(), retailer);
            if (previous != null) {
                throw new IllegalArgumentException("duplicate retailer comparison view: " + retailer.retailerId());
            }
        }
        this.retailersById = Map.copyOf(byId);
    }

    public List<RetailerComparisonView> retailers() {
        return retailers;
    }

    public RetailerComparisonView require(RetailerId retailerId) {
        Objects.requireNonNull(retailerId, "retailerId must not be null");
        var retailer = retailersById.get(retailerId);
        if (retailer == null) {
            throw new IllegalArgumentException("retailer is not present in comparison catalog: " + retailerId);
        }
        return retailer;
    }
}
