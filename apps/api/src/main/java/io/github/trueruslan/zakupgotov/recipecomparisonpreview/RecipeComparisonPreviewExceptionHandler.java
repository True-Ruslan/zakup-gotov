package io.github.trueruslan.zakupgotov.recipecomparisonpreview;

import io.github.trueruslan.zakupgotov.preview.InvalidComparisonPreviewProblem;
import io.github.trueruslan.zakupgotov.preview.InvalidComparisonPreviewRequestException;
import io.github.trueruslan.zakupgotov.recipepreview.InvalidRecipeShoppingPreviewProblem;
import io.github.trueruslan.zakupgotov.recipepreview.InvalidRecipeShoppingPreviewRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = RecipeComparisonPreviewController.class)
public final class RecipeComparisonPreviewExceptionHandler {

    @ExceptionHandler(InvalidRecipeShoppingPreviewRequestException.class)
    public ResponseEntity<InvalidRecipeShoppingPreviewProblem> invalidRecipe(
            InvalidRecipeShoppingPreviewRequestException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(InvalidRecipeShoppingPreviewProblem.of(exception.errors()));
    }

    @ExceptionHandler(InvalidComparisonPreviewRequestException.class)
    public ResponseEntity<InvalidComparisonPreviewProblem> invalidComparison(
            InvalidComparisonPreviewRequestException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(InvalidComparisonPreviewProblem.of(exception.errors()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<InvalidRecipeComparisonPreviewProblem> malformedJson() {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(InvalidRecipeComparisonPreviewProblem.malformedJson());
    }
}
