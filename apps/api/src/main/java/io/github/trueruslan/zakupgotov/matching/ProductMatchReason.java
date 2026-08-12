package io.github.trueruslan.zakupgotov.matching;

public enum ProductMatchReason {
    SINGLE_EXACT_TEXT_MATCH,
    MULTIPLE_EXACT_TEXT_MATCHES,
    SINGLE_NORMALIZED_TEXT_MATCH,
    MULTIPLE_NORMALIZED_TEXT_MATCHES,
    NO_TEXT_MATCH
}
