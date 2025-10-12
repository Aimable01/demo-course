package com.rca.demo_course.integration;

import com.rca.demo_course.domain.Course;
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

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * End-to-End tests for GradeController.
 * Tests complete user workflows and real-world scenarios.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("e2e")
@DisplayName("Grade Controller End-to-End Tests")
public class GradeControllerE2ETest {

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

    private String baseUrl;
    private HttpHeaders headers;
    private Student testStudent;
    private Course testCourse;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/grades";
        headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Clear database before each test
        gradeRepository.deleteAll();
        studentRepository.deleteAll();
        courseRepository.deleteAll();

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

    @Test
    @DisplayName("Complete Grade Management Workflow (CRUD)")
    void testCompleteGradeManagementWorkflow() {
        System.out.println("🚀 Starting Complete Grade Management Workflow Test");

        // STEP 1: Create a new grade
        System.out.println("➕ Step 1: Creating a new grade");
        GradeDTO newGrade = new GradeDTO();
        newGrade.setStudentId(testStudent.getId());
        newGrade.setCourseId(testCourse.getId());
        newGrade.setScore(88.5);
        newGrade.setLetterGrade("B"); // Service will calculate this automatically

        ResponseEntity<GradeDTO> createResponse = restTemplate.postForEntity(
                baseUrl, newGrade, GradeDTO.class);

        assertEquals(HttpStatus.CREATED, createResponse.getStatusCode());
        assertNotNull(createResponse.getBody());

        GradeDTO createdGrade = createResponse.getBody();
        assertNotNull(createdGrade);
        Long gradeId = createdGrade.getId();
        System.out.println("✅ Grade created with ID: " + gradeId);

        // STEP 2: Retrieve the created grade
        System.out.println("🔍 Step 2: Retrieving the created grade");
        ResponseEntity<GradeDTO> getResponse = restTemplate.getForEntity(
                baseUrl + "/" + gradeId, GradeDTO.class);

        assertEquals(HttpStatus.OK, getResponse.getStatusCode());
        assertNotNull(getResponse.getBody());
        assertEquals(88.5, getResponse.getBody().getScore());
        System.out.println("✅ Grade retrieved: Score = " + getResponse.getBody().getScore());

        // STEP 3: Update the grade
        System.out.println("✏️ Step 3: Updating the grade");
        GradeDTO updatedGrade = getResponse.getBody();
        assertNotNull(updatedGrade);
        updatedGrade.setScore(92.0);
        updatedGrade.setLetterGrade("A"); // Service will calculate this automatically

        HttpEntity<GradeDTO> updateRequest = new HttpEntity<>(updatedGrade, headers);
        ResponseEntity<GradeDTO> updateResponse = restTemplate.exchange(
                baseUrl + "/" + gradeId, HttpMethod.PUT, updateRequest, GradeDTO.class);

        assertEquals(HttpStatus.OK, updateResponse.getStatusCode());
        assertNotNull(updateResponse.getBody());
        assertEquals(92.0, updateResponse.getBody().getScore());
        assertEquals("A", updateResponse.getBody().getLetterGrade());
        System.out.println("✅ Grade updated to: Score = " + updateResponse.getBody().getScore() + ", Letter = " + updateResponse.getBody().getLetterGrade());

        // STEP 4: Verify the update by retrieving again
        System.out.println("🔄 Step 4: Verifying the update");
        ResponseEntity<GradeDTO> verifyResponse = restTemplate.getForEntity(
                baseUrl + "/" + gradeId, GradeDTO.class);

        assertEquals(HttpStatus.OK, verifyResponse.getStatusCode());
        assertNotNull(verifyResponse.getBody());
        assertEquals(92.0, verifyResponse.getBody().getScore());
        assertEquals("A", verifyResponse.getBody().getLetterGrade());
        System.out.println("✅ Update verified.");

        // STEP 5: Calculate GPA for the student
        System.out.println("📊 Step 5: Calculating student GPA");
        ResponseEntity<Double> gpaResponse = restTemplate.getForEntity(
                baseUrl + "/student/" + testStudent.getId() + "/gpa", Double.class);

        assertEquals(HttpStatus.OK, gpaResponse.getStatusCode());
        assertNotNull(gpaResponse.getBody());
        assertTrue(gpaResponse.getBody() > 0);
        System.out.println("✅ Student GPA calculated: " + gpaResponse.getBody());

        // STEP 6: Delete the grade
        System.out.println("🗑️ Step 6: Deleting the grade");
        restTemplate.delete(baseUrl + "/" + gradeId);
        System.out.println("✅ Grade with ID " + gradeId + " deleted.");

        // STEP 7: Verify deletion
        System.out.println("❌ Step 7: Verifying deletion");
        ResponseEntity<GradeDTO> deletedResponse = restTemplate.getForEntity(
                baseUrl + "/" + gradeId, GradeDTO.class);

        assertEquals(HttpStatus.NOT_FOUND, deletedResponse.getStatusCode());
        System.out.println("✅ Deletion verified. Grade not found.");

        System.out.println("🎉 Complete Grade Management Workflow Test completed successfully!");
    }

    @Test
    @DisplayName("E2E Performance and Load Test: Concurrent Grade Operations")
    void testE2EPerformanceAndLoad() throws InterruptedException {
        System.out.println("⏱️ Starting E2E Performance and Load Test");

        int numberOfGrades = 30;
        int numberOfConcurrentRequests = 5;

        // Create additional test data for load testing
        Student[] students = new Student[5];
        Course[] courses = new Course[3];

        for (int i = 0; i < 5; i++) {
            students[i] = new Student();
            students[i].setFirstName("Student" + i);
            students[i].setLastName("Test" + i);
            students[i].setEmail("student" + i + "@test.com");
            students[i] = studentRepository.save(students[i]);
        }

        for (int i = 0; i < 3; i++) {
            courses[i] = new Course();
            courses[i].setName("Course " + i);
            courses[i].setCode("C" + i + "01");
            courses[i].setCredits(3);
            courses[i] = courseRepository.save(courses[i]);
        }

        // Test 1: Concurrent grade creation
        System.out.println("➕ Test 1: " + numberOfGrades + " concurrent grade creation operations");
        Instant startTime = Instant.now();

        ExecutorService executor = Executors.newFixedThreadPool(numberOfConcurrentRequests);
        @SuppressWarnings("unchecked")
        CompletableFuture<ResponseEntity<GradeDTO>>[] futures = new CompletableFuture[numberOfGrades];

        for (int i = 0; i < numberOfGrades; i++) {
            final int index = i;
            futures[i] = CompletableFuture.supplyAsync(() -> {
                GradeDTO grade = new GradeDTO();
                grade.setStudentId(students[index % 5].getId());
                grade.setCourseId(courses[index % 3].getId());
                grade.setScore(70.0 + (index % 30)); // Scores from 70 to 99
                grade.setLetterGrade("B");
                return restTemplate.postForEntity(baseUrl, grade, GradeDTO.class);
            }, executor);
        }

        // Wait for all requests to complete
        CompletableFuture.allOf(futures).join();
        executor.shutdown();

        Instant endTime = Instant.now();
        Duration duration = Duration.between(startTime, endTime);

        System.out.println("✅ Created " + numberOfGrades + " grades in " +
                duration.toMillis() + "ms");
        System.out.println("📊 Average time per grade: " +
                (duration.toMillis() / numberOfGrades) + "ms");

        // Test 2: Concurrent GPA calculations
        System.out.println("🔄 Test 2: " + numberOfConcurrentRequests + " concurrent GPA calculations");
        startTime = Instant.now();

        executor = Executors.newFixedThreadPool(numberOfConcurrentRequests);
        @SuppressWarnings("unchecked")
        CompletableFuture<ResponseEntity<Double>>[] gpaFutures = new CompletableFuture[numberOfConcurrentRequests];

        for (int i = 0; i < numberOfConcurrentRequests; i++) {
            final int studentIndex = i % 5;
            gpaFutures[i] = CompletableFuture.supplyAsync(() -> {
                return restTemplate.getForEntity(
                        baseUrl + "/student/" + students[studentIndex].getId() + "/gpa", Double.class);
            }, executor);
        }

        // Wait for all requests to complete
        CompletableFuture.allOf(gpaFutures).join();
        executor.shutdown();

        endTime = Instant.now();
        duration = Duration.between(startTime, endTime);

        // Verify all requests were successful
        for (CompletableFuture<ResponseEntity<Double>> future : gpaFutures) {
            assertEquals(HttpStatus.OK, future.join().getStatusCode());
        }

        System.out.println("✅ Completed " + numberOfConcurrentRequests +
                " concurrent GPA calculations in " + duration.toMillis() + "ms");
        System.out.println("📊 Average time per concurrent request: " +
                (duration.toMillis() / numberOfConcurrentRequests) + "ms");

        System.out.println("🎉 E2E Performance and Load Test completed successfully!");
    }

    @Test
    @DisplayName("Real-world User Scenario: Academic Grade Management System")
    void testRealWorldUserScenario() {
        System.out.println("🌍 Starting Real-world User Scenario Test");

        // Scenario: A teacher wants to manage grades for multiple students across different courses,
        // calculate GPAs, and generate reports

        // Step 1: Create multiple students
        System.out.println("👨‍🎓 Step 1: Creating multiple students");
        Student student1 = new Student();
        student1.setFirstName("Alice");
        student1.setLastName("Johnson");
        student1.setEmail("alice.johnson@university.edu");
        student1 = studentRepository.save(student1);

        Student student2 = new Student();
        student2.setFirstName("Bob");
        student2.setLastName("Smith");
        student2.setEmail("bob.smith@university.edu");
        student2 = studentRepository.save(student2);

        Student student3 = new Student();
        student3.setFirstName("Carol");
        student3.setLastName("Davis");
        student3.setEmail("carol.davis@university.edu");
        student3 = studentRepository.save(student3);

        System.out.println("✅ Created 3 students.");

        // Step 2: Create multiple courses
        System.out.println("📚 Step 2: Creating multiple courses");
        Course course1 = new Course();
        course1.setName("Introduction to Programming");
        course1.setCode("CS101");
        course1.setCredits(3);
        course1 = courseRepository.save(course1);

        Course course2 = new Course();
        course2.setName("Data Structures");
        course2.setCode("CS201");
        course2.setCredits(4);
        course2 = courseRepository.save(course2);

        Course course3 = new Course();
        course3.setName("Algorithms");
        course3.setCode("CS301");
        course3.setCredits(3);
        course3 = courseRepository.save(course3);

        System.out.println("✅ Created 3 courses.");

        // Step 3: Teacher enters grades for all students in all courses
        System.out.println("📝 Step 3: Teacher entering grades for all students");

        // Alice's grades
        GradeDTO aliceGrade1 = new GradeDTO(null, student1.getId(), course1.getId(), 95.0, "A");
        GradeDTO aliceGrade2 = new GradeDTO(null, student1.getId(), course2.getId(), 88.0, "B");
        GradeDTO aliceGrade3 = new GradeDTO(null, student1.getId(), course3.getId(), 92.0, "A");

        ResponseEntity<GradeDTO> alice1 = restTemplate.postForEntity(baseUrl, aliceGrade1, GradeDTO.class);
        ResponseEntity<GradeDTO> alice2 = restTemplate.postForEntity(baseUrl, aliceGrade2, GradeDTO.class);
        ResponseEntity<GradeDTO> alice3 = restTemplate.postForEntity(baseUrl, aliceGrade3, GradeDTO.class);

        assertEquals(HttpStatus.CREATED, alice1.getStatusCode());
        assertEquals(HttpStatus.CREATED, alice2.getStatusCode());
        assertEquals(HttpStatus.CREATED, alice3.getStatusCode());

        // Bob's grades
        GradeDTO bobGrade1 = new GradeDTO(null, student2.getId(), course1.getId(), 78.0, "C");
        GradeDTO bobGrade2 = new GradeDTO(null, student2.getId(), course2.getId(), 82.0, "B");
        GradeDTO bobGrade3 = new GradeDTO(null, student2.getId(), course3.getId(), 85.0, "B");

        ResponseEntity<GradeDTO> bob1 = restTemplate.postForEntity(baseUrl, bobGrade1, GradeDTO.class);
        ResponseEntity<GradeDTO> bob2 = restTemplate.postForEntity(baseUrl, bobGrade2, GradeDTO.class);
        ResponseEntity<GradeDTO> bob3 = restTemplate.postForEntity(baseUrl, bobGrade3, GradeDTO.class);

        assertEquals(HttpStatus.CREATED, bob1.getStatusCode());
        assertEquals(HttpStatus.CREATED, bob2.getStatusCode());
        assertEquals(HttpStatus.CREATED, bob3.getStatusCode());

        // Carol's grades
        GradeDTO carolGrade1 = new GradeDTO(null, student3.getId(), course1.getId(), 91.0, "A");
        GradeDTO carolGrade2 = new GradeDTO(null, student3.getId(), course2.getId(), 94.0, "A");
        GradeDTO carolGrade3 = new GradeDTO(null, student3.getId(), course3.getId(), 89.0, "B");

        ResponseEntity<GradeDTO> carol1 = restTemplate.postForEntity(baseUrl, carolGrade1, GradeDTO.class);
        ResponseEntity<GradeDTO> carol2 = restTemplate.postForEntity(baseUrl, carolGrade2, GradeDTO.class);
        ResponseEntity<GradeDTO> carol3 = restTemplate.postForEntity(baseUrl, carolGrade3, GradeDTO.class);

        assertEquals(HttpStatus.CREATED, carol1.getStatusCode());
        assertEquals(HttpStatus.CREATED, carol2.getStatusCode());
        assertEquals(HttpStatus.CREATED, carol3.getStatusCode());

        System.out.println("✅ All grades entered successfully.");

        // Step 4: Teacher reviews grades for a specific student (Alice)
        System.out.println("🔍 Step 4: Teacher reviewing Alice's grades");
        ResponseEntity<GradeDTO[]> aliceGradesResponse = restTemplate.getForEntity(
                baseUrl + "/student/" + student1.getId(), GradeDTO[].class);

        assertEquals(HttpStatus.OK, aliceGradesResponse.getStatusCode());
        assertNotNull(aliceGradesResponse.getBody());
        assertEquals(3, aliceGradesResponse.getBody().length);
        System.out.println("✅ Alice has " + aliceGradesResponse.getBody().length + " grades.");

        // Step 5: Teacher calculates GPA for all students
        System.out.println("📊 Step 5: Calculating GPAs for all students");
        ResponseEntity<Double> aliceGpa = restTemplate.getForEntity(
                baseUrl + "/student/" + student1.getId() + "/gpa", Double.class);
        ResponseEntity<Double> bobGpa = restTemplate.getForEntity(
                baseUrl + "/student/" + student2.getId() + "/gpa", Double.class);
        ResponseEntity<Double> carolGpa = restTemplate.getForEntity(
                baseUrl + "/student/" + student3.getId() + "/gpa", Double.class);

        assertEquals(HttpStatus.OK, aliceGpa.getStatusCode());
        assertEquals(HttpStatus.OK, bobGpa.getStatusCode());
        assertEquals(HttpStatus.OK, carolGpa.getStatusCode());

        assertNotNull(aliceGpa.getBody());
        assertNotNull(bobGpa.getBody());
        assertNotNull(carolGpa.getBody());

        System.out.println("✅ Alice's GPA: " + aliceGpa.getBody());
        System.out.println("✅ Bob's GPA: " + bobGpa.getBody());
        System.out.println("✅ Carol's GPA: " + carolGpa.getBody());

        // Step 6: Teacher reviews grades for a specific course (CS201 - Data Structures)
        System.out.println("📖 Step 6: Teacher reviewing Data Structures course grades");
        ResponseEntity<GradeDTO[]> courseGradesResponse = restTemplate.getForEntity(
                baseUrl + "/course/" + course2.getId(), GradeDTO[].class);

        assertEquals(HttpStatus.OK, courseGradesResponse.getStatusCode());
        assertNotNull(courseGradesResponse.getBody());
        assertEquals(3, courseGradesResponse.getBody().length);
        System.out.println("✅ Data Structures course has " + courseGradesResponse.getBody().length + " student grades.");

        // Step 7: Teacher updates a grade (Bob's CS201 grade from B- to B)
        System.out.println("✏️ Step 7: Teacher updating Bob's Data Structures grade");
        GradeDTO updatedBobGrade = bob2.getBody();
        assertNotNull(updatedBobGrade);
        updatedBobGrade.setScore(85.0);
        updatedBobGrade.setLetterGrade("B");

        HttpEntity<GradeDTO> updateRequest = new HttpEntity<>(updatedBobGrade, headers);
        ResponseEntity<GradeDTO> updateResponse = restTemplate.exchange(
                baseUrl + "/" + updatedBobGrade.getId(), HttpMethod.PUT, updateRequest, GradeDTO.class);

        assertEquals(HttpStatus.OK, updateResponse.getStatusCode());
        assertNotNull(updateResponse.getBody());
        assertEquals(85.0, updateResponse.getBody().getScore());
        assertEquals("B", updateResponse.getBody().getLetterGrade());
        System.out.println("✅ Bob's Data Structures grade updated to B (85.0).");

        // Step 8: Teacher recalculates Bob's GPA after the update
        System.out.println("🔄 Step 8: Recalculating Bob's GPA after grade update");
        ResponseEntity<Double> updatedBobGpa = restTemplate.getForEntity(
                baseUrl + "/student/" + student2.getId() + "/gpa", Double.class);

        assertEquals(HttpStatus.OK, updatedBobGpa.getStatusCode());
        assertNotNull(updatedBobGpa.getBody());
        System.out.println("✅ Bob's updated GPA: " + updatedBobGpa.getBody());

        // Step 9: Teacher uses letter grade calculator for a new score
        System.out.println("🧮 Step 9: Teacher calculating letter grade for score 87.5");
        ResponseEntity<String> letterGradeResponse = restTemplate.postForEntity(
                baseUrl + "/calculate-letter-grade?score=87.5", null, String.class);

        assertEquals(HttpStatus.OK, letterGradeResponse.getStatusCode());
        assertNotNull(letterGradeResponse.getBody());
        System.out.println("✅ Letter grade for 87.5: " + letterGradeResponse.getBody());

        System.out.println("🎉 Real-world User Scenario Test completed successfully!");
    }
}
