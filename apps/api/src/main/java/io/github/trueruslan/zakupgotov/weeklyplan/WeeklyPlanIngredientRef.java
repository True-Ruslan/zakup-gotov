package io.github.trueruslan.zakupgotov.weeklyplan;

import io.github.trueruslan.zakupgotov.recipe.RecipeIngredientRef;
import java.util.Objects;

public record WeeklyPlanIngredientRef(
        WeeklyMealOccurrenceId occurrenceId,
        RecipeIngredientRef recipeIngredient) {

    public WeeklyPlanIngredientRef {
        occurrenceId = Objects.requireNonNull(occurrenceId, "occurrenceId must not be null");
        recipeIngredient = Objects.requireNonNull(recipeIngredient, "recipeIngredient must not be null");
    }
}
