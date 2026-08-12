package io.github.trueruslan.zakupgotov.shopping;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class ShoppingList {

    private final ShoppingListId id;
    private final Map<ShoppingItemId, ShoppingItem> items = new LinkedHashMap<>();

    public ShoppingList(ShoppingListId id) {
        this.id = Objects.requireNonNull(id, "id must not be null");
    }

    public ShoppingListId id() {
        return id;
    }

    public List<ShoppingItem> items() {
        return List.copyOf(items.values());
    }

    public void add(ShoppingItem item) {
        Objects.requireNonNull(item, "item must not be null");
        if (items.putIfAbsent(item.id(), item) != null) {
            throw new IllegalArgumentException("duplicate shopping item id: " + item.id().value());
        }
    }

    public void replace(ShoppingItem item) {
        Objects.requireNonNull(item, "item must not be null");
        if (!items.containsKey(item.id())) {
            throw new IllegalArgumentException("unknown shopping item id: " + item.id().value());
        }
        items.put(item.id(), item);
    }

    public void remove(ShoppingItemId itemId) {
        Objects.requireNonNull(itemId, "itemId must not be null");
        if (items.remove(itemId) == null) {
            throw new IllegalArgumentException("unknown shopping item id: " + itemId.value());
        }
    }
}
