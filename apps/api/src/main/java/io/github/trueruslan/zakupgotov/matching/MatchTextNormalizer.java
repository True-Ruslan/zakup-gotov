package io.github.trueruslan.zakupgotov.matching;

import java.text.Normalizer;
import java.util.Locale;
import java.util.Objects;

final class MatchTextNormalizer {

    private MatchTextNormalizer() {}

    static String normalize(String raw) {
        Objects.requireNonNull(raw, "raw must not be null");

        var normalized = Normalizer.normalize(raw, Normalizer.Form.NFKC)
                .toLowerCase(Locale.ROOT)
                .replace('ё', 'е');

        var builder = new StringBuilder(normalized.length());
        var previousWasSeparator = true;
        for (var offset = 0; offset < normalized.length(); ) {
            var codePoint = normalized.codePointAt(offset);
            offset += Character.charCount(codePoint);

            if (Character.isLetterOrDigit(codePoint)) {
                builder.appendCodePoint(codePoint);
                previousWasSeparator = false;
            } else if (!previousWasSeparator) {
                builder.append(' ');
                previousWasSeparator = true;
            }
        }

        var result = builder.toString().strip();
        if (result.isBlank()) {
            throw new IllegalArgumentException("normalized text must not be blank");
        }
        return result;
    }
}
