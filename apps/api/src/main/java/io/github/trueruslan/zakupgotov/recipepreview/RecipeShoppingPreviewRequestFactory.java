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
    private final RecipeShoppingPreviewIdGenerator idGenerator;

    public RecipeShoppingPreviewRequestFactory(RecipeShoppingPreviewIdGenerator idGenerator) {
        this.idGenerator = Objects.requireNonNull(idGenerator, "idGenerator must not be null");
    }

    public RecipeShoppingPreviewInput create(RecipeShoppingPreviewRequest request) {
        if (request == null) {
            throw new InvalidRecipeShoppingPreviewRequestException(
                    List.of(new RecipeShoppingPreviewValidationError("$request", "must not be null")));
        }

        var errors = new ArrayList<RecipeShoppingPreviewValidationError>();
        var title = request.title() == null
                ? null
                : request.title().strip().replaceAll("\\s+", " ");
        if (title == null || title.isBlank()) {
            errors.add(new RecipeShoppingPreviewValidationError("title", "must not be blank"));
        }

        if (request.baseServings() == null) {
            errors.add(new RecipeShoppingPreviewValidationError("baseServings", "must not be null"));
        } else if (request.baseServings() <= 0) {
            errors.add(new RecipeShoppingPreviewValidationError("baseServings", "must be greater than 0"));
        }

        if (request.targetServings() == null) {
            errors.add(new RecipeShoppingPreviewValidationError("targetServings", "must not be null"));
        } else if (request.targetServings() <= 0) {
            errors.add(new RecipeShoppingPreviewValidationError("targetServings", "must be greater than 0"));
        }

        if (request.ingredients() == null) {
            errors.add(new RecipeShoppingPreviewValidationError("ingredients", "must not be null"));
        } else if (request.ingredients().isEmpty()) {
            errors.add(new RecipeShoppingPreviewValidationError(
                    "ingredients", "must contain at least one ingredient"));
        }

        if (!errors.isEmpty()) {
            throw new InvalidRecipeShoppingPreviewRequestException(errors);
        }

        var recipeId = idGenerator.nextRecipeId();
        var ingredients = new ArrayList<RecipeIngredient>();
        for (var ingredient : request.ingredients()) {
            ingredients.add(new RecipeIngredient(
                    idGenerator.nextIngredientId(),
                    new ShoppingRequirement(ingredient.requirement()),
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
}
