package io.github.trueruslan.zakupgotov.weeklyplancomparisonpreview;

import java.util.Objects;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/weekly-plan-comparison-previews")
public final class WeeklyPlanComparisonPreviewController {

    private final WeeklyPlanComparisonPreviewService service;

    public WeeklyPlanComparisonPreviewController(WeeklyPlanComparisonPreviewService service) {
        this.service = Objects.requireNonNull(service, "service must not be null");
    }

    @PostMapping
    public WeeklyPlanComparisonPreview create(@RequestBody WeeklyPlanComparisonPreviewRequest request) {
        return service.create(request);
    }
}
