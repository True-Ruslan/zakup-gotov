package io.github.trueruslan.zakupgotov.weeklyplanpantrypreview;

import io.github.trueruslan.zakupgotov.pantry.PantryAdjustmentStatus;
import io.github.trueruslan.zakupgotov.shopping.Quantity;
import java.util.Objects;
import java.util.UUID;

public record WeeklyPlanPantryAdjustmentEvidence(
        UUID itemId,
        String requirement,
        Quantity required,
        Quantity pantryUsed,
        Quantity remaining,
        PantryAdjustmentStatus status) {

    public WeeklyPlanPantryAdjustmentEvidence {
        itemId = Objects.requireNonNull(itemId, "itemId must not be null");
        if (requirement == null || requirement.isBlank()) {
            throw new IllegalArgumentException("requirement must not be blank");
        }
        required = Objects.requireNonNull(required, "required must not be null");
        status = Objects.requireNonNull(status, "status must not be null");
    }
}
