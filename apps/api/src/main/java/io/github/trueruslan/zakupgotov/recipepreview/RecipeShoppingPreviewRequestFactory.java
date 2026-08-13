package io.github.trueruslan.zakupgotov.recipepreview;

import io.github.trueruslan.zakupgotov.recipe.Recipe;
import io.github.trueruslan.zakupgotov.recipe.RecipeIngredient;
import io.github.trueruslan.zakupgotov.recipe.RecipeServings;
import io.github.trueruslan.zakupgotov.recipe.RecipeTitle;
import io.github.trueruslan.zakupgotov.shopping.Quantity;
import io.github.trueruslan.zakupgotov.shopping.ShoppingRequirement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class RecipeShoppingPreviewRequestFactory {
    private static final int MAX_TEXT_LENGTH = 240;
    private static final int MAX_INGREDIENTS = 100;

    private final RecipeShoppingPreviewIdGenerator idGenerator;

    public RecipeShoppingPreviewRequestFactory(RecipeShoppingPreviewIdGenerator idGenerator) {
        this.idGenerator = Objects.requireNonNull(idGenerator, "idGenerator must not be null");
    }

    public RecipeShoppingPreviewInput create(RecipeShoppingPreviewRequest request) {
        if (request == null) {
            throw new InvalidRecipeShoppingPreviewRequestException(
                    List.of(error("$request", "must not be null")));
        }

        var errors = new ArrayList<RecipeShoppingPreviewValidationError>();
        var title = normalizeNullable(request.title());
        validateText(title, "title", errors);
        validateServings(request.baseServings(), "baseServings", errors);
        validateServings(request.targetServings(), "targetServings", errors);

        var normalizedRequirements = new ArrayList<String>();
        if (request.ingredients() == null) {
            errors.add(error("ingredients", "must not be null"));
        } else {
            if (request.ingredients().isEmpty()) {
                errors.add(error("ingredients", "must contain at least one ingredient"));
            }
            if (request.ingredients().size() > MAX_INGREDIENTS) {
                errors.add(error("ingredients", "must not exceed 100 ingredients"));
            }
            validateIngredients(request.ingredients(), normalizedRequirements, errors);
        }

        if (!errors.isEmpty()) {
            throw new InvalidRecipeShoppingPreviewRequestException(errors);
        }

        var recipeId = idGenerator.nextRecipeId();
        var ingredients = new ArrayList<RecipeIngredient>();
        for (var index = 0; index < request.ingredients().size(); index++) {
            var ingredient = request.ingredients().get(index);
            ingredients.add(new RecipeIngredient(
                    idGenerator.nextIngredientId(),
                    new ShoppingRequirement(normalizedRequirements.get(index)),
                    new Quantity(ingredient.quantity().amount(), ingredient.quantity().unit())));
        }

        var recipe = new Recipe(
                recipeId,
                new RecipeTitle(title),
                new RecipeServings(request.baseServings()),
                ingredients);
        var shoppingListId = idGenerator.nextShoppingListId();
        return new RecipeShoppingPreviewInput(
                recipe,
                new RecipeServings(request.targetServings()),
                shoppingListId);
    }

    private static void validateIngredients(
            List<RecipeShoppingPreviewIngredientRequest> ingredients,
            List<String> normalizedRequirements,
            List<RecipeShoppingPreviewValidationError> errors) {
        for (var index = 0; index < ingredients.size(); index++) {
            var ingredient = ingredients.get(index);
            var prefix = "ingredients[" + index + "]";
            if (ingredient == null) {
                normalizedRequirements.add(null);
                errors.add(error(prefix, "must not be null"));
                continue;
            }

            var requirement = normalizeNullable(ingredient.requirement());
            normalizedRequirements.add(requirement);
            validateText(requirement, prefix + ".requirement", errors);

            var quantity = ingredient.quantity();
            if (quantity == null) {
                errors.ad(error(prefix + ".quantity", "must not be null"));
                continue;
            }
            if (quantity.amount() == null) {
                errors.add(error(prefix + ".quantity.amount", "must not be null"));
            } else if (quantity.amount().signum() <= 0) {
                errors.ad(error(prefix + ".quantity.amount", "must be greater than 0"));
            }
            if (quantity.unit() == null) {
                errors.add(error(prefix + ".quantity.unit", "must not be null"));
            }
        }
    }

    private static void validateText(
            String value,
            String field,
            List<RecipeShoppingPreviewValidationError> errors) {
        if (value == null || value.isBlank()) {
            errors.add(error(field, "must not be blank"));
        } else if (value.length() > MAX_TEXT_LENGTH) {
            errors.add(error(field, "must not exceed 240 characters"));
        }
    }

    private static void validateServings(
            Integer value,
            String field,
            List<RecipeShoppingPreviewValidationError> errors) {
        if (value == null) {
            errors.add(error(field, "must not be null"));
        } else if (value <= 0) {
            errors.add(error(field, "must be greater than 0"));
        }
    }

    private static String normalizeNullable(String value) {
        return value == null ? null : value.strip().replaceAll("\\s+", " ");
    }

    private static RecipeShoppingPreviewValidationError error(String field, String message) {
        return new RecipeShoppingPreviewValidationError(field, message);
    }
}
