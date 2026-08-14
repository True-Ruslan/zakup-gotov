package io.github.trueruslan.zakupgotov.recipecomparisonpreview;

import io.github.trueruslan.zakupgotov.preview.ComparisonPreview;
import io.github.trueruslan.zakupgotov.preview.ComparisonPreviewItemRequest;
import io.github.trueruslan.zakupgotov.preview.ComparisonPreviewQuantityRequest;
import io.github.trueruslan.zakupgotov.preview.ComparisonPreviewRequest;
import io.github.trueruslan.zakupgotov.preview.ComparisonPreviewService;
import io.github.trueruslan.zakupgotov.recipepreview.RecipeShoppingPreview;
import io.github.trueruslan.zakupgotov.recipepreview.RecipeShoppingPreviewService;
import java.util.List;
import java.util.Objects;

public final class RecipeComparisonPreviewService {

    private final RecipeShoppingPreviewService recipeShoppingPreviewService;
    private final ComparisonPreviewService comparisonPreviewService;

    public RecipeComparisonPreviewService(
            RecipeShoppingPreviewService recipeShoppingPreviewService,
            ComparisonPreviewService comparisonPreviewService) {
        this.recipeShoppingPreviewService = Objects.requireNonNull(
                recipeShoppingPreviewService,
                "recipeShoppingPreviewService must not be null");
        this.comparisonPreviewService = Objects.requireNonNull(
                comparisonPreviewService,
                "comparisonPreviewService must not be null");
    }

    public RecipeComparisonPreview create(RecipeComparisonPreviewRequest request) {
        if (request == null) {
            throw new InvalidRecipeComparisonPreviewRequestException(List.of(
                    new RecipeComparisonPreviewValidationError("$request", "must not be null")));
        }

        var recipeShoppingPreview = recipeShoppingPreviewService.create(request.recipe());
        var comparisonItems = recipeShoppingPreview.shoppingList().items().stream()
                .map(item -> new ComparisonPreviewItemRequest(
                        item.id(),
                        item.requirement(),
                        new ComparisonPreviewQuantityRequest(
                                item.quantity().amount(),
                                item.quantity().unit())))
                .toList();
        var comparisonPreview = comparisonPreviewService.create(
                new ComparisonPreviewRequest(request.locality(), comparisonItems));

        verifyComposition(recipeShoppingPreview, comparisonPreview);
        return new RecipeComparisonPreview(recipeShoppingPreview, comparisonPreview);
    }

    static void verifyComposition(
            RecipeShoppingPreview recipeShoppingPreview,
            ComparisonPreview comparisonPreview) {
        var generated = recipeShoppingPreview.shoppingList().items();
        var compared = comparisonPreview.items();
        if (generated.size() != compared.size()) {
            throw new IllegalStateException("comparison item cardinality drift");
        }

        for (var index = 0; index < generated.size(); index++) {
            var generatedItem = generated.get(index);
            var comparedItem = compared.get(index);
            if (!generatedItem.id().equals(comparedItem.id())) {
                throw new IllegalStateException("comparison item identity/order drift");
            }
            if (!generatedItem.requirement().equals(comparedItem.requirement())) {
                throw new IllegalStateException("comparison item requirement drift");
            }
            if (!generatedItem.quantity().equals(comparedItem.quantity())) {
                throw new IllegalStateException("comparison item quantity drift");
            }
        }
    }
}
