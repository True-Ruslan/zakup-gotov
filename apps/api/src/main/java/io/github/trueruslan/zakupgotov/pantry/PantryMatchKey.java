package io.github.trueruslan.zakupgotov.pantry;

import io.github.trueruslan.zakupgotov.shopping.QuantityUnit;
import io.github.trueruslan.zakupgotov.shopping.ShoppingRequirement;
import java.util.Objects;

record PantryMatchKey(ShoppingRequirement requirement, QuantityUnit unit) {

    PantryMatchKey {
        requirement = Objects.requireNonNull(requirement, "requirement must not be null");
        unit = Objects.requireNonNull(unit, "unit must not be null");
    }
}
