package io.github.trueruslan.zakupgotov.location;

import java.util.Objects;
import java.util.Optional;

public final class ProductLocation {

    private final ProductLocationId id;
    private final String locality;
    private final SensitiveAddress address;

    private ProductLocation(ProductLocationId id, String locality, SensitiveAddress address) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.locality = normalizeLocality(locality);
        this.address = address;
    }

    public static ProductLocation localityOnly(ProductLocationId id, String locality) {
        return new ProductLocation(id, locality, null);
    }

    public static ProductLocation withAddress(ProductLocationId id, String locality, String address) {
        return new ProductLocation(id, locality, SensitiveAddress.of(address));
    }

    public ProductLocationId id() {
        return id;
    }

    public String locality() {
        return locality;
    }

    public Optional<SensitiveAddress> address() {
        return Optional.ofNullable(address);
    }

    private static String normalizeLocality(String locality) {
        Objects.requireNonNull(locality, "locality must not be null");
        var normalized = locality.strip().replaceAll("\\s+", " ");
        if (normalized.isBlank()) {
            throw new IllegalArgumentException("locality must not be blank");
        }
        return normalized;
    }

    @Override
    public boolean equals(Object other) {
        return this == other
                || other instanceof ProductLocation location
                        && id.equals(location.id)
                        && locality.equals(location.locality)
                        && Objects.equals(address, location.address);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, locality, address);
    }

    @Override
    public String toString() {
        return "ProductLocation[id=" + id + ", locality=" + locality + ", address="
                + (address == null ? "empty" : address) + "]";
    }
}
