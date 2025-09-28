package com.rca.demo_course.exception;

public class ValidationException extends RuntimeException {
    
    public ValidationException(String message) {
        super(message);
    }
    
    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public ValidationException(String field, String value) {
        super("Invalid " + field + ": " + value);
    }
}
