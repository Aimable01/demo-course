package com.rca.demo_course.integration;

import com.rca.demo_course.domain.Student;
import com.rca.demo_course.repository.StudentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
public class StudentRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private StudentRepository studentRepository;

    @Test
    public void testSaveAndFindStudent() {
        // Given
        Student student = new Student();
        student.setFirstName("John");
        student.setLastName("Doe");
        student.setEmail("john.doe@example.com");

        // When
        Student savedStudent = studentRepository.save(student);
        entityManager.flush();

        // Then
        assertNotNull(savedStudent.getId());
        assertEquals("John", savedStudent.getFirstName());
        assertEquals("Doe", savedStudent.getLastName());
        assertEquals("john.doe@example.com", savedStudent.getEmail());
    }

    @Test
    public void testFindByEmail() {
        // Given
        Student student = new Student();
        student.setFirstName("Jane");
        student.setLastName("Smith");
        student.setEmail("jane.smith@example.com");
        studentRepository.save(student);
        entityManager.flush();

        // When
        Optional<Student> found = studentRepository.findByEmail("jane.smith@example.com");

        // Then
        assertTrue(found.isPresent());
        assertEquals("Jane", found.get().getFirstName());
        assertEquals("Smith", found.get().getLastName());
    }

    @Test
    public void testExistsByEmail() {
        // Given
        Student student = new Student();
        student.setFirstName("Mike");
        student.setLastName("Johnson");
        student.setEmail("mike.johnson@example.com");
        studentRepository.save(student);
        entityManager.flush();

        // When & Then
        assertTrue(studentRepository.existsByEmail("mike.johnson@example.com"));
        assertFalse(studentRepository.existsByEmail("nonexistent@example.com"));
    }
}

