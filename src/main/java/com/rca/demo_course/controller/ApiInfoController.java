package com.rca.demo_course.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ApiInfoController {

    @GetMapping("/info")
    public Map<String, Object> getApiInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("name", "Demo Course Management API");
        info.put("version", "1.0.0");
        info.put("description", "A simple educational project for teaching students how tests are done");

        Map<String, String> endpoints = new HashMap<>();
        endpoints.put("Students", "/api/students");
        endpoints.put("Courses", "/api/courses");
        endpoints.put("Grades", "/api/grades");
        endpoints.put("API Info", "/api/info");

        info.put("endpoints", endpoints);

        Map<String, Object> features = new HashMap<>();
        features.put("CRUD Operations", "Create, Read, Update, Delete for all entities");
        features.put("Validation", "Input validation with custom error messages");
        features.put("Exception Handling", "Global exception handling with proper HTTP status codes");
        features.put("Grading System", "Automatic letter grade calculation and GPA computation");
        features.put("Database", "JPA entities with PostgreSQL/H2 support");

        info.put("features", features);

        return info;
    }
}
