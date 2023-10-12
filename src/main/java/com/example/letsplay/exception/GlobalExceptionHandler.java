package com.example.letsplay.exception;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidJwtTokenException.class)
    public ResponseEntity<String> handleInvalidJwtTokenException(InvalidJwtTokenException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid JWT token.");
    }

    @ExceptionHandler(io.jsonwebtoken.ExpiredJwtException.class)
    public ResponseEntity<String> handleExpiredJwtException(io.jsonwebtoken.ExpiredJwtException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("JWT token has expired.");
    }

    @ExceptionHandler(MissingJwtTokenException.class)
    public ResponseEntity<String> handleMissingJwtTokenException(MissingJwtTokenException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("JWT token is missing.");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> customValidationErrorHandling(MethodArgumentNotValidException exception) {
        Map<String, Object> errors = new HashMap<>();
        errors.put("status", HttpStatus.BAD_REQUEST);
        errors.put("errors", exception.getBindingResult()
                                        .getFieldErrors()
                                        .stream()
                                        .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                                        .collect(Collectors.toList()));
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    @ResponseStatus(HttpStatus.CONFLICT)  // 409 Conflict
    @ExceptionHandler(DuplicateUserException.class)
    public Map<String, String> handleDuplicateUserException(DuplicateUserException ex) {
        Map<String, String> errorMap = new HashMap<>();
        errorMap.put("error", ex.getMessage());
        return errorMap;
    }

    @ExceptionHandler(CustomAuthenticationException.class)
    public ResponseEntity<String> handleCustomAuthenticationException(CustomAuthenticationException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.getMessage());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Object> handleResourceNotFoundException(ResourceNotFoundException ex, WebRequest request) {
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("timestamp", new Date());
        responseBody.put("message", ex.getMessage());
        responseBody.put("status", HttpStatus.NOT_FOUND.value());
        responseBody.put("error", "Not Found");

        return new ResponseEntity<>(responseBody, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ResponseEntity<?> handleUsernameNotFoundException(UsernameNotFoundException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", new Date());
        response.put("status", HttpStatus.FORBIDDEN.value());
        response.put("error", "Forbidden");
        response.put("message", ex.getMessage());
        response.put("path", "/products/product"); // Or dynamically fetch the path
        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }
}
