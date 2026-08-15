package io.github.trueruslan.zakupgotov.weeklyplanpantrypreview;

import io.github.trueruslan.zakupgotov.pantry.PantryAdjustment;
import io.github.trueruslan.zakupgotov.pantry.PantryAdjustmentEvidence;
import io.github.trueruslan.zakupgotov.pantry.PantryItem;
import io.github.trueruslan.zakupgotov.pantry.PantryShoppingListAdjuster;
import io.github.trueruslan.zakupgotov.shopping.Quantity;
import io.github.trueruslan.zakupgotov.shopping.ShoppingItem;
import io.github.trueruslan.zakupgotov.shopping.ShoppingItemId;
import io.github.trueruslan.zakupgotov.shopping.ShoppingList;
import io.github.trueruslan.zakupgotov.shopping.ShoppingListId;
import io.github.trueruslan.zakupgotov.shopping.ShoppingRequirement;
import io.github.trueruslan.zakupgotov.weeklyplanpreview.InvalidWeeklyPlanShoppingPreviewRequestException;
import io.github.trueruslan.zakupgotov.weeklyplanpreview.WeeklyPlanShoppingPreview;
import io.github.trueruslan.zakupgotov.weeklyplanpreview.WeeklyPlanShoppingPreviewService;
import io.github.trueruslan.zakupgotov.weeklyplanpreview.WeeklyPlanShoppingPreviewShoppingItem;
import io.github.trueruslan.zakupgotov.weeklyplanpreview.WeeklyPlanShoppingPreviewShoppingList;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public final class WeeklyPlanPantryShoppingPreviewService {

    private final WeeklyPlanShoppingPreviewService weeklyPlanShoppingPreviewService;
    private final PantryShoppingListAdjuster pantryShoppingListAdjuster;

    public WeeklyPlanPantryShoppingPreviewService(
            WeeklyPlanShoppingPreviewService weeklyPlanShoppingPreviewService) {
        this(weeklyPlanShoppingPreviewService, new PantryShoppingListAdjuster());
    }

    WeeklyPlanPantryShoppingPreviewService(
            WeeklyPlanShoppingPreviewService weeklyPlanShoppingPreviewService,
            PantryShoppingListAdjuster pantryShoppingListAdjuster) {
        this.weeklyPlanShoppingPreviewService = Objects.requireNonNull(
                weeklyPlanShoppingPreviewService,
                "weeklyPlanShoppingPreviewService must not be null");
        this.pantryShoppingListAdjuster = Objects.requireNonNull(
                pantryShoppingListAdjuster,
                "pantryShoppingListAdjuster must not be null");
    }

    public WeeklyPlanPantryShoppingPreview create(WeeklyPlanPantryShoppingPreviewRequest request) {
        if (request == null) {
            throw invalid("$request", "request must not be null");
        }
        if (request.weeklyPlan() == null) {
            throw invalid("weeklyPlan", "weeklyPlan must not be null");
        }
        if (request.pantry() == null) {
            throw invalid("pantry", "pantry must not be null");
        }

        var pantryItems = validatePantry(request.pantry());
        final WeeklyPlanShoppingPreview original;
        try {
            original = weeklyPlanShoppingPreviewService.create(request.weeklyPlan());
        } catch (InvalidWeeklyPlanShoppingPreviewRequestException exception) {
            var errors = exception.errors().stream()
                    .map(error -> new WeeklyPlanPantryShoppingPreviewValidationError(
                            "weeklyPlan." + error.field(),
                            error.message()))
                    .toList();
            throw new InvalidWeeklyPlanPantryShoppingPreviewRequestException(errors);
        }

        var source = toShoppingList(original.shoppingList());
        var adjustment = pantryShoppingListAdjuster.adjust(source, pantryItems);
        return project(original, adjustment);
    }

    private static List<PantryItem> validatePantry(List<WeeklyPlanPantryItemRequest> requests) {
        var result = new ArrayList<PantryItem>(requests.size());
        for (var index = 0; index < requests.size(); index++) {
            var request = requests.get(index);
            var prefix = "pantry[" + index + "]";
            if (request == null) {
                throw invalid(prefix, "pantry item must not be null");
            }
            if (request.requirement() == null || request.requirement().isBlank()) {
                throw invalid(prefix + ".requirement", "requirement must not be blank");
            }
            if (request.quantity() == null) {
                throw invalid(prefix + ".quantity", "quantity must not be null");
            }
            if (request.quantity().amount() == null) {
                throw invalid(prefix + ".quantity.amount", "amount must not be null");
            }
            if (request.quantity().amount().signum() <= 0) {
                throw invalid(prefix + ".quantity.amount", "amount must be positive");
            }
            if (request.quantity().unit() == null) {
                throw invalid(prefix + ".quantity.unit", "unit must not be null");
            }
            result.add(new PantryItem(
                    new ShoppingRequirement(request.requirement()),
                    new Quantity(request.quantity().amount(), request.quantity().unit())));
        }
        return List.copyOf(result);
    }

    private static InvalidWeeklyPlanPantryShoppingPreviewRequestException invalid(
            String field,
            String message) {
        return new InvalidWeeklyPlanPantryShoppingPreviewRequestException(
                List.of(new WeeklyPlanPantryShoppingPreviewValidationError(field, message)));
    }

    private static ShoppingList toShoppingList(WeeklyPlanShoppingPreviewShoppingList projection) {
        var shoppingList = new ShoppingList(new ShoppingListId(projection.id()));
        for (var item : projection.items()) {
            shoppingList.add(new ShoppingItem(
                    new ShoppingItemId(item.id()),
                    new ShoppingRequirement(item.requirement()),
                    item.quantity()));
        }
        return shoppingList;
    }

    private static WeeklyPlanPantryShoppingPreview project(
            WeeklyPlanShoppingPreview original,
            PantryAdjustment adjustment) {
        Objects.requireNonNull(adjustment, "adjustment must not be null");
        var originalItems = original.shoppingList().items();
        var evidence = adjustment.evidence();
        if (evidence.size() != originalItems.size()) {
            throw new IllegalStateException("pantry evidence cardinality drift");
        }
        if (!adjustment.remainingShoppingList().id().value().equals(original.shoppingList().id())) {
            throw new IllegalStateException("remaining shopping list identity drift");
        }

        var originalById = new LinkedHashMap<UUID, WeeklyPlanShoppingPreviewShoppingItem>();
        var evidenceById = new LinkedHashMap<UUID, PantryAdjustmentEvidence>();
        var projectedEvidence = new ArrayList<WeeklyPlanPantryAdjustmentEvidence>(evidence.size());

        for (var index = 0; index < originalItems.size(); index++) {
            var originalItem = originalItems.get(index);
            var itemEvidence = evidence.get(index);
            verifyEvidence(originalItem, itemEvidence);
            if (originalById.put(originalItem.id(), originalItem) != null
                    || evidenceById.put(originalItem.id(), itemEvidence) != null) {
                throw new IllegalStateException("duplicate source shopping item identity");
            }
            projectedEvidence.add(projectEvidence(itemEvidence));
        }

        var remainingItems = new ArrayList<WeeklyPlanShoppingPreviewShoppingItem>();
        var seenRemaining = new LinkedHashMap<UUID, Boolean>();
        var lastOriginalIndex = -1;
        for (var remaining : adjustment.remainingShoppingList().items()) {
            var id = remaining.id().value();
            var originalItem = originalById.get(id);
            var itemEvidence = evidenceById.get(id);
            if (originalItem == null || itemEvidence == null) {
                throw new IllegalStateException("remaining shopping item must resolve to original item");
            }
            if (seenRemaining.put(id, Boolean.TRUE) != null) {
                throw new IllegalStateException("remaining shopping item identity duplicated");
            }
            var originalIndex = indexOf(originalItems, id);
            if (originalIndex <= lastOriginalIndex) {
                throw new IllegalStateException("remaining shopping item order drift");
            }
            lastOriginalIndex = originalIndex;
            verifyRemaining(originalItem, remaining, itemEvidence);
            remainingItems.add(new WeeklyPlanShoppingPreviewShoppingItem(
                    originalItem.id(),
                    originalItem.requirement(),
                    remaining.quantity(),
                    originalItem.sources()));
        }

        verifyNoHiddenLoss(evidence, seenRemaining);

        return new WeeklyPlanPantryShoppingPreview(
                original.weeklyPlan(),
                original.shoppingList(),
                projectedEvidence,
                new WeeklyPlanPantryRemainingShoppingList(
                        original.shoppingList().id(),
                        remainingItems));
    }

    private static void verifyEvidence(
            WeeklyPlanShoppingPreviewShoppingItem original,
            PantryAdjustmentEvidence evidence) {
        if (!evidence.itemId().value().equals(original.id())) {
            throw new IllegalStateException("pantry evidence identity/order drift");
        }
        if (!evidence.requirement().text().equals(original.requirement())) {
            throw new IllegalStateException("pantry evidence requirement drift");
        }
        if (!evidence.required().equals(original.quantity())) {
            throw new IllegalStateException("pantry evidence required quantity drift");
        }
    }

    private static WeeklyPlanPantryAdjustmentEvidence projectEvidence(PantryAdjustmentEvidence evidence) {
        return new WeeklyPlanPantryAdjustmentEvidence(
                evidence.itemId().value(),
                evidence.requirement().text(),
                evidence.required(),
                evidence.pantryUsed().orElse(null),
                evidence.remaining().orElse(null),
                evidence.status());
    }

    private static void verifyRemaining(
            WeeklyPlanShoppingPreviewShoppingItem original,
            ShoppingItem remaining,
            PantryAdjustmentEvidence evidence) {
        if (!remaining.requirement().text().equals(original.requirement())) {
            throw new IllegalStateException("remaining shopping item requirement drift");
        }
        var expected = evidence.remaining().orElseThrow(
                () -> new IllegalStateException("fully covered item must not remain"));
        if (!remaining.quantity().equals(expected)) {
            throw new IllegalStateException("remaining shopping item quantity drift");
        }
    }

    private static void verifyNoHiddenLoss(
            List<PantryAdjustmentEvidence> evidence,
            Map<UUID, Boolean> remainingById) {
        for (var itemEvidence : evidence) {
            var present = remainingById.containsKey(itemEvidence.itemId().value());
            if (itemEvidence.remaining().isPresent() != present) {
                throw new IllegalStateException("pantry adjustment hidden item loss/drift");
            }
        }
    }

    private static int indexOf(
            List<WeeklyPlanShoppingPreviewShoppingItem> items,
            UUID id) {
        for (var index = 0; index < items.size(); index++) {
            if (items.get(index).id().equals(id)) {
                return index;
            }
        }
        return -1;
    }
}
