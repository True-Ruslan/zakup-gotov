package io.github.trueruslan.zakupgotov.shopping;

import java.util.Objects;

public record ShoppingRequirement(String text) {

    public ShoppingRequirement {
        text = Objects.requireNonNull(text, "text must not be null")
                .strip()
                .replaceAll("\\s+", " ");
        if (text.isBlank()) {
            throw new IllegalArgumentException("text must not be blank");
        }
    }
}
