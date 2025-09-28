package com.rca.demo_course.controller;

import com.rca.demo_course.domain.Grade;
import com.rca.demo_course.dto.GradeDTO;
import com.rca.demo_course.mapper.GradeMapper;
import com.rca.demo_course.service.GradeService;
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
public class GradeController {

    @Autowired
    private GradeService gradeService;

    @Autowired
    private GradeMapper gradeMapper;

    @PostMapping
    public ResponseEntity<GradeDTO> createGrade(@Valid @RequestBody GradeDTO gradeDTO) {
        Grade grade = gradeMapper.toEntity(gradeDTO);
        Grade createdGrade = gradeService.create(grade);
        GradeDTO responseDTO = gradeMapper.toDTO(createdGrade);
        return new ResponseEntity<>(responseDTO, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<GradeDTO> getGradeById(@PathVariable String id) {
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

    @GetMapping("/student/{studentId}/gpa")
    public ResponseEntity<Double> calculateStudentGPA(@PathVariable String studentId) {
        double gpa = gradeService.calculateGPA(studentId);
        return new ResponseEntity<>(gpa, HttpStatus.OK);
    }

    @PostMapping("/calculate-letter-grade")
    public ResponseEntity<String> calculateLetterGrade(@RequestParam double score) {
        String letterGrade = gradeService.calculateLetterGrade(score);
        return new ResponseEntity<>(letterGrade, HttpStatus.OK);
    }
}
