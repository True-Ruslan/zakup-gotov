package io.github.trueruslan.zakupgotov.weeklyplanpantrypreview;

import com.fasterxml.jackson.annotation.JsonAnySetter;

public record WeeklyPlanPantryItemRequest(
        String requirement,
        WeeklyPlanPantryQuantityRequest quantity) {

    @JsonAnySetter
    public void rejectUnknownProperty(String property, Object value) {
        throw new IllegalArgumentException("Unknown weekly plan pantry item property");
    }
}
