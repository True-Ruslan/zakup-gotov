package io.github.trueruslan.zakupgotov.weeklyplanpantrycomparisonpreview;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = WeeklyPlanPantryComparisonPreviewController.class)
public final class WeeklyPlanPantryComparisonPreviewExceptionHandler {
    @ExceptionHandler(InvalidWeeklyPlanPantryComparisonPreviewRequestException.class)
    public ResponseEntity<InvalidWeeklyPlanPantryComparisonPreviewProblem> invalidRequest(
            InvalidWeeklyPlanPantryComparisonPreviewRequestException exception) {
        return problem(exception.errors());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<InvalidWeeklyPlanPantryComparisonPreviewProblem> unreadableRequest() {
        return problem(List.of(new WeeklyPlanPantryComparisonPreviewValidationError(
                "$request", "malformed JSON request")));
    }

    private static ResponseEntity<InvalidWeeklyPlanPantryComparisonPreviewProblem> problem(
            List<WeeklyPlanPantryComparisonPreviewValidationError> errors) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(InvalidWeeklyPlanPantryComparisonPreviewProblem.of(errors));
    }
}
