package io.github.trueruslan.zakupgotov.weeklyplancomparisonpreview;

import io.github.trueruslan.zakupgotov.preview.InvalidComparisonPreviewProblem;
import io.github.trueruslan.zakupgotov.preview.InvalidComparisonPreviewRequestException;
import io.github.trueruslan.zakupgotov.weeklyplanpreview.InvalidWeeklyPlanShoppingPreviewProblem;
import io.github.trueruslan.zakupgotov.weeklyplanpreview.InvalidWeeklyPlanShoppingPreviewRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = WeeklyPlanComparisonPreviewController.class)
public final class WeeklyPlanComparisonPreviewExceptionHandler {

    @ExceptionHandler(InvalidWeeklyPlanShoppingPreviewRequestException.class)
    public ResponseEntity<InvalidWeeklyPlanShoppingPreviewProblem> invalidWeeklyPlan(
            InvalidWeeklyPlanShoppingPreviewRequestException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(InvalidWeeklyPlanShoppingPreviewProblem.of(exception.errors()));
    }

    @ExceptionHandler(InvalidComparisonPreviewRequestException.class)
    public ResponseEntity<InvalidComparisonPreviewProblem> invalidComparison(
            InvalidComparisonPreviewRequestException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(InvalidComparisonPreviewProblem.of(exception.errors()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<InvalidWeeklyPlanComparisonPreviewProblem> malformedJson() {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(InvalidWeeklyPlanComparisonPreviewProblem.malformedJson());
    }
}
