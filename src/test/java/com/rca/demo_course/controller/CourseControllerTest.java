package com.rca.demo_course.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rca.demo_course.domain.Course;
import com.rca.demo_course.dto.CourseDTO;
import com.rca.demo_course.exception.CourseNotFoundException;
import com.rca.demo_course.exception.DuplicateResourceException;
import com.rca.demo_course.exception.ValidationException;
import com.rca.demo_course.mapper.CourseMapper;
import com.rca.demo_course.service.CourseService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive end-to-end tests for CourseController.
 * Tests all CRUD operations, validation, error handling, and edge cases.
 */
@WebMvcTest(CourseController.class)
@DisplayName("Course Controller End-to-End Tests")
public class CourseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CourseService courseService;

    @MockBean
    private CourseMapper courseMapper;

    @Autowired
    private ObjectMapper objectMapper;

    // Test Data
    private CourseDTO createValidCourseDTO() {
        CourseDTO courseDTO = new CourseDTO();
        courseDTO.setName("Introduction to Programming");
        courseDTO.setCode("CS101");
        courseDTO.setCredits(3);
        return courseDTO;
    }

    private CourseDTO createValidCourseDTOWithId() {
        CourseDTO courseDTO = new CourseDTO();
        courseDTO.setId(1L);
        courseDTO.setName("Introduction to Programming");
        courseDTO.setCode("CS101");
        courseDTO.setCredits(3);
        return courseDTO;
    }

    private Course createValidCourse() {
        Course course = new Course();
        course.setId(1L);
        course.setName("Introduction to Programming");
        course.setCode("CS101");
        course.setCredits(3);
        return course;
    }

    // CREATE COURSE TESTS

    @Test
    @DisplayName("POST /api/courses - Should create course successfully")
    void testCreateCourse_Success() throws Exception {
        // Arrange
        CourseDTO requestDTO = createValidCourseDTO();
        CourseDTO responseDTO = createValidCourseDTOWithId();
        Course course = createValidCourse();

        when(courseMapper.toEntity(any(CourseDTO.class))).thenReturn(course);
        when(courseService.create(any(Course.class))).thenReturn(course);
        when(courseMapper.toDTO(any(Course.class))).thenReturn(responseDTO);

        // Act & Assert
        mockMvc.perform(post("/api/courses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Introduction to Programming"))
                .andExpect(jsonPath("$.code").value("CS101"))
                .andExpect(jsonPath("$.credits").value(3));

        verify(courseMapper).toEntity(any(CourseDTO.class));
        verify(courseService).create(any(Course.class));
        verify(courseMapper).toDTO(any(Course.class));
    }

    @Test
    @DisplayName("POST /api/courses - Should return 400 for invalid course data")
    void testCreateCourse_InvalidData() throws Exception {
        // Arrange
        CourseDTO invalidDTO = new CourseDTO();
        // Missing required fields

        // Act & Assert
        mockMvc.perform(post("/api/courses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest());

        verify(courseService, never()).create(any());
    }

    @Test
    @DisplayName("POST /api/courses - Should return 409 for duplicate course code")
    void testCreateCourse_DuplicateCode() throws Exception {
        // Arrange
        CourseDTO requestDTO = createValidCourseDTO();
        Course course = createValidCourse();

        when(courseMapper.toEntity(any(CourseDTO.class))).thenReturn(course);
        when(courseService.create(any(Course.class))).thenThrow(new DuplicateResourceException("Course code already exists: CS101"));

        // Act & Assert
        mockMvc.perform(post("/api/courses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("DUPLICATE_RESOURCE"))
                .andExpect(jsonPath("$.message").value("Course code already exists: CS101"));

        verify(courseMapper).toEntity(any(CourseDTO.class));
        verify(courseService).create(any(Course.class));
    }

    @Test
    @DisplayName("POST /api/courses - Should return 400 for validation errors")
    void testCreateCourse_ValidationErrors() throws Exception {
        // Arrange
        CourseDTO requestDTO = createValidCourseDTO();
        Course course = createValidCourse();

        when(courseMapper.toEntity(any(CourseDTO.class))).thenReturn(course);
        when(courseService.create(any(Course.class))).thenThrow(new ValidationException("Course name cannot be null or empty"));

        // Act & Assert
        mockMvc.perform(post("/api/courses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("Course name cannot be null or empty"));

        verify(courseMapper).toEntity(any(CourseDTO.class));
        verify(courseService).create(any(Course.class));
    }

    // GET COURSE BY ID TESTS

    @Test
    @DisplayName("GET /api/courses/{id} - Should return course successfully")
    void testGetCourseById_Success() throws Exception {
        // Arrange
        Course course = createValidCourse();
        CourseDTO courseDTO = createValidCourseDTOWithId();

        when(courseService.findById("1")).thenReturn(course);
        when(courseMapper.toDTO(course)).thenReturn(courseDTO);

        // Act & Assert
        mockMvc.perform(get("/api/courses/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Introduction to Programming"))
                .andExpect(jsonPath("$.code").value("CS101"))
                .andExpect(jsonPath("$.credits").value(3));

        verify(courseService).findById("1");
        verify(courseMapper).toDTO(course);
    }

    @Test
    @DisplayName("GET /api/courses/{id} - Should return 404 when course not found")
    void testGetCourseById_NotFound() throws Exception {
        // Arrange
        when(courseService.findById("999")).thenReturn(null);

        // Act & Assert
        mockMvc.perform(get("/api/courses/999"))
                .andExpect(status().isNotFound());

        verify(courseService).findById("999");
        verify(courseMapper, never()).toDTO(any());
    }

    @Test
    @DisplayName("GET /api/courses/{id} - Should return 400 for invalid ID format")
    void testGetCourseById_InvalidId() throws Exception {
        // Arrange
        when(courseService.findById("invalid")).thenThrow(new ValidationException("Invalid course ID format: invalid"));

        // Act & Assert
        mockMvc.perform(get("/api/courses/invalid"))
                .andExpect(status().isBadRequest());

        verify(courseService).findById("invalid");
    }

    // GET ALL COURSES TESTS

    @Test
    @DisplayName("GET /api/courses - Should return all courses successfully")
    void testGetAllCourses_Success() throws Exception {
        // Arrange
        Course course1 = createValidCourse();
        Course course2 = new Course();
        course2.setId(2L);
        course2.setName("Data Structures");
        course2.setCode("CS201");
        course2.setCredits(4);

        CourseDTO courseDTO1 = createValidCourseDTOWithId();
        CourseDTO courseDTO2 = new CourseDTO();
        courseDTO2.setId(2L);
        courseDTO2.setName("Data Structures");
        courseDTO2.setCode("CS201");
        courseDTO2.setCredits(4);

        List<Course> courses = Arrays.asList(course1, course2);
        List<CourseDTO> courseDTOs = Arrays.asList(courseDTO1, courseDTO2);

        when(courseService.findAll()).thenReturn(courses);
        when(courseMapper.toDTO(course1)).thenReturn(courseDTO1);
        when(courseMapper.toDTO(course2)).thenReturn(courseDTO2);

        // Act & Assert
        mockMvc.perform(get("/api/courses"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Introduction to Programming"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Data Structures"));

        verify(courseService).findAll();
        verify(courseMapper, times(2)).toDTO(any(Course.class));
    }

    @Test
    @DisplayName("GET /api/courses - Should return empty list when no courses exist")
    void testGetAllCourses_EmptyList() throws Exception {
        // Arrange
        when(courseService.findAll()).thenReturn(Arrays.asList());

        // Act & Assert
        mockMvc.perform(get("/api/courses"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        verify(courseService).findAll();
        verify(courseMapper, never()).toDTO(any());
    }

    // UPDATE COURSE TESTS

    @Test
    @DisplayName("PUT /api/courses/{id} - Should update course successfully")
    void testUpdateCourse_Success() throws Exception {
        // Arrange
        CourseDTO requestDTO = createValidCourseDTO();
        CourseDTO responseDTO = createValidCourseDTOWithId();
        Course course = createValidCourse();

        when(courseMapper.toEntity(any(CourseDTO.class))).thenReturn(course);
        when(courseService.update(course)).thenReturn(course);
        when(courseMapper.toDTO(course)).thenReturn(responseDTO);

        // Act & Assert
        mockMvc.perform(put("/api/courses/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Introduction to Programming"))
                .andExpect(jsonPath("$.code").value("CS101"))
                .andExpect(jsonPath("$.credits").value(3));

        verify(courseMapper).toEntity(any(CourseDTO.class));
        verify(courseService).update(course);
        verify(courseMapper).toDTO(course);
    }

    @Test
    @DisplayName("PUT /api/courses/{id} - Should return 400 for invalid course data")
    void testUpdateCourse_InvalidData() throws Exception {
        // Arrange
        CourseDTO invalidDTO = new CourseDTO();
        // Missing required fields

        // Act & Assert
        mockMvc.perform(put("/api/courses/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest());

        verify(courseService, never()).update(any());
    }

    @Test
    @DisplayName("PUT /api/courses/{id} - Should return 404 when course not found")
    void testUpdateCourse_NotFound() throws Exception {
        // Arrange
        CourseDTO requestDTO = createValidCourseDTO();
        Course course = createValidCourse();

        when(courseMapper.toEntity(any(CourseDTO.class))).thenReturn(course);
        when(courseService.update(course)).thenThrow(new CourseNotFoundException(1L));

        // Act & Assert
        mockMvc.perform(put("/api/courses/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isNotFound());

        verify(courseMapper).toEntity(any(CourseDTO.class));
        verify(courseService).update(course);
    }

    @Test
    @DisplayName("PUT /api/courses/{id} - Should return 400 for validation errors")
    void testUpdateCourse_ValidationErrors() throws Exception {
        // Arrange
        CourseDTO requestDTO = createValidCourseDTO();
        Course course = createValidCourse();

        when(courseMapper.toEntity(any(CourseDTO.class))).thenReturn(course);
        when(courseService.update(course)).thenThrow(new ValidationException("Course name cannot be null or empty"));

        // Act & Assert
        mockMvc.perform(put("/api/courses/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest());

        verify(courseMapper).toEntity(any(CourseDTO.class));
        verify(courseService).update(course);
    }

    // DELETE COURSE TESTS

    @Test
    @DisplayName("DELETE /api/courses/{id} - Should delete course successfully")
    void testDeleteCourse_Success() throws Exception {
        // Arrange
        doNothing().when(courseService).delete("1");

        // Act & Assert
        mockMvc.perform(delete("/api/courses/1"))
                .andExpect(status().isNoContent());

        verify(courseService).delete("1");
    }

    @Test
    @DisplayName("DELETE /api/courses/{id} - Should return 404 when course not found")
    void testDeleteCourse_NotFound() throws Exception {
        // Arrange
        doThrow(new CourseNotFoundException(999L)).when(courseService).delete("999");

        // Act & Assert
        mockMvc.perform(delete("/api/courses/999"))
                .andExpect(status().isNotFound());

        verify(courseService).delete("999");
    }

    @Test
    @DisplayName("DELETE /api/courses/{id} - Should return 400 for invalid ID format")
    void testDeleteCourse_InvalidId() throws Exception {
        // Arrange
        doThrow(new ValidationException("Invalid course ID format: invalid")).when(courseService).delete("invalid");

        // Act & Assert
        mockMvc.perform(delete("/api/courses/invalid"))
                .andExpect(status().isBadRequest());

        verify(courseService).delete("invalid");
    }

    // VALIDATION TESTS

    @Test
    @DisplayName("POST /api/courses - Should validate course name constraints")
    void testCreateCourse_NameValidation() throws Exception {
        // Test name too short
        CourseDTO shortNameDTO = createValidCourseDTO();
        shortNameDTO.setName("AB"); // Less than 3 characters

        mockMvc.perform(post("/api/courses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(shortNameDTO)))
                .andExpect(status().isBadRequest());

        // Test name too long
        CourseDTO longNameDTO = createValidCourseDTO();
        longNameDTO.setName("A".repeat(101)); // More than 100 characters

        mockMvc.perform(post("/api/courses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(longNameDTO)))
                .andExpect(status().isBadRequest());

        verify(courseService, never()).create(any());
    }

    @Test
    @DisplayName("POST /api/courses - Should validate course code constraints")
    void testCreateCourse_CodeValidation() throws Exception {
        // Test code too short
        CourseDTO shortCodeDTO = createValidCourseDTO();
        shortCodeDTO.setCode("A"); // Less than 2 characters

        mockMvc.perform(post("/api/courses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(shortCodeDTO)))
                .andExpect(status().isBadRequest());

        // Test code too long
        CourseDTO longCodeDTO = createValidCourseDTO();
        longCodeDTO.setCode("A".repeat(11)); // More than 10 characters

        mockMvc.perform(post("/api/courses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(longCodeDTO)))
                .andExpect(status().isBadRequest());

        verify(courseService, never()).create(any());
    }

    @Test
    @DisplayName("POST /api/courses - Should validate credits constraints")
    void testCreateCourse_CreditsValidation() throws Exception {
        // Test negative credits
        CourseDTO negativeCreditsDTO = createValidCourseDTO();
        negativeCreditsDTO.setCredits(-1);

        mockMvc.perform(post("/api/courses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(negativeCreditsDTO)))
                .andExpect(status().isBadRequest());

        // Test zero credits
        CourseDTO zeroCreditsDTO = createValidCourseDTO();
        zeroCreditsDTO.setCredits(0);

        mockMvc.perform(post("/api/courses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(zeroCreditsDTO)))
                .andExpect(status().isBadRequest());

        verify(courseService, never()).create(any());
    }

    // CONTENT TYPE TESTS

    @Test
    @DisplayName("All endpoints should return JSON content type")
    void testContentType() throws Exception {
        Course course = createValidCourse();
        CourseDTO courseDTO = createValidCourseDTOWithId();

        when(courseService.findById("1")).thenReturn(course);
        when(courseMapper.toDTO(course)).thenReturn(courseDTO);

        mockMvc.perform(get("/api/courses/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("POST and PUT endpoints should accept JSON content type")
    void testAcceptContentType() throws Exception {
        CourseDTO requestDTO = createValidCourseDTO();
        Course course = createValidCourse();
        CourseDTO responseDTO = createValidCourseDTOWithId();

        when(courseMapper.toEntity(requestDTO)).thenReturn(course);
        when(courseService.create(course)).thenReturn(course);
        when(courseMapper.toDTO(course)).thenReturn(responseDTO);

        mockMvc.perform(post("/api/courses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated());
    }

    // CORS TESTS

    @Test
    @DisplayName("Endpoints should support CORS")
    void testCorsSupport() throws Exception {
        Course course = createValidCourse();
        CourseDTO courseDTO = createValidCourseDTOWithId();

        when(courseService.findById("1")).thenReturn(course);
        when(courseMapper.toDTO(course)).thenReturn(courseDTO);

        mockMvc.perform(get("/api/courses/1")
                .header("Origin", "http://localhost:3000"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "*"));
    }

    // EDGE CASE TESTS

    @Test
    @DisplayName("Should handle large course names and codes")
    void testLargeData() throws Exception {
        CourseDTO largeDataDTO = createValidCourseDTO();
        largeDataDTO.setName("A".repeat(100)); // Maximum allowed length
        largeDataDTO.setCode("A".repeat(10)); // Maximum allowed length
        largeDataDTO.setCredits(Integer.MAX_VALUE);

        Course course = createValidCourse();
        CourseDTO responseDTO = createValidCourseDTOWithId();

        when(courseMapper.toEntity(largeDataDTO)).thenReturn(course);
        when(courseService.create(course)).thenReturn(course);
        when(courseMapper.toDTO(course)).thenReturn(responseDTO);

        mockMvc.perform(post("/api/courses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(largeDataDTO)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Should handle special characters in course data")
    void testSpecialCharacters() throws Exception {
        CourseDTO specialCharDTO = createValidCourseDTO();
        specialCharDTO.setName("Introduction to Programming & Data Structures");
        specialCharDTO.setCode("CS-101");

        Course course = createValidCourse();
        CourseDTO responseDTO = createValidCourseDTOWithId();

        when(courseMapper.toEntity(specialCharDTO)).thenReturn(course);
        when(courseService.create(course)).thenReturn(course);
        when(courseMapper.toDTO(course)).thenReturn(responseDTO);

        mockMvc.perform(post("/api/courses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(specialCharDTO)))
                .andExpect(status().isCreated());
    }

    // ERROR HANDLING TESTS

    @Test
    @DisplayName("Should handle malformed JSON")
    void testMalformedJson() throws Exception {
        mockMvc.perform(post("/api/courses")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ invalid json }"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errorCode").value("INTERNAL_SERVER_ERROR"));
    }

    @Test
    @DisplayName("Should handle missing content type")
    void testMissingContentType() throws Exception {
        CourseDTO requestDTO = createValidCourseDTO();

        mockMvc.perform(post("/api/courses")
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errorCode").value("INTERNAL_SERVER_ERROR"));
    }

    @Test
    @DisplayName("Should handle empty request body")
    void testEmptyRequestBody() throws Exception {
        mockMvc.perform(post("/api/courses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(""))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errorCode").value("INTERNAL_SERVER_ERROR"));
    }
}
