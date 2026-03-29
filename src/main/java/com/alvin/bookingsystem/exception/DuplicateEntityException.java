package com.alvin.bookingsystem.exception;

public class DuplicateEntityException extends RuntimeException {
    
    public DuplicateEntityException(String message) {
        super(message);
    }
    
    public DuplicateEntityException(String fieldName, String fieldValue) {
        super(String.format("%s '%s' already exists", fieldName, fieldValue));
    }
    
    public DuplicateEntityException(String message, Throwable cause) {
        super(message, cause);
    }
}
