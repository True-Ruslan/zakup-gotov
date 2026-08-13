package io.github.trueruslan.zakupgotov.recipe;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class RecipeValueObjectsTest {

    @Test
    void preservesRecipeIdentity() {
        var value = UUID.fromString("5d3fa22c-014f-4b08-b5b3-4f759be8f920");

        assertThat(new RecipeId(value).value()).isEqualTo(value);
    }

    @Test
    void rejectsMissingRecipeIdentity() {
        assertThatThrownBy(() -> new RecipeId(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("value");
    }

    @Test
    void preservesIngredientIdentity() {
        var value = UUID.fromString("81d23cd8-cd6f-4692-8a0f-a49e05c779cc");

        assertThat(new RecipeIngredientId(value).value()).isEqualTo(value);
    }

    @Test
    void rejectsMissingIngredientIdentity() {
        assertThatThrownBy(() -> new RecipeIngredientId(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("value");
    }

    @Test
    void normalizesRecipeTitleWhitespaceWithoutChangingCaseOrPunctuation() {
        assertThat(new RecipeTitle("  Pasta   Carbonara!  ").value())
                .isEqualTo("Pasta Carbonara!");
    }

    @Test
    void rejectsBlankOrMissingRecipeTitle() {
        assertThatThrownBy(() -> new RecipeTitle("   \t  "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("value");
        assertThatThrownBy(() -> new RecipeTitle(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("value");
    }

    @Test
    void acceptsPositiveIntegerServings() {
        assertThat(new RecipeServings(3).value()).isEqualTo(3);
    }

    @Test
    void rejectsNonPositiveServings() {
        assertThatThrownBy(() -> new RecipeServings(0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("positive");
        assertThatThrownBy(() -> new RecipeServings(-1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("positive");
    }
}
