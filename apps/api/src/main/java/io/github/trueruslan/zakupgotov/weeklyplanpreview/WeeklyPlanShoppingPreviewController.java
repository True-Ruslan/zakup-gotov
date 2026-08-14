package io.github.trueruslan.zakupgotov.weeklyplanpreview;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/weekly-plan-shopping-previews")
public final class WeeklyPlanShoppingPreviewController {
    private final WeeklyPlanShoppingPreviewService service;

    public WeeklyPlanShoppingPreviewController(WeeklyPlanShoppingPreviewService service) {
        this.service = java.util.Objects.requireNonNull(service, "service must not be null");
    }

    @PostMapping
    public ResponseEntity<WeeklyPlanShoppingPreview> create(
            @RequestBody WeeklyPlanShoppingPreviewRequest request) {
        return ResponseEntity.ok(service.create(request));
    }
}
