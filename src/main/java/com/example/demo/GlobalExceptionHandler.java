package com.example.demo;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

// Shared error handling for all REST controllers discovered by Spring.
@RestControllerAdvice
public class GlobalExceptionHandler {

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

    public record ApiError(String message, Map<String, String> fieldErrors) {
    }
}
