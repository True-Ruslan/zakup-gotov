package io.github.trueruslan.zakupgotov.preview;

import io.github.trueruslan.zakupgotov.location.ProductLocation;
import io.github.trueruslan.zakupgotov.location.ProductLocationId;
import io.github.trueruslan.zakupgotov.shopping.Quantity;
import io.github.trueruslan.zakupgotov.shopping.ShoppingItem;
import io.github.trueruslan.zakupgotov.shopping.ShoppingItemId;
import io.github.trueruslan.zakupgotov.shopping.ShoppingList;
import io.github.trueruslan.zakupgotov.shopping.ShoppingListId;
import io.github.trueruslan.zakupgotov.shopping.ShoppingRequirement;
import java.util.HashSet;
import java.util.UUID;

public final class ComparisonPreviewRequestFactory {

    static final int MAX_LOCALITY_LENGTH = 160;
    static final int MAX_ITEMS = 100;
    static final int MAX_REQUIREMENT_LENGTH = 240;

    private ComparisonPreviewRequestFactory() {}

    public static ComparisonPreviewInput create(ComparisonPreviewRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("request must not be null");
        }

        var locality = normalize(request.locality(), "locality");
        if (locality.length() > MAX_LOCALITY_LENGTH) {
            throw new IllegalArgumentException("locality must not exceed 160 characters");
        }
        if (request.items() == null || request.items().isEmpty()) {
            throw new IllegalArgumentException("items must contain at least one item");
        }
        if (request.items().size() > MAX_ITEMS) {
            throw new IllegalArgumentException("items must not exceed 100 items");
        }

        var shoppingList = new ShoppingList(new ShoppingListId(UUID.randomUUID()));
        var seenIds = new HashSet<UUID>();
        for (var item : request.items()) {
            if (item == null) {
                throw new IllegalArgumentException("item must not be null");
            }
            if (item.id() == null) {
                throw new IllegalArgumentException("item id must not be null");
            }
            if (!seenIds.add(item.id())) {
                throw new IllegalArgumentException("duplicate item id: " + item.id());
            }

            var requirementText = normalize(item.requirement(), "requirement");
            if (requirementText.length() > MAX_REQUIREMENT_LENGTH) {
                throw new IllegalArgumentException("requirement must not exceed 240 characters");
            }
            if (item.amount() == null || item.amount().signum() <= 0) {
                throw new IllegalArgumentException("amount must be greater than 0");
            }
            if (item.unit() == null) {
                throw new IllegalArgumentException("unit must not be null");
            }

            shoppingList.add(new ShoppingItem(
                    new ShoppingItemId(item.id()),
                    new ShoppingRequirement(requirementText),
                    new Quantity(item.amount(), item.unit())));
        }

        var productLocation = ProductLocation.localityOnly(
                new ProductLocationId(UUID.randomUUID()),
                locality);
        return new ComparisonPreviewInput(shoppingList, productLocation);
    }

    private static String normalize(String value, String field) {
        if (value == null) {
            throw new IllegalArgumentException(field + " must not be null");
        }
        var normalized = value.strip().replaceAll("\\s+", " ");
        if (normalized.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return normalized;
    }
}
