package com.rca.demo_course.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import java.util.Map;

public class ValidationErrorResponse extends ErrorResponse {
    
    private Map<String, String> fieldErrors;
    
    public ValidationErrorResponse() {}
    
    public ValidationErrorResponse(String errorCode, String message, LocalDateTime timestamp, Map<String, String> fieldErrors) {
        super(errorCode, message, timestamp);
        this.fieldErrors = fieldErrors;
    }
    
    public Map<String, String> getFieldErrors() {
        return fieldErrors;
    }
    
    public void setFieldErrors(Map<String, String> fieldErrors) {
        this.fieldErrors = fieldErrors;
    }
}
