package com.crodrigo47.trelloBackend.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.Instant;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException ex) {
        ErrorResponse body = new ErrorResponse(404, "Not Found", ex.getMessage(), Instant.now());
        return ResponseEntity.status(404).body(body);
    }

    @ExceptionHandler(BoardNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleBoardNotFound(BoardNotFoundException ex){
        ErrorResponse body = new ErrorResponse(404, "Not Found", ex.getMessage(), Instant.now());
        return ResponseEntity.status(404).body(body);
    }

    @ExceptionHandler(TaskNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleTaskNotFound(TaskNotFoundException ex){
        ErrorResponse body = new ErrorResponse(404, "Not Found", ex.getMessage(), Instant.now());
        return ResponseEntity.status(404).body(body);
    }

    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<ErrorResponse> handleInvalidPassword(InvalidPasswordException ex){
        ErrorResponse body = new ErrorResponse(401, "Auth Required", ex.getMessage(), Instant.now());
        return ResponseEntity.status(401).body(body);
    }
}
