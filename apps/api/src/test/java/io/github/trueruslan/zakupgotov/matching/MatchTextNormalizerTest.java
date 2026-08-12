package io.github.trueruslan.zakupgotov.matching;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class MatchTextNormalizerTest {

    @Test
    void normalizesCompatibilityCaseWhitespacePunctuationAndRussianYo() {
        assertThat(MatchTextNormalizer.normalize("  МОЛОКО\t3,2%  "))
                .isEqualTo("молоко 3 2");
        assertThat(MatchTextNormalizer.normalize("Ёжик ёлка"))
                .isEqualTo("ежик елка");
        assertThat(MatchTextNormalizer.normalize("Молоко－ультра"))
                .isEqualTo("молоко ультра");
        assertThat(MatchTextNormalizer.normalize("ＡＢＣ Молоко"))
                .isEqualTo("abc молоко");
    }

    @Test
    void keepsBaselineNarrowInsteadOfInventingSemanticEquivalence() {
        assertThat(MatchTextNormalizer.normalize("томаты"))
                .isNotEqualTo(MatchTextNormalizer.normalize("помидоры"));
        assertThat(MatchTextNormalizer.normalize("молоко"))
                .isNotEqualTo(MatchTextNormalizer.normalize("молочный"));
        assertThat(MatchTextNormalizer.normalize("молоко ультра"))
                .isNotEqualTo(MatchTextNormalizer.normalize("ультра молоко"));
    }

    @Test
    void rejectsMissingOrContentlessText() {
        assertThatThrownBy(() -> MatchTextNormalizer.normalize(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("raw");
        assertThatThrownBy(() -> MatchTextNormalizer.normalize("--- %%%"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("blank");
    }
}
