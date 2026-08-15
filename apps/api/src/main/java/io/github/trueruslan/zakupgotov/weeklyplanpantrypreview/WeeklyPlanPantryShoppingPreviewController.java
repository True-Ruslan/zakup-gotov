package io.github.trueruslan.zakupgotov.weeklyplanpantrypreview;

import java.util.Objects;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/weekly-plan-pantry-shopping-previews")
public final class WeeklyPlanPantryShoppingPreviewController {

    private final WeeklyPlanPantryShoppingPreviewService service;

    public WeeklyPlanPantryShoppingPreviewController(WeeklyPlanPantryShoppingPreviewService service) {
        this.service = Objects.requireNonNull(service, "service must not be null");
    }

    @PostMapping
    public WeeklyPlanPantryShoppingPreview create(@RequestBody WeeklyPlanPantryShoppingPreviewRequest request) {
        return service.create(request);
    }
}
