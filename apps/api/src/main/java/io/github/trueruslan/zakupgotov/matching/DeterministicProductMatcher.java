package io.github.trueruslan.zakupgotov.matching;

import io.github.trueruslan.zakupgotov.provider.OfferSnapshot;
import io.github.trueruslan.zakupgotov.shopping.ShoppingRequirement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class DeterministicProductMatcher {

    public ProductMatchResult match(
            MatchScope scope,
            ShoppingRequirement requirement,
            List<OfferSnapshot> candidates) {
        Objects.requireNonNull(scope, "scope must not be null");
        Objects.requireNonNull(requirement, "requirement must not be null");
        Objects.requireNonNull(candidates, "candidates must not be null");

        validateScope(scope, candidates);

        var exactMatches = new ArrayList<OfferSnapshot>();
        for (var candidate : candidates) {
            if (candidate.productName().equals(requirement.text())) {
                exactMatches.add(candidate);
            }
        }
        if (exactMatches.size() == 1) {
            return new ProductMatchResult(
                    ProductMatchStatus.MATCHED,
                    ProductMatchStrength.EXACT,
                    ProductMatchReason.SINGLE_EXACT_TEXT_MATCH,
                    exactMatches);
        }
        if (exactMatches.size() > 1) {
            return new ProductMatchResult(
                    ProductMatchStatus.AMBIGUOUS,
                    ProductMatchStrength.EXACT,
                    ProductMatchReason.MULTIPLE_EXACT_TEXT_MATCHES,
                    exactMatches);
        }

        var normalizedRequirement = MatchTextNormalizer.normalize(requirement.text());
        var normalizedMatches = new ArrayList<OfferSnapshot>();
        for (var candidate : candidates) {
            if (MatchTextNormalizer.normalize(candidate.productName()).equals(normalizedRequirement)) {
                normalizedMatches.add(candidate);
            }
        }
        if (normalizedMatches.size() == 1) {
            return new ProductMatchResult(
                    ProductMatchStatus.MATCHED,
                    ProductMatchStrength.NORMALIZED,
                    ProductMatchReason.SINGLE_NORMALIZED_TEXT_MATCH,
                    normalizedMatches);
        }
        if (normalizedMatches.size() > 1) {
            return new ProductMatchResult(
                    ProductMatchStatus.AMBIGUOUS,
                    ProductMatchStrength.NORMALIZED,
                    ProductMatchReason.MULTIPLE_NORMALIZED_TEXT_MATCHES,
                    normalizedMatches);
        }

        return new ProductMatchResult(
                ProductMatchStatus.UNMATCHED,
                ProductMatchStrength.NONE,
                ProductMatchReason.NO_TEXT_MATCH,
                List.of());
    }

    private static void validateScope(MatchScope scope, List<OfferSnapshot> candidates) {
        for (var candidate : candidates) {
            Objects.requireNonNull(candidate, "candidate must not be null");
            if (candidate.retailerId() != scope.retailerId()) {
                throw new IllegalArgumentException(
                        "candidate retailerId must match match scope retailerId");
            }
            if (!candidate.fulfillmentContextId().equals(scope.fulfillmentContextId())) {
                throw new IllegalArgumentException(
                        "candidate fulfillmentContextId must match match scope fulfillmentContextId");
            }
        }
    }
}
