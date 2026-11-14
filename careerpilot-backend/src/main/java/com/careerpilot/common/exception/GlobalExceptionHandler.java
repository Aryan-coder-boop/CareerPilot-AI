package com.careerpilot.common.exception;

import com.careerpilot.common.payload.ApiResponse;
import com.careerpilot.common.payload.ErrorDetails;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<ErrorDetails>> handleValidationException(MethodArgumentNotValidException ex) {
        List<String> errors = new ArrayList<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errors.add(fieldError.getField() + ": " + fieldError.getDefaultMessage());
        }

        ErrorDetails errorDetails = new ErrorDetails("Validation failed", errors);
        return new ResponseEntity<>(
                new ApiResponse<>(false, "Invalid input data", errorDetails),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<ErrorDetails>> handleBadCredentials(BadCredentialsException ex) {
        ErrorDetails errorDetails = new ErrorDetails("Authentication error", List.of(ex.getMessage()));
        return new ResponseEntity<>(
                new ApiResponse<>(false, "Invalid email or password", errorDetails),
                HttpStatus.UNAUTHORIZED
        );
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiResponse<ErrorDetails>> handleUnauthorized(UnauthorizedException ex) {
        ErrorDetails errorDetails = new ErrorDetails("Unauthorized access", List.of(ex.getMessage()));
        return new ResponseEntity<>(
                new ApiResponse<>(false, ex.getMessage(), errorDetails),
                HttpStatus.UNAUTHORIZED
        );
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<ErrorDetails>> handleResourceNotFound(ResourceNotFoundException ex) {
        ErrorDetails errorDetails = new ErrorDetails("Resource not found", List.of(ex.getMessage()));
        return new ResponseEntity<>(
                new ApiResponse<>(false, ex.getMessage(), errorDetails),
                HttpStatus.NOT_FOUND
        );
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiResponse<ErrorDetails>> handleBadRequest(BadRequestException ex) {
        ErrorDetails errorDetails = new ErrorDetails("Bad request", List.of(ex.getMessage()));
        return new ResponseEntity<>(
                new ApiResponse<>(false, ex.getMessage(), errorDetails),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<ErrorDetails>> handleGlobalException(Exception ex) {
        ErrorDetails errorDetails = new ErrorDetails("An unexpected server error occurred", List.of(ex.getMessage() != null ? ex.getMessage() : "Unknown error"));
        return new ResponseEntity<>(
                new ApiResponse<>(false, "Internal server error", errorDetails),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }
}

