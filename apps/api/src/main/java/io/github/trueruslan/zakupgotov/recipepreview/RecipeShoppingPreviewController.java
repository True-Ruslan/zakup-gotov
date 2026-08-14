package io.github.trueruslan.zakupgotov.recipepreview;

import java.util.Objects;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/recipe-shopping-previews")
public final class RecipeShoppingPreviewController {
    private final RecipeShoppingPreviewService service;

    public RecipeShoppingPreviewController(RecipeShoppingPreviewService service) {
        this.service = Objects.requireNonNull(service, "service must not be null");
    }

    @PostMapping
    public RecipeShoppingPreview create(@RequestBody RecipeShoppingPreviewRequest request) {
        return service.create(request);
    }
}
