package com.rca.demo_course.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rca.demo_course.domain.Course;
import com.rca.demo_course.dto.CourseDTO;
import com.rca.demo_course.repository.CourseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for CourseController.
 * Tests the full integration from HTTP layer through Controller, Service, Repository, and Database.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DisplayName("Course Controller Integration Tests")
public class CourseControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;
    private String baseUrl;

    @BeforeEach
    void setUp() {
        // Clear database before each test
        courseRepository.deleteAll();

        // Setup MockMvc for additional testing
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        // Setup base URL for REST template
        baseUrl = "http://localhost:" + port + "/api/courses";
    }

    // CREATE COURSE TESTS

    @Test
    @DisplayName("Should create course successfully through full integration")
    void testCreateCourseIntegration() {
        // Given
        CourseDTO courseDTO = new CourseDTO();
        courseDTO.setName("Spring Boot Fundamentals");
        courseDTO.setCode("SB101");
        courseDTO.setCredits(3);

        // When
        ResponseEntity<CourseDTO> response = restTemplate.postForEntity(
                baseUrl, courseDTO, CourseDTO.class);

        // Then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());

        CourseDTO createdCourse = response.getBody();
        assertNotNull(createdCourse.getId());
        assertEquals("Spring Boot Fundamentals", createdCourse.getName());
        assertEquals("SB101", createdCourse.getCode());
        assertEquals(3, createdCourse.getCredits());

        // Verify course was actually saved in database
        Course savedCourse = courseRepository.findByCode("SB101").get();
        assertNotNull(savedCourse);
        assertEquals("Spring Boot Fundamentals", savedCourse.getName());
        assertEquals("SB101", savedCourse.getCode());
        assertEquals(3, savedCourse.getCredits());
    }

    @Test
    @DisplayName("Should return validation error for invalid course data")
    void testCreateCourseWithInvalidData() {
        // Given
        CourseDTO invalidCourse = new CourseDTO();
        invalidCourse.setName(""); // Empty name should fail validation
        invalidCourse.setCode("SB101");
        invalidCourse.setCredits(3);

        // When
        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl, invalidCourse, String.class);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        // Verify no course was saved in database
        List<Course> courses = courseRepository.findAll();
        assertTrue(courses.isEmpty());
    }

    @Test
    @DisplayName("Should return validation error for negative credits")
    void testCreateCourseWithNegativeCredits() {
        // Given
        CourseDTO invalidCourse = new CourseDTO();
        invalidCourse.setName("Spring Boot Fundamentals");
        invalidCourse.setCode("SB101");
        invalidCourse.setCredits(-1); // Negative credits should fail

        // When
        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl, invalidCourse, String.class);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    // READ COURSE TESTS

    @Test
    @DisplayName("Should retrieve course by ID through full integration")
    void testGetCourseByIdIntegration() {
        // Given - Create a course first
        Course course = new Course();
        course.setName("Advanced Java");
        course.setCode("AJ201");
        course.setCredits(4);
        Course savedCourse = courseRepository.save(course);

        // When
        ResponseEntity<CourseDTO> response = restTemplate.getForEntity(
                baseUrl + "/" + savedCourse.getId(), CourseDTO.class);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        CourseDTO retrievedCourse = response.getBody();
        assertEquals(savedCourse.getId(), retrievedCourse.getId());
        assertEquals("Advanced Java", retrievedCourse.getName());
        assertEquals("AJ201", retrievedCourse.getCode());
        assertEquals(4, retrievedCourse.getCredits());
    }

    @Test
    @DisplayName("Should return 404 for non-existent course")
    void testGetNonExistentCourse() {
        // When
        ResponseEntity<CourseDTO> response = restTemplate.getForEntity(
                baseUrl + "/999", CourseDTO.class);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("Should retrieve all courses through full integration")
    void testGetAllCoursesIntegration() {
        // Given - Create multiple courses
        Course course1 = new Course();
        course1.setName("Data Structures");
        course1.setCode("DS101");
        course1.setCredits(3);
        courseRepository.save(course1);

        Course course2 = new Course();
        course2.setName("Algorithms");
        course2.setCode("AL201");
        course2.setCredits(4);
        courseRepository.save(course2);

        // When
        ResponseEntity<CourseDTO[]> response = restTemplate.getForEntity(
                baseUrl, CourseDTO[].class);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        CourseDTO[] courses = response.getBody();
        assertEquals(2, courses.length);

        // Verify course data
        assertTrue(java.util.Arrays.stream(courses)
                .anyMatch(c -> "Data Structures".equals(c.getName())));
        assertTrue(java.util.Arrays.stream(courses)
                .anyMatch(c -> "Algorithms".equals(c.getName())));
    }

    // UPDATE COURSE TESTS

    @Test
    @DisplayName("Should update course successfully through full integration")
    void testUpdateCourseIntegration() {
        // Given - Create a course first
        Course course = new Course();
        course.setName("Web Development");
        course.setCode("WD101");
        course.setCredits(3);
        Course savedCourse = courseRepository.save(course);

        // Prepare update
        CourseDTO updateDTO = new CourseDTO();
        updateDTO.setId(savedCourse.getId());
        updateDTO.setName("Advanced Web Development");
        updateDTO.setCode("WD101");
        updateDTO.setCredits(4);

        // When
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<CourseDTO> request = new HttpEntity<>(updateDTO, headers);

        ResponseEntity<CourseDTO> response = restTemplate.exchange(
                baseUrl + "/" + savedCourse.getId(),
                HttpMethod.PUT,
                request,
                CourseDTO.class);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        CourseDTO updatedCourse = response.getBody();
        assertEquals("Advanced Web Development", updatedCourse.getName());
        assertEquals(4, updatedCourse.getCredits());

        // Verify course was actually updated in database
        Course dbCourse = courseRepository.findById(savedCourse.getId()).orElse(null);
        assertNotNull(dbCourse);
        assertEquals("Advanced Web Development", dbCourse.getName());
        assertEquals(4, dbCourse.getCredits());
    }

    @Test
    @DisplayName("Should return 404 when updating non-existent course")
    void testUpdateNonExistentCourse() {
        // Given
        CourseDTO updateDTO = new CourseDTO();
        updateDTO.setId(999L);
        updateDTO.setName("Non-existent Course");
        updateDTO.setCode("NE101");
        updateDTO.setCredits(3);

        // When
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<CourseDTO> request = new HttpEntity<>(updateDTO, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/999",
                HttpMethod.PUT,
                request,
                String.class);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    // DELETE COURSE TESTS

    @Test
    @DisplayName("Should delete course successfully through full integration")
    void testDeleteCourseIntegration() {
        // Given - Create a course first
        Course course = new Course();
        course.setName("Database Design");
        course.setCode("DD101");
        course.setCredits(3);
        Course savedCourse = courseRepository.save(course);

        // Verify course exists
        assertTrue(courseRepository.existsById(savedCourse.getId()));

        // When
        ResponseEntity<Void> response = restTemplate.exchange(
                baseUrl + "/" + savedCourse.getId(),
                HttpMethod.DELETE,
                null,
                Void.class);

        // Then
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        // Verify course was actually deleted from database
        assertFalse(courseRepository.existsById(savedCourse.getId()));
    }

    @Test
    @DisplayName("Should return 404 when deleting non-existent course")
    void testDeleteNonExistentCourse() {
        // When
        ResponseEntity<Void> response = restTemplate.exchange(
                baseUrl + "/999",
                HttpMethod.DELETE,
                null,
                Void.class);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    // MOCK MVC TESTS FOR ADDITIONAL VALIDATION

    @Test
    @DisplayName("Should handle JSON serialization/deserialization correctly")
    void testJsonSerialization() throws Exception {
        // Given
        CourseDTO courseDTO = new CourseDTO();
        courseDTO.setName("REST API Design");
        courseDTO.setCode("RA101");
        courseDTO.setCredits(3);

        String jsonContent = objectMapper.writeValueAsString(courseDTO);

        // When & Then
        mockMvc.perform(post("/api/courses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("REST API Design"))
                .andExpect(jsonPath("$.code").value("RA101"))
                .andExpect(jsonPath("$.credits").value(3))
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    @DisplayName("Should handle CORS headers correctly")
    void testCorsHeaders() throws Exception {
        // Given
        CourseDTO courseDTO = new CourseDTO();
        courseDTO.setName("CORS Test Course");
        courseDTO.setCode("CT101");
        courseDTO.setCredits(2);

        String jsonContent = objectMapper.writeValueAsString(courseDTO);

        // When & Then
        mockMvc.perform(post("/api/courses")
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
        mockMvc.perform(post("/api/courses")
                .contentType(MediaType.TEXT_PLAIN)
                .content("invalid content"))
                .andExpect(status().isUnsupportedMediaType()); // Should return 415 for unsupported media type
    }

    // EDGE CASES AND ERROR SCENARIOS

    @Test
    @DisplayName("Should handle concurrent course creation")
    void testConcurrentCourseCreation() throws InterruptedException {
        // Given
        CourseDTO courseDTO = new CourseDTO();
        courseDTO.setName("Concurrent Course");
        courseDTO.setCode("CC101");
        courseDTO.setCredits(3);

        // When - Simulate concurrent requests
        Thread[] threads = new Thread[5];
        ResponseEntity<CourseDTO>[] responses = new ResponseEntity[5];

        for (int i = 0; i < 5; i++) {
            final int index = i;
            final CourseDTO courseCopy = new CourseDTO();
            courseCopy.setName("Concurrent Course " + index);
            courseCopy.setCode("CC10" + index);
            courseCopy.setCredits(3);

            threads[i] = new Thread(() -> {
                responses[index] = restTemplate.postForEntity(baseUrl, courseCopy, CourseDTO.class);
            });
            threads[i].start();
        }

        // Wait for all threads to complete
        for (Thread thread : threads) {
            thread.join();
        }

        // Then - All requests should succeed
        for (int i = 0; i < 5; i++) {
            assertEquals(HttpStatus.CREATED, responses[i].getStatusCode());
            assertNotNull(responses[i].getBody());
        }

        // Verify all courses were saved
        List<Course> allCourses = courseRepository.findAll();
        assertEquals(5, allCourses.size());
    }

    @Test
    @DisplayName("Should handle large course names within validation limits")
    void testLargeCourseName() {
        // Given
        CourseDTO courseDTO = new CourseDTO();
        courseDTO.setName("A".repeat(100)); // Maximum allowed name length (100 characters)
        courseDTO.setCode("LC101");
        courseDTO.setCredits(3);

        // When
        ResponseEntity<CourseDTO> response = restTemplate.postForEntity(
                baseUrl, courseDTO, CourseDTO.class);

        // Then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(100, response.getBody().getName().length());
    }

    @Test
    @DisplayName("Should return validation error for course name exceeding maximum length")
    void testCourseNameTooLong() {
        // Given
        CourseDTO courseDTO = new CourseDTO();
        courseDTO.setName("A".repeat(101)); // Exceeds maximum allowed name length (100 characters)
        courseDTO.setCode("LC101");
        courseDTO.setCredits(3);

        // When
        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl, courseDTO, String.class);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("Should handle special characters in course data")
    void testSpecialCharacters() {
        // Given
        CourseDTO courseDTO = new CourseDTO();
        courseDTO.setName("Café & C++ Programming: 100% Success!");
        courseDTO.setCode("SC-101");
        courseDTO.setCredits(3);

        // When
        ResponseEntity<CourseDTO> response = restTemplate.postForEntity(
                baseUrl, courseDTO, CourseDTO.class);

        // Then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Café & C++ Programming: 100% Success!", response.getBody().getName());
        assertEquals("SC-101", response.getBody().getCode());
    }
}
