package io.github.trueruslan.zakupgotov.weeklyplanpantrycomparisonpreview;

import io.github.trueruslan.zakupgotov.preview.ComparisonPreview;
import io.github.trueruslan.zakupgotov.preview.ComparisonPreviewItemRequest;
import io.github.trueruslan.zakupgotov.preview.ComparisonPreviewQuantityRequest;
import io.github.trueruslan.zakupgotov.preview.ComparisonPreviewRequest;
import io.github.trueruslan.zakupgotov.preview.ComparisonPreviewService;
import io.github.trueruslan.zakupgotov.weeklyplanpantrypreview.InvalidWeeklyPlanPantryShoppingPreviewRequestException;
import io.github.trueruslan.zakupgotov.weeklyplanpantrypreview.WeeklyPlanPantryShoppingPreview;
import io.github.trueruslan.zakupgotov.weeklyplanpantrypreview.WeeklyPlanPantryShoppingPreviewRequest;
import io.github.trueruslan.zakupgotov.weeklyplanpantrypreview.WeeklyPlanPantryShoppingPreviewService;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public final class WeeklyPlanPantryComparisonPreviewService {

    private static final int MAX_LOCALITY_LENGTH = 160;

    private final WeeklyPlanPantryShoppingPreviewService pantryShoppingPreviewService;
    private final Function<ComparisonPreviewRequest, ComparisonPreview> comparisonCreator;

    public WeeklyPlanPantryComparisonPreviewService(
            WeeklyPlanPantryShoppingPreviewService pantryShoppingPreviewService,
            ComparisonPreviewService comparisonPreviewService) {
        this(
                pantryShoppingPreviewService,
                Objects.requireNonNull(comparisonPreviewService, "comparisonPreviewService must not be null")::create);
    }

    WeeklyPlanPantryComparisonPreviewService(
            WeeklyPlanPantryShoppingPreviewService pantryShoppingPreviewService,
            Function<ComparisonPreviewRequest, ComparisonPreview> comparisonCreator) {
        this.pantryShoppingPreviewService = Objects.requireNonNull(
                pantryShoppingPreviewService,
                "pantryShoppingPreviewService must not be null");
        this.comparisonCreator = Objects.requireNonNull(
                comparisonCreator,
                "comparisonCreator must not be null");
    }

    public WeeklyPlanPantryComparisonPreview create(WeeklyPlanPantryComparisonPreviewRequest request) {
        if (request == null) {
            throw invalid("$request", "must not be null");
        }
        var locality = normalizeNullable(request.locality());
        if (locality == null || locality.isBlank()) {
            throw invalid("locality", "must not be blank");
        }
        if (locality.length() > MAX_LOCALITY_LENGTH) {
            throw invalid("locality", "must not exceed 160 characters");
        }

        final WeeklyPlanPantryShoppingPreview pantryShoppingPreview;
        try {
            pantryShoppingPreview = pantryShoppingPreviewService.create(
                    new WeeklyPlanPantryShoppingPreviewRequest(request.weeklyPlan(), request.pantry()));
        } catch (InvalidWeeklyPlanPantryShoppingPreviewRequestException exception) {
            throw new InvalidWeeklyPlanPantryComparisonPreviewRequestException(
                    exception.errors().stream()
                            .map(error -> new WeeklyPlanPantryComparisonPreviewValidationError(
                                    error.field(),
                                    error.message()))
                            .toList());
        }

        var remainingItems = pantryShoppingPreview.remainingShoppingList().items();
        if (remainingItems.isEmpty()) {
            return new WeeklyPlanPantryComparisonPreview(
                    pantryShoppingPreview,
                    WeeklyPlanPantryComparisonOutcome.NO_REMAINING_DEMAND,
                    null);
        }

        var comparisonItems = remainingItems.stream()
                .map(item -> new ComparisonPreviewItemRequest(
                        item.id(),
                        item.requirement(),
                        new ComparisonPreviewQuantityRequest(
                                item.quantity().amount(),
                                item.quantity().unit())))
                .toList();
        var comparisonPreview = Objects.requireNonNull(
                comparisonCreator.apply(new ComparisonPreviewRequest(locality, comparisonItems)),
                "comparisonPreview must not be null");
        verifyComposition(pantryShoppingPreview, comparisonPreview);

        return new WeeklyPlanPantryComparisonPreview(
                pantryShoppingPreview,
                WeeklyPlanPantryComparisonOutcome.COMPARED,
                comparisonPreview);
    }

    static void verifyComposition(
            WeeklyPlanPantryShoppingPreview pantryShoppingPreview,
            ComparisonPreview comparisonPreview) {
        var remaining = pantryShoppingPreview.remainingShoppingList().items();
        var compared = comparisonPreview.items();
        if (remaining.size() != compared.size()) {
            throw new IllegalStateException("comparison item cardinality drift");
        }

        for (var index = 0; index < remaining.size(); index++) {
            var remainingItem = remaining.get(index);
            var comparedItem = compared.get(index);
            if (!remainingItem.id().equals(comparedItem.id())) {
                throw new IllegalStateException("comparison item identity/order drift");
            }
            if (!remainingItem.requirement().equals(comparedItem.requirement())) {
                throw new IllegalStateException("comparison item requirement drift");
            }
            if (!remainingItem.quantity().equals(comparedItem.quantity())) {
                throw new IllegalStateException("comparison item quantity drift");
            }
        }
    }

    private static InvalidWeeklyPlanPantryComparisonPreviewRequestException invalid(
            String field,
            String message) {
        return new InvalidWeeklyPlanPantryComparisonPreviewRequestException(
                List.of(new WeeklyPlanPantryComparisonPreviewValidationError(field, message)));
    }

    private static String normalizeNullable(String value) {
        return value == null ? null : value.strip().replaceAll("\\s+", " ");
    }
}
