package io.github.trueruslan.zakupgotov.weeklyplanpreview;

import io.github.trueruslan.zakupgotov.weeklyplan.WeeklyMealOccurrence;
import io.github.trueruslan.zakupgotov.weeklyplan.WeeklyMealOccurrenceId;
import io.github.trueruslan.zakupgotov.weeklyplan.WeeklyPlan;
import io.github.trueruslan.zakupgotov.weeklyplan.WeeklyPlanShoppingListComposer;
import io.github.trueruslan.zakupgotov.weeklyplan.WeeklyPlanShoppingListComposition;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class WeeklyPlanShoppingPreviewService {
    private final WeeklyPlanShoppingPreviewRequestFactory requestFactory;
    private final WeeklyPlanCompositionBoundary compositionBoundary;

    public WeeklyPlanShoppingPreviewService(
            WeeklyPlanShoppingPreviewRequestFactory requestFactory,
            WeeklyPlanShoppingListComposer composer) {
        this(requestFactory, Objects.requireNonNull(composer, "composer must not be null")::compose);
    }

    WeeklyPlanShoppingPreviewService(
            WeeklyPlanShoppingPreviewRequestFactory requestFactory,
            WeeklyPlanCompositionBoundary compositionBoundary) {
        this.requestFactory = Objects.requireNonNull(requestFactory, "requestFactory must not be null");
        this.compositionBoundary = Objects.requireNonNull(
                compositionBoundary, "compositionBoundary must not be null");
    }

    public WeeklyPlanShoppingPreview create(WeeklyPlanShoppingPreviewRequest request) {
        var input = requestFactory.create(request);
        var composition = compositionBoundary.compose(input.weeklyPlan());
        return project(input.weeklyPlan(), composition);
    }

    private static WeeklyPlanShoppingPreview project(
            WeeklyPlan weeklyPlan,
            WeeklyPlanShoppingListComposition composition) {
        Objects.requireNonNull(composition, "composition must not be null");

        var occurrencesById = new LinkedHashMap<WeeklyMealOccurrenceId, WeeklyMealOccurrence>();
        var occurrenceProjections = new ArrayList<WeeklyPlanShoppingPreviewOccurrence>(weeklyPlan.occurrences().size());
        for (var occurrence : weeklyPlan.occurrences()) {
            occurrencesById.put(occurrence.id(), occurrence);
            occurrenceProjections.add(projectOccurrence(occurrence));
        }

        var itemProjections = new ArrayList<WeeklyPlanShoppingPreviewShoppingItem>(
                composition.shoppingList().items().size());
        for (var item : composition.shoppingList().items()) {
            var refs = composition.provenance().get(item.id());
            if (refs == null || refs.isEmpty()) {
                throw new IllegalStateException("weekly shopping item provenance must not be missing or empty");
            }
            var sources = new ArrayList<WeeklyPlanShoppingPreviewSource>(refs.size());
            for (var ref : refs) {
                sources.add(projectSource(ref.occurrenceId(), ref.recipeIngredient(), occurrencesById));
            }
            itemProjections.add(new WeeklyPlanShoppingPreviewShoppingItem(
                    item.id().value(),
                    item.requirement().text(),
                    item.quantity(),
                    sources));
        }

        if (composition.provenance().size() != itemProjections.size()) {
            throw new IllegalStateException("weekly shopping list and provenance cardinality must match");
        }

        return new WeeklyPlanShoppingPreview(
                new WeeklyPlanShoppingPreviewPlan(weeklyPlan.id().value(), occurrenceProjections),
                new WeeklyPlanShoppingPreviewShoppingList(
                        composition.shoppingList().id().value(),
                        itemProjections));
    }

    private static WeeklyPlanShoppingPreviewOccurrence projectOccurrence(WeeklyMealOccurrence occurrence) {
        var recipe = occurrence.recipe();
        var ingredients = recipe.ingredients().stream()
                .map(ingredient -> new WeeklyPlanShoppingPreviewIngredient(
                        ingredient.id().value(),
                        ingredient.requirement().text(),
                        ingredient.quantity()))
                .toList();
        return new WeeklyPlanShoppingPreviewOccurrence(
                occurrence.id().value(),
                occurrence.day(),
                occurrence.targetServings().value(),
                new WeeklyPlanShoppingPreviewRecipe(
                        recipe.id().value(),
                        recipe.title().value(),
                        recipe.baseServings().value(),
                        ingredients));
    }

    private static WeeklyPlanShoppingPreviewSource projectSource(
            WeeklyMealOccurrenceId occurrenceId,
            io.github.trueruslan.zakupgotov.recipe.RecipeIngredientRef recipeIngredient,
            Map<WeeklyMealOccurrenceId, WeeklyMealOccurrence> occurrencesById) {
        var occurrence = occurrencesById.get(occurrenceId);
        if (occurrence == null) {
            throw new IllegalStateException("provenance occurrence must resolve inside weekly plan response");
        }
        if (!occurrence.recipe().id().equals(recipeIngredient.recipeId())) {
            throw new IllegalStateException("provenance recipe must resolve inside its weekly occurrence");
        }
        var ingredientExists = occurrence.recipe().ingredients().stream()
                .anyMatch(ingredient -> ingredient.id().equals(recipeIngredient.ingredientId()));
        if (!ingredientExists) {
            throw new IllegalStateException("provenance ingredient must resolve inside its recipe response");
        }
        return new WeeklyPlanShoppingPreviewSource(
                occurrenceId.value(),
                recipeIngredient.recipeId().value(),
                recipeIngredient.ingredientId().value());
    }
}
