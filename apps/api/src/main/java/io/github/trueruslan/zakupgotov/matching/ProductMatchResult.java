package io.github.trueruslan.zakupgotov.matching;

import io.github.trueruslan.zakupgotov.provider.OfferSnapshot;
import java.util.List;
import java.util.Objects;

public record ProductMatchResult(
        ProductMatchStatus status,
        ProductMatchStrength strength,
        ProductMatchReason reason,
        List<OfferSnapshot> candidates) {

    public ProductMatchResult {
        status = Objects.requireNonNull(status, "status must not be null");
        strength = Objects.requireNonNull(strength, "strength must not be null");
        reason = Objects.requireNonNull(reason, "reason must not be null");
        candidates = List.copyOf(Objects.requireNonNull(candidates, "candidates must not be null"));

        switch (status) {
            case MATCHED -> validateMatched(strength, reason, candidates);
            case AMBIGUOUS -> validateAmbiguous(strength, reason, candidates);
            case UNMATCHED -> validateUnmatched(strength, reason, candidates);
        }
    }

    private static void validateMatched(
            ProductMatchStrength strength,
            ProductMatchReason reason,
            List<OfferSnapshot> candidates) {
        if (candidates.size() != 1) {
            throw new IllegalArgumentException("MATCHED result must contain exactly one candidate");
        }
        var valid = switch (strength) {
            case EXACT -> reason == ProductMatchReason.SINGLE_EXACT_TEXT_MATCH;
            case NORMALIZED -> reason == ProductMatchReason.SINGLE_NORMALIZED_TEXT_MATCH;
            case NONE -> false;
        };
        if (!valid) {
            throw new IllegalArgumentException("MATCHED result requires matching strength and single-match reason");
        }
    }

    private static void validateAmbiguous(
            ProductMatchStrength strength,
            ProductMatchReason reason,
            List<OfferSnapshot> candidates) {
        if (candidates.size() < 2) {
            throw new IllegalArgumentException("AMBIGUOUS result must contain at least two candidates");
        }
        var valid = switch (strength) {
            case EXACT -> reason == ProductMatchReason.MULTIPLE_EXACT_TEXT_MATCHES;
            case NORMALIZED -> reason == ProductMatchReason.MULTIPLE_NORMALIZED_TEXT_MATCHES;
            case NONE -> false;
        };
        if (!valid) {
            throw new IllegalArgumentException("AMBIGUOUS result requires matching strength and multiple-match reason");
        }
    }

    private static void validateUnmatched(
            ProductMatchStrength strength,
            ProductMatchReason reason,
            List<OfferSnapshot> candidates) {
        if (!candidates.isEmpty()
                || strength != ProductMatchStrength.NONE
                || reason != ProductMatchReason.NO_TEXT_MATCH) {
            throw new IllegalArgumentException("UNMATCHED result requires no candidates, NONE strength and NO_TEXT_MATCH reason");
        }
    }
}
