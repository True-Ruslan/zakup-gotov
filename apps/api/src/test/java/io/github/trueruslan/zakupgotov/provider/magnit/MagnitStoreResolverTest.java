package io.github.trueruslan.zakupgotov.provider.magnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.trueruslan.zakupgotov.location.ProductLocationId;
import io.github.trueruslan.zakupgotov.provider.FulfillmentContextSelectionMode;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class MagnitStoreResolverTest {

    private static final ProductLocationId LOCATION_ID =
            new ProductLocationId(UUID.fromString("69696969-6969-6969-6969-696969696969"));

    @Test
    void resolvesExactlyOneCandidate() {
        var candidate = candidate("992301", 45.067057, 38.973527);

        var resolution = MagnitStoreResolver.resolve(new MagnitStoreSearchEvidence(List.of(candidate), false));

        assertThat(resolution.status()).isEqualTo(MagnitStoreResolutionStatus.RESOLVED);
        assertThat(resolution.candidate()).contains(candidate);
    }

    @Test
    void zeroCandidatesFailClosedAsNoStores() {
        var resolution = MagnitStoreResolver.resolve(MagnitStoreSearchEvidence.empty());

        assertThat(resolution.status()).isEqualTo(MagnitStoreResolutionStatus.NO_STORES);
        assertThat(resolution.candidate()).isEmpty();
    }

    @Test
    void multipleCandidatesRemainAmbiguousRegardlessOfInputOrder() {
        var first = candidate("100", 55.70, 37.60);
        var second = candidate("200", 55.71, 37.61);

        var left = MagnitStoreResolver.resolve(new MagnitStoreSearchEvidence(List.of(first, second), false));
        var right = MagnitStoreResolver.resolve(new MagnitStoreSearchEvidence(List.of(second, first), false));

        assertThat(left.status()).isEqualTo(MagnitStoreResolutionStatus.AMBIGUOUS);
        assertThat(right.status()).isEqualTo(MagnitStoreResolutionStatus.AMBIGUOUS);
        assertThat(left.candidate()).isEmpty();
        assertThat(right.candidate()).isEmpty();
    }

    @Test
    void parserConflictRemainsAnExplicitResolutionState() {
        var resolution = MagnitStoreResolver.resolve(MagnitStoreSearchEvidence.conflict());

        assertThat(resolution.status()).isEqualTo(MagnitStoreResolutionStatus.CONFLICTING_STORE_EVIDENCE);
        assertThat(resolution.candidate()).isEmpty();
    }

    @Test
    void automaticBindingUsesExistingProviderIdentityAndResolvedMode() {
        var candidate = candidate("992301", 45.067057, 38.973527);
        var resolution = MagnitStoreResolver.resolve(new MagnitStoreSearchEvidence(List.of(candidate), false));

        var binding = MagnitFulfillmentContextBindings.autoResolved(LOCATION_ID, "Краснодар", resolution).orElseThrow();

        assertThat(binding.productLocationId()).isEqualTo(LOCATION_ID);
        assertThat(binding.context().sourceProviderId()).isEqualTo("magnit-public-page");
        assertThat(binding.context().fulfillmentContextId()).isEqualTo("992301");
        assertThat(binding.context().locality()).isEqualTo("Краснодар");
        assertThat(binding.mode()).isEqualTo(FulfillmentContextSelectionMode.RESOLVED);
        assertThat(binding.toString()).doesNotContain("45.067057").doesNotContain("38.973527");
    }

    @Test
    void nonResolvedResultsCannotCreateAutomaticBindings() {
        assertThat(MagnitFulfillmentContextBindings.autoResolved(
                        LOCATION_ID,
                        "Москва",
                        MagnitStoreResolver.resolve(MagnitStoreSearchEvidence.empty())))
                .isEmpty();
        assertThat(MagnitFulfillmentContextBindings.autoResolved(
                        LOCATION_ID,
                        "Москва",
                        MagnitStoreResolver.resolve(new MagnitStoreSearchEvidence(
                                List.of(candidate("100", 55.70, 37.60), candidate("200", 55.71, 37.61)),
                                false))))
                .isEmpty();
        assertThat(MagnitFulfillmentContextBindings.autoResolved(
                        LOCATION_ID,
                        "Москва",
                        MagnitStoreResolver.resolve(MagnitStoreSearchEvidence.conflict())))
                .isEmpty();
    }

    @Test
    void explicitManualSelectionUsesTheSameProviderScopedShopCode() {
        var binding = MagnitFulfillmentContextBindings.manual(
                LOCATION_ID,
                "Москва",
                candidate("011830", 55.75, 37.62));

        assertThat(binding.context().sourceProviderId()).isEqualTo("magnit-public-page");
        assertThat(binding.context().fulfillmentContextId()).isEqualTo("011830");
        assertThat(binding.context().locality()).isEqualTo("Москва");
        assertThat(binding.mode()).isEqualTo(FulfillmentContextSelectionMode.MANUAL);
    }

    @Test
    void impossibleResolutionShapesFailAtConstruction() {
        assertThatThrownBy(() -> new MagnitStoreResolution(
                        MagnitStoreResolutionStatus.RESOLVED,
                        java.util.Optional.empty()))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new MagnitStoreResolution(
                        MagnitStoreResolutionStatus.AMBIGUOUS,
                        java.util.Optional.of(candidate("100", 55.7, 37.6))))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private static MagnitStoreCandidate candidate(String code, double latitude, double longitude) {
        return new MagnitStoreCandidate(code, new MagnitGeoPoint(latitude, longitude));
    }
}
