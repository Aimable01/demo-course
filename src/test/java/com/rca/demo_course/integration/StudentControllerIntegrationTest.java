package com.rca.demo_course.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rca.demo_course.domain.Student;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for StudentController.
 * Tests the full integration from HTTP layer through Controller, Service, Repository, and Database.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DisplayName("Student Controller Integration Tests")
public class StudentControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;
    private String baseUrl;

    @BeforeEach
    void setUp() {
        // Clear database before each test
        studentRepository.deleteAll();

        // Setup MockMvc for additional testing
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        // Setup base URL for REST template
        baseUrl = "http://localhost:" + port + "/api/students";
    }

    // CREATE STUDENT TESTS

    @Test
    @DisplayName("Should create student successfully through full integration")
    void testCreateStudentIntegration() {
        // Given
        StudentDTO studentDTO = new StudentDTO();
        studentDTO.setFirstName("John");
        studentDTO.setLastName("Doe");
        studentDTO.setEmail("john.doe@example.com");

        // When
        ResponseEntity<StudentDTO> response = restTemplate.postForEntity(
                baseUrl, studentDTO, StudentDTO.class);

        // Then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());

        StudentDTO createdStudent = response.getBody();
        assertNotNull(createdStudent);
        assertNotNull(createdStudent.getId());
        assertEquals("John", createdStudent.getFirstName());
        assertEquals("Doe", createdStudent.getLastName());
        assertEquals("john.doe@example.com", createdStudent.getEmail());

        // Verify student was actually saved in database
        Student savedStudent = studentRepository.findByEmail("john.doe@example.com").orElse(null);
        assertNotNull(savedStudent);
        assertEquals("John", savedStudent.getFirstName());
        assertEquals("Doe", savedStudent.getLastName());
        assertEquals("john.doe@example.com", savedStudent.getEmail());
    }

    @Test
    @DisplayName("Should return validation error for invalid student data")
    void testCreateStudentWithInvalidData() {
        // Given
        StudentDTO invalidStudent = new StudentDTO();
        invalidStudent.setFirstName(""); // Empty first name should fail validation
        invalidStudent.setLastName("Doe");
        invalidStudent.setEmail("john.doe@example.com");

        // When
        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl, invalidStudent, String.class);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        // Verify no student was saved in database
        List<Student> students = studentRepository.findAll();
        assertTrue(students.isEmpty());
    }

    @Test
    @DisplayName("Should return validation error for invalid email format")
    void testCreateStudentWithInvalidEmail() {
        // Given
        StudentDTO invalidStudent = new StudentDTO();
        invalidStudent.setFirstName("John");
        invalidStudent.setLastName("Doe");
        invalidStudent.setEmail("invalid-email"); // Invalid email format

        // When
        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl, invalidStudent, String.class);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("Should return validation error for duplicate email")
    void testCreateStudentWithDuplicateEmail() {
        // Given - Create a student first
        Student existingStudent = new Student();
        existingStudent.setFirstName("Jane");
        existingStudent.setLastName("Smith");
        existingStudent.setEmail("john.doe@example.com");
        studentRepository.save(existingStudent);

        StudentDTO duplicateEmailStudent = new StudentDTO();
        duplicateEmailStudent.setFirstName("John");
        duplicateEmailStudent.setLastName("Doe");
        duplicateEmailStudent.setEmail("john.doe@example.com"); // Duplicate email

        // When
        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl, duplicateEmailStudent, String.class);

        // Then
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    @DisplayName("Should return validation error for name too short")
    void testCreateStudentWithNameTooShort() {
        // Given
        StudentDTO invalidStudent = new StudentDTO();
        invalidStudent.setFirstName("J"); // Too short (minimum 2 characters)
        invalidStudent.setLastName("Doe");
        invalidStudent.setEmail("john.doe@example.com");

        // When
        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl, invalidStudent, String.class);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("Should return validation error for name too long")
    void testCreateStudentWithNameTooLong() {
        // Given
        StudentDTO invalidStudent = new StudentDTO();
        invalidStudent.setFirstName("A".repeat(51)); // Too long (maximum 50 characters)
        invalidStudent.setLastName("Doe");
        invalidStudent.setEmail("john.doe@example.com");

        // When
        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl, invalidStudent, String.class);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    // READ STUDENT TESTS

    @Test
    @DisplayName("Should retrieve student by ID through full integration")
    void testGetStudentByIdIntegration() {
        // Given - Create a student first
        Student student = new Student();
        student.setFirstName("Alice");
        student.setLastName("Johnson");
        student.setEmail("alice.johnson@example.com");
        Student savedStudent = studentRepository.save(student);

        // When
        ResponseEntity<StudentDTO> response = restTemplate.getForEntity(
                baseUrl + "/" + savedStudent.getId(), StudentDTO.class);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        StudentDTO retrievedStudent = response.getBody();
        assertEquals(savedStudent.getId(), retrievedStudent.getId());
        assertEquals("Alice", retrievedStudent.getFirstName());
        assertEquals("Johnson", retrievedStudent.getLastName());
        assertEquals("alice.johnson@example.com", retrievedStudent.getEmail());
    }

    @Test
    @DisplayName("Should return 404 for non-existent student")
    void testGetNonExistentStudent() {
        // When
        ResponseEntity<StudentDTO> response = restTemplate.getForEntity(
                baseUrl + "/999", StudentDTO.class);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("Should retrieve student by email through full integration")
    void testGetStudentByEmailIntegration() {
        // Given - Create a student first
        Student student = new Student();
        student.setFirstName("Bob");
        student.setLastName("Wilson");
        student.setEmail("bob.wilson@example.com");
        studentRepository.save(student);

        // When
        ResponseEntity<StudentDTO> response = restTemplate.getForEntity(
                baseUrl + "/email/bob.wilson@example.com", StudentDTO.class);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        StudentDTO retrievedStudent = response.getBody();
        assertEquals("Bob", retrievedStudent.getFirstName());
        assertEquals("Wilson", retrievedStudent.getLastName());
        assertEquals("bob.wilson@example.com", retrievedStudent.getEmail());
    }

    @Test
    @DisplayName("Should return 404 for non-existent email")
    void testGetStudentByNonExistentEmail() {
        // When
        ResponseEntity<StudentDTO> response = restTemplate.getForEntity(
                baseUrl + "/email/nonexistent@example.com", StudentDTO.class);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("Should retrieve all students through full integration")
    void testGetAllStudentsIntegration() {
        // Given - Create multiple students
        Student student1 = new Student();
        student1.setFirstName("Charlie");
        student1.setLastName("Brown");
        student1.setEmail("charlie.brown@example.com");
        studentRepository.save(student1);

        Student student2 = new Student();
        student2.setFirstName("Diana");
        student2.setLastName("Prince");
        student2.setEmail("diana.prince@example.com");
        studentRepository.save(student2);

        // When
        ResponseEntity<StudentDTO[]> response = restTemplate.getForEntity(
                baseUrl, StudentDTO[].class);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        StudentDTO[] students = response.getBody();
        assertEquals(2, students.length);

        // Verify student data
        assertTrue(java.util.Arrays.stream(students)
                .anyMatch(s -> "Charlie".equals(s.getFirstName())));
        assertTrue(java.util.Arrays.stream(students)
                .anyMatch(s -> "Diana".equals(s.getFirstName())));
    }

    // UPDATE STUDENT TESTS

    @Test
    @DisplayName("Should update student successfully through full integration")
    void testUpdateStudentIntegration() {
        // Given - Create a student first
        Student student = new Student();
        student.setFirstName("Edward");
        student.setLastName("Norton");
        student.setEmail("edward.norton@example.com");
        Student savedStudent = studentRepository.save(student);

        // Prepare update
        StudentDTO updateDTO = new StudentDTO();
        updateDTO.setId(savedStudent.getId());
        updateDTO.setFirstName("Edward");
        updateDTO.setLastName("Norton");
        updateDTO.setEmail("ed.norton@example.com"); // Changed email

        // When
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<StudentDTO> request = new HttpEntity<>(updateDTO, headers);

        ResponseEntity<StudentDTO> response = restTemplate.exchange(
                baseUrl + "/" + savedStudent.getId(),
                HttpMethod.PUT,
                request,
                StudentDTO.class);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        StudentDTO updatedStudent = response.getBody();
        assertEquals("ed.norton@example.com", updatedStudent.getEmail());

        // Verify student was actually updated in database
        Student dbStudent = studentRepository.findById(savedStudent.getId()).orElse(null);
        assertNotNull(dbStudent);
        assertEquals("ed.norton@example.com", dbStudent.getEmail());
    }

    @Test
    @DisplayName("Should return 404 when updating non-existent student")
    void testUpdateNonExistentStudent() {
        // Given
        StudentDTO updateDTO = new StudentDTO();
        updateDTO.setId(999L);
        updateDTO.setFirstName("Non");
        updateDTO.setLastName("Existent");
        updateDTO.setEmail("non.existent@example.com");

        // When
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<StudentDTO> request = new HttpEntity<>(updateDTO, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/999",
                HttpMethod.PUT,
                request,
                String.class);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    // DELETE STUDENT TESTS

    @Test
    @DisplayName("Should delete student successfully through full integration")
    void testDeleteStudentIntegration() {
        // Given - Create a student first
        Student student = new Student();
        student.setFirstName("Frank");
        student.setLastName("Miller");
        student.setEmail("frank.miller@example.com");
        Student savedStudent = studentRepository.save(student);

        // Verify student exists
        assertTrue(studentRepository.existsById(savedStudent.getId()));

        // When
        ResponseEntity<Void> response = restTemplate.exchange(
                baseUrl + "/" + savedStudent.getId(),
                HttpMethod.DELETE,
                null,
                Void.class);

        // Then
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        // Verify student was actually deleted from database
        assertFalse(studentRepository.existsById(savedStudent.getId()));
    }

    @Test
    @DisplayName("Should return 404 when deleting non-existent student")
    void testDeleteNonExistentStudent() {
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
        StudentDTO studentDTO = new StudentDTO();
        studentDTO.setFirstName("Grace");
        studentDTO.setLastName("Hopper");
        studentDTO.setEmail("grace.hopper@example.com");

        String jsonContent = objectMapper.writeValueAsString(studentDTO);

        // When & Then
        mockMvc.perform(post("/api/students")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.firstName").value("Grace"))
                .andExpect(jsonPath("$.lastName").value("Hopper"))
                .andExpect(jsonPath("$.email").value("grace.hopper@example.com"))
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    @DisplayName("Should handle CORS headers correctly")
    void testCorsHeaders() throws Exception {
        // Given
        StudentDTO studentDTO = new StudentDTO();
        studentDTO.setFirstName("Henry");
        studentDTO.setLastName("Ford");
        studentDTO.setEmail("henry.ford@example.com");

        String jsonContent = objectMapper.writeValueAsString(studentDTO);

        // When & Then
        mockMvc.perform(post("/api/students")
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
        mockMvc.perform(post("/api/students")
                .contentType(MediaType.TEXT_PLAIN)
                .content("invalid content"))
                .andExpect(status().isUnsupportedMediaType());
    }

    // EDGE CASES AND ERROR SCENARIOS

    @Test
    @DisplayName("Should handle concurrent student creation")
    void testConcurrentStudentCreation() throws InterruptedException {
        // Given
        StudentDTO studentDTO = new StudentDTO();
        studentDTO.setFirstName("Ivy");
        studentDTO.setLastName("Chen");
        studentDTO.setEmail("ivy.chen@example.com");

        // When - Simulate concurrent requests
        Thread[] threads = new Thread[3];
        @SuppressWarnings("unchecked")
        ResponseEntity<StudentDTO>[] responses = new ResponseEntity[3];

        for (int i = 0; i < 3; i++) {
            final int index = i;
            final StudentDTO studentCopy = new StudentDTO();
            studentCopy.setFirstName("Ivy" + index);
            studentCopy.setLastName("Chen" + index);
            studentCopy.setEmail("ivy.chen" + index + "@example.com");

            threads[i] = new Thread(() -> {
                responses[index] = restTemplate.postForEntity(baseUrl, studentCopy, StudentDTO.class);
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

        // Verify all students were saved
        List<Student> allStudents = studentRepository.findAll();
        assertEquals(3, allStudents.size());
    }

    @Test
    @DisplayName("Should handle special characters in student data")
    void testSpecialCharacters() {
        // Given
        StudentDTO studentDTO = new StudentDTO();
        studentDTO.setFirstName("José");
        studentDTO.setLastName("García-López");
        studentDTO.setEmail("jose.garcia-lopez@universidad.edu");

        // When
        ResponseEntity<StudentDTO> response = restTemplate.postForEntity(
                baseUrl, studentDTO, StudentDTO.class);

        // Then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("José", response.getBody().getFirstName());
        assertEquals("García-López", response.getBody().getLastName());
        assertEquals("jose.garcia-lopez@universidad.edu", response.getBody().getEmail());
    }

    @Test
    @DisplayName("Should handle long email addresses")
    void testLongEmailAddress() {
        // Given
        StudentDTO studentDTO = new StudentDTO();
        studentDTO.setFirstName("Karen");
        studentDTO.setLastName("Smith");
        studentDTO.setEmail("karen.smith.with.very.long.name@verylonguniversityname.edu");

        // When
        ResponseEntity<StudentDTO> response = restTemplate.postForEntity(
                baseUrl, studentDTO, StudentDTO.class);

        // Then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("karen.smith.with.very.long.name@verylonguniversityname.edu", response.getBody().getEmail());
    }

    @Test
    @DisplayName("Should handle email with special characters")
    void testEmailWithSpecialCharacters() {
        // Given
        StudentDTO studentDTO = new StudentDTO();
        studentDTO.setFirstName("Liam");
        studentDTO.setLastName("O'Connor");
        studentDTO.setEmail("liam.o'connor+test@university.edu");

        // When
        ResponseEntity<StudentDTO> response = restTemplate.postForEntity(
                baseUrl, studentDTO, StudentDTO.class);

        // Then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("liam.o'connor+test@university.edu", response.getBody().getEmail());
    }
}
