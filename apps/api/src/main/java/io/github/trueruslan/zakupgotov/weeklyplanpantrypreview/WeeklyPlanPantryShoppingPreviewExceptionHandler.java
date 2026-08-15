package io.github.trueruslan.zakupgotov.weeklyplanpantrypreview;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = WeeklyPlanPantryShoppingPreviewController.class)
public final class WeeklyPlanPantryShoppingPreviewExceptionHandler {

    @ExceptionHandler(InvalidWeeklyPlanPantryShoppingPreviewRequestException.class)
    public ResponseEntity<InvalidWeeklyPlanPantryShoppingPreviewProblem> invalidRequest(
            InvalidWeeklyPlanPantryShoppingPreviewRequestException exception) {
        return problem(exception.errors());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<InvalidWeeklyPlanPantryShoppingPreviewProblem> malformedJson() {
        return problem(List.of(new WeeklyPlanPantryShoppingPreviewValidationError(
                "$request", "malformed JSON request")));
    }

    private static ResponseEntity<InvalidWeeklyPlanPantryShoppingPreviewProblem> problem(
            List<WeeklyPlanPantryShoppingPreviewValidationError> errors) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(InvalidWeeklyPlanPantryShoppingPreviewProblem.of(errors));
    }
}
