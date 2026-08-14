package io.github.trueruslan.zakupgotov.weeklyplancomparisonpreview;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import io.github.trueruslan.zakupgotov.weeklyplanpreview.WeeklyPlanShoppingPreviewRequest;

public record WeeklyPlanComparisonPreviewRequest(
        String locality,
        WeeklyPlanShoppingPreviewRequest weeklyPlan) {

    @JsonAnySetter
    public void rejectUnknownProperty(String property, Object value) {
        throw new IllegalArgumentException("Unknown weekly plan comparison preview property");
    }
}
