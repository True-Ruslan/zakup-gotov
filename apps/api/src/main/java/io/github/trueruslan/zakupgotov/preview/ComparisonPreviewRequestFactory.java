package io.github.trueruslan.zakupgotov.preview;

import io.github.trueruslan.zakupgotov.location.ProductLocation;
import io.github.trueruslan.zakupgotov.location.ProductLocationId;
import io.github.trueruslan.zakupgotov.shopping.Quantity;
import io.github.trueruslan.zakupgotov.shopping.ShoppingItem;
import io.github.trueruslan.zakupgotov.shopping.ShoppingItemId;
import io.github.trueruslan.zakupgotov.shopping.ShoppingList;
import io.github.trueruslan.zakupgotov.shopping.ShoppingListId;
import io.github.trueruslan.zakupgotov.shopping.ShoppingRequirement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

public final class ComparisonPreviewRequestFactory {

    static final int MAX_LOCALITY_LENGTH = 160;
    static final int MAX_ITEMS = 100;
    static final int MAX_REQUIREMENT_LENGTH = 240;

    private ComparisonPreviewRequestFactory() {}

    public static ComparisonPreviewInput create(ComparisonPreviewRequest request) {
        var errors = validate(request);
        if (!errors.isEmpty()) {
            throw new InvalidComparisonPreviewRequestException(errors);
        }

        var locality = normalize(request.locality());
        var shoppingList = new ShoppingList(new ShoppingListId(UUID.randomUUID()));
        for (var item : request.items()) {
            shoppingList.add(new ShoppingItem(
                    new ShoppingItemId(item.id()),
                    new ShoppingRequirement(normalize(item.requirement())),
                    new Quantity(item.quantity().amount(), item.quantity().unit())));
        }

        var productLocation = ProductLocation.localityOnly(
                new ProductLocationId(UUID.randomUUID()),
                locality);
        return new ComparisonPreviewInput(shoppingList, productLocation);
    }

    private static List<ComparisonPreviewValidationError> validate(ComparisonPreviewRequest request) {
        var errors = new ArrayList<ComparisonPreviewValidationError>();
        if (request == null) {
            errors.add(error("$request", "must not be null"));
            return errors;
        }

        var locality = normalizeNullable(request.locality());
        if (locality == null || locality.isBlank()) {
            errors.add(error("locality", "must not be blank"));
        } else if (locality.length() > MAX_LOCALITY_LENGTH) {
            errors.add(error("locality", "must not exceed 160 characters"));
        }

        if (request.items() == null) {
            errors.add(error("items", "must not be null"));
            return errors;
        }
        if (request.items().isEmpty()) {
            errors.add(error("items", "must contain at least one item"));
        }
        if (request.items().size() > MAX_ITEMS) {
            errors.add(error("items", "must not exceed 100 items"));
        }

        var seenIds = new HashSet<UUID>();
        for (var index = 0; index < request.items().size(); index++) {
            var item = request.items().get(index);
            var prefix = "items[" + index + "]";
            if (item == null) {
                errors.add(error(prefix, "must not be null"));
                continue;
            }

            if (item.id() == null) {
                errors.add(error(prefix + ".id", "must not be null"));
            } else if (!seenIds.add(item.id())) {
                errors.add(error(prefix + ".id", "duplicate item id"));
            }

            var requirement = normalizeNullable(item.requirement());
            if (requirement == null || requirement.isBlank()) {
                errors.add(error(prefix + ".requirement", "must not be blank"));
            } else if (requirement.length() > MAX_REQUIREMENT_LENGTH) {
                errors.add(error(prefix + ".requirement", "must not exceed 240 characters"));
            }

            if (item.quantity() == null) {
                errors.add(error(prefix + ".quantity", "must not be null"));
                continue;
            }
            if (item.quantity().amount() == null) {
                errors.add(error(prefix + ".quantity.amount", "must not be null"));
            } else if (item.quantity().amount().signum() <= 0) {
                errors.add(error(prefix + ".quantity.amount", "must be greater than 0"));
            }
            if (item.quantity().unit() == null) {
                errors.add(error(prefix + ".quantity.unit", "must not be null"));
            }
        }
        return List.copyOf(errors);
    }

    private static ComparisonPreviewValidationError error(String field, String message) {
        return new ComparisonPreviewValidationError(field, message);
    }

    private static String normalize(String value) {
        return value.strip().replaceAll("\\s+", " ");
    }

    private static String normalizeNullable(String value) {
        return value == null ? null : normalize(value);
    }
}
