package io.github.trueruslan.zakupgotov.preview;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = ComparisonPreviewController.class)
public final class ComparisonPreviewExceptionHandler {

    @ExceptionHandler(InvalidComparisonPreviewRequestException.class)
    public ResponseEntity<InvalidComparisonPreviewProblem> invalidRequest(
            InvalidComparisonPreviewRequestException exception) {
        return problem(exception.errors());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<InvalidComparisonPreviewProblem> malformedJson() {
        return problem(List.of(new ComparisonPreviewValidationError("$request", "malformed JSON request")));
    }

    private static ResponseEntity<InvalidComparisonPreviewProblem> problem(
            List<ComparisonPreviewValidationError> errors) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(InvalidComparisonPreviewProblem.of(errors));
    }
}
