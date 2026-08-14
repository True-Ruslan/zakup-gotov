package io.github.trueruslan.zakupgotov.weeklyplanpreview;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import java.util.List;

public record WeeklyPlanShoppingPreviewRequest(
        List<WeeklyPlanShoppingPreviewOccurrenceRequest> occurrences) {

    @JsonAnySetter
    public void rejectUnknownProperty(String property, Object value) {
        throw new IllegalArgumentException("Unknown weekly plan shopping preview property");
    }
}
