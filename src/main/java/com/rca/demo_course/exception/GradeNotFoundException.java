package com.rca.demo_course.exception;

public class GradeNotFoundException extends RuntimeException {

    public GradeNotFoundException(String message) {
        super(message);
    }

    public GradeNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public GradeNotFoundException(Long gradeId) {
        super("Grade not found with ID: " + gradeId);
    }
}
