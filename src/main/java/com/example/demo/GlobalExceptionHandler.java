package com.example.demo;

import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.ErrorResponse;

// Shared error handling for all REST controllers discovered by Spring.
@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // @Valid throws this exception when a request violates @NotNull, etc.
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException exception) {
        Map<String, String> fields = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors().forEach(error ->
                fields.putIfAbsent(error.getField(), error.getDefaultMessage()));

        return ResponseEntity.badRequest()
                .body(new ApiError("Validation failed", fields));
    }

    // Handles malformed JSON or a missing required request body.
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleUnreadableBody(HttpMessageNotReadableException exception) {
        return ResponseEntity.badRequest()
                .body(new ApiError("Request body is missing or contains invalid JSON", Map.of()));
    }

    @ExceptionHandler(com.example.demo.todo.TodoNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(com.example.demo.todo.TodoNotFoundException exception) {
        return ResponseEntity.status(404).body(new ApiError(exception.getMessage(), Map.of()));
    }

    @ExceptionHandler(org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleTypeMismatch(
            org.springframework.web.method.annotation.MethodArgumentTypeMismatchException exception) {
        return ResponseEntity.badRequest().body(new ApiError("Invalid value for " + exception.getName(), Map.of()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpected(Exception exception) throws Exception {
        // Let Spring's resolvers preserve HTTP statuses and headers (for example, 405 Allow).
        if (exception instanceof ErrorResponse
                || AnnotatedElementUtils.hasAnnotation(exception.getClass(), ResponseStatus.class)) {
            throw exception;
        }

        log.error("Unexpected error while processing request", exception);
        return ResponseEntity.internalServerError()
                .body(new ApiError("An unexpected server error occurred", Map.of()));
    }

    public record ApiError(String message, Map<String, String> fieldErrors) {
    }
}
