package io.github.trueruslan.zakupgotov.retailer;

import java.util.Objects;

public final class Retailer {

    private final RetailerId id;

    public Retailer(RetailerId id) {
        this.id = Objects.requireNonNull(id, "id must not be null");
    }

    public RetailerId id() {
        return id;
    }

    public String canonicalId() {
        return id.canonicalId();
    }

    @Override
    public boolean equals(Object other) {
        return this == other || other instanceof Retailer retailer && id == retailer.id;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return canonicalId();
    }
}
