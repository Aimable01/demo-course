package com.rca.demo_course.exception;

public class InvalidGradeException extends RuntimeException {
    
    public InvalidGradeException(String message) {
        super(message);
    }
    
    public InvalidGradeException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public InvalidGradeException(double score) {
        super("Invalid grade score: " + score + ". Score must be between 0 and 100");
    }
}
