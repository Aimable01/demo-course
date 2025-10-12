package com.rca.demo_course.integration;

import com.rca.demo_course.dto.StudentDTO;
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

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * End-to-End tests for StudentController.
 * Tests complete user workflows and real-world scenarios.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("e2e")
@DisplayName("Student Controller End-to-End Tests")
public class StudentControllerE2ETest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private StudentRepository studentRepository;

    private String baseUrl;
    private HttpHeaders headers;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/students";
        headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Clear database before each test
        studentRepository.deleteAll();
    }

    @Test
    @DisplayName("Complete Student Management Workflow (CRUD)")
    void testCompleteStudentManagementWorkflow() {
        System.out.println("🚀 Starting Complete Student Management Workflow Test");

        // STEP 1: Create a new student
        System.out.println("➕ Step 1: Creating a new student");
        StudentDTO newStudent = new StudentDTO();
        newStudent.setFirstName("Alex");
        newStudent.setLastName("Thompson");
        newStudent.setEmail("alex.thompson@university.edu");

        ResponseEntity<StudentDTO> createResponse = restTemplate.postForEntity(
                baseUrl, newStudent, StudentDTO.class);

        assertEquals(HttpStatus.CREATED, createResponse.getStatusCode());
        assertNotNull(createResponse.getBody());

        StudentDTO createdStudent = createResponse.getBody();
        assertNotNull(createdStudent);
        Long studentId = createdStudent.getId();
        System.out.println("✅ Student created with ID: " + studentId);

        // STEP 2: Retrieve the created student by ID
        System.out.println("🔍 Step 2: Retrieving the created student by ID");
        ResponseEntity<StudentDTO> getByIdResponse = restTemplate.getForEntity(
                baseUrl + "/" + studentId, StudentDTO.class);

        assertEquals(HttpStatus.OK, getByIdResponse.getStatusCode());
        assertNotNull(getByIdResponse.getBody());
        assertEquals("Alex", getByIdResponse.getBody().getFirstName());
        System.out.println("✅ Student retrieved by ID: " + getByIdResponse.getBody().getFirstName() + " " + getByIdResponse.getBody().getLastName());

        // STEP 3: Retrieve the student by email
        System.out.println("📧 Step 3: Retrieving the student by email");
        ResponseEntity<StudentDTO> getByEmailResponse = restTemplate.getForEntity(
                baseUrl + "/email/alex.thompson@university.edu", StudentDTO.class);

        assertEquals(HttpStatus.OK, getByEmailResponse.getStatusCode());
        assertNotNull(getByEmailResponse.getBody());
        assertEquals(studentId, getByEmailResponse.getBody().getId());
        System.out.println("✅ Student retrieved by email: " + getByEmailResponse.getBody().getEmail());

        // STEP 4: Update the student
        System.out.println("✏️ Step 4: Updating the student");
        StudentDTO updatedStudent = getByIdResponse.getBody();
        assertNotNull(updatedStudent);
        updatedStudent.setFirstName("Alexander");
        updatedStudent.setEmail("alexander.thompson@university.edu");

        HttpEntity<StudentDTO> updateRequest = new HttpEntity<>(updatedStudent, headers);
        ResponseEntity<StudentDTO> updateResponse = restTemplate.exchange(
                baseUrl + "/" + studentId, HttpMethod.PUT, updateRequest, StudentDTO.class);

        assertEquals(HttpStatus.OK, updateResponse.getStatusCode());
        assertNotNull(updateResponse.getBody());
        assertEquals("Alexander", updateResponse.getBody().getFirstName());
        assertEquals("alexander.thompson@university.edu", updateResponse.getBody().getEmail());
        System.out.println("✅ Student updated to: " + updateResponse.getBody().getFirstName() + " " + updateResponse.getBody().getLastName());

        // STEP 5: Verify the update by retrieving again
        System.out.println("🔄 Step 5: Verifying the update");
        ResponseEntity<StudentDTO> verifyResponse = restTemplate.getForEntity(
                baseUrl + "/" + studentId, StudentDTO.class);

        assertEquals(HttpStatus.OK, verifyResponse.getStatusCode());
        assertNotNull(verifyResponse.getBody());
        assertEquals("Alexander", verifyResponse.getBody().getFirstName());
        assertEquals("alexander.thompson@university.edu", verifyResponse.getBody().getEmail());
        System.out.println("✅ Update verified.");

        // STEP 6: Retrieve all students
        System.out.println("📋 Step 6: Retrieving all students");
        ResponseEntity<StudentDTO[]> getAllResponse = restTemplate.getForEntity(
                baseUrl, StudentDTO[].class);

        assertEquals(HttpStatus.OK, getAllResponse.getStatusCode());
        assertNotNull(getAllResponse.getBody());
        assertEquals(1, getAllResponse.getBody().length);
        System.out.println("✅ Retrieved " + getAllResponse.getBody().length + " student(s).");

        // STEP 7: Delete the student
        System.out.println("🗑️ Step 7: Deleting the student");
        restTemplate.delete(baseUrl + "/" + studentId);
        System.out.println("✅ Student with ID " + studentId + " deleted.");

        // STEP 8: Verify deletion
        System.out.println("❌ Step 8: Verifying deletion");
        ResponseEntity<StudentDTO> deletedResponse = restTemplate.getForEntity(
                baseUrl + "/" + studentId, StudentDTO.class);

        assertEquals(HttpStatus.NOT_FOUND, deletedResponse.getStatusCode());
        System.out.println("✅ Deletion verified. Student not found.");

        System.out.println("🎉 Complete Student Management Workflow Test completed successfully!");
    }

    @Test
    @DisplayName("E2E Performance and Load Test: Concurrent Student Operations")
    void testE2EPerformanceAndLoad() throws InterruptedException {
        System.out.println("⏱️ Starting E2E Performance and Load Test");

        int numberOfStudents = 50;
        int numberOfConcurrentRequests = 10;

        // Test 1: Concurrent student creation
        System.out.println("➕ Test 1: " + numberOfStudents + " concurrent student creation operations");
        Instant startTime = Instant.now();

        ExecutorService executor = Executors.newFixedThreadPool(numberOfConcurrentRequests);
        @SuppressWarnings("unchecked")
        CompletableFuture<ResponseEntity<StudentDTO>>[] futures = new CompletableFuture[numberOfStudents];

        for (int i = 0; i < numberOfStudents; i++) {
            final int index = i;
            futures[i] = CompletableFuture.supplyAsync(() -> {
                StudentDTO student = new StudentDTO();
                student.setFirstName("Student" + index);
                student.setLastName("Test" + index);
                student.setEmail("student" + index + "@test.com");
                return restTemplate.postForEntity(baseUrl, student, StudentDTO.class);
            }, executor);
        }

        // Wait for all requests to complete
        CompletableFuture.allOf(futures).join();
        executor.shutdown();

        Instant endTime = Instant.now();
        Duration duration = Duration.between(startTime, endTime);

        System.out.println("✅ Created " + numberOfStudents + " students in " +
                duration.toMillis() + "ms");
        System.out.println("📊 Average time per student: " +
                (duration.toMillis() / numberOfStudents) + "ms");

        // Test 2: Concurrent read operations
        System.out.println("🔄 Test 2: " + numberOfConcurrentRequests + " concurrent read operations");
        startTime = Instant.now();

        executor = Executors.newFixedThreadPool(numberOfConcurrentRequests);
        @SuppressWarnings("unchecked")
        CompletableFuture<ResponseEntity<StudentDTO[]>>[] readFutures = new CompletableFuture[numberOfConcurrentRequests];

        for (int i = 0; i < numberOfConcurrentRequests; i++) {
            readFutures[i] = CompletableFuture.supplyAsync(() -> {
                return restTemplate.getForEntity(baseUrl, StudentDTO[].class);
            }, executor);
        }

        // Wait for all requests to complete
        CompletableFuture.allOf(readFutures).join();
        executor.shutdown();

        endTime = Instant.now();
        duration = Duration.between(startTime, endTime);

        // Verify all requests were successful
        for (CompletableFuture<ResponseEntity<StudentDTO[]>> future : readFutures) {
            assertEquals(HttpStatus.OK, future.join().getStatusCode());
        }

        System.out.println("✅ Completed " + numberOfConcurrentRequests +
                " concurrent reads in " + duration.toMillis() + "ms");
        System.out.println("📊 Average time per concurrent request: " +
                (duration.toMillis() / numberOfConcurrentRequests) + "ms");

        System.out.println("🎉 E2E Performance and Load Test completed successfully!");
    }

    @Test
    @DisplayName("Real-world User Scenario: University Student Registration System")
    void testRealWorldUserScenario() {
        System.out.println("🌍 Starting Real-world User Scenario Test");

        // Scenario: A university administrator wants to manage student enrollment,
        // search for students, and maintain student records

        // Step 1: Bulk student registration (new semester enrollment)
        System.out.println("👨‍🎓 Step 1: Bulk student registration for new semester");

        StudentDTO[] newStudents = {
            new StudentDTO(null, "Emily", "Johnson", "emily.johnson@university.edu"),
            new StudentDTO(null, "Michael", "Brown", "michael.brown@university.edu"),
            new StudentDTO(null, "Sarah", "Davis", "sarah.davis@university.edu"),
            new StudentDTO(null, "David", "Wilson", "david.wilson@university.edu"),
            new StudentDTO(null, "Lisa", "Miller", "lisa.miller@university.edu")
        };

        for (int i = 0; i < newStudents.length; i++) {
            ResponseEntity<StudentDTO> response = restTemplate.postForEntity(
                    baseUrl, newStudents[i], StudentDTO.class);
            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            assertNotNull(response.getBody());
            newStudents[i].setId(response.getBody().getId()); // Store the ID for later use
        }
        System.out.println("✅ Registered " + newStudents.length + " new students.");

        // Step 2: Administrator searches for a specific student by email
        System.out.println("🔎 Step 2: Searching for student by email");
        ResponseEntity<StudentDTO> searchResponse = restTemplate.getForEntity(
                baseUrl + "/email/sarah.davis@university.edu", StudentDTO.class);

        assertEquals(HttpStatus.OK, searchResponse.getStatusCode());
        assertNotNull(searchResponse.getBody());
        assertEquals("Sarah", searchResponse.getBody().getFirstName());
        assertEquals("Davis", searchResponse.getBody().getLastName());
        System.out.println("✅ Found student: " + searchResponse.getBody().getFirstName() + " " + searchResponse.getBody().getLastName());

        // Step 3: Administrator updates student information (email change)
        System.out.println("✏️ Step 3: Updating student email address");
        StudentDTO studentToUpdate = searchResponse.getBody();
        assertNotNull(studentToUpdate);
        studentToUpdate.setEmail("sarah.davis.new@university.edu");

        HttpEntity<StudentDTO> updateRequest = new HttpEntity<>(studentToUpdate, headers);
        ResponseEntity<StudentDTO> updateResponse = restTemplate.exchange(
                baseUrl + "/" + studentToUpdate.getId(), HttpMethod.PUT, updateRequest, StudentDTO.class);

        assertEquals(HttpStatus.OK, updateResponse.getStatusCode());
        assertNotNull(updateResponse.getBody());
        assertEquals("sarah.davis.new@university.edu", updateResponse.getBody().getEmail());
        System.out.println("✅ Student email updated to: " + updateResponse.getBody().getEmail());

        // Step 4: Administrator generates a student roster
        System.out.println("📋 Step 4: Generating student roster");
        ResponseEntity<StudentDTO[]> rosterResponse = restTemplate.getForEntity(baseUrl, StudentDTO[].class);

        assertEquals(HttpStatus.OK, rosterResponse.getStatusCode());
        assertNotNull(rosterResponse.getBody());
        assertEquals(5, rosterResponse.getBody().length);
        System.out.println("✅ Student roster generated with " + rosterResponse.getBody().length + " students.");

        // Step 5: Administrator processes a student withdrawal
        System.out.println("📤 Step 5: Processing student withdrawal");
        Long studentToWithdrawId = newStudents[2].getId(); // Sarah Davis
        restTemplate.delete(baseUrl + "/" + studentToWithdrawId);

        // Verify withdrawal
        ResponseEntity<StudentDTO> withdrawnStudentResponse = restTemplate.getForEntity(
                baseUrl + "/" + studentToWithdrawId, StudentDTO.class);
        assertEquals(HttpStatus.NOT_FOUND, withdrawnStudentResponse.getStatusCode());
        System.out.println("✅ Student withdrawal processed successfully.");

        // Step 6: Administrator generates updated roster after withdrawal
        System.out.println("📊 Step 6: Generating updated roster after withdrawal");
        ResponseEntity<StudentDTO[]> updatedRosterResponse = restTemplate.getForEntity(baseUrl, StudentDTO[].class);

        assertEquals(HttpStatus.OK, updatedRosterResponse.getStatusCode());
        assertNotNull(updatedRosterResponse.getBody());
        assertEquals(4, updatedRosterResponse.getBody().length);
        System.out.println("✅ Updated roster has " + updatedRosterResponse.getBody().length + " students.");

        // Step 7: Administrator searches for student by partial name (simulated by checking all students)
        System.out.println("🔍 Step 7: Searching for students with specific names");
        ResponseEntity<StudentDTO[]> allStudentsResponse = restTemplate.getForEntity(baseUrl, StudentDTO[].class);
        assertEquals(HttpStatus.OK, allStudentsResponse.getStatusCode());
        assertNotNull(allStudentsResponse.getBody());

        // Count students with specific first names
        long johnsonCount = java.util.Arrays.stream(allStudentsResponse.getBody())
                .filter(s -> s.getFirstName().equals("Emily"))
                .count();
        long brownCount = java.util.Arrays.stream(allStudentsResponse.getBody())
                .filter(s -> s.getFirstName().equals("Michael"))
                .count();

        assertEquals(1, johnsonCount);
        assertEquals(1, brownCount);
        System.out.println("✅ Found Emily Johnson and Michael Brown in the system.");

        // Step 8: Administrator processes multiple student updates (bulk update simulation)
        System.out.println("📝 Step 8: Processing multiple student updates");
        StudentDTO[] studentsToUpdate = allStudentsResponse.getBody();
        int updateCount = 0;

        for (StudentDTO student : studentsToUpdate) {
            if (student.getFirstName().equals("Michael")) {
                student.setLastName("Brown-Smith"); // Name change due to marriage
                HttpEntity<StudentDTO> updateReq = new HttpEntity<>(student, headers);
                ResponseEntity<StudentDTO> updateResp = restTemplate.exchange(
                        baseUrl + "/" + student.getId(), HttpMethod.PUT, updateReq, StudentDTO.class);
                assertEquals(HttpStatus.OK, updateResp.getStatusCode());
                updateCount++;
            }
        }

        assertEquals(1, updateCount);
        System.out.println("✅ Updated " + updateCount + " student record(s).");

        // Step 9: Final roster verification
        System.out.println("✅ Step 9: Final roster verification");
        ResponseEntity<StudentDTO[]> finalRosterResponse = restTemplate.getForEntity(baseUrl, StudentDTO[].class);
        assertEquals(HttpStatus.OK, finalRosterResponse.getStatusCode());
        assertNotNull(finalRosterResponse.getBody());
        assertEquals(4, finalRosterResponse.getBody().length);

        // Verify Michael's name was updated
        boolean michaelFound = java.util.Arrays.stream(finalRosterResponse.getBody())
                .anyMatch(s -> s.getFirstName().equals("Michael") && s.getLastName().equals("Brown-Smith"));
        assertTrue(michaelFound);
        System.out.println("✅ Final roster verified with " + finalRosterResponse.getBody().length + " active students.");

        System.out.println("🎉 Real-world User Scenario Test completed successfully!");
    }

    @Test
    @DisplayName("International Student Registration Scenario")
    void testInternationalStudentRegistration() {
        System.out.println("🌍 Starting International Student Registration Scenario");

        // Step 1: Register international students with special characters in names
        System.out.println("👨‍🎓 Step 1: Registering international students");

        StudentDTO[] internationalStudents = {
            new StudentDTO(null, "José", "García-López", "jose.garcia@university.edu"),
            new StudentDTO(null, "François", "Dubois", "francois.dubois@university.edu"),
            new StudentDTO(null, "Müller", "Schmidt", "mueller.schmidt@university.edu"),
            new StudentDTO(null, "María", "Fernández", "maria.fernandez@university.edu"),
            new StudentDTO(null, "Ahmed", "Al-Rashid", "ahmed.alrashid@university.edu")
        };

        for (StudentDTO student : internationalStudents) {
            ResponseEntity<StudentDTO> response = restTemplate.postForEntity(
                    baseUrl, student, StudentDTO.class);
            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            assertNotNull(response.getBody());
            student.setId(response.getBody().getId());
        }
        System.out.println("✅ Registered " + internationalStudents.length + " international students.");

        // Step 2: Verify all students were created correctly
        System.out.println("✅ Step 2: Verifying international student records");
        ResponseEntity<StudentDTO[]> allStudentsResponse = restTemplate.getForEntity(baseUrl, StudentDTO[].class);
        assertEquals(HttpStatus.OK, allStudentsResponse.getStatusCode());
        assertNotNull(allStudentsResponse.getBody());
        assertEquals(5, allStudentsResponse.getBody().length);

        // Verify special characters are preserved
        boolean joseFound = java.util.Arrays.stream(allStudentsResponse.getBody())
                .anyMatch(s -> s.getFirstName().equals("José") && s.getLastName().equals("García-López"));
        boolean francoisFound = java.util.Arrays.stream(allStudentsResponse.getBody())
                .anyMatch(s -> s.getFirstName().equals("François") && s.getLastName().equals("Dubois"));
        boolean ahmedFound = java.util.Arrays.stream(allStudentsResponse.getBody())
                .anyMatch(s -> s.getFirstName().equals("Ahmed") && s.getLastName().equals("Al-Rashid"));

        assertTrue(joseFound);
        assertTrue(francoisFound);
        assertTrue(ahmedFound);
        System.out.println("✅ All international student records verified with special characters preserved.");

        System.out.println("🎉 International Student Registration Scenario completed successfully!");
    }
}
