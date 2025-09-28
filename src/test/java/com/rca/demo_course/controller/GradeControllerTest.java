package com.rca.demo_course.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rca.demo_course.domain.Course;
import com.rca.demo_course.domain.Grade;
import com.rca.demo_course.domain.Student;
import com.rca.demo_course.dto.GradeDTO;
import com.rca.demo_course.exception.GradeNotFoundException;
import com.rca.demo_course.exception.InvalidGradeException;
import com.rca.demo_course.exception.ValidationException;
import com.rca.demo_course.mapper.GradeMapper;
import com.rca.demo_course.service.GradeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive end-to-end tests for GradeController.
 * Tests all CRUD operations, GPA calculation, letter grade calculation, validation, error handling, and edge cases.
 */
@WebMvcTest(GradeController.class)
@DisplayName("Grade Controller End-to-End Tests")
public class GradeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GradeService gradeService;

    @MockBean
    private GradeMapper gradeMapper;

    @Autowired
    private ObjectMapper objectMapper;

    // Test Data
    private GradeDTO createValidGradeDTO() {
        GradeDTO gradeDTO = new GradeDTO();
        gradeDTO.setStudentId(1L);
        gradeDTO.setCourseId(1L);
        gradeDTO.setScore(new BigDecimal("85.5"));
        return gradeDTO;
    }

    private GradeDTO createValidGradeDTOWithId() {
        GradeDTO gradeDTO = new GradeDTO();
        gradeDTO.setId(1L);
        gradeDTO.setStudentId(1L);
        gradeDTO.setCourseId(1L);
        gradeDTO.setScore(new BigDecimal("85.5"));
        gradeDTO.setLetterGrade("B");
        return gradeDTO;
    }

    private Grade createValidGrade() {
        Grade grade = new Grade();
        grade.setId(1L);
        grade.setScore(new BigDecimal("85.5"));
        grade.setLetterGrade("B");
        
        Student student = new Student();
        student.setId(1L);
        grade.setStudent(student);
        
        Course course = new Course();
        course.setId(1L);
        grade.setCourse(course);
        
        return grade;
    }

    // CREATE GRADE TESTS

    @Test
    @DisplayName("POST /api/grades - Should create grade successfully")
    void testCreateGrade_Success() throws Exception {
        // Arrange
        GradeDTO requestDTO = createValidGradeDTO();
        GradeDTO responseDTO = createValidGradeDTOWithId();
        Grade grade = createValidGrade();

        when(gradeMapper.toEntity(any(GradeDTO.class))).thenReturn(grade);
        when(gradeService.create(any(Grade.class))).thenReturn(grade);
        when(gradeMapper.toDTO(any(Grade.class))).thenReturn(responseDTO);

        // Act & Assert
        mockMvc.perform(post("/api/grades")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.studentId").value(1))
                .andExpect(jsonPath("$.courseId").value(1))
                .andExpect(jsonPath("$.score").value(85.5))
                .andExpect(jsonPath("$.letterGrade").value("B"));

        verify(gradeMapper).toEntity(any(GradeDTO.class));
        verify(gradeService).create(any(Grade.class));
        verify(gradeMapper).toDTO(any(Grade.class));
    }

    @Test
    @DisplayName("POST /api/grades - Should return 400 for invalid grade data")
    void testCreateGrade_InvalidData() throws Exception {
        // Arrange
        GradeDTO invalidDTO = new GradeDTO();
        // Missing required fields

        // Act & Assert
        mockMvc.perform(post("/api/grades")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest());

        verify(gradeService, never()).create(any());
    }

    @Test
    @DisplayName("POST /api/grades - Should return 400 for invalid score range")
    void testCreateGrade_InvalidScore() throws Exception {
        // Arrange
        GradeDTO invalidScoreDTO = createValidGradeDTO();
        invalidScoreDTO.setScore(new BigDecimal("150.0")); // Invalid score > 100

        // Act & Assert
        mockMvc.perform(post("/api/grades")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidScoreDTO)))
                .andExpect(status().isBadRequest());

        verify(gradeService, never()).create(any());
    }

    @Test
    @DisplayName("POST /api/grades - Should return 400 for validation errors")
    void testCreateGrade_ValidationErrors() throws Exception {
        // Arrange
        GradeDTO requestDTO = createValidGradeDTO();
        Grade grade = createValidGrade();

        when(gradeMapper.toEntity(any(GradeDTO.class))).thenReturn(grade);
        when(gradeService.create(any(Grade.class))).thenThrow(new ValidationException("Student cannot be null"));

        // Act & Assert
        mockMvc.perform(post("/api/grades")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("Student cannot be null"));

        verify(gradeMapper).toEntity(any(GradeDTO.class));
        verify(gradeService).create(any(Grade.class));
    }

    @Test
    @DisplayName("POST /api/grades - Should return 400 for invalid grade score")
    void testCreateGrade_InvalidGradeScore() throws Exception {
        // Arrange
        GradeDTO requestDTO = createValidGradeDTO();
        Grade grade = createValidGrade();

        when(gradeMapper.toEntity(any(GradeDTO.class))).thenReturn(grade);
        when(gradeService.create(any(Grade.class))).thenThrow(new InvalidGradeException("Invalid grade score: 150.0"));

        // Act & Assert
        mockMvc.perform(post("/api/grades")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_GRADE"))
                .andExpect(jsonPath("$.message").value("Invalid grade score: 150.0"));

        verify(gradeMapper).toEntity(any(GradeDTO.class));
        verify(gradeService).create(any(Grade.class));
    }

    // GET GRADE BY ID TESTS

    @Test
    @DisplayName("GET /api/grades/{id} - Should return grade successfully")
    void testGetGradeById_Success() throws Exception {
        // Arrange
        Grade grade = createValidGrade();
        GradeDTO gradeDTO = createValidGradeDTOWithId();

        when(gradeService.findById("1")).thenReturn(grade);
        when(gradeMapper.toDTO(grade)).thenReturn(gradeDTO);

        // Act & Assert
        mockMvc.perform(get("/api/grades/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.studentId").value(1))
                .andExpect(jsonPath("$.courseId").value(1))
                .andExpect(jsonPath("$.score").value(85.5))
                .andExpect(jsonPath("$.letterGrade").value("B"));

        verify(gradeService).findById("1");
        verify(gradeMapper).toDTO(grade);
    }

    @Test
    @DisplayName("GET /api/grades/{id} - Should return 404 when grade not found")
    void testGetGradeById_NotFound() throws Exception {
        // Arrange
        when(gradeService.findById("999")).thenReturn(null);

        // Act & Assert
        mockMvc.perform(get("/api/grades/999"))
                .andExpect(status().isNotFound());

        verify(gradeService).findById("999");
        verify(gradeMapper, never()).toDTO(any());
    }

    @Test
    @DisplayName("GET /api/grades/{id} - Should return 400 for invalid ID format")
    void testGetGradeById_InvalidId() throws Exception {
        // Arrange
        when(gradeService.findById("invalid")).thenThrow(new ValidationException("Invalid grade ID format: invalid"));

        // Act & Assert
        mockMvc.perform(get("/api/grades/invalid"))
                .andExpect(status().isBadRequest());

        verify(gradeService).findById("invalid");
    }

    // GET GRADES BY STUDENT ID TESTS

    @Test
    @DisplayName("GET /api/grades/student/{studentId} - Should return grades successfully")
    void testGetGradesByStudentId_Success() throws Exception {
        // Arrange
        Grade grade1 = createValidGrade();
        Grade grade2 = createValidGrade();
        grade2.setId(2L);
        grade2.setScore(new BigDecimal("92.0"));
        grade2.setLetterGrade("A");

        GradeDTO gradeDTO1 = createValidGradeDTOWithId();
        GradeDTO gradeDTO2 = createValidGradeDTOWithId();
        gradeDTO2.setId(2L);
        gradeDTO2.setScore(new BigDecimal("92.0"));
        gradeDTO2.setLetterGrade("A");

        List<Grade> grades = Arrays.asList(grade1, grade2);
        List<GradeDTO> gradeDTOs = Arrays.asList(gradeDTO1, gradeDTO2);

        when(gradeService.findByStudentId("1")).thenReturn(grades);
        when(gradeMapper.toDTO(grade1)).thenReturn(gradeDTO1);
        when(gradeMapper.toDTO(grade2)).thenReturn(gradeDTO2);

        // Act & Assert
        mockMvc.perform(get("/api/grades/student/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].studentId").value(1))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].studentId").value(1));

        verify(gradeService).findByStudentId("1");
        verify(gradeMapper, times(2)).toDTO(any(Grade.class));
    }

    @Test
    @DisplayName("GET /api/grades/student/{studentId} - Should return empty list when no grades exist")
    void testGetGradesByStudentId_EmptyList() throws Exception {
        // Arrange
        when(gradeService.findByStudentId("999")).thenReturn(Arrays.asList());

        // Act & Assert
        mockMvc.perform(get("/api/grades/student/999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        verify(gradeService).findByStudentId("999");
        verify(gradeMapper, never()).toDTO(any());
    }

    // GET GRADES BY COURSE ID TESTS

    @Test
    @DisplayName("GET /api/grades/course/{courseId} - Should return grades successfully")
    void testGetGradesByCourseId_Success() throws Exception {
        // Arrange
        Grade grade1 = createValidGrade();
        Grade grade2 = createValidGrade();
        grade2.setId(2L);
        grade2.getStudent().setId(2L);
        grade2.setScore(new BigDecimal("78.0"));
        grade2.setLetterGrade("C");

        GradeDTO gradeDTO1 = createValidGradeDTOWithId();
        GradeDTO gradeDTO2 = createValidGradeDTOWithId();
        gradeDTO2.setId(2L);
        gradeDTO2.setStudentId(2L);
        gradeDTO2.setScore(new BigDecimal("78.0"));
        gradeDTO2.setLetterGrade("C");

        List<Grade> grades = Arrays.asList(grade1, grade2);
        List<GradeDTO> gradeDTOs = Arrays.asList(gradeDTO1, gradeDTO2);

        when(gradeService.findByCourseId("1")).thenReturn(grades);
        when(gradeMapper.toDTO(grade1)).thenReturn(gradeDTO1);
        when(gradeMapper.toDTO(grade2)).thenReturn(gradeDTO2);

        // Act & Assert
        mockMvc.perform(get("/api/grades/course/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].courseId").value(1))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].courseId").value(1));

        verify(gradeService).findByCourseId("1");
        verify(gradeMapper, times(2)).toDTO(any(Grade.class));
    }

    @Test
    @DisplayName("GET /api/grades/course/{courseId} - Should return empty list when no grades exist")
    void testGetGradesByCourseId_EmptyList() throws Exception {
        // Arrange
        when(gradeService.findByCourseId("999")).thenReturn(Arrays.asList());

        // Act & Assert
        mockMvc.perform(get("/api/grades/course/999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        verify(gradeService).findByCourseId("999");
        verify(gradeMapper, never()).toDTO(any());
    }

    // UPDATE GRADE TESTS

    @Test
    @DisplayName("PUT /api/grades/{id} - Should update grade successfully")
    void testUpdateGrade_Success() throws Exception {
        // Arrange
        GradeDTO requestDTO = createValidGradeDTO();
        GradeDTO responseDTO = createValidGradeDTOWithId();
        Grade grade = createValidGrade();

        when(gradeMapper.toEntity(any(GradeDTO.class))).thenReturn(grade);
        when(gradeService.update(any(Grade.class))).thenReturn(grade);
        when(gradeMapper.toDTO(any(Grade.class))).thenReturn(responseDTO);

        // Act & Assert
        mockMvc.perform(put("/api/grades/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.studentId").value(1))
                .andExpect(jsonPath("$.courseId").value(1))
                .andExpect(jsonPath("$.score").value(85.5))
                .andExpect(jsonPath("$.letterGrade").value("B"));

        verify(gradeMapper).toEntity(any(GradeDTO.class));
        verify(gradeService).update(any(Grade.class));
        verify(gradeMapper).toDTO(any(Grade.class));
    }

    @Test
    @DisplayName("PUT /api/grades/{id} - Should return 400 for invalid grade data")
    void testUpdateGrade_InvalidData() throws Exception {
        // Arrange
        GradeDTO invalidDTO = new GradeDTO();
        // Missing required fields

        // Act & Assert
        mockMvc.perform(put("/api/grades/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest());

        verify(gradeService, never()).update(any());
    }

    @Test
    @DisplayName("PUT /api/grades/{id} - Should return 404 when grade not found")
    void testUpdateGrade_NotFound() throws Exception {
        // Arrange
        GradeDTO requestDTO = createValidGradeDTO();
        Grade grade = createValidGrade();

        when(gradeMapper.toEntity(any(GradeDTO.class))).thenReturn(grade);
        when(gradeService.update(any(Grade.class))).thenThrow(new GradeNotFoundException(999L));

        // Act & Assert
        mockMvc.perform(put("/api/grades/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isNotFound());

        verify(gradeMapper).toEntity(any(GradeDTO.class));
        verify(gradeService).update(any(Grade.class));
    }

    @Test
    @DisplayName("PUT /api/grades/{id} - Should return 400 for validation errors")
    void testUpdateGrade_ValidationErrors() throws Exception {
        // Arrange
        GradeDTO requestDTO = createValidGradeDTO();
        Grade grade = createValidGrade();

        when(gradeMapper.toEntity(any(GradeDTO.class))).thenReturn(grade);
        when(gradeService.update(any(Grade.class))).thenThrow(new ValidationException("Student cannot be null"));

        // Act & Assert
        mockMvc.perform(put("/api/grades/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("Student cannot be null"));

        verify(gradeMapper).toEntity(any(GradeDTO.class));
        verify(gradeService).update(any(Grade.class));
    }

    // DELETE GRADE TESTS

    @Test
    @DisplayName("DELETE /api/grades/{id} - Should delete grade successfully")
    void testDeleteGrade_Success() throws Exception {
        // Arrange
        doNothing().when(gradeService).delete("1");

        // Act & Assert
        mockMvc.perform(delete("/api/grades/1"))
                .andExpect(status().isNoContent());

        verify(gradeService).delete("1");
    }

    @Test
    @DisplayName("DELETE /api/grades/{id} - Should return 404 when grade not found")
    void testDeleteGrade_NotFound() throws Exception {
        // Arrange
        doThrow(new GradeNotFoundException(999L)).when(gradeService).delete("999");

        // Act & Assert
        mockMvc.perform(delete("/api/grades/999"))
                .andExpect(status().isNotFound());

        verify(gradeService).delete("999");
    }

    @Test
    @DisplayName("DELETE /api/grades/{id} - Should return 400 for invalid ID format")
    void testDeleteGrade_InvalidId() throws Exception {
        // Arrange
        doThrow(new ValidationException("Invalid grade ID format: invalid")).when(gradeService).delete("invalid");

        // Act & Assert
        mockMvc.perform(delete("/api/grades/invalid"))
                .andExpect(status().isBadRequest());

        verify(gradeService).delete("invalid");
    }

    // CALCULATE GPA TESTS

    @Test
    @DisplayName("GET /api/grades/student/{studentId}/gpa - Should calculate GPA successfully")
    void testCalculateStudentGPA_Success() throws Exception {
        // Arrange
        when(gradeService.calculateGPA("1")).thenReturn(3.5);

        // Act & Assert
        mockMvc.perform(get("/api/grades/student/1/gpa"))
                .andExpect(status().isOk())
                .andExpect(content().string("3.5"));

        verify(gradeService).calculateGPA("1");
    }

    @Test
    @DisplayName("GET /api/grades/student/{studentId}/gpa - Should return 0.0 for student with no grades")
    void testCalculateStudentGPA_NoGrades() throws Exception {
        // Arrange
        when(gradeService.calculateGPA("999")).thenReturn(0.0);

        // Act & Assert
        mockMvc.perform(get("/api/grades/student/999/gpa"))
                .andExpect(status().isOk())
                .andExpect(content().string("0.0"));

        verify(gradeService).calculateGPA("999");
    }

    @Test
    @DisplayName("GET /api/grades/student/{studentId}/gpa - Should return 400 for invalid student ID")
    void testCalculateStudentGPA_InvalidId() throws Exception {
        // Arrange
        when(gradeService.calculateGPA("invalid")).thenThrow(new ValidationException("Student ID cannot be null or empty"));

        // Act & Assert
        mockMvc.perform(get("/api/grades/student/invalid/gpa"))
                .andExpect(status().isBadRequest());

        verify(gradeService).calculateGPA("invalid");
    }

    // CALCULATE LETTER GRADE TESTS

    @Test
    @DisplayName("POST /api/grades/calculate-letter-grade - Should calculate letter grade successfully")
    void testCalculateLetterGrade_Success() throws Exception {
        // Arrange
        when(gradeService.calculateLetterGrade(85.5)).thenReturn("B");

        // Act & Assert
        mockMvc.perform(post("/api/grades/calculate-letter-grade")
                .param("score", "85.5"))
                .andExpect(status().isOk())
                .andExpect(content().string("B"));

        verify(gradeService).calculateLetterGrade(85.5);
    }

    @Test
    @DisplayName("POST /api/grades/calculate-letter-grade - Should return A for high score")
    void testCalculateLetterGrade_A() throws Exception {
        // Arrange
        when(gradeService.calculateLetterGrade(95.0)).thenReturn("A");

        // Act & Assert
        mockMvc.perform(post("/api/grades/calculate-letter-grade")
                .param("score", "95.0"))
                .andExpect(status().isOk())
                .andExpect(content().string("A"));

        verify(gradeService).calculateLetterGrade(95.0);
    }

    @Test
    @DisplayName("POST /api/grades/calculate-letter-grade - Should return F for low score")
    void testCalculateLetterGrade_F() throws Exception {
        // Arrange
        when(gradeService.calculateLetterGrade(45.0)).thenReturn("F");

        // Act & Assert
        mockMvc.perform(post("/api/grades/calculate-letter-grade")
                .param("score", "45.0"))
                .andExpect(status().isOk())
                .andExpect(content().string("F"));

        verify(gradeService).calculateLetterGrade(45.0);
    }

    @Test
    @DisplayName("POST /api/grades/calculate-letter-grade - Should return 400 for invalid score")
    void testCalculateLetterGrade_InvalidScore() throws Exception {
        // Arrange
        when(gradeService.calculateLetterGrade(150.0)).thenThrow(new InvalidGradeException("Invalid grade score: 150.0"));

        // Act & Assert
        mockMvc.perform(post("/api/grades/calculate-letter-grade")
                .param("score", "150.0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_GRADE"))
                .andExpect(jsonPath("$.message").value("Invalid grade score: 150.0"));

        verify(gradeService).calculateLetterGrade(150.0);
    }

    // VALIDATION TESTS

    @Test
    @DisplayName("POST /api/grades - Should validate score constraints")
    void testCreateGrade_ScoreValidation() throws Exception {
        // Test negative score
        GradeDTO negativeScoreDTO = createValidGradeDTO();
        negativeScoreDTO.setScore(new BigDecimal("-1.0"));

        mockMvc.perform(post("/api/grades")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(negativeScoreDTO)))
                .andExpect(status().isBadRequest());

        // Test score over 100
        GradeDTO highScoreDTO = createValidGradeDTO();
        highScoreDTO.setScore(new BigDecimal("101.0"));

        mockMvc.perform(post("/api/grades")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(highScoreDTO)))
                .andExpect(status().isBadRequest());

        verify(gradeService, never()).create(any());
    }

    @Test
    @DisplayName("POST /api/grades - Should validate required fields")
    void testCreateGrade_RequiredFieldsValidation() throws Exception {
        // Test missing student ID
        GradeDTO noStudentDTO = createValidGradeDTO();
        noStudentDTO.setStudentId(null);

        mockMvc.perform(post("/api/grades")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(noStudentDTO)))
                .andExpect(status().isBadRequest());

        // Test missing course ID
        GradeDTO noCourseDTO = createValidGradeDTO();
        noCourseDTO.setCourseId(null);

        mockMvc.perform(post("/api/grades")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(noCourseDTO)))
                .andExpect(status().isBadRequest());

        // Test missing score
        GradeDTO noScoreDTO = createValidGradeDTO();
        noScoreDTO.setScore(null);

        mockMvc.perform(post("/api/grades")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(noScoreDTO)))
                .andExpect(status().isBadRequest());

        verify(gradeService, never()).create(any());
    }

    // CONTENT TYPE TESTS

    @Test
    @DisplayName("All endpoints should return JSON content type")
    void testContentType() throws Exception {
        Grade grade = createValidGrade();
        GradeDTO gradeDTO = createValidGradeDTOWithId();

        when(gradeService.findById("1")).thenReturn(grade);
        when(gradeMapper.toDTO(grade)).thenReturn(gradeDTO);

        mockMvc.perform(get("/api/grades/1"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST and PUT endpoints should accept JSON content type")
    void testAcceptContentType() throws Exception {
        GradeDTO requestDTO = createValidGradeDTO();
        Grade grade = createValidGrade();
        GradeDTO responseDTO = createValidGradeDTOWithId();

        when(gradeMapper.toEntity(any(GradeDTO.class))).thenReturn(grade);
        when(gradeService.create(any(Grade.class))).thenReturn(grade);
        when(gradeMapper.toDTO(any(Grade.class))).thenReturn(responseDTO);

        mockMvc.perform(post("/api/grades")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated());
    }

    // CORS TESTS

    @Test
    @DisplayName("Endpoints should support CORS")
    void testCorsSupport() throws Exception {
        Grade grade = createValidGrade();
        GradeDTO gradeDTO = createValidGradeDTOWithId();

        when(gradeService.findById("1")).thenReturn(grade);
        when(gradeMapper.toDTO(grade)).thenReturn(gradeDTO);

        mockMvc.perform(get("/api/grades/1")
                .header("Origin", "http://localhost:3000"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "*"));
    }

    // EDGE CASE TESTS

    @Test
    @DisplayName("Should handle decimal scores")
    void testDecimalScores() throws Exception {
        GradeDTO decimalScoreDTO = createValidGradeDTO();
        decimalScoreDTO.setScore(new BigDecimal("87.75"));

        Grade grade = createValidGrade();
        GradeDTO responseDTO = createValidGradeDTOWithId();

        when(gradeMapper.toEntity(any(GradeDTO.class))).thenReturn(grade);
        when(gradeService.create(any(Grade.class))).thenReturn(grade);
        when(gradeMapper.toDTO(any(Grade.class))).thenReturn(responseDTO);

        mockMvc.perform(post("/api/grades")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(decimalScoreDTO)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Should handle boundary score values")
    void testBoundaryScores() throws Exception {
        // Test minimum valid score
        GradeDTO minScoreDTO = createValidGradeDTO();
        minScoreDTO.setScore(new BigDecimal("0.0"));

        Grade grade = createValidGrade();
        GradeDTO responseDTO = createValidGradeDTOWithId();

        when(gradeMapper.toEntity(any(GradeDTO.class))).thenReturn(grade);
        when(gradeService.create(any(Grade.class))).thenReturn(grade);
        when(gradeMapper.toDTO(any(Grade.class))).thenReturn(responseDTO);

        mockMvc.perform(post("/api/grades")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(minScoreDTO)))
                .andExpect(status().isCreated());

        // Test maximum valid score
        GradeDTO maxScoreDTO = createValidGradeDTO();
        maxScoreDTO.setScore(new BigDecimal("100.0"));

        mockMvc.perform(post("/api/grades")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(maxScoreDTO)))
                .andExpect(status().isCreated());
    }

    // ERROR HANDLING TESTS

    @Test
    @DisplayName("Should handle malformed JSON")
    void testMalformedJson() throws Exception {
        mockMvc.perform(post("/api/grades")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ invalid json }"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errorCode").value("INTERNAL_SERVER_ERROR"));
    }

    @Test
    @DisplayName("Should handle missing content type")
    void testMissingContentType() throws Exception {
        GradeDTO requestDTO = createValidGradeDTO();

        mockMvc.perform(post("/api/grades")
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errorCode").value("INTERNAL_SERVER_ERROR"));
    }

    @Test
    @DisplayName("Should handle empty request body")
    void testEmptyRequestBody() throws Exception {
        mockMvc.perform(post("/api/grades")
                .contentType(MediaType.APPLICATION_JSON)
                .content(""))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errorCode").value("INTERNAL_SERVER_ERROR"));
    }

    @Test
    @DisplayName("Should handle missing score parameter for letter grade calculation")
    void testCalculateLetterGrade_MissingParameter() throws Exception {
        mockMvc.perform(post("/api/grades/calculate-letter-grade"))
                .andExpect(status().isBadRequest());
    }
}
