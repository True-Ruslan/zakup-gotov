package io.github.trueruslan.zakupgotov.weeklyplancomparisonpreview;

import io.github.trueruslan.zakupgotov.preview.ComparisonPreview;
import io.github.trueruslan.zakupgotov.weeklyplanpreview.WeeklyPlanShoppingPreview;
import java.util.Objects;

public record WeeklyPlanComparisonPreview(
        WeeklyPlanShoppingPreview weeklyPlanShoppingPreview,
        ComparisonPreview comparisonPreview) {

    public WeeklyPlanComparisonPreview {
        weeklyPlanShoppingPreview = Objects.requireNonNull(
                weeklyPlanShoppingPreview,
                "weeklyPlanShoppingPreview must not be null");
        comparisonPreview = Objects.requireNonNull(
                comparisonPreview,
                "comparisonPreview must not be null");
    }
}
