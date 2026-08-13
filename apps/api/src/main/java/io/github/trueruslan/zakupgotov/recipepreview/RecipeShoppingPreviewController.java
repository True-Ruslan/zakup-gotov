package io.github.trueruslan.zakupgotov.recipepreview;

import java.util.List;
import java.util.Objects;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
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

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<InvalidRecipeShoppingPreviewProblem> malformedJson() {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(InvalidRecipeShoppingPreviewProblem.of(
                        List.of(new RecipeShoppingPreviewValidationError(
                                "$request", "malformed JSON request"))));
    }
}
