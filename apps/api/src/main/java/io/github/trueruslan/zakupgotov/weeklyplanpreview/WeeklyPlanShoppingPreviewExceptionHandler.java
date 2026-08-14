package io.github.trueruslan.zakupgotov.weeklyplanpreview;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = WeeklyPlanShoppingPreviewController.class)
public final class WeeklyPlanShoppingPreviewExceptionHandler {

    @ExceptionHandler(InvalidWeeklyPlanShoppingPreviewRequestException.class)
    public ResponseEntity<InvalidWeeklyPlanShoppingPreviewProblem> invalidRequest(
            InvalidWeeklyPlanShoppingPreviewRequestException exception) {
        return problem(exception.errors());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<InvalidWeeklyPlanShoppingPreviewProblem> malformedJson() {
        return problem(List.of(new WeeklyPlanShoppingPreviewValidationError(
                "$request", "malformed JSON request")));
    }

    private static ResponseEntity<InvalidWeeklyPlanShoppingPreviewProblem> problem(
            List<WeeklyPlanShoppingPreviewValidationError> errors) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(InvalidWeeklyPlanShoppingPreviewProblem.of(errors));
    }
}
