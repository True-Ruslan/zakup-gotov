package io.github.trueruslan.zakupgotov.weeklyplanpantrypreview;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import io.github.trueruslan.zakupgotov.weeklyplanpreview.WeeklyPlanShoppingPreviewRequest;
import java.util.List;

public record WeeklyPlanPantryShoppingPreviewRequest(
        WeeklyPlanShoppingPreviewRequest weeklyPlan,
        List<WeeklyPlanPantryItemRequest> pantry) {

    @JsonAnySetter
    public void rejectUnknownProperty(String property, Object value) {
        throw new IllegalArgumentException("Unknown weekly plan pantry shopping preview property");
    }
}
