package io.github.trueruslan.zakupgotov.weeklyplanpantrycomparisonpreview;

import java.util.Objects;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/weekly-plan-pantry-comparison-previews")
public final class WeeklyPlanPantryComparisonPreviewController {

    private final WeeklyPlanPantryComparisonPreviewService service;

    public WeeklyPlanPantryComparisonPreviewController(WeeklyPlanPantryComparisonPreviewService service) {
        this.service = Objects.requireNonNull(service, "service must not be null");
    }

    @PostMapping
    public WeeklyPlanPantryComparisonPreview create(@RequestBody WeeklyPlanPantryComparisonPreviewRequest request) {
        return service.create(request);
    }
}
