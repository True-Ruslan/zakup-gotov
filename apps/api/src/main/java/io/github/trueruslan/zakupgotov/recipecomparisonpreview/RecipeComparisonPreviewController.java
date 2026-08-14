package io.github.trueruslan.zakupgotov.recipecomparisonpreview;

import java.util.Objects;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/recipe-comparison-previews")
public final class RecipeComparisonPreviewController {

    private final RecipeComparisonPreviewService service;

    public RecipeComparisonPreviewController(RecipeComparisonPreviewService service) {
        this.service = Objects.requireNonNull(service, "service must not be null");
    }

    @PostMapping
    public RecipeComparisonPreview create(@RequestBody RecipeComparisonPreviewRequest request) {
        return service.create(request);
    }
}
