package com.rca.demo_course.service;

import com.rca.demo_course.domain.Course;
import com.rca.demo_course.domain.Grade;
import com.rca.demo_course.domain.Student;
import com.rca.demo_course.service.impl.GradeServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Grade Service Implementation Tests")
public class GradeServiceImplTest {

    private GradeServiceImpl gradeService;

    @BeforeEach
    void setUp() {
        gradeService = new GradeServiceImpl();
    }

    @Test
    @DisplayName("Should create grade with valid data")
    void testCreateGradeWithValidData() {
        // Arrange
        Grade grade = new Grade();
        Student student = new Student();
        student.setId(1L);
        Course course = new Course();
        course.setId(1L);
        grade.setStudent(student);
        grade.setCourse(course);
        grade.setScore(85.5);

        // Act
        Grade created = gradeService.create(grade);

        // Assert
        assertNotNull(created);
        assertNotNull(created.getId());
        assertEquals(student, created.getStudent());
        assertEquals(course, created.getCourse());
        assertEquals(85.5, created.getScore(), 0.001);
        assertEquals("B", created.getLetterGrade());
    }

    @Test
    @DisplayName("Should throw exception when grade is null")
    void testCreateGradeWithNullGrade() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> gradeService.create(null));
        assertEquals("Grade cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when student ID is null")
    void testCreateGradeWithNullStudentId() {
        // Arrange
        Grade grade = new Grade();
        grade.setStudent(null);
        Course course = new Course();
        course.setId(1L);
        grade.setCourse(course);
        grade.setScore(85.5);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> gradeService.create(grade));
        assertEquals("Student ID cannot be null or empty", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when course ID is null")
    void testCreateGradeWithNullCourseId() {
        // Arrange
        Grade grade = new Grade();
        Student student = new Student();
        student.setId(1L);
        grade.setStudent(student);
        grade.setCourse(null);
        grade.setScore(85.5);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> gradeService.create(grade));
        assertEquals("Course ID cannot be null or empty", exception.getMessage());
    }

    @ParameterizedTest(name = "Should throw exception for invalid score: {0}")
    @ValueSource(doubles = {-1.0, 101.0, -0.1, 100.1})
    @DisplayName("Should throw exception for invalid score values")
    void testCreateGradeWithInvalidScore(double invalidScore) {
        // Arrange
        Grade grade = new Grade();
        Student student = new Student();
        student.setId(1L);
        Course course = new Course();
        course.setId(1L);
        grade.setStudent(student);
        grade.setCourse(course);
        grade.setScore(invalidScore);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> gradeService.create(grade));
        assertEquals("Score must be between 0 and 100", exception.getMessage());
    }

    @ParameterizedTest(name = "Score {0} should result in letter grade {1}")
    @CsvSource({
            "95.0, A",
            "85.0, B",
            "75.0, C",
            "65.0, D",
            "45.0, F",
            "90.0, A",
            "80.0, B",
            "70.0, C",
            "60.0, D",
            "0.0, F"
    })
    @DisplayName("Should calculate correct letter grades")
    void testCalculateLetterGrade(double score, String expectedLetterGrade) {
        // Act
        String result = gradeService.calculateLetterGrade(score);

        // Assert
        assertEquals(expectedLetterGrade, result);
    }

    @ParameterizedTest(name = "Should throw exception for invalid score in letter grade calculation: {0}")
    @ValueSource(doubles = {-1.0, 101.0, -0.1, 100.1})
    @DisplayName("Should throw exception for invalid scores in letter grade calculation")
    void testCalculateLetterGradeWithInvalidScore(double invalidScore) {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> gradeService.calculateLetterGrade(invalidScore));
        assertEquals("Score must be between 0 and 100", exception.getMessage());
    }

    @Test
    @DisplayName("Should find grade by ID")
    void testFindGradeById() {
        // Arrange
        Grade grade = new Grade();
        Student student = new Student();
        student.setId(1L);
        Course course = new Course();
        course.setId(1L);
        grade.setStudent(student);
        grade.setCourse(course);
        grade.setScore(85.5);
        Grade created = gradeService.create(grade);

        // Act
        Grade found = gradeService.findById(created.getId().toString());

        // Assert
        assertNotNull(found);
        assertEquals(created.getId(), found.getId());
        assertEquals(student, found.getStudent());
    }

    @Test
    @DisplayName("Should return null when grade not found by ID")
    void testFindGradeByIdNotFound() {
        // Act
        Grade found = gradeService.findById("999");

        // Assert
        assertNull(found);
    }

    @Test
    @DisplayName("Should find grades by student ID")
    void testFindGradesByStudentId() {
        // Arrange
        Grade grade1 = new Grade();
        Student student1 = new Student();
        student1.setId(1L);
        Course course1 = new Course();
        course1.setId(1L);
        grade1.setStudent(student1);
        grade1.setCourse(course1);
        grade1.setScore(85.5);

        Grade grade2 = new Grade();
        Student student2 = new Student();
        student2.setId(1L);
        Course course2 = new Course();
        course2.setId(2L);
        grade2.setStudent(student2);
        grade2.setCourse(course2);
        grade2.setScore(92.0);

        Grade grade3 = new Grade();
        Student student3 = new Student();
        student3.setId(2L);
        Course course3 = new Course();
        course3.setId(1L);
        grade3.setStudent(student3);
        grade3.setCourse(course3);
        grade3.setScore(78.0);
        gradeService.create(grade1);
        gradeService.create(grade2);
        gradeService.create(grade3);

        // Act
        var studentGrades = gradeService.findByStudentId("STU001");

        // Assert
        assertNotNull(studentGrades);
        assertEquals(2, studentGrades.size());
        assertTrue(studentGrades.stream().allMatch(g -> g.getStudent().getId().equals(1L)));
    }

    @Test
    @DisplayName("Should find grades by course ID")
    void testFindGradesByCourseId() {
        // Arrange
        Grade grade1 = new Grade();
        Student student1 = new Student();
        student1.setId(1L);
        Course course1 = new Course();
        course1.setId(1L);
        grade1.setStudent(student1);
        grade1.setCourse(course1);
        grade1.setScore(85.5);

        Grade grade2 = new Grade();
        Student student2 = new Student();
        student2.setId(2L);
        Course course2 = new Course();
        course2.setId(1L);
        grade2.setStudent(student2);
        grade2.setCourse(course2);
        grade2.setScore(92.0);

        Grade grade3 = new Grade();
        Student student3 = new Student();
        student3.setId(1L);
        Course course3 = new Course();
        course3.setId(2L);
        grade3.setStudent(student3);
        grade3.setCourse(course3);
        grade3.setScore(78.0);
        gradeService.create(grade1);
        gradeService.create(grade2);
        gradeService.create(grade3);

        // Act
        var courseGrades = gradeService.findByCourseId("CS101");

        // Assert
        assertNotNull(courseGrades);
        assertEquals(2, courseGrades.size());
        assertTrue(courseGrades.stream().allMatch(g -> g.getCourse().getId().equals(1L)));
    }

    @Test
    @DisplayName("Should update existing grade")
    void testUpdateGrade() {
        // Arrange
        Grade grade = new Grade();
        Student student = new Student();
        student.setId(1L);
        Course course = new Course();
        course.setId(1L);
        grade.setStudent(student);
        grade.setCourse(course);
        grade.setScore(85.5);
        Grade created = gradeService.create(grade);
        created.setScore(92.0);

        // Act
        Grade updated = gradeService.update(created);

        // Assert
        assertNotNull(updated);
        assertEquals(92.0, updated.getScore(), 0.001);
        assertEquals("A", updated.getLetterGrade());
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent grade")
    void testUpdateNonExistentGrade() {
        // Arrange
        Grade grade = new Grade();
        grade.setId(999L);
        Student student = new Student();
        student.setId(1L);
        Course course = new Course();
        course.setId(1L);
        grade.setStudent(student);
        grade.setCourse(course);
        grade.setScore(85.5);
        grade.setLetterGrade("B");

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> gradeService.update(grade));
        assertEquals("Grade not found with ID: 999", exception.getMessage());
    }

    @Test
    @DisplayName("Should delete existing grade")
    void testDeleteGrade() {
        // Arrange
        Grade grade = new Grade();
        Student student = new Student();
        student.setId(1L);
        Course course = new Course();
        course.setId(1L);
        grade.setStudent(student);
        grade.setCourse(course);
        grade.setScore(85.5);
        Grade created = gradeService.create(grade);

        // Act
        gradeService.delete(created.getId().toString());

        // Assert
        Grade found = gradeService.findById(created.getId().toString());
        assertNull(found);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent grade")
    void testDeleteNonExistentGrade() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> gradeService.delete("999"));
        assertEquals("Grade not found with ID: 999", exception.getMessage());
    }

    @Test
    @DisplayName("Should calculate correct GPA for student")
    void testCalculateGPA() {
        // Arrange
        Grade grade1 = new Grade();
        Student student1 = new Student();
        student1.setId(1L);
        Course course1 = new Course();
        course1.setId(1L);
        grade1.setStudent(student1);
        grade1.setCourse(course1);
        grade1.setScore(90.0); // A = 4.0

        Grade grade2 = new Grade();
        Student student2 = new Student();
        student2.setId(1L);
        Course course2 = new Course();
        course2.setId(2L);
        grade2.setStudent(student2);
        grade2.setCourse(course2);
        grade2.setScore(80.0); // B = 3.0

        Grade grade3 = new Grade();
        Student student3 = new Student();
        student3.setId(1L);
        Course course3 = new Course();
        course3.setId(3L);
        grade3.setStudent(student3);
        grade3.setCourse(course3);
        grade3.setScore(70.0); // C = 2.0
        gradeService.create(grade1);
        gradeService.create(grade2);
        gradeService.create(grade3);

        // Act
        double gpa = gradeService.calculateGPA("STU001");

        // Assert
        assertEquals(3.0, gpa, 0.001); // (4.0 + 3.0 + 2.0) / 3 = 3.0
    }

    @Test
    @DisplayName("Should return 0.0 GPA for student with no grades")
    void testCalculateGPAForStudentWithNoGrades() {
        // Act
        double gpa = gradeService.calculateGPA("STU999");

        // Assert
        assertEquals(0.0, gpa, 0.001);
    }

    @Test
    @DisplayName("Should throw exception when calculating GPA with null student ID")
    void testCalculateGPAWithNullStudentId() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> gradeService.calculateGPA(null));
        assertEquals("Student ID cannot be null or empty", exception.getMessage());
    }
}

