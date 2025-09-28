package com.rca.demo_course.exception;

public class CourseNotFoundException extends RuntimeException {
    
    public CourseNotFoundException(String message) {
        super(message);
    }
    
    public CourseNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public CourseNotFoundException(Long courseId) {
        super("Course not found with ID: " + courseId);
    }
}
