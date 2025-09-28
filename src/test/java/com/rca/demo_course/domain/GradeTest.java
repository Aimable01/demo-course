package com.rca.demo_course.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;

@DisplayName("Grade Model Tests")
public class GradeTest {

    @Test
    @DisplayName("Should create grade with all fields")
    void testCreateGradeWithAllFields() {
        // Arrange
        Long id = 1L;
        Student student = new Student();
        student.setId(1L);
        Course course = new Course();
        course.setId(1L);
        BigDecimal score = new BigDecimal("85.5");
        String letterGrade = "B";

        // Act
        Grade grade = new Grade();
        grade.setId(id);
        grade.setStudent(student);
        grade.setCourse(course);
        grade.setScore(score);
        grade.setLetterGrade(letterGrade);

        // Assert
        assertNotNull(grade);
        assertEquals(id, grade.getId());
        assertEquals(student, grade.getStudent());
        assertEquals(course, grade.getCourse());
        assertEquals(score, grade.getScore());
        assertEquals(letterGrade, grade.getLetterGrade());
    }

    @Test
    @DisplayName("Should create grade with no-args constructor")
    void testCreateGradeWithNoArgsConstructor() {
        // Act
        Grade grade = new Grade();

        // Assert
        assertNotNull(grade);
        assertNull(grade.getId());
        assertNull(grade.getStudent());
        assertNull(grade.getCourse());
        assertNull(grade.getScore());
        assertNull(grade.getLetterGrade());
    }

    @Test
    @DisplayName("Should set and get grade properties")
    void testSetAndGetGradeProperties() {
        // Arrange
        Grade grade = new Grade();
        Long id = 2L;
        Student student = new Student();
        student.setId(2L);
        Course course = new Course();
        course.setId(2L);
        BigDecimal score = new BigDecimal("92.0");
        String letterGrade = "A";

        // Act
        grade.setId(id);
        grade.setStudent(student);
        grade.setCourse(course);
        grade.setScore(score);
        grade.setLetterGrade(letterGrade);

        // Assert
        assertEquals(id, grade.getId());
        assertEquals(student, grade.getStudent());
        assertEquals(course, grade.getCourse());
        assertEquals(score, grade.getScore());
        assertEquals(letterGrade, grade.getLetterGrade());
    }

    @Test
    @DisplayName("Should handle perfect score")
    void testPerfectScore() {
        // Arrange & Act
        Grade grade = new Grade();
        grade.setId(1L);
        Student student = new Student();
        student.setId(1L);
        Course course = new Course();
        course.setId(1L);
        grade.setStudent(student);
        grade.setCourse(course);
        grade.setScore(new BigDecimal("100.0"));
        grade.setLetterGrade("A");

        // Assert
        assertEquals(new BigDecimal("100.0"), grade.getScore());
        assertEquals("A", grade.getLetterGrade());
    }

    @Test
    @DisplayName("Should handle failing score")
    void testFailingScore() {
        // Arrange & Act
        Grade grade = new Grade();
        grade.setId(1L);
        Student student = new Student();
        student.setId(1L);
        Course course = new Course();
        course.setId(1L);
        grade.setStudent(student);
        grade.setCourse(course);
        grade.setScore(new BigDecimal("45.0"));
        grade.setLetterGrade("F");

        // Assert
        assertEquals(new BigDecimal("45.0"), grade.getScore());
        assertEquals("F", grade.getLetterGrade());
    }

    @Test
    @DisplayName("Should handle decimal scores")
    void testDecimalScores() {
        // Arrange & Act
        Grade grade = new Grade();
        grade.setId(1L);
        Student student = new Student();
        student.setId(1L);
        Course course = new Course();
        course.setId(1L);
        grade.setStudent(student);
        grade.setCourse(course);
        grade.setScore(new BigDecimal("87.5"));
        grade.setLetterGrade("B");

        // Assert
        assertEquals(new BigDecimal("87.5"), grade.getScore());
        assertEquals("B", grade.getLetterGrade());
    }

    @Test
    @DisplayName("Should generate correct toString")
    void testToString() {
        // Arrange
        Grade grade = new Grade();
        grade.setId(1L);
        Student student = new Student();
        student.setId(1L);
        Course course = new Course();
        course.setId(1L);
        grade.setStudent(student);
        grade.setCourse(course);
        grade.setScore(new BigDecimal("85.5"));
        grade.setLetterGrade("B");

        // Act
        String result = grade.toString();

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("1"));
    }
}

