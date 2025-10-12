package com.rca.demo_course.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rca.demo_course.domain.Course;
import com.rca.demo_course.domain.Grade;
import com.rca.demo_course.domain.Student;
import com.rca.demo_course.dto.GradeDTO;
import com.rca.demo_course.repository.CourseRepository;
import com.rca.demo_course.repository.GradeRepository;
import com.rca.demo_course.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for GradeController.
 * Tests the full integration from HTTP layer through Controller, Service, Repository, and Database.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DisplayName("Grade Controller Integration Tests")
public class GradeControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private GradeRepository gradeRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;
    private String baseUrl;
    private Student testStudent;
    private Course testCourse;

    @BeforeEach
    void setUp() {
        // Clear database before each test
        gradeRepository.deleteAll();
        studentRepository.deleteAll();
        courseRepository.deleteAll();

        // Setup MockMvc for additional testing
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        // Setup base URL for REST template
        baseUrl = "http://localhost:" + port + "/api/grades";

        // Create test data
        testStudent = new Student();
        testStudent.setFirstName("John");
        testStudent.setLastName("Doe");
        testStudent.setEmail("john.doe@example.com");
        testStudent = studentRepository.save(testStudent);

        testCourse = new Course();
        testCourse.setName("Advanced Java");
        testCourse.setCode("AJ201");
        testCourse.setCredits(4);
        testCourse = courseRepository.save(testCourse);
    }

    // CREATE GRADE TESTS

    @Test
    @DisplayName("Should create grade successfully through full integration")
    void testCreateGradeIntegration() {
        // Given
        GradeDTO gradeDTO = new GradeDTO();
        gradeDTO.setStudentId(testStudent.getId());
        gradeDTO.setCourseId(testCourse.getId());
        gradeDTO.setScore(BigDecimal.valueOf(85.5));
        gradeDTO.setLetterGrade("B"); // Service will calculate this automatically

        // When
        ResponseEntity<GradeDTO> response = restTemplate.postForEntity(
                baseUrl, gradeDTO, GradeDTO.class);

        // Then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());

        GradeDTO createdGrade = response.getBody();
        assertNotNull(createdGrade);
        assertNotNull(createdGrade.getId());
        assertEquals(testStudent.getId(), createdGrade.getStudentId());
        assertEquals(testCourse.getId(), createdGrade.getCourseId());
        assertEquals(0, BigDecimal.valueOf(85.5).compareTo(createdGrade.getScore()));
        assertEquals("B", createdGrade.getLetterGrade());

        // Verify grade was actually saved in database
        List<Grade> grades = gradeRepository.findAll();
        assertEquals(1, grades.size());
        Grade savedGrade = grades.get(0);
        assertEquals(testStudent.getId(), savedGrade.getStudent().getId());
        assertEquals(testCourse.getId(), savedGrade.getCourse().getId());
        assertEquals(85.5, savedGrade.getScore());
        assertEquals("B", savedGrade.getLetterGrade());
    }

    @Test
    @DisplayName("Should return validation error for invalid grade data")
    void testCreateGradeWithInvalidData() {
        // Given
        GradeDTO invalidGrade = new GradeDTO();
        invalidGrade.setStudentId(testStudent.getId());
        invalidGrade.setCourseId(testCourse.getId());
        invalidGrade.setScore(BigDecimal.valueOf(150.0)); // Invalid score > 100
        invalidGrade.setLetterGrade("A+");

        // When
        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl, invalidGrade, String.class);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        // Verify no grade was saved in database
        List<Grade> grades = gradeRepository.findAll();
        assertTrue(grades.isEmpty());
    }

    @Test
    @DisplayName("Should return validation error for negative score")
    void testCreateGradeWithNegativeScore() {
        // Given
        GradeDTO invalidGrade = new GradeDTO();
        invalidGrade.setStudentId(testStudent.getId());
        invalidGrade.setCourseId(testCourse.getId());
        invalidGrade.setScore(BigDecimal.valueOf(-10.0)); // Negative score should fail

        // When
        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl, invalidGrade, String.class);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("Should return validation error for missing student ID")
    void testCreateGradeWithMissingStudentId() {
        // Given
        GradeDTO invalidGrade = new GradeDTO();
        invalidGrade.setCourseId(testCourse.getId());
        invalidGrade.setScore(BigDecimal.valueOf(85.0));

        // When
        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl, invalidGrade, String.class);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    // READ GRADE TESTS

    @Test
    @DisplayName("Should retrieve grade by ID through full integration")
    void testGetGradeByIdIntegration() {
        // Given - Create a grade first
        Grade grade = new Grade();
        grade.setStudent(testStudent);
        grade.setCourse(testCourse);
        grade.setScore(92.0);
        grade.setLetterGrade("A"); // Service will calculate this automatically
        Grade savedGrade = gradeRepository.save(grade);

        // When
        ResponseEntity<GradeDTO> response = restTemplate.getForEntity(
                baseUrl + "/" + savedGrade.getId(), GradeDTO.class);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        GradeDTO retrievedGrade = response.getBody();
        assertNotNull(retrievedGrade);
        assertEquals(savedGrade.getId(), retrievedGrade.getId());
        assertEquals(testStudent.getId(), retrievedGrade.getStudentId());
        assertEquals(testCourse.getId(), retrievedGrade.getCourseId());
        assertEquals(0, BigDecimal.valueOf(92.0).compareTo(retrievedGrade.getScore()));
        assertEquals("A", retrievedGrade.getLetterGrade());
    }

    @Test
    @DisplayName("Should return 404 for non-existent grade")
    void testGetNonExistentGrade() {
        // When
        ResponseEntity<GradeDTO> response = restTemplate.getForEntity(
                baseUrl + "/999", GradeDTO.class);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("Should retrieve grades by student ID through full integration")
    void testGetGradesByStudentIdIntegration() {
        // Given - Create multiple grades for the student
        Grade grade1 = new Grade();
        grade1.setStudent(testStudent);
        grade1.setCourse(testCourse);
        grade1.setScore(85.0);
        grade1.setLetterGrade("B");
        gradeRepository.save(grade1);

        // Create another course and grade
        Course course2 = new Course();
        course2.setName("Data Structures");
        course2.setCode("DS101");
        course2.setCredits(3);
        course2 = courseRepository.save(course2);

        Grade grade2 = new Grade();
        grade2.setStudent(testStudent);
        grade2.setCourse(course2);
        grade2.setScore(78.0);
        grade2.setLetterGrade("C+");
        gradeRepository.save(grade2);

        // When
        ResponseEntity<GradeDTO[]> response = restTemplate.getForEntity(
                baseUrl + "/student/" + testStudent.getId(), GradeDTO[].class);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        GradeDTO[] grades = response.getBody();
        assertNotNull(grades);
        assertEquals(2, grades.length);

        // Verify both grades belong to the same student
        for (GradeDTO grade : grades) {
            assertEquals(testStudent.getId(), grade.getStudentId());
        }
    }

    @Test
    @DisplayName("Should retrieve grades by course ID through full integration")
    void testGetGradesByCourseIdIntegration() {
        // Given - Create multiple grades for the course
        Grade grade1 = new Grade();
        grade1.setStudent(testStudent);
        grade1.setCourse(testCourse);
        grade1.setScore(88.0);
        grade1.setLetterGrade("B+");
        gradeRepository.save(grade1);

        // Create another student and grade
        Student student2 = new Student();
        student2.setFirstName("Jane");
        student2.setLastName("Smith");
        student2.setEmail("jane.smith@example.com");
        student2 = studentRepository.save(student2);

        Grade grade2 = new Grade();
        grade2.setStudent(student2);
        grade2.setCourse(testCourse);
        grade2.setScore(95.0);
        grade2.setLetterGrade("A");
        gradeRepository.save(grade2);

        // When
        ResponseEntity<GradeDTO[]> response = restTemplate.getForEntity(
                baseUrl + "/course/" + testCourse.getId(), GradeDTO[].class);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        GradeDTO[] grades = response.getBody();
        assertNotNull(grades);
        assertEquals(2, grades.length);

        // Verify both grades belong to the same course
        for (GradeDTO grade : grades) {
            assertEquals(testCourse.getId(), grade.getCourseId());
        }
    }

    // UPDATE GRADE TESTS

    @Test
    @DisplayName("Should update grade successfully through full integration")
    void testUpdateGradeIntegration() {
        // Given - Create a grade first
        Grade grade = new Grade();
        grade.setStudent(testStudent);
        grade.setCourse(testCourse);
        grade.setScore(80.0);
        grade.setLetterGrade("B-");
        Grade savedGrade = gradeRepository.save(grade);

        // Prepare update
        GradeDTO updateDTO = new GradeDTO();
        updateDTO.setId(savedGrade.getId());
        updateDTO.setStudentId(testStudent.getId());
        updateDTO.setCourseId(testCourse.getId());
        updateDTO.setScore(BigDecimal.valueOf(87.0));
        updateDTO.setLetterGrade("B"); // Service will calculate this automatically

        // When
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<GradeDTO> request = new HttpEntity<>(updateDTO, headers);

        ResponseEntity<GradeDTO> response = restTemplate.exchange(
                baseUrl + "/" + savedGrade.getId(),
                HttpMethod.PUT,
                request,
                GradeDTO.class);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        GradeDTO updatedGrade = response.getBody();
        assertNotNull(updatedGrade);
        assertEquals(0, BigDecimal.valueOf(87.0).compareTo(updatedGrade.getScore()));
        assertEquals("B", updatedGrade.getLetterGrade());

        // Verify grade was actually updated in database
        Grade dbGrade = gradeRepository.findById(savedGrade.getId()).orElse(null);
        assertNotNull(dbGrade);
        assertEquals(87.0, dbGrade.getScore());
        assertEquals("B", dbGrade.getLetterGrade());
    }

    @Test
    @DisplayName("Should return 404 when updating non-existent grade")
    void testUpdateNonExistentGrade() {
        // Given
        GradeDTO updateDTO = new GradeDTO();
        updateDTO.setId(999L);
        updateDTO.setStudentId(testStudent.getId());
        updateDTO.setCourseId(testCourse.getId());
        updateDTO.setScore(BigDecimal.valueOf(90.0));

        // When
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<GradeDTO> request = new HttpEntity<>(updateDTO, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/999",
                HttpMethod.PUT,
                request,
                String.class);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    // DELETE GRADE TESTS

    @Test
    @DisplayName("Should delete grade successfully through full integration")
    void testDeleteGradeIntegration() {
        // Given - Create a grade first
        Grade grade = new Grade();
        grade.setStudent(testStudent);
        grade.setCourse(testCourse);
        grade.setScore(75.0);
        grade.setLetterGrade("C");
        Grade savedGrade = gradeRepository.save(grade);

        // Verify grade exists
        assertTrue(gradeRepository.existsById(savedGrade.getId()));

        // When
        ResponseEntity<Void> response = restTemplate.exchange(
                baseUrl + "/" + savedGrade.getId(),
                HttpMethod.DELETE,
                null,
                Void.class);

        // Then
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        // Verify grade was actually deleted from database
        assertFalse(gradeRepository.existsById(savedGrade.getId()));
    }

    @Test
    @DisplayName("Should return 404 when deleting non-existent grade")
    void testDeleteNonExistentGrade() {
        // When
        ResponseEntity<Void> response = restTemplate.exchange(
                baseUrl + "/999",
                HttpMethod.DELETE,
                null,
                Void.class);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    // GPA CALCULATION TESTS

    @Test
    @DisplayName("Should calculate student GPA through full integration")
    void testCalculateStudentGPAIntegration() {
        // Given - Create multiple grades for the student
        Grade grade1 = new Grade();
        grade1.setStudent(testStudent);
        grade1.setCourse(testCourse);
        grade1.setScore(90.0); // A
        grade1.setLetterGrade("A");
        gradeRepository.save(grade1);

        Course course2 = new Course();
        course2.setName("Data Structures");
        course2.setCode("DS101");
        course2.setCredits(3);
        course2 = courseRepository.save(course2);

        Grade grade2 = new Grade();
        grade2.setStudent(testStudent);
        grade2.setCourse(course2);
        grade2.setScore(80.0); // B
        grade2.setLetterGrade("B");
        gradeRepository.save(grade2);

        // When
        ResponseEntity<Double> response = restTemplate.getForEntity(
                baseUrl + "/student/" + testStudent.getId() + "/gpa", Double.class);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() > 0); // GPA should be positive
    }

    @Test
    @DisplayName("Should calculate letter grade through full integration")
    void testCalculateLetterGradeIntegration() {
        // When
        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl + "/calculate-letter-grade?score=87.5", null, String.class);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().matches("[A-F][+-]?")); // Should be a valid letter grade
    }

    // MOCK MVC TESTS FOR ADDITIONAL VALIDATION

    @Test
    @DisplayName("Should handle JSON serialization/deserialization correctly")
    void testJsonSerialization() throws Exception {
        // Given
        GradeDTO gradeDTO = new GradeDTO();
        gradeDTO.setStudentId(testStudent.getId());
        gradeDTO.setCourseId(testCourse.getId());
        gradeDTO.setScore(BigDecimal.valueOf(92.5));
        gradeDTO.setLetterGrade("A"); // Service will calculate this automatically

        String jsonContent = objectMapper.writeValueAsString(gradeDTO);

        // When & Then
        mockMvc.perform(post("/api/grades")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.studentId").value(testStudent.getId()))
                .andExpect(jsonPath("$.courseId").value(testCourse.getId()))
                .andExpect(jsonPath("$.score").value(92.5))
                .andExpect(jsonPath("$.letterGrade").value("A"))
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    @DisplayName("Should handle CORS headers correctly")
    void testCorsHeaders() throws Exception {
        // Given
        GradeDTO gradeDTO = new GradeDTO();
        gradeDTO.setStudentId(testStudent.getId());
        gradeDTO.setCourseId(testCourse.getId());
        gradeDTO.setScore(BigDecimal.valueOf(88.0));
        gradeDTO.setLetterGrade("B"); // Service will calculate this automatically

        String jsonContent = objectMapper.writeValueAsString(gradeDTO);

        // When & Then
        mockMvc.perform(post("/api/grades")
                .header("Origin", "http://localhost:3000")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent))
                .andExpect(status().isCreated())
                .andExpect(header().string("Access-Control-Allow-Origin", "*"));
    }

    @Test
    @DisplayName("Should handle content type validation")
    void testContentTypeValidation() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/grades")
                .contentType(MediaType.TEXT_PLAIN)
                .content("invalid content"))
                .andExpect(status().isUnsupportedMediaType());
    }

    // EDGE CASES AND ERROR SCENARIOS

    @Test
    @DisplayName("Should handle concurrent grade creation")
    void testConcurrentGradeCreation() throws InterruptedException {
        // Given
        GradeDTO gradeDTO = new GradeDTO();
        gradeDTO.setStudentId(testStudent.getId());
        gradeDTO.setCourseId(testCourse.getId());
        gradeDTO.setScore(BigDecimal.valueOf(85.0));
        gradeDTO.setLetterGrade("B");

        // When - Simulate concurrent requests
        Thread[] threads = new Thread[3];
        @SuppressWarnings("unchecked")
        ResponseEntity<GradeDTO>[] responses = new ResponseEntity[3];

        for (int i = 0; i < 3; i++) {
            final int index = i;
            final GradeDTO gradeCopy = new GradeDTO();
            gradeCopy.setStudentId(testStudent.getId());
            gradeCopy.setCourseId(testCourse.getId());
            gradeCopy.setScore(BigDecimal.valueOf(85.0 + index));
            gradeCopy.setLetterGrade("B" + (index > 0 ? "+" : ""));

            threads[i] = new Thread(() -> {
                responses[index] = restTemplate.postForEntity(baseUrl, gradeCopy, GradeDTO.class);
            });
            threads[i].start();
        }

        // Wait for all threads to complete
        for (Thread thread : threads) {
            thread.join();
        }

        // Then - All requests should succeed
        for (int i = 0; i < 3; i++) {
            assertEquals(HttpStatus.CREATED, responses[i].getStatusCode());
            assertNotNull(responses[i].getBody());
        }

        // Verify all grades were saved
        List<Grade> allGrades = gradeRepository.findAll();
        assertEquals(3, allGrades.size());
    }

    @Test
    @DisplayName("Should handle boundary score values")
    void testBoundaryScoreValues() {
        // Test minimum valid score
        GradeDTO gradeDTO = new GradeDTO();
        gradeDTO.setStudentId(testStudent.getId());
        gradeDTO.setCourseId(testCourse.getId());
        gradeDTO.setScore(BigDecimal.valueOf(0.0)); // Minimum valid score
        gradeDTO.setLetterGrade("F");

        ResponseEntity<GradeDTO> response = restTemplate.postForEntity(
                baseUrl, gradeDTO, GradeDTO.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());

        // Test maximum valid score
        GradeDTO gradeDTO2 = new GradeDTO();
        gradeDTO2.setStudentId(testStudent.getId());
        gradeDTO2.setCourseId(testCourse.getId());
        gradeDTO2.setScore(BigDecimal.valueOf(100.0)); // Maximum valid score
        gradeDTO2.setLetterGrade("A+");

        ResponseEntity<GradeDTO> response2 = restTemplate.postForEntity(
                baseUrl, gradeDTO2, GradeDTO.class);

        assertEquals(HttpStatus.CREATED, response2.getStatusCode());
        assertNotNull(response2.getBody());
    }

    @Test
    @DisplayName("Should handle decimal precision in scores")
    void testDecimalPrecisionInScores() {
        // Given
        GradeDTO gradeDTO = new GradeDTO();
        gradeDTO.setStudentId(testStudent.getId());
        gradeDTO.setCourseId(testCourse.getId());
        gradeDTO.setScore(BigDecimal.valueOf(87.75)); // Decimal precision test
        gradeDTO.setLetterGrade("B+");

        // When
        ResponseEntity<GradeDTO> response = restTemplate.postForEntity(
                baseUrl, gradeDTO, GradeDTO.class);

        // Then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(0, BigDecimal.valueOf(87.75).compareTo(response.getBody().getScore()));
    }
}
