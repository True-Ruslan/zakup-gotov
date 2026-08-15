package io.github.trueruslan.zakupgotov.weeklyplanpantrypreview;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import io.github.trueruslan.zakupgotov.shopping.QuantityUnit;
import java.math.BigDecimal;

public record WeeklyPlanPantryQuantityRequest(BigDecimal amount, QuantityUnit unit) {

    @JsonAnySetter
    public void rejectUnknownProperty(String property, Object value) {
        throw new IllegalArgumentException("Unknown weekly plan pantry quantity property");
    }
}
