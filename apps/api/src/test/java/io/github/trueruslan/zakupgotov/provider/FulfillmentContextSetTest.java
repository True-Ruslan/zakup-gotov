package io.github.trueruslan.zakupgotov.provider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.trueruslan.zakupgotov.location.ProductLocationId;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class FulfillmentContextSetTest {

    private static final ProductLocationId LOCATION_ID = new ProductLocationId(
            UUID.fromString("55555555-5555-5555-5555-555555555555"));
    private static final ProductLocationId OTHER_LOCATION_ID = new ProductLocationId(
            UUID.fromString("66666666-6666-6666-6666-666666666666"));

    @Test
    void preservesManualAndResolvedProviderContextsInStableOrder() {
        var manual = binding(
                LOCATION_ID,
                "magnit-public-web",
                "shop-42",
                FulfillmentContextSelectionMode.MANUAL);
        var resolved = binding(
                LOCATION_ID,
                "kuper",
                "fulfillment-99",
                FulfillmentContextSelectionMode.RESOLVED);

        var contexts = FulfillmentContextSet.of(LOCATION_ID, List.of(manual, resolved));

        assertThat(contexts.productLocationId()).isEqualTo(LOCATION_ID);
        assertThat(contexts.bindings()).containsExactly(manual, resolved);
        assertThat(contexts.bindingFor("magnit-public-web")).contains(manual);
        assertThat(contexts.contextFor("kuper")).contains(resolved.context());
        assertThat(contexts.bindingFor("unknown-provider")).isEmpty();
    }

    @Test
    void rejectsBindingThatBelongsToAnotherProductLocation() {
        var foreign = binding(
                OTHER_LOCATION_ID,
                "magnit-public-web",
                "shop-42",
                FulfillmentContextSelectionMode.MANUAL);

        assertThatThrownBy(() -> FulfillmentContextSet.of(LOCATION_ID, List.of(foreign)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("productLocationId");
    }

    @Test
    void rejectsDuplicateSourceProviderContext() {
        var first = binding(
                LOCATION_ID,
                "kuper",
                "fulfillment-1",
                FulfillmentContextSelectionMode.RESOLVED);
        var second = binding(
                LOCATION_ID,
                "kuper",
                "fulfillment-2",
                FulfillmentContextSelectionMode.MANUAL);

        assertThatThrownBy(() -> FulfillmentContextSet.of(LOCATION_ID, List.of(first, second)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("duplicate")
                .hasMessageContaining("kuper");
    }

    @Test
    void exposesImmutableBindingSnapshot() {
        var contexts = FulfillmentContextSet.of(
                LOCATION_ID,
                List.of(binding(
                        LOCATION_ID,
                        "kuper",
                        "fulfillment-99",
                        FulfillmentContextSelectionMode.RESOLVED)));

        assertThatThrownBy(() -> contexts.bindings().clear())
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void rejectsMissingBindingParts() {
        assertThatThrownBy(() -> new FulfillmentContextBinding(
                        null,
                        new LocationContext("kuper", "fulfillment-99", "Москва"),
                        FulfillmentContextSelectionMode.RESOLVED))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("productLocationId");
        assertThatThrownBy(() -> new FulfillmentContextBinding(
                        LOCATION_ID,
                        null,
                        FulfillmentContextSelectionMode.RESOLVED))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("context");
        assertThatThrownBy(() -> new FulfillmentContextBinding(
                        LOCATION_ID,
                        new LocationContext("kuper", "fulfillment-99", "Москва"),
                        null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("mode");
    }

    private static FulfillmentContextBinding binding(
            ProductLocationId productLocationId,
            String sourceProviderId,
            String fulfillmentContextId,
            FulfillmentContextSelectionMode mode) {
        return new FulfillmentContextBinding(
                productLocationId,
                new LocationContext(sourceProviderId, fulfillmentContextId, "Москва"),
                mode);
    }
}
