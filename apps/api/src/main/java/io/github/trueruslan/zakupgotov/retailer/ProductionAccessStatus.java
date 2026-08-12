package io.github.trueruslan.zakupgotov.retailer;

public enum ProductionAccessStatus {
    NOT_ASSESSED,
    UNRESOLVED,
    ACCEPTABLE,
    BLOCKED;

    public boolean isProductionReady() {
        return this == ACCEPTABLE;
    }
}
