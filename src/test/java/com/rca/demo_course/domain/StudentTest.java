package com.rca.demo_course.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Student Model Tests")
public class StudentTest {

    @Test
    @DisplayName("Should create student with all fields")
    void testCreateStudentWithAllFields() {
        // Arrange
        Long id = 1L;
        String firstName = "John";
        String lastName = "Doe";
        String email = "john.doe@example.com";

        // Act
        Student student = new Student();
        student.setId(id);
        student.setFirstName(firstName);
        student.setLastName(lastName);
        student.setEmail(email);

        // Assert
        assertNotNull(student);
        assertEquals(id, student.getId());
        assertEquals(firstName, student.getFirstName());
        assertEquals(lastName, student.getLastName());
        assertEquals(email, student.getEmail());
    }

    @Test
    @DisplayName("Should create student with no-args constructor")
    void testCreateStudentWithNoArgsConstructor() {
        // Act
        Student student = new Student();

        // Assert
        assertNotNull(student);
        assertNull(student.getId());
        assertNull(student.getFirstName());
        assertNull(student.getLastName());
        assertNull(student.getEmail());
    }

    @Test
    @DisplayName("Should set and get student properties")
    void testSetAndGetStudentProperties() {
        // Arrange
        Student student = new Student();
        Long id = 2L;
        String firstName = "Jane";
        String lastName = "Smith";
        String email = "jane.smith@example.com";

        // Act
        student.setId(id);
        student.setFirstName(firstName);
        student.setLastName(lastName);
        student.setEmail(email);

        // Assert
        assertEquals(id, student.getId());
        assertEquals(firstName, student.getFirstName());
        assertEquals(lastName, student.getLastName());
        assertEquals(email, student.getEmail());
    }

    @Test
    @DisplayName("Should generate correct toString")
    void testToString() {
        // Arrange
        Student student = new Student();
        student.setId(1L);
        student.setFirstName("John");
        student.setLastName("Doe");
        student.setEmail("john.doe@example.com");

        // Act
        String result = student.toString();

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("John"));
        assertTrue(result.contains("Doe"));
        assertTrue(result.contains("john.doe@example.com"));
    }

    @Test
    @DisplayName("Should be equal when all fields match")
    void testEquals() {
        // Arrange
        Student student1 = new Student();
        student1.setId(1L);
        student1.setFirstName("John");
        student1.setLastName("Doe");
        student1.setEmail("john.doe@example.com");

        Student student2 = new Student();
        student2.setId(1L);
        student2.setFirstName("John");
        student2.setLastName("Doe");
        student2.setEmail("john.doe@example.com");

        // Act & Assert
        assertEquals(student1, student2);
    }

    @Test
    @DisplayName("Should have same hash code when equal")
    void testHashCode() {
        // Arrange
        Student student1 = new Student();
        student1.setId(1L);
        student1.setFirstName("John");
        student1.setLastName("Doe");
        student1.setEmail("john.doe@example.com");

        Student student2 = new Student();
        student2.setId(1L);
        student2.setFirstName("John");
        student2.setLastName("Doe");
        student2.setEmail("john.doe@example.com");

        // Act & Assert
        assertEquals(student1.hashCode(), student2.hashCode());
    }
}

