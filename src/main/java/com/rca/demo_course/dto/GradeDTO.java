package com.rca.demo_course.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Schema(description = "Grade information for a student in a specific course")
public class GradeDTO {

    @Schema(description = "Unique identifier for the grade", example = "1")
    private Long id;

    @NotNull(message = "Student ID is required")
    @Schema(description = "ID of the student", required = true, example = "1")
    private Long studentId;

    @NotNull(message = "Course ID is required")
    @Schema(description = "ID of the course", required = true, example = "1")
    private Long courseId;

    @NotNull(message = "Score is required")
    @DecimalMin(value = "0.0", message = "Score must be at least 0")
    @DecimalMax(value = "100.0", message = "Score must be at most 100")
    @Schema(description = "Numerical score (0-100)", required = true, example = "85.5", minimum = "0.0", maximum = "100.0")
    private BigDecimal score;

    @Schema(description = "Letter grade (A, B, C, D, F)", example = "B")
    private String letterGrade;

    public GradeDTO() {}

    public GradeDTO(Long id, Long studentId, Long courseId, BigDecimal score, String letterGrade) {
        this.id = id;
        this.studentId = studentId;
        this.courseId = courseId;
        this.score = score;
        this.letterGrade = letterGrade;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public BigDecimal getScore() {
        return score;
    }

    public void setScore(BigDecimal score) {
        this.score = score;
    }

    public String getLetterGrade() {
        return letterGrade;
    }

    public void setLetterGrade(String letterGrade) {
        this.letterGrade = letterGrade;
    }
}
