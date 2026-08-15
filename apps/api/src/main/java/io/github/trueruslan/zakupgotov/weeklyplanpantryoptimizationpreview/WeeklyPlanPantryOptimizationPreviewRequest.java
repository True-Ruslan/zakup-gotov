package io.github.trueruslan.zakupgotov.weeklyplanpantryoptimizationpreview;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import io.github.trueruslan.zakupgotov.weeklyplanpantrypreview.WeeklyPlanPantryItemRequest;
import io.github.trueruslan.zakupgotov.weeklyplanpreview.WeeklyPlanShoppingPreviewRequest;
import java.util.List;

public record WeeklyPlanPantryOptimizationPreviewRequest(
        String locality,
        WeeklyPlanShoppingPreviewRequest weeklyPlan,
        List<WeeklyPlanPantryItemRequest> pantry) {

    @JsonAnySetter
    public void rejectUnknownProperty(String property, Object value) {
        throw new IllegalArgumentException("Unknown weekly plan pantry optimization preview property");
    }
}
