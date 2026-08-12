package io.github.trueruslan.zakupgotov.basket;

import io.github.trueruslan.zakupgotov.provider.OfferSnapshotId;
import io.github.trueruslan.zakupgotov.shopping.Quantity;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class PackageQuantitySet {

    private final List<PackageQuantityBinding> bindings;
    private final Map<OfferSnapshotId, PackageQuantityBinding> bindingsBySnapshotId;

    private PackageQuantitySet(List<PackageQuantityBinding> bindings) {
        var input = Objects.requireNonNull(bindings, "bindings must not be null");
        var bySnapshot = new LinkedHashMap<OfferSnapshotId, PackageQuantityBinding>();
        for (var binding : input) {
            Objects.requireNonNull(binding, "binding must not be null");
            var previous = bySnapshot.putIfAbsent(binding.snapshotId(), binding);
            if (previous != null) {
                throw new IllegalArgumentException("duplicate package quantity binding for snapshotId: "
                        + binding.snapshotId().value());
            }
        }

        this.bindings = List.copyOf(input);
        this.bindingsBySnapshotId = Map.copyOf(bySnapshot);
    }

    public static PackageQuantitySet of(List<PackageQuantityBinding> bindings) {
        return new PackageQuantitySet(bindings);
    }

    public List<PackageQuantityBinding> bindings() {
        return bindings;
    }

    public Optional<Quantity> quantityFor(OfferSnapshotId snapshotId) {
        Objects.requireNonNull(snapshotId, "snapshotId must not be null");
        return Optional.ofNullable(bindingsBySnapshotId.get(snapshotId))
                .map(PackageQuantityBinding::packageQuantity);
    }
}
