package com.rca.demo_course.controller;

import com.rca.demo_course.domain.Grade;
import com.rca.demo_course.dto.GradeDTO;
import com.rca.demo_course.mapper.GradeMapper;
import com.rca.demo_course.service.GradeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/grades")
@CrossOrigin(origins = "*")
@Tag(name = "Grades", description = "API endpoints for managing student grades")
public class GradeController {

    @Autowired
    private GradeService gradeService;

    @Autowired
    private GradeMapper gradeMapper;

    @Operation(
            summary = "Create a new grade",
            description = "Creates a new grade record for a student in a specific course. The system will automatically calculate the letter grade based on the score."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Grade created successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = GradeDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Student or course not found",
                    content = @Content(mediaType = "application/json"))
    })
    @PostMapping
    public ResponseEntity<GradeDTO> createGrade(
            @Parameter(description = "Grade information", required = true)
            @Valid @RequestBody GradeDTO gradeDTO) {
        Grade grade = gradeMapper.toEntity(gradeDTO);
        Grade createdGrade = gradeService.create(grade);
        GradeDTO responseDTO = gradeMapper.toDTO(createdGrade);
        return new ResponseEntity<>(responseDTO, HttpStatus.CREATED);
    }

    @Operation(
            summary = "Get grade by ID",
            description = "Retrieves a specific grade record by its unique identifier"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Grade found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = GradeDTO.class))),
            @ApiResponse(responseCode = "404", description = "Grade not found",
                    content = @Content(mediaType = "application/json"))
    })
    @GetMapping("/{id}")
    public ResponseEntity<GradeDTO> getGradeById(
            @Parameter(description = "Grade ID", required = true, example = "1")
            @PathVariable String id) {
        Grade grade = gradeService.findById(id);
        if (grade != null) {
            GradeDTO gradeDTO = gradeMapper.toDTO(grade);
            return new ResponseEntity<>(gradeDTO, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<GradeDTO>> getGradesByStudentId(@PathVariable String studentId) {
        List<Grade> grades = gradeService.findByStudentId(studentId);
        List<GradeDTO> gradeDTOs = grades.stream()
                .map(gradeMapper::toDTO)
                .collect(Collectors.toList());
        return new ResponseEntity<>(gradeDTOs, HttpStatus.OK);
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<GradeDTO>> getGradesByCourseId(@PathVariable String courseId) {
        List<Grade> grades = gradeService.findByCourseId(courseId);
        List<GradeDTO> gradeDTOs = grades.stream()
                .map(gradeMapper::toDTO)
                .collect(Collectors.toList());
        return new ResponseEntity<>(gradeDTOs, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<GradeDTO> updateGrade(@PathVariable String id, @Valid @RequestBody GradeDTO gradeDTO) {
        gradeDTO.setId(Long.parseLong(id));
        Grade grade = gradeMapper.toEntity(gradeDTO);
        Grade updatedGrade = gradeService.update(grade);
        GradeDTO responseDTO = gradeMapper.toDTO(updatedGrade);
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGrade(@PathVariable String id) {
        gradeService.delete(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Operation(
            summary = "Calculate student GPA",
            description = "Calculates the Grade Point Average (GPA) for a specific student based on all their grades"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "GPA calculated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(type = "number", example = "3.5"))),
            @ApiResponse(responseCode = "404", description = "Student not found",
                    content = @Content(mediaType = "application/json"))
    })
    @GetMapping("/student/{studentId}/gpa")
    public ResponseEntity<Double> calculateStudentGPA(
            @Parameter(description = "Student ID", required = true, example = "1")
            @PathVariable String studentId) {
        double gpa = gradeService.calculateGPA(studentId);
        return new ResponseEntity<>(gpa, HttpStatus.OK);
    }

    @PostMapping("/calculate-letter-grade")
    public ResponseEntity<String> calculateLetterGrade(@RequestParam double score) {
        String letterGrade = gradeService.calculateLetterGrade(score);
        return new ResponseEntity<>(letterGrade, HttpStatus.OK);
    }
}
