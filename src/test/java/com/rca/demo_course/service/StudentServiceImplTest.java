package com.rca.demo_course.service;

import com.rca.demo_course.domain.Student;
import com.rca.demo_course.exception.DuplicateResourceException;
import com.rca.demo_course.exception.StudentNotFoundException;
import com.rca.demo_course.exception.ValidationException;
import com.rca.demo_course.service.impl.StudentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Student Service Implementation Tests")
public class StudentServiceImplTest {

    private StudentServiceImpl studentService;

    @BeforeEach
    void setUp() {
        studentService = new StudentServiceImpl();
    }

    @Test
    @DisplayName("Should create student with valid data")
    void testCreateStudentWithValidData() {
        // Arrange
        Student student = new Student();
        student.setFirstName("John");
        student.setLastName("Doe");
        student.setEmail("john.doe@example.com");

        // Act
        Student created = studentService.create(student);

        // Assert
        assertNotNull(created);
        assertNotNull(created.getId());
        assertEquals("John", created.getFirstName());
        assertEquals("Doe", created.getLastName());
        assertEquals("john.doe@example.com", created.getEmail());
    }

    @Test
    @DisplayName("Should throw exception when student is null")
    void testCreateStudentWithNullStudent() {
        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class,
                () -> studentService.create(null));
        assertEquals("Student cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when first name is null")
    void testCreateStudentWithNullFirstName() {
        // Arrange
        Student student = new Student();
        student.setFirstName(null);
        student.setLastName("Doe");
        student.setEmail("john.doe@example.com");

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> studentService.create(student));
        assertEquals("First name cannot be null or empty", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when first name is empty")
    void testCreateStudentWithEmptyFirstName() {
        // Arrange
        Student student = new Student();
        student.setFirstName("");
        student.setLastName("Doe");
        student.setEmail("john.doe@example.com");

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> studentService.create(student));
        assertEquals("First name cannot be null or empty", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when last name is null")
    void testCreateStudentWithNullLastName() {
        // Arrange
        Student student = new Student();
        student.setFirstName("John");
        student.setLastName(null);
        student.setEmail("john.doe@example.com");

        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class,
                () -> studentService.create(student));
        assertEquals("Last name cannot be null or empty", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when email is null")
    void testCreateStudentWithNullEmail() {
        // Arrange
        Student student = new Student();
        student.setFirstName("John");
        student.setLastName("Doe");
        student.setEmail(null);

        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class,
                () -> studentService.create(student));
        assertEquals("Email cannot be null or empty", exception.getMessage());
    }

    @ParameterizedTest(name = "Should throw exception for invalid email: {0}")
    @ValueSource(strings = {"invalid-email", "missing@domain", "nodot@domain", ""})
    @DisplayName("Should throw exception for invalid email formats")
    void testCreateStudentWithInvalidEmail(String invalidEmail) {
        // Arrange
        Student student = new Student();
        student.setFirstName("John");
        student.setLastName("Doe");
        student.setEmail(invalidEmail);

        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class,
                () -> studentService.create(student));
        assertEquals("Invalid email format", exception.getMessage());
    }

    @Test
    @DisplayName("Should find student by ID")
    void testFindStudentById() {
        // Arrange
        Student student = new Student();
        student.setFirstName("John");
        student.setLastName("Doe");
        student.setEmail("john.doe@example.com");
        Student created = studentService.create(student);

        // Act
        Student found = studentService.findById(created.getId().toString());

        // Assert
        assertNotNull(found);
        assertEquals(created.getId(), found.getId());
        assertEquals("John", found.getFirstName());
    }

    @Test
    @DisplayName("Should return null when student not found by ID")
    void testFindStudentByIdNotFound() {
        // Act
        Student found = studentService.findById("999");

        // Assert
        assertNull(found);
    }

    @Test
    @DisplayName("Should throw exception when finding by null ID")
    void testFindStudentByNullId() {
        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class,
                () -> studentService.findById(null));
        assertEquals("Student ID cannot be null or empty", exception.getMessage());
    }

    @Test
    @DisplayName("Should find student by email")
    void testFindStudentByEmail() {
        // Arrange
        Student student = new Student();
        student.setFirstName("John");
        student.setLastName("Doe");
        student.setEmail("john.doe@example.com");
        studentService.create(student);

        // Act
        Student found = studentService.findByEmail("john.doe@example.com");

        // Assert
        assertNotNull(found);
        assertEquals("john.doe@example.com", found.getEmail());
    }

    @Test
    @DisplayName("Should return null when student not found by email")
    void testFindStudentByEmailNotFound() {
        // Act
        Student found = studentService.findByEmail("notfound@example.com");

        // Assert
        assertNull(found);
    }

    @Test
    @DisplayName("Should return all students")
    void testFindAllStudents() {
        // Arrange
        Student student1 = new Student();
        student1.setFirstName("John");
        student1.setLastName("Doe");
        student1.setEmail("john.doe@example.com");

        Student student2 = new Student();
        student2.setFirstName("Jane");
        student2.setLastName("Smith");
        student2.setEmail("jane.smith@example.com");
        studentService.create(student1);
        studentService.create(student2);

        // Act
        var allStudents = studentService.findAll();

        // Assert
        assertNotNull(allStudents);
        assertEquals(2, allStudents.size());
    }

    @Test
    @DisplayName("Should update existing student")
    void testUpdateStudent() {
        // Arrange
        Student student = new Student();
        student.setFirstName("John");
        student.setLastName("Doe");
        student.setEmail("john.doe@example.com");
        Student created = studentService.create(student);
        created.setFirstName("Johnny");

        // Act
        Student updated = studentService.update(created);

        // Assert
        assertNotNull(updated);
        assertEquals("Johnny", updated.getFirstName());
        assertEquals("Doe", updated.getLastName());
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent student")
    void testUpdateNonExistentStudent() {
        // Arrange
        Student student = new Student();
        student.setId(999L);
        student.setFirstName("John");
        student.setLastName("Doe");
        student.setEmail("john.doe@example.com");

        // Act & Assert
        StudentNotFoundException exception = assertThrows(StudentNotFoundException.class,
                () -> studentService.update(student));
        assertEquals("Student not found with ID: 999", exception.getMessage());
    }

    @Test
    @DisplayName("Should delete existing student")
    void testDeleteStudent() {
        // Arrange
        Student student = new Student();
        student.setFirstName("John");
        student.setLastName("Doe");
        student.setEmail("john.doe@example.com");
        Student created = studentService.create(student);

        // Act
        studentService.delete(created.getId().toString());

        // Assert
        Student found = studentService.findById(created.getId().toString());
        assertNull(found);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent student")
    void testDeleteNonExistentStudent() {
        // Act & Assert
        StudentNotFoundException exception = assertThrows(StudentNotFoundException.class,
                () -> studentService.delete("999"));
        assertEquals("Student not found with ID: 999", exception.getMessage());
    }
}
