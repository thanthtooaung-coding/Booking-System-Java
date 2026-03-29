package com.alvin.bookingsystem.exception;

import com.alvin.bookingsystem.dto.response.ApiResponse;
import com.alvin.bookingsystem.exception.CustomException;
import com.alvin.bookingsystem.exception.DuplicateEntityException;
import com.alvin.bookingsystem.exception.UnauthorizedException;
import com.alvin.bookingsystem.util.ApiResponseUtil;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiResponse> handleEntityNotFoundException(
            EntityNotFoundException ex,
            HttpServletRequest request) {
        log.error("Entity not found: {}", ex.getMessage());
        ApiResponse response = ApiResponseUtil.error(
                ex.getMessage(),
                HttpStatus.NOT_FOUND,
                request
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        log.error("Validation failed: {}", errors);
        ApiResponse response = ApiResponseUtil.error(
                "Validation failed",
                HttpStatus.BAD_REQUEST,
                errors,
                request
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse> handleConstraintViolationException(
            ConstraintViolationException ex,
            HttpServletRequest request) {
        Map<String, String> errors = new HashMap<>();
        ex.getConstraintViolations().forEach(violation -> {
            String fieldName = violation.getPropertyPath().toString();
            String errorMessage = violation.getMessage();
            errors.put(fieldName, errorMessage);
        });
        log.error("Constraint violation: {}", errors);
        ApiResponse response = ApiResponseUtil.error(
                "Validation failed",
                HttpStatus.BAD_REQUEST,
                errors,
                request
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ex,
            HttpServletRequest request) {
        log.error("Invalid request body: {}", ex.getMessage());
        String message = "Invalid request body. Please check your JSON format.";
        if (ex.getMessage() != null && ex.getMessage().contains("JSON parse error")) {
            message = "Invalid JSON format: " + ex.getMessage();
        }
        ApiResponse response = ApiResponseUtil.error(
                message,
                HttpStatus.BAD_REQUEST,
                request
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse> handleIllegalArgumentException(
            IllegalArgumentException ex,
            HttpServletRequest request) {
        log.error("Illegal argument: {}", ex.getMessage());
        ApiResponse response = ApiResponseUtil.error(
                ex.getMessage(),
                HttpStatus.BAD_REQUEST,
                request
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse> handleNoResourceFoundException(
            NoResourceFoundException ex,
            HttpServletRequest request) {
        log.warn("Resource not found: {} - {}", request.getRequestURI(), ex.getMessage());
        ApiResponse response = ApiResponseUtil.error(
                "Resource not found: " + request.getRequestURI(),
                HttpStatus.NOT_FOUND,
                request
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(DuplicateEntityException.class)
    public ResponseEntity<ApiResponse> handleDuplicateEntityException(
            DuplicateEntityException ex,
            HttpServletRequest request) {
        log.error("Duplicate entity: {}", ex.getMessage());
        ApiResponse response = ApiResponseUtil.error(
                ex.getMessage(),
                HttpStatus.CONFLICT,
                request
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiResponse> handleUnauthorizedException(
            UnauthorizedException ex,
            HttpServletRequest request) {
        log.error("Unauthorized: {}", ex.getMessage());
        ApiResponse response = ApiResponseUtil.error(
                ex.getMessage(),
                HttpStatus.UNAUTHORIZED,
                request
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponse> handleCustomException(
            CustomException ex,
            HttpServletRequest request) {
        log.error("Custom exception: {}", ex.getMessage());
        ApiResponse response = ApiResponseUtil.error(
                ex.getMessage(),
                HttpStatus.BAD_REQUEST,
                request
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse> handleGenericException(
            Exception ex,
            HttpServletRequest request) {
        log.error("Unexpected error occurred: ", ex);
        ApiResponse response = ApiResponseUtil.error(
                "An unexpected error occurred. Please try again later.",
                HttpStatus.INTERNAL_SERVER_ERROR,
                request
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
