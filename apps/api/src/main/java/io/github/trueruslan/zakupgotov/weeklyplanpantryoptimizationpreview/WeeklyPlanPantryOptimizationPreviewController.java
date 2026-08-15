package io.github.trueruslan.zakupgotov.weeklyplanpantryoptimizationpreview;

import java.util.Objects;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/weekly-plan-pantry-optimization-previews")
public final class WeeklyPlanPantryOptimizationPreviewController {

    private final WeeklyPlanPantryOptimizationPreviewService service;

    public WeeklyPlanPantryOptimizationPreviewController(WeeklyPlanPantryOptimizationPreviewService service) {
        this.service = Objects.requireNonNull(service, "service must not be null");
    }

    @PostMapping
    public WeeklyPlanPantryOptimizationPreview create(
            @RequestBody WeeklyPlanPantryOptimizationPreviewRequest request) {
        return service.create(request);
    }
}
