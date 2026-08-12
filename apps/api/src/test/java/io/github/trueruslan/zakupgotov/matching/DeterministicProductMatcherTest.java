package io.github.trueruslan.zakupgotov.matching;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.trueruslan.zakupgotov.provider.AcquisitionMode;
import io.github.trueruslan.zakupgotov.provider.AvailabilityStatus;
import io.github.trueruslan.zakupgotov.provider.ObservedOffer;
import io.github.trueruslan.zakupgotov.provider.OfferSnapshot;
import io.github.trueruslan.zakupgotov.provider.OfferSnapshotId;
import io.github.trueruslan.zakupgotov.retailer.RetailerId;
import io.github.trueruslan.zakupgotov.shopping.ShoppingRequirement;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class DeterministicProductMatcherTest {

    private static final MatchScope SCOPE = new MatchScope(RetailerId.PYATEROCHKA, "store-42");
    private static final Instant OBSERVED_AT = Instant.parse("2026-08-12T09:00:00Z");
    private final DeterministicProductMatcher matcher = new DeterministicProductMatcher();

    @Test
    void returnsOneExactMatchBeforeConsideringNormalizedAlternatives() {
        var exact = snapshot("sku-exact", "Молоко 3,2%", "99.90", AvailabilityStatus.UNKNOWN);
        var normalizedOnly = snapshot("sku-normalized", "МОЛОКО 3 2", "89.90", AvailabilityStatus.AVAILABLE);

        var result = matcher.match(
                SCOPE,
                new ShoppingRequirement("Молоко 3,2%"),
                List.of(normalizedOnly, exact));

        assertThat(result.status()).isEqualTo(ProductMatchStatus.MATCHED);
        assertThat(result.strength()).isEqualTo(ProductMatchStrength.EXACT);
        assertThat(result.reason()).isEqualTo(ProductMatchReason.SINGLE_EXACT_TEXT_MATCH);
        assertThat(result.candidates()).containsExactly(exact);
    }

    @Test
    void keepsMultipleExactMatchesAmbiguousWithoutPriceOrAvailabilityTieBreaker() {
        var expensiveAvailable = snapshot("sku-a", "Молоко 3,2%", "119.90", AvailabilityStatus.AVAILABLE);
        var cheapUnknown = snapshot("sku-b", "Молоко 3,2%", "79.90", AvailabilityStatus.UNKNOWN);

        var result = matcher.match(
                SCOPE,
                new ShoppingRequirement("Молоко 3,2%"),
                List.of(expensiveAvailable, cheapUnknown));

        assertThat(result.status()).isEqualTo(ProductMatchStatus.AMBIGUOUS);
        assertThat(result.strength()).isEqualTo(ProductMatchStrength.EXACT);
        assertThat(result.reason()).isEqualTo(ProductMatchReason.MULTIPLE_EXACT_TEXT_MATCHES);
        assertThat(result.candidates()).containsExactly(expensiveAvailable, cheapUnknown);
        assertThatThrownBy(() -> result.candidates().clear())
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void returnsSingleNormalizedMatchWhenThereIsNoExactMatch() {
        var normalized = snapshot("sku-normalized", "МОЛОКО 3-2", "99.90", AvailabilityStatus.AVAILABLE);
        var unrelated = snapshot("sku-bread", "Хлеб ржаной", "59.90", AvailabilityStatus.AVAILABLE);

        var result = matcher.match(
                SCOPE,
                new ShoppingRequirement("Молоко 3,2%"),
                List.of(unrelated, normalized));

        assertThat(result.status()).isEqualTo(ProductMatchStatus.MATCHED);
        assertThat(result.strength()).isEqualTo(ProductMatchStrength.NORMALIZED);
        assertThat(result.reason()).isEqualTo(ProductMatchReason.SINGLE_NORMALIZED_TEXT_MATCH);
        assertThat(result.candidates()).containsExactly(normalized);
    }

    @Test
    void keepsMultipleNormalizedMatchesAmbiguousInInputOrder() {
        var first = snapshot("sku-a", "МОЛОКО 3-2", "99.90", AvailabilityStatus.AVAILABLE);
        var second = snapshot("sku-b", "молоко 3 2", "79.90", AvailabilityStatus.UNAVAILABLE);

        var result = matcher.match(
                SCOPE,
                new ShoppingRequirement("Молоко 3,2%"),
                List.of(first, second));

        assertThat(result.status()).isEqualTo(ProductMatchStatus.AMBIGUOUS);
        assertThat(result.strength()).isEqualTo(ProductMatchStrength.NORMALIZED);
        assertThat(result.reason()).isEqualTo(ProductMatchReason.MULTIPLE_NORMALIZED_TEXT_MATCHES);
        assertThat(result.candidates()).containsExactly(first, second);
    }

    @Test
    void returnsExplicitUnmatchedResultInsteadOfGuessing() {
        var result = matcher.match(
                SCOPE,
                new ShoppingRequirement("Молоко"),
                List.of(
                        snapshot("sku-bread", "Хлеб", "59.90", AvailabilityStatus.AVAILABLE),
                        snapshot("sku-kefir", "Кефир", "89.90", AvailabilityStatus.AVAILABLE)));

        assertThat(result.status()).isEqualTo(ProductMatchStatus.UNMATCHED);
        assertThat(result.strength()).isEqualTo(ProductMatchStrength.NONE);
        assertThat(result.reason()).isEqualTo(ProductMatchReason.NO_TEXT_MATCH);
        assertThat(result.candidates()).isEmpty();
    }

    @Test
    void rejectsCandidateFromAnotherRetailerInsteadOfSilentlyFiltering() {
        var foreign = snapshot(
                RetailerId.PEREKRESTOK,
                "store-42",
                "sku-foreign",
                "Молоко",
                "99.90",
                AvailabilityStatus.AVAILABLE);

        assertThatThrownBy(() -> matcher.match(SCOPE, new ShoppingRequirement("Молоко"), List.of(foreign)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("retailer");
    }

    @Test
    void rejectsCandidateFromAnotherFulfillmentContextInsteadOfSilentlyFiltering() {
        var foreign = snapshot(
                RetailerId.PYATEROCHKA,
                "store-99",
                "sku-foreign",
                "Молоко",
                "99.90",
                AvailabilityStatus.AVAILABLE);

        assertThatThrownBy(() -> matcher.match(SCOPE, new ShoppingRequirement("Молоко"), List.of(foreign)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("fulfillmentContextId");
    }

    @Test
    void rejectsMissingInputsAndNullCandidateElements() {
        var requirement = new ShoppingRequirement("Молоко");
        var candidate = snapshot("sku-a", "Молоко", "99.90", AvailabilityStatus.AVAILABLE);

        assertThatThrownBy(() -> matcher.match(null, requirement, List.of(candidate)))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("scope");
        assertThatThrownBy(() -> matcher.match(SCOPE, null, List.of(candidate)))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("requirement");
        assertThatThrownBy(() -> matcher.match(SCOPE, requirement, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("candidates");
        assertThatThrownBy(() -> matcher.match(SCOPE, requirement, Arrays.asList((OfferSnapshot) null)))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("candidate");
    }

    @Test
    void resultModelRejectsImpossibleStatusStrengthAndCardinalityCombinations() {
        var candidate = snapshot("sku-a", "Молоко", "99.90", AvailabilityStatus.AVAILABLE);

        assertThatThrownBy(() -> new ProductMatchResult(
                        ProductMatchStatus.MATCHED,
                        ProductMatchStrength.EXACT,
                        ProductMatchReason.SINGLE_EXACT_TEXT_MATCH,
                        List.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("MATCHED");

        assertThatThrownBy(() -> new ProductMatchResult(
                        ProductMatchStatus.AMBIGUOUS,
                        ProductMatchStrength.NORMALIZED,
                        ProductMatchReason.MULTIPLE_NORMALIZED_TEXT_MATCHES,
                        List.of(candidate)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("AMBIGUOUS");

        assertThatThrownBy(() -> new ProductMatchResult(
                        ProductMatchStatus.UNMATCHED,
                        ProductMatchStrength.EXACT,
                        ProductMatchReason.NO_TEXT_MATCH,
                        List.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("UNMATCHED");
    }

    private static OfferSnapshot snapshot(
            String sku,
            String productName,
            String price,
            AvailabilityStatus availability) {
        return snapshot(RetailerId.PYATEROCHKA, "store-42", sku, productName, price, availability);
    }

    private static OfferSnapshot snapshot(
            RetailerId retailerId,
            String fulfillmentContextId,
            String sku,
            String productName,
            String price,
            AvailabilityStatus availability) {
        var offer = new ObservedOffer(
                retailerId,
                "fixture-provider",
                AcquisitionMode.DIRECT_API,
                fulfillmentContextId,
                sku,
                productName,
                new BigDecimal(price),
                "RUB",
                availability,
                OBSERVED_AT,
                "fixture://fixture-provider/products/" + sku);
        var id = new OfferSnapshotId(UUID.nameUUIDFromBytes(sku.getBytes(StandardCharsets.UTF_8)));
        return OfferSnapshot.observationOnly(id, offer);
    }
}
