package com.app.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * GlobalExceptionHandler handles exceptions thrown by the application.
 * It provides specific responses for 404 (Not Found) and 403 (Forbidden) errors,
 * as well as a generic handler for all other exceptions.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /** Handles ResourceNotFoundException and returns a 404 response.
     *
     * @param ex the exception thrown
     * @return a ResponseEntity with the error details
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<?> handleNotFound(ResourceNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    /**
     * Handles ForbiddenException and returns a 403 response.
     *
     * @param ex the exception thrown
     * @return a ResponseEntity with the error details
     */
    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<?> handleForbidden(ForbiddenException ex) {
        return buildResponse(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    /**
     * Handles all other exceptions and returns a 500 response.
     *
     * @param ex the exception thrown
     * @return a ResponseEntity with the error details
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGeneric(Exception ex) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
    }

    /**
     * Builds a standardized error response.
     *
     * @param status the HTTP status
     * @param message the error message
     * @return a ResponseEntity containing the error details
     */
    private ResponseEntity<?> buildResponse(HttpStatus status, String message) {
        Map<String, Object> error = new HashMap<>();
        error.put("timestamp", LocalDateTime.now());
        error.put("status", status.value());
        error.put("error", status.getReasonPhrase());
        error.put("message", message);
        return new ResponseEntity<>(error, status);
    }
}
