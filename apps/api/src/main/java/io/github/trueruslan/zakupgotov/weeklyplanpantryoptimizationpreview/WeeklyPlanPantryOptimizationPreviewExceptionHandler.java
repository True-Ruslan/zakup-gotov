package io.github.trueruslan.zakupgotov.weeklyplanpantryoptimizationpreview;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = WeeklyPlanPantryOptimizationPreviewController.class)
public final class WeeklyPlanPantryOptimizationPreviewExceptionHandler {

    @ExceptionHandler(InvalidWeeklyPlanPantryOptimizationPreviewRequestException.class)
    public ResponseEntity<InvalidWeeklyPlanPantryOptimizationPreviewProblem> invalidRequest(
            InvalidWeeklyPlanPantryOptimizationPreviewRequestException exception) {
        return problem(exception.errors());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<InvalidWeeklyPlanPantryOptimizationPreviewProblem> unreadableRequest() {
        return problem(List.of(new WeeklyPlanPantryOptimizationPreviewValidationError(
                "$request", "malformed JSON request")));
    }

    private static ResponseEntity<InvalidWeeklyPlanPantryOptimizationPreviewProblem> problem(
            List<WeeklyPlanPantryOptimizationPreviewValidationError> errors) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(InvalidWeeklyPlanPantryOptimizationPreviewProblem.of(errors));
    }
}
