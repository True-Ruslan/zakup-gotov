package io.github.trueruslan.zakupgotov.weeklyplan;

import io.github.trueruslan.zakupgotov.recipe.Recipe;
import io.github.trueruslan.zakupgotov.recipe.RecipeServings;
import java.util.Objects;

public record WeeklyMealOccurrence(
        WeeklyMealOccurrenceId id,
        WeeklyPlanDay day,
        Recipe recipe,
        RecipeServings targetServings) {

    public WeeklyMealOccurrence {
        id = Objects.requireNonNull(id, "id must not be null");
        day = Objects.requireNonNull(day, "day must not be null");
        recipe = Objects.requireNonNull(recipe, "recipe must not be null");
        targetServings = Objects.requireNonNull(targetServings, "targetServings must not be null");
    }
}
