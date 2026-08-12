package io.github.trueruslan.zakupgotov.retailer;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class RetailerRegistry {

    private static final RetailerRegistry INITIAL = new RetailerRegistry(List.of(
            entry(
                    RetailerId.PYATEROCHKA,
                    RetailerCoverageState.AVAILABLE_BROWSER_BRIDGE,
                    ProductionAccessStatus.NOT_ASSESSED),
            entry(
                    RetailerId.PEREKRESTOK,
                    RetailerCoverageState.AVAILABLE_BROWSER_BRIDGE,
                    ProductionAccessStatus.NOT_ASSESSED),
            entry(
                    RetailerId.CHIZHIK,
                    RetailerCoverageState.DISCOVERY,
                    ProductionAccessStatus.NOT_ASSESSED),
            entry(
                    RetailerId.MAGNIT,
                    RetailerCoverageState.AVAILABLE_PUBLIC_WEB,
                    ProductionAccessStatus.BLOCKED),
            entry(
                    RetailerId.LENTA,
                    RetailerCoverageState.DISCOVERY,
                    ProductionAccessStatus.NOT_ASSESSED),
            entry(
                    RetailerId.VKUSVILL,
                    RetailerCoverageState.DISCOVERY,
                    ProductionAccessStatus.NOT_ASSESSED),
            entry(
                    RetailerId.OZON_FRESH,
                    RetailerCoverageState.DISCOVERY,
                    ProductionAccessStatus.NOT_ASSESSED),
            entry(
                    RetailerId.SAMOKAT,
                    RetailerCoverageState.DISCOVERY,
                    ProductionAccessStatus.NOT_ASSESSED)));

    private final List<RetailerRegistryEntry> entries;
    private final Map<RetailerId, RetailerRegistryEntry> entriesById;

    private RetailerRegistry(List<RetailerRegistryEntry> entries) {
        this.entries = List.copyOf(Objects.requireNonNull(entries, "entries must not be null"));

        var byId = new EnumMap<RetailerId, RetailerRegistryEntry>(RetailerId.class);
        for (var entry : this.entries) {
            Objects.requireNonNull(entry, "retailer registry entry must not be null");
            var previous = byId.put(entry.retailer().id(), entry);
            if (previous != null) {
                throw new IllegalArgumentException("duplicate retailer id: " + entry.retailer().canonicalId());
            }
        }

        if (!byId.keySet().equals(EnumSet.allOf(RetailerId.class))) {
            throw new IllegalStateException("retailer registry must cover every canonical retailer id");
        }
        this.entriesById = Map.copyOf(byId);
    }

    public static RetailerRegistry initial() {
        return INITIAL;
    }

    public static RetailerRegistry of(List<RetailerRegistryEntry> entries) {
        return new RetailerRegistry(entries);
    }

    public List<RetailerRegistryEntry> entries() {
        return entries;
    }

    public RetailerRegistryEntry require(RetailerId retailerId) {
        Objects.requireNonNull(retailerId, "retailerId must not be null");
        var entry = entriesById.get(retailerId);
        if (entry == null) {
            throw new IllegalArgumentException("unknown retailer id: " + retailerId);
        }
        return entry;
    }

    private static RetailerRegistryEntry entry(
            RetailerId retailerId,
            RetailerCoverageState coverageState,
            ProductionAccessStatus productionAccessStatus) {
        return new RetailerRegistryEntry(new Retailer(retailerId), coverageState, productionAccessStatus);
    }
}
