package io.github.trueruslan.zakupgotov.recipepreview;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = RecipeShoppingPreviewController.class)
public final class RecipeShoppingPreviewExceptionHandler {

    @ExceptionHandler(InvalidRecipeShoppingPreviewRequestException.class)
    public ResponseEntity<InvalidRecipeShoppingPreviewProblem> invalidRequest(
            InvalidRecipeShoppingPreviewRequestException exception) {
        return problem(exception.errors());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<InvalidRecipeShoppingPreviewProblem> malformedJson() {
        return problem(List.of(new RecipeShoppingPreviewValidationError(
                "$request", "malformed JSON request")));
    }

    private static ResponseEntity<InvalidRecipeShoppingPreviewProblem> problem(
            List<RecipeShoppingPreviewValidationError> errors) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(InvalidRecipeShoppingPreviewProblem.of(errors));
    }
}
