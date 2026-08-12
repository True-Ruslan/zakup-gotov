package io.github.trueruslan.zakupgotov.retailer;

public enum RetailerCoverageState {
    REQUIRED_UNIMPLEMENTED(false),
    DISCOVERY(false),
    AVAILABLE_DIRECT(true),
    AVAILABLE_AGGREGATOR(true),
    AVAILABLE_PUBLIC_WEB(true),
    AVAILABLE_BROWSER_BRIDGE(true),
    DEGRADED(false),
    BLOCKED_EXTERNAL(false);

    private final boolean technicallyAvailable;

    RetailerCoverageState(boolean technicallyAvailable) {
        this.technicallyAvailable = technicallyAvailable;
    }

    public boolean isTechnicallyAvailable() {
        return technicallyAvailable;
    }
}
