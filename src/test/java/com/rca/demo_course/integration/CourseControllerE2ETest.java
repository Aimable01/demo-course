package com.rca.demo_course.integration;

import com.rca.demo_course.dto.CourseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * End-to-End tests for CourseController.
 * These tests simulate real user workflows and test the complete application
 * from the perspective of an external client.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("e2e")
@DisplayName("Course Controller End-to-End Tests")
public class CourseControllerE2ETest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;


    private String baseUrl;
    private HttpHeaders headers;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/courses";

        headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Accept", "application/json");
        headers.set("User-Agent", "E2E-Test-Client/1.0");
    }

    @Test
    @DisplayName("Complete Course Management Workflow - Happy Path")
    void testCompleteCourseManagementWorkflow() {
        // This test simulates a complete user journey from creating a course
        // to managing it through all CRUD operations

        System.out.println("🚀 Starting E2E Course Management Workflow Test");

        // STEP 1: Create a new course
        System.out.println("📝 Step 1: Creating a new course");
        CourseDTO newCourse = createSampleCourse("Advanced Spring Boot", "ASB401", 4);

        ResponseEntity<CourseDTO> createResponse = restTemplate.postForEntity(
                baseUrl, newCourse, CourseDTO.class);

        assertEquals(HttpStatus.CREATED, createResponse.getStatusCode());
        assertNotNull(createResponse.getBody());

        CourseDTO createdCourse = createResponse.getBody();
        assertNotNull(createdCourse);
        Long courseId = createdCourse.getId();
        System.out.println("✅ Course created with ID: " + courseId);

        // STEP 2: Retrieve the created course
        System.out.println("🔍 Step 2: Retrieving the created course");
        ResponseEntity<CourseDTO> getResponse = restTemplate.getForEntity(
                baseUrl + "/" + courseId, CourseDTO.class);

        assertEquals(HttpStatus.OK, getResponse.getStatusCode());
        assertNotNull(getResponse.getBody());
        assertEquals("Advanced Spring Boot", getResponse.getBody().getName());
        System.out.println("✅ Course retrieved successfully");

        // STEP 3: Update the course
        System.out.println("✏️ Step 3: Updating the course");
        CourseDTO updatedCourse = getResponse.getBody();
        assertNotNull(updatedCourse);
        updatedCourse.setName("Advanced Spring Boot with Microservices");
        updatedCourse.setCredits(5);

        HttpEntity<CourseDTO> updateRequest = new HttpEntity<>(updatedCourse, headers);
        ResponseEntity<CourseDTO> updateResponse = restTemplate.exchange(
                baseUrl + "/" + courseId, HttpMethod.PUT, updateRequest, CourseDTO.class);

        assertEquals(HttpStatus.OK, updateResponse.getStatusCode());
        assertNotNull(updateResponse.getBody());
        assertEquals("Advanced Spring Boot with Microservices", updateResponse.getBody().getName());
        assertEquals(5, updateResponse.getBody().getCredits());
        System.out.println("✅ Course updated successfully");

        // STEP 4: Verify the update by retrieving again
        System.out.println("🔍 Step 4: Verifying the update");
        ResponseEntity<CourseDTO> verifyResponse = restTemplate.getForEntity(
                baseUrl + "/" + courseId, CourseDTO.class);

        assertEquals(HttpStatus.OK, verifyResponse.getStatusCode());
        assertNotNull(verifyResponse.getBody());
        assertEquals("Advanced Spring Boot with Microservices", verifyResponse.getBody().getName());
        assertEquals(5, verifyResponse.getBody().getCredits());
        System.out.println("✅ Update verified successfully");

        // STEP 5: Create additional courses to test list functionality
        System.out.println("📚 Step 5: Creating additional courses");
        CourseDTO course2 = createSampleCourse("RESTful API Design", "RAD301", 3);
        CourseDTO course3 = createSampleCourse("Database Optimization", "DBO201", 2);

        restTemplate.postForEntity(baseUrl, course2, CourseDTO.class);
        restTemplate.postForEntity(baseUrl, course3, CourseDTO.class);
        System.out.println("✅ Additional courses created");

        // STEP 6: Retrieve all courses
        System.out.println("📋 Step 6: Retrieving all courses");
        ResponseEntity<CourseDTO[]> listResponse = restTemplate.getForEntity(
                baseUrl, CourseDTO[].class);

        assertEquals(HttpStatus.OK, listResponse.getStatusCode());
        assertNotNull(listResponse.getBody());
        assertTrue(listResponse.getBody().length >= 3);
        System.out.println("✅ Retrieved " + listResponse.getBody().length + " courses");

        // STEP 7: Delete the original course
        System.out.println("🗑️ Step 7: Deleting the original course");
        ResponseEntity<Void> deleteResponse = restTemplate.exchange(
                baseUrl + "/" + courseId, HttpMethod.DELETE, null, Void.class);

        assertEquals(HttpStatus.NO_CONTENT, deleteResponse.getStatusCode());
        System.out.println("✅ Course deleted successfully");

        // STEP 8: Verify deletion
        System.out.println("🔍 Step 8: Verifying deletion");
        ResponseEntity<CourseDTO> deletedResponse = restTemplate.getForEntity(
                baseUrl + "/" + courseId, CourseDTO.class);

        assertEquals(HttpStatus.NOT_FOUND, deletedResponse.getStatusCode());
        System.out.println("✅ Deletion verified - course not found");

        System.out.println("🎉 E2E Course Management Workflow completed successfully!");
    }

    @Test
    @DisplayName("Error Handling and Edge Cases Workflow")
    void testErrorHandlingWorkflow() {
        System.out.println("🚨 Starting E2E Error Handling Workflow Test");

        // Test 1: Invalid course creation
        System.out.println("❌ Test 1: Invalid course creation");
        CourseDTO invalidCourse = new CourseDTO();
        invalidCourse.setName(""); // Empty name
        invalidCourse.setCode("INVALID");
        invalidCourse.setCredits(-1); // Negative credits

        ResponseEntity<String> invalidResponse = restTemplate.postForEntity(
                baseUrl, invalidCourse, String.class);

        assertEquals(HttpStatus.BAD_REQUEST, invalidResponse.getStatusCode());
        System.out.println("✅ Invalid course creation properly rejected");

        // Test 2: Non-existent course retrieval
        System.out.println("❌ Test 2: Non-existent course retrieval");
        ResponseEntity<CourseDTO> notFoundResponse = restTemplate.getForEntity(
                baseUrl + "/99999", CourseDTO.class);

        assertEquals(HttpStatus.NOT_FOUND, notFoundResponse.getStatusCode());
        System.out.println("✅ Non-existent course properly handled");

        // Test 3: Update non-existent course
        System.out.println("❌ Test 3: Update non-existent course");
        CourseDTO updateCourse = createSampleCourse("Update Test", "UT101", 3);
        updateCourse.setId(99999L);

        HttpEntity<CourseDTO> updateRequest = new HttpEntity<>(updateCourse, headers);
        ResponseEntity<String> updateResponse = restTemplate.exchange(
                baseUrl + "/99999", HttpMethod.PUT, updateRequest, String.class);

        assertEquals(HttpStatus.NOT_FOUND, updateResponse.getStatusCode());
        System.out.println("✅ Update of non-existent course properly handled");

        // Test 4: Delete non-existent course
        System.out.println("❌ Test 4: Delete non-existent course");
        ResponseEntity<Void> deleteResponse = restTemplate.exchange(
                baseUrl + "/99999", HttpMethod.DELETE, null, Void.class);

        assertEquals(HttpStatus.NOT_FOUND, deleteResponse.getStatusCode());
        System.out.println("✅ Delete of non-existent course properly handled");

        System.out.println("🎉 E2E Error Handling Workflow completed successfully!");
    }

    @Test
    @DisplayName("Performance and Load Testing")
    void testPerformanceAndLoad() {
        System.out.println("⚡ Starting E2E Performance and Load Test");

        int numberOfCourses = 50;
        int numberOfConcurrentRequests = 10;

        // Test 1: Bulk course creation
        System.out.println("📊 Test 1: Creating " + numberOfCourses + " courses");
        Instant startTime = Instant.now();

        for (int i = 1; i <= numberOfCourses; i++) {
            CourseDTO course = createSampleCourse(
                    "Performance Test Course " + i,
                    "PTC" + String.format("%03d", i),
                    3);

            ResponseEntity<CourseDTO> response = restTemplate.postForEntity(
                    baseUrl, course, CourseDTO.class);

            assertEquals(HttpStatus.CREATED, response.getStatusCode());

            if (i % 10 == 0) {
                System.out.println("📈 Created " + i + " courses");
            }
        }

        Instant endTime = Instant.now();
        Duration duration = Duration.between(startTime, endTime);

        System.out.println("✅ Created " + numberOfCourses + " courses in " +
                duration.toMillis() + "ms");
        System.out.println("📊 Average time per course: " +
                (duration.toMillis() / numberOfCourses) + "ms");

        // Test 2: Concurrent read operations
        System.out.println("🔄 Test 2: " + numberOfConcurrentRequests + " concurrent read operations");
        startTime = Instant.now();

        ExecutorService executor = Executors.newFixedThreadPool(numberOfConcurrentRequests);
        @SuppressWarnings("unchecked")
        CompletableFuture<ResponseEntity<CourseDTO[]>>[] futures = new CompletableFuture[numberOfConcurrentRequests];

        for (int i = 0; i < numberOfConcurrentRequests; i++) {
            futures[i] = CompletableFuture.supplyAsync(() -> {
                return restTemplate.getForEntity(baseUrl, CourseDTO[].class);
            }, executor);
        }

        // Wait for all requests to complete
        CompletableFuture.allOf(futures).join();
        executor.shutdown();

        endTime = Instant.now();
        duration = Duration.between(startTime, endTime);

        // Verify all requests were successful
        for (CompletableFuture<ResponseEntity<CourseDTO[]>> future : futures) {
            assertEquals(HttpStatus.OK, future.join().getStatusCode());
        }

        System.out.println("✅ Completed " + numberOfConcurrentRequests +
                " concurrent reads in " + duration.toMillis() + "ms");
        System.out.println("📊 Average time per concurrent request: " +
                (duration.toMillis() / numberOfConcurrentRequests) + "ms");

        System.out.println("🎉 E2E Performance and Load Test completed successfully!");
    }

    @Test
    @DisplayName("Real-world User Scenario: Course Registration System")
    void testRealWorldUserScenario() {
        System.out.println("🌍 Starting Real-world User Scenario Test");

        // Scenario: A student wants to browse available courses,
        // get details about specific courses, and understand the course catalog

        // Step 1: Browse all available courses (like a student would)
        System.out.println("👨‍🎓 Step 1: Student browsing course catalog");
        ResponseEntity<CourseDTO[]> catalogResponse = restTemplate.getForEntity(
                baseUrl, CourseDTO[].class);

        assertEquals(HttpStatus.OK, catalogResponse.getStatusCode());
        System.out.println("✅ Course catalog loaded with " +
                catalogResponse.getBody().length + " courses");

        // Step 2: Student creates a new course (admin functionality)
        System.out.println("👨‍💼 Step 2: Admin creating a new course");
        CourseDTO newCourse = createSampleCourse(
                "Introduction to DevOps",
                "DEVOPS101",
                3);

        ResponseEntity<CourseDTO> createResponse = restTemplate.postForEntity(
                baseUrl, newCourse, CourseDTO.class);

        assertEquals(HttpStatus.CREATED, createResponse.getStatusCode());
        Long courseId = createResponse.getBody().getId();
        System.out.println("✅ New course 'Introduction to DevOps' created");

        // Step 3: Student gets detailed information about the course
        System.out.println("🔍 Step 3: Student getting course details");
        ResponseEntity<CourseDTO> detailsResponse = restTemplate.getForEntity(
                baseUrl + "/" + courseId, CourseDTO.class);

        assertEquals(HttpStatus.OK, detailsResponse.getStatusCode());
        assertEquals("Introduction to DevOps", detailsResponse.getBody().getName());
        assertEquals(3, detailsResponse.getBody().getCredits());
        System.out.println("✅ Course details retrieved: " +
                detailsResponse.getBody().getName() + " (" +
                detailsResponse.getBody().getCredits() + " credits)");

        // Step 4: Admin updates course information
        System.out.println("✏️ Step 4: Admin updating course information");
        CourseDTO updatedCourse = detailsResponse.getBody();
        updatedCourse.setCredits(4);
        updatedCourse.setName("Introduction to DevOps and CI/CD");

        HttpEntity<CourseDTO> updateRequest = new HttpEntity<>(updatedCourse, headers);
        ResponseEntity<CourseDTO> updateResponse = restTemplate.exchange(
                baseUrl + "/" + courseId, HttpMethod.PUT, updateRequest, CourseDTO.class);

        assertEquals(HttpStatus.OK, updateResponse.getStatusCode());
        assertEquals(4, updateResponse.getBody().getCredits());
        System.out.println("✅ Course updated: Now " +
                updateResponse.getBody().getCredits() + " credits");

        // Step 5: Student checks updated course information
        System.out.println("🔍 Step 5: Student checking updated information");
        ResponseEntity<CourseDTO> finalCheckResponse = restTemplate.getForEntity(
                baseUrl + "/" + courseId, CourseDTO.class);

        assertEquals(HttpStatus.OK, finalCheckResponse.getStatusCode());
        assertEquals("Introduction to DevOps and CI/CD", finalCheckResponse.getBody().getName());
        assertEquals(4, finalCheckResponse.getBody().getCredits());
        System.out.println("✅ Student sees updated course: " +
                finalCheckResponse.getBody().getName() + " (" +
                finalCheckResponse.getBody().getCredits() + " credits)");

        System.out.println("🎉 Real-world User Scenario completed successfully!");
        System.out.println("💡 This demonstrates how the API supports real user workflows");
    }

    @Test
    @DisplayName("API Contract and Response Format Validation")
    void testApiContractValidation() {
        System.out.println("📋 Starting API Contract Validation Test");

        // Test 1: Create course and validate response structure
        System.out.println("🔍 Test 1: Validating CREATE response structure");
        CourseDTO course = createSampleCourse("Contract Test", "CT101", 3);

        ResponseEntity<CourseDTO> createResponse = restTemplate.postForEntity(
                baseUrl, course, CourseDTO.class);

        assertEquals(HttpStatus.CREATED, createResponse.getStatusCode());
        assertNotNull(createResponse.getBody().getId());
        assertNotNull(createResponse.getBody().getName());
        assertNotNull(createResponse.getBody().getCode());
        assertTrue(createResponse.getBody().getCredits() > 0);

        System.out.println("✅ CREATE response structure validated");

        // Test 2: Validate GET response structure
        System.out.println("🔍 Test 2: Validating GET response structure");
        Long courseId = createResponse.getBody().getId();

        ResponseEntity<CourseDTO> getResponse = restTemplate.getForEntity(
                baseUrl + "/" + courseId, CourseDTO.class);

        assertEquals(HttpStatus.OK, getResponse.getStatusCode());
        assertEquals(courseId, getResponse.getBody().getId());
        assertEquals("Contract Test", getResponse.getBody().getName());

        System.out.println("✅ GET response structure validated");

        // Test 3: Validate PUT response structure
        System.out.println("🔍 Test 3: Validating PUT response structure");
        CourseDTO updatedCourse = getResponse.getBody();
        updatedCourse.setCredits(4);

        HttpEntity<CourseDTO> updateRequest = new HttpEntity<>(updatedCourse, headers);
        ResponseEntity<CourseDTO> updateResponse = restTemplate.exchange(
                baseUrl + "/" + courseId, HttpMethod.PUT, updateRequest, CourseDTO.class);

        assertEquals(HttpStatus.OK, updateResponse.getStatusCode());
        assertEquals(4, updateResponse.getBody().getCredits());

        System.out.println("✅ PUT response structure validated");

        // Test 4: Validate DELETE response structure
        System.out.println("🔍 Test 4: Validating DELETE response structure");
        ResponseEntity<Void> deleteResponse = restTemplate.exchange(
                baseUrl + "/" + courseId, HttpMethod.DELETE, null, Void.class);

        assertEquals(HttpStatus.NO_CONTENT, deleteResponse.getStatusCode());
        assertNull(deleteResponse.getBody());

        System.out.println("✅ DELETE response structure validated");

        // Test 5: Validate LIST response structure
        System.out.println("🔍 Test 5: Validating LIST response structure");
        ResponseEntity<CourseDTO[]> listResponse = restTemplate.getForEntity(
                baseUrl, CourseDTO[].class);

        assertEquals(HttpStatus.OK, listResponse.getStatusCode());
        assertNotNull(listResponse.getBody());
        assertTrue(listResponse.getBody() instanceof CourseDTO[]);

        System.out.println("✅ LIST response structure validated");

        System.out.println("🎉 API Contract Validation completed successfully!");
    }

    // Helper method to create sample courses
    private CourseDTO createSampleCourse(String name, String code, int credits) {
        CourseDTO course = new CourseDTO();
        course.setName(name);
        course.setCode(code);
        course.setCredits(credits);
        return course;
    }
}
