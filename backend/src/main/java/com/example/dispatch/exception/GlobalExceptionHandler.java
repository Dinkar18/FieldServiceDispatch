package com.example.dispatch.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(org.springframework.web.bind.MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
            .map(e -> e.getField() + ": " + e.getDefaultMessage())
            .reduce((a,b) -> a + ", " + b)
            .orElse("Validation failed");
        return new ResponseEntity<>(new ErrorResponse("VALIDATION_ERROR", msg), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AiPlanningException.class)
    public ResponseEntity<ErrorResponse> handleAiPlanningException(AiPlanningException ex) {
        ex.printStackTrace();
        ErrorResponse response = new ErrorResponse("AI_PLANNING_FAILED", ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_GATEWAY);
    }

    @ExceptionHandler(ScheduleConflictException.class)
    public ResponseEntity<ErrorResponse> handleScheduleConflictException(ScheduleConflictException ex) {
        ex.printStackTrace();
        ErrorResponse response = new ErrorResponse("SCHEDULE_CONFLICT", ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException ex) {
        ex.printStackTrace();
        String code = "INTERNAL_SERVER_ERROR";
        HttpStatus status = HttpStatus.BAD_REQUEST;

        if (ex.getMessage() != null && ex.getMessage().startsWith("STALE_SCHEDULE")) {
            code = "STALE_SCHEDULE";
            status = HttpStatus.CONFLICT;
        } else if (ex.getMessage() != null && ex.getMessage().startsWith("ASSIGNMENT_CONFLICT")) {
            code = "ASSIGNMENT_CONFLICT";
            status = HttpStatus.UNPROCESSABLE_ENTITY;
        } else if (ex.getMessage() != null && ex.getMessage().startsWith("RESOURCE_NOT_FOUND")) {
            code = "RESOURCE_NOT_FOUND";
            status = HttpStatus.NOT_FOUND;
        }

        ErrorResponse response = new ErrorResponse(code, ex.getMessage());
        return new ResponseEntity<>(response, status);
    }
}
