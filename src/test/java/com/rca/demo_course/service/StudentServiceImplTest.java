package com.rca.demo_course.service;

import com.rca.demo_course.domain.Student;
import com.rca.demo_course.exception.DuplicateResourceException;
import com.rca.demo_course.exception.StudentNotFoundException;
import com.rca.demo_course.exception.ValidationException;
import com.rca.demo_course.repository.StudentRepository;
import com.rca.demo_course.service.impl.StudentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
@DisplayName("Student Service Implementation Tests")
public class StudentServiceImplTest {

    @Mock
    private StudentRepository studentRepository;

    @InjectMocks
    private StudentServiceImpl studentService;

    @BeforeEach
    void setUp() {
        // Reset mocks before each test
        reset(studentRepository);
    }

    @Test
    @DisplayName("Should create student with valid data")
    void testCreateStudentWithValidData() {
        // Arrange
        Student student = new Student();
        student.setFirstName("John");
        student.setLastName("Doe");
        student.setEmail("john.doe@example.com");

        Student savedStudent = new Student();
        savedStudent.setId(1L);
        savedStudent.setFirstName("John");
        savedStudent.setLastName("Doe");
        savedStudent.setEmail("john.doe@example.com");

        when(studentRepository.existsByEmail("john.doe@example.com")).thenReturn(false);
        when(studentRepository.save(any(Student.class))).thenReturn(savedStudent);

        // Act
        Student created = studentService.create(student);

        // Assert
        assertNotNull(created);
        assertNotNull(created.getId());
        assertEquals("John", created.getFirstName());
        assertEquals("Doe", created.getLastName());
        assertEquals("john.doe@example.com", created.getEmail());
        verify(studentRepository).existsByEmail("john.doe@example.com");
        verify(studentRepository).save(student);
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
        ValidationException exception = assertThrows(ValidationException.class,
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
        ValidationException exception = assertThrows(ValidationException.class,
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
    @ValueSource(strings = {"invalid-email", "missing@domain", "nodot@domain"})
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
    @DisplayName("Should throw exception when email is empty")
    void testCreateStudentWithEmptyEmail() {
        // Arrange
        Student student = new Student();
        student.setFirstName("John");
        student.setLastName("Doe");
        student.setEmail("");

        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class,
                () -> studentService.create(student));
        assertEquals("Email cannot be null or empty", exception.getMessage());
    }

    @Test
    @DisplayName("Should find student by ID")
    void testFindStudentById() {
        // Arrange
        Student student = new Student();
        student.setId(1L);
        student.setFirstName("John");
        student.setLastName("Doe");
        student.setEmail("john.doe@example.com");
        
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));

        // Act
        Student found = studentService.findById("1");

        // Assert
        assertNotNull(found);
        assertEquals(student.getId(), found.getId());
        assertEquals("John", found.getFirstName());
        verify(studentRepository).findById(1L);
    }

    @Test
    @DisplayName("Should return null when student not found by ID")
    void testFindStudentByIdNotFound() {
        // Arrange
        when(studentRepository.findById(999L)).thenReturn(Optional.empty());
        
        // Act
        Student found = studentService.findById("999");

        // Assert
        assertNull(found);
        verify(studentRepository).findById(999L);
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
        student.setId(1L);
        student.setFirstName("John");
        student.setLastName("Doe");
        student.setEmail("john.doe@example.com");
        
        when(studentRepository.findByEmail("john.doe@example.com")).thenReturn(Optional.of(student));

        // Act
        Student found = studentService.findByEmail("john.doe@example.com");

        // Assert
        assertNotNull(found);
        assertEquals("john.doe@example.com", found.getEmail());
        verify(studentRepository).findByEmail("john.doe@example.com");
    }

    @Test
    @DisplayName("Should return null when student not found by email")
    void testFindStudentByEmailNotFound() {
        // Arrange
        when(studentRepository.findByEmail("notfound@example.com")).thenReturn(Optional.empty());
        
        // Act
        Student found = studentService.findByEmail("notfound@example.com");

        // Assert
        assertNull(found);
        verify(studentRepository).findByEmail("notfound@example.com");
    }

    @Test
    @DisplayName("Should return all students")
    void testFindAllStudents() {
        // Arrange
        Student student1 = new Student();
        student1.setId(1L);
        student1.setFirstName("John");
        student1.setLastName("Doe");
        student1.setEmail("john.doe@example.com");

        Student student2 = new Student();
        student2.setId(2L);
        student2.setFirstName("Jane");
        student2.setLastName("Smith");
        student2.setEmail("jane.smith@example.com");
        
        List<Student> students = Arrays.asList(student1, student2);
        when(studentRepository.findAll()).thenReturn(students);

        // Act
        var allStudents = studentService.findAll();

        // Assert
        assertNotNull(allStudents);
        assertEquals(2, allStudents.size());
        verify(studentRepository).findAll();
    }

    @Test
    @DisplayName("Should update existing student")
    void testUpdateStudent() {
        // Arrange
        Student student = new Student();
        student.setId(1L);
        student.setFirstName("Johnny");
        student.setLastName("Doe");
        student.setEmail("john.doe@example.com");
        
        when(studentRepository.existsById(1L)).thenReturn(true);
        when(studentRepository.save(any(Student.class))).thenReturn(student);

        // Act
        Student updated = studentService.update(student);

        // Assert
        assertNotNull(updated);
        assertEquals("Johnny", updated.getFirstName());
        assertEquals("Doe", updated.getLastName());
        verify(studentRepository).existsById(1L);
        verify(studentRepository).save(student);
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
        
        when(studentRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        StudentNotFoundException exception = assertThrows(StudentNotFoundException.class,
                () -> studentService.update(student));
        assertEquals("Student not found with ID: 999", exception.getMessage());
        verify(studentRepository).existsById(999L);
    }

    @Test
    @DisplayName("Should delete existing student")
    void testDeleteStudent() {
        // Arrange
        Long studentId = 1L;
        when(studentRepository.existsById(studentId)).thenReturn(true);
        doNothing().when(studentRepository).deleteById(studentId);

        // Act
        studentService.delete("1");

        // Assert
        verify(studentRepository).existsById(studentId);
        verify(studentRepository).deleteById(studentId);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent student")
    void testDeleteNonExistentStudent() {
        // Arrange
        when(studentRepository.existsById(999L)).thenReturn(false);
        
        // Act & Assert
        StudentNotFoundException exception = assertThrows(StudentNotFoundException.class,
                () -> studentService.delete("999"));
        assertEquals("Student not found with ID: 999", exception.getMessage());
        verify(studentRepository).existsById(999L);
    }
}
