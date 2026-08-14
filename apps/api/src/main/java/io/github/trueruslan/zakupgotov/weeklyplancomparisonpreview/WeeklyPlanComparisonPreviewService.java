package io.github.trueruslan.zakupgotov.weeklyplancomparisonpreview;

import io.github.trueruslan.zakupgotov.preview.ComparisonPreview;
import io.github.trueruslan.zakupgotov.preview.ComparisonPreviewItemRequest;
import io.github.trueruslan.zakupgotov.preview.ComparisonPreviewQuantityRequest;
import io.github.trueruslan.zakupgotov.preview.ComparisonPreviewRequest;
import io.github.trueruslan.zakupgotov.preview.ComparisonPreviewService;
import io.github.trueruslan.zakupgotov.weeklyplanpreview.WeeklyPlanShoppingPreview;
import io.github.trueruslan.zakupgotov.weeklyplanpreview.WeeklyPlanShoppingPreviewService;
import java.util.Objects;

public final class WeeklyPlanComparisonPreviewService {

    private final WeeklyPlanShoppingPreviewService weeklyPlanShoppingPreviewService;
    private final ComparisonPreviewService comparisonPreviewService;

    public WeeklyPlanComparisonPreviewService(
            WeeklyPlanShoppingPreviewService weeklyPlanShoppingPreviewService,
            ComparisonPreviewService comparisonPreviewService) {
        this.weeklyPlanShoppingPreviewService = Objects.requireNonNull(
                weeklyPlanShoppingPreviewService,
                "weeklyPlanShoppingPreviewService must not be null");
        this.comparisonPreviewService = Objects.requireNonNull(
                comparisonPreviewService,
                "comparisonPreviewService must not be null");
    }

    public WeeklyPlanComparisonPreview create(WeeklyPlanComparisonPreviewRequest request) {
        Objects.requireNonNull(request, "request must not be null");

        var weeklyPlanShoppingPreview = weeklyPlanShoppingPreviewService.create(request.weeklyPlan());
        var comparisonItems = weeklyPlanShoppingPreview.shoppingList().items().stream()
                .map(item -> new ComparisonPreviewItemRequest(
                        item.id(),
                        item.requirement(),
                        new ComparisonPreviewQuantityRequest(
                                item.quantity().amount(),
                                item.quantity().unit())))
                .toList();
        var comparisonPreview = comparisonPreviewService.create(
                new ComparisonPreviewRequest(request.locality(), comparisonItems));

        verifyComposition(weeklyPlanShoppingPreview, comparisonPreview);
        return new WeeklyPlanComparisonPreview(weeklyPlanShoppingPreview, comparisonPreview);
    }

    static void verifyComposition(
            WeeklyPlanShoppingPreview weeklyPlanShoppingPreview,
            ComparisonPreview comparisonPreview) {
        var generated = weeklyPlanShoppingPreview.shoppingList().items();
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
