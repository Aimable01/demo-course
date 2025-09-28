package com.rca.demo_course.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class CourseDTO {

    private Long id;

    @NotBlank(message = "Course name is required")
    @Size(min = 3, max = 100, message = "Course name must be between 3 and 100 characters")
    private String name;

    @NotBlank(message = "Course code is required")
    @Size(min = 2, max = 10, message = "Course code must be between 2 and 10 characters")
    private String code;

    @NotNull(message = "Credits are required")
    @Positive(message = "Credits must be positive")
    private Integer credits;

    public CourseDTO() {}

    public CourseDTO(Long id, String name, String code, Integer credits) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.credits = credits;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Integer getCredits() {
        return credits;
    }

    public void setCredits(Integer credits) {
        this.credits = credits;
    }
}
