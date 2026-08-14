package io.github.trueruslan.zakupgotov.weeklyplanpreview;

import io.github.trueruslan.zakupgotov.recipepreview.InvalidRecipeShoppingPreviewRequestException;
import io.github.trueruslan.zakupgotov.recipepreview.RecipeShoppingPreviewInput;
import io.github.trueruslan.zakupgotov.recipepreview.RecipeShoppingPreviewRequest;
import io.github.trueruslan.zakupgotov.recipepreview.RecipeShoppingPreviewRequestFactory;
import io.github.trueruslan.zakupgotov.weeklyplan.WeeklyMealOccurrence;
import io.github.trueruslan.zakupgotov.weeklyplan.WeeklyPlan;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class WeeklyPlanShoppingPreviewRequestFactory {
    private static final int MAX_OCCURRENCES = 35;

    private final WeeklyPlanShoppingPreviewIdGenerator idGenerator;
    private final RecipeShoppingPreviewRequestFactory recipeFactory;

    public WeeklyPlanShoppingPreviewRequestFactory(
            WeeklyPlanShoppingPreviewIdGenerator idGenerator,
            RecipeShoppingPreviewRequestFactory recipeFactory) {
        this.idGenerator = Objects.requireNonNull(idGenerator, "idGenerator must not be null");
        this.recipeFactory = Objects.requireNonNull(recipeFactory, "recipeFactory must not be null");
    }

    public WeeklyPlanShoppingPreviewInput create(WeeklyPlanShoppingPreviewRequest request) {
        if (request == null) {
            throw invalid(error("$request", "must not be null"));
        }

        var occurrences = request.occurrences();
        if (occurrences == null) {
            throw invalid(error("occurrences", "must not be null"));
        }
        if (occurrences.isEmpty()) {
            throw invalid(error("occurrences", "must contain at least one occurrence"));
        }
        if (occurrences.size() > MAX_OCCURRENCES) {
            throw invalid(error("occurrences", "must not exceed 35 occurrences"));
        }

        var errors = new ArrayList<WeeklyPlanShoppingPreviewValidationError>();
        var recipeInputs = new ArrayList<RecipeShoppingPreviewInput>(occurrences.size());

        for (var index = 0; index < occurrences.size(); index++) {
            var occurrence = occurrences.get(index);
            var prefix = "occurrences[" + index + "]";
            if (occurrence == null) {
                errors.add(error(prefix, "must not be null"));
                recipeInputs.add(null);
                continue;
            }

            if (occurrence.day() == null) {
                errors.add(error(prefix + ".day", "must not be null"));
            }

            var recipe = occurrence.recipe();
            if (recipe == null) {
                validateTargetServingsThroughRecipeBoundary(
                        occurrence.targetServings(), prefix, errors);
                errors.add(error(prefix + ".recipe", "must not be null"));
                recipeInputs.add(null);
                continue;
            }

            try {
                recipeInputs.add(recipeFactory.create(new RecipeShoppingPreviewRequest(
                        recipe.title(),
                        recipe.baseServings(),
                        occurrence.targetServings(),
                        recipe.ingredients())));
            } catch (InvalidRecipeShoppingPreviewRequestException exception) {
                translateRecipeErrors(exception, prefix, errors);
                recipeInputs.add(null);
            }
        }

        if (!errors.isEmpty()) {
            throw new InvalidWeeklyPlanShoppingPreviewRequestException(errors);
        }

        var planId = Objects.requireNonNull(
                idGenerator.nextWeeklyPlanId(),
                "generated WeeklyPlanId must not be null");
        var domainOccurrences = new ArrayList<WeeklyMealOccurrence>(occurrences.size());
        for (var index = 0; index < occurrences.size(); index++) {
            var requestOccurrence = occurrences.get(index);
            var recipeInput = recipeInputs.get(index);
            domainOccurrences.add(new WeeklyMealOccurrence(
                    Objects.requireNonNull(
                            idGenerator.nextOccurrenceId(),
                            "generated WeeklyMealOccurrenceId must not be null"),
                    requestOccurrence.day(),
                    recipeInput.recipe(),
                    recipeInput.targetServings()));
        }

        return new WeeklyPlanShoppingPreviewInput(new WeeklyPlan(planId, domainOccurrences));
    }

    private void validateTargetServingsThroughRecipeBoundary(
            Integer targetServings,
            String prefix,
            List<WeeklyPlanShoppingPreviewValidationError> errors) {
        try {
            recipeFactory.create(new RecipeShoppingPreviewRequest(null, null, targetServings, null));
        } catch (InvalidRecipeShoppingPreviewRequestException exception) {
            exception.errors().stream()
                    .filter(nested -> nested.field().equals("targetServings"))
                    .forEach(nested -> errors.add(error(
                            prefix + ".targetServings",
                            nested.message())));
        }
    }

    private static void translateRecipeErrors(
            InvalidRecipeShoppingPreviewRequestException exception,
            String prefix,
            List<WeeklyPlanShoppingPreviewValidationError> errors) {
        exception.errors().stream()
                .filter(nested -> nested.field().equals("targetServings"))
                .forEach(nested -> errors.add(error(
                        prefix + ".targetServings",
                        nested.message())));

        exception.errors().stream()
                .filter(nested -> !nested.field().equals("targetServings"))
                .forEach(nested -> {
                    var field = nested.field().equals("$request")
                            ? prefix + ".recipe"
                            : prefix + ".recipe." + nested.field();
                    errors.add(error(field, nested.message()));
                });
    }

    private static InvalidWeeklyPlanShoppingPreviewRequestException invalid(
            WeeklyPlanShoppingPreviewValidationError error) {
        return new InvalidWeeklyPlanShoppingPreviewRequestException(List.of(error));
    }

    private static WeeklyPlanShoppingPreviewValidationError error(String field, String message) {
        return new WeeklyPlanShoppingPreviewValidationError(field, message);
    }
}
