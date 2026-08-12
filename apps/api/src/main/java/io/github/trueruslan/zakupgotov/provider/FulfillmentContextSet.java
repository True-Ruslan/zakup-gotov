package io.github.trueruslan.zakupgotov.provider;

import io.github.trueruslan.zakupgotov.location.ProductLocationId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class FulfillmentContextSet {

    private final ProductLocationId productLocationId;
    private final List<FulfillmentContextBinding> bindings;
    private final Map<String, FulfillmentContextBinding> bindingsBySourceProvider;

    private FulfillmentContextSet(
            ProductLocationId productLocationId,
            List<FulfillmentContextBinding> bindings) {
        this.productLocationId = Objects.requireNonNull(productLocationId, "productLocationId must not be null");
        this.bindings = List.copyOf(Objects.requireNonNull(bindings, "bindings must not be null"));

        var byProvider = new LinkedHashMap<String, FulfillmentContextBinding>();
        for (var binding : this.bindings) {
            Objects.requireNonNull(binding, "binding must not be null");
            if (!this.productLocationId.equals(binding.productLocationId())) {
                throw new IllegalArgumentException("binding productLocationId must match context set productLocationId");
            }
            var sourceProviderId = binding.context().sourceProviderId();
            if (byProvider.putIfAbsent(sourceProviderId, binding) != null) {
                throw new IllegalArgumentException("duplicate sourceProviderId binding: " + sourceProviderId);
            }
        }
        this.bindingsBySourceProvider = Map.copyOf(byProvider);
    }

    public static FulfillmentContextSet of(
            ProductLocationId productLocationId,
            List<FulfillmentContextBinding> bindings) {
        return new FulfillmentContextSet(productLocationId, bindings);
    }

    public ProductLocationId productLocationId() {
        return productLocationId;
    }

    public List<FulfillmentContextBinding> bindings() {
        return bindings;
    }

    public Optional<FulfillmentContextBinding> bindingFor(String sourceProviderId) {
        if (sourceProviderId == null || sourceProviderId.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(bindingsBySourceProvider.get(sourceProviderId));
    }

    public Optional<LocationContext> contextFor(String sourceProviderId) {
        return bindingFor(sourceProviderId).map(FulfillmentContextBinding::context);
    }
}
