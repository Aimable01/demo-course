package com.rca.demo_course.exception;

public class StudentNotFoundException extends RuntimeException {
    
    public StudentNotFoundException(String message) {
        super(message);
    }
    
    public StudentNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public StudentNotFoundException(Long studentId) {
        super("Student not found with ID: " + studentId);
    }
}
