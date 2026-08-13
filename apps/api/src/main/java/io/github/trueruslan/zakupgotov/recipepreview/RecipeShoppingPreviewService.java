package io.github.trueruslan.zakupgotov.recipepreview;

import io.github.trueruslan.zakupgotov.recipe.RecipeShoppingListConverter;
import java.util.ArrayList;

public final class RecipeShoppingPreviewService {
    private final RecipeShoppingPreviewRequestFactory factory;
    private final RecipeShoppingListConverter converter;

    public RecipeShoppingPreviewService(RecipeShoppingPreviewRequestFactory factory, RecipeShoppingListConverter converter) {
        this.factory = factory;
        this.converter = converter;
    }

    public RecipeShoppingPreview create(RecipeShoppingPreviewRequest request) {
        var input = factory.create(request);
        var converted = converter.convert(input.recipe(), input.targetServings(), input.shoppingListId());
        var source = input.recipe().ingredients().stream()
                .map(i -> new RecipeShoppingPreviewRecipeIngredient(i.id().value(), i.requirement().text(), i.quantity()))
                .toList();
        var items = new ArrayList<RecipeShoppingPreviewShoppingItem>();
        for (var item : converted.shoppingList().items()) {
            var refs = converted.provenance().get(item.id());
            var ids = refs.stream().map(ref -> ref.ingredientId().value()).toList();
            items.add(new RecipeShoppingPreviewShoppingItem(
                    item.id().value(), item.requirement().text(), item.quantity(), ids));
        }
        return new RecipeShoppingPreview(
                new RecipeShoppingPreviewRecipe(
                        input.recipe().id().value(), input.recipe().title().value(),
                        input.recipe().baseServings().value(), input.targetServings().value(), source),
                new RecipeShoppingPreviewShoppingList(converted.shoppingList().id().value(), items));
    }
}
