package io.github.trueruslan.zakupgotov.provider;

public record ProductQuery(String term) {

    public ProductQuery {
        if (term == null || term.isBlank()) {
            throw new IllegalArgumentException("term must not be blank");
        }
    }
}
