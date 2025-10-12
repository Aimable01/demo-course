package com.rca.demo_course.service;

import com.rca.demo_course.domain.Course;
import com.rca.demo_course.domain.Grade;
import com.rca.demo_course.domain.Student;
import com.rca.demo_course.exception.GradeNotFoundException;
import com.rca.demo_course.exception.InvalidGradeException;
import com.rca.demo_course.exception.ValidationException;
import com.rca.demo_course.repository.CourseRepository;
import com.rca.demo_course.repository.GradeRepository;
import com.rca.demo_course.repository.StudentRepository;
import com.rca.demo_course.service.impl.GradeServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Grade Service Implementation Tests")
public class GradeServiceImplTest {

    @Mock
    private GradeRepository gradeRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private CourseRepository courseRepository;

    @InjectMocks
    private GradeServiceImpl gradeService;

    private Grade testGrade;
    private Student testStudent;
    private Course testCourse;

    @BeforeEach
    void setUp() {
        testStudent = new Student();
        testStudent.setId(1L);
        testStudent.setFirstName("John");
        testStudent.setLastName("Doe");
        testStudent.setEmail("john.doe@example.com");

        testCourse = new Course();
        testCourse.setId(1L);
        testCourse.setName("Introduction to Programming");
        testCourse.setCode("CS101");
        testCourse.setCredits(3);

        testGrade = new Grade();
        testGrade.setId(1L);
        testGrade.setStudent(testStudent);
        testGrade.setCourse(testCourse);
        testGrade.setScore(85.5);
        testGrade.setLetterGrade("B");
    }

    @Test
    @DisplayName("Should create grade with valid data")
    void testCreateGradeWithValidData() {
        // Arrange
        when(studentRepository.existsById(1L)).thenReturn(true);
        when(courseRepository.existsById(1L)).thenReturn(true);
        when(gradeRepository.save(any(Grade.class))).thenReturn(testGrade);

        // Act
        Grade created = gradeService.create(testGrade);

        // Assert
        assertNotNull(created);
        assertEquals(testGrade.getId(), created.getId());
        assertEquals(testStudent.getId(), created.getStudent().getId());
        assertEquals(testCourse.getId(), created.getCourse().getId());
        assertEquals(85.5, created.getScore(), 0.001);
        assertEquals("B", created.getLetterGrade());

        verify(studentRepository).existsById(1L);
        verify(courseRepository).existsById(1L);
        verify(gradeRepository).save(testGrade);
    }

    @Test
    @DisplayName("Should throw exception when grade is null")
    void testCreateGradeWithNullGrade() {
        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class,
                () -> gradeService.create(null));
        assertEquals("Grade cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when student ID is null")
    void testCreateGradeWithNullStudentId() {
        // Arrange
        Grade grade = new Grade();
        grade.setStudent(null);
        grade.setCourse(testCourse);
        grade.setScore(85.5);

        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class,
                () -> gradeService.create(grade));
        assertEquals("Student cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when course ID is null")
    void testCreateGradeWithNullCourseId() {
        // Arrange
        Grade grade = new Grade();
        grade.setStudent(testStudent);
        grade.setCourse(null);
        grade.setScore(85.5);

        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class,
                () -> gradeService.create(grade));
        assertEquals("Course cannot be null", exception.getMessage());
    }

    @ParameterizedTest(name = "Should throw exception for invalid score: {0}")
    @ValueSource(doubles = {-1.0, 101.0, -0.1, 100.1})
    @DisplayName("Should throw exception for invalid score values")
    void testCreateGradeWithInvalidScore(double invalidScore) {
        // Arrange
        Grade grade = new Grade();
        grade.setStudent(testStudent);
        grade.setCourse(testCourse);
        grade.setScore(invalidScore);

        // Act & Assert
        InvalidGradeException exception = assertThrows(InvalidGradeException.class,
                () -> gradeService.create(grade));
        assertTrue(exception.getMessage().contains("Invalid grade score: " + invalidScore));
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
        InvalidGradeException exception = assertThrows(InvalidGradeException.class,
                () -> gradeService.calculateLetterGrade(invalidScore));
        assertTrue(exception.getMessage().contains("Invalid grade score: " + invalidScore));
    }

    @Test
    @DisplayName("Should find grade by ID")
    void testFindGradeById() {
        // Arrange
        when(gradeRepository.findById(1L)).thenReturn(Optional.of(testGrade));

        // Act
        Grade found = gradeService.findById("1");

        // Assert
        assertNotNull(found);
        assertEquals(testGrade.getId(), found.getId());
        assertEquals(testStudent.getId(), found.getStudent().getId());
        assertEquals(testCourse.getId(), found.getCourse().getId());

        verify(gradeRepository).findById(1L);
    }

    @Test
    @DisplayName("Should return null when grade not found by ID")
    void testFindGradeByIdNotFound() {
        // Arrange
        when(gradeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        Grade found = gradeService.findById("999");

        // Assert
        assertNull(found);

        verify(gradeRepository).findById(999L);
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
        // Mock repository to return grades for student ID 1
        when(gradeRepository.findByStudentId(1L)).thenReturn(Arrays.asList(grade1, grade2));

        // Act
        var studentGrades = gradeService.findByStudentId("1");

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
        // Mock repository to return grades for course ID 1
        when(gradeRepository.findByCourseId(1L)).thenReturn(Arrays.asList(grade1, grade2));

        // Act
        var courseGrades = gradeService.findByCourseId("1");

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
        grade.setId(1L); // Set the ID for update operation
        grade.setStudent(testStudent);
        grade.setCourse(testCourse);
        grade.setScore(85.5);
        // Mock repository behavior for update operation
        when(gradeRepository.existsById(1L)).thenReturn(true);
        when(gradeRepository.save(any(Grade.class))).thenAnswer(invocation -> {
            Grade gradeToSave = invocation.getArgument(0);
            gradeToSave.setScore(92.0);
            gradeToSave.setLetterGrade("A");
            return gradeToSave;
        });

        // Act
        grade.setScore(92.0);
        Grade updated = gradeService.update(grade);

        // Assert
        assertNotNull(updated);
        assertEquals(92.0, updated.getScore(), 0.001);
        assertEquals("A", updated.getLetterGrade());

        verify(gradeRepository).existsById(1L);
        verify(gradeRepository).save(grade);
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent grade")
    void testUpdateNonExistentGrade() {
        // Arrange
        Grade grade = new Grade();
        grade.setId(999L);
        grade.setStudent(testStudent);
        grade.setCourse(testCourse);
        grade.setScore(85.5);
        grade.setLetterGrade("B");

        when(gradeRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        GradeNotFoundException exception = assertThrows(GradeNotFoundException.class,
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
        // Mock repository behavior for delete operation
        when(gradeRepository.existsById(1L)).thenReturn(true);
        doNothing().when(gradeRepository).deleteById(1L);

        // Act
        gradeService.delete("1");

        // Assert
        verify(gradeRepository).existsById(1L);
        verify(gradeRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent grade")
    void testDeleteNonExistentGrade() {
        // Arrange
        when(gradeRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        GradeNotFoundException exception = assertThrows(GradeNotFoundException.class,
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
        grade1.setLetterGrade("A");

        Grade grade2 = new Grade();
        Student student2 = new Student();
        student2.setId(1L);
        Course course2 = new Course();
        course2.setId(2L);
        grade2.setStudent(student2);
        grade2.setCourse(course2);
        grade2.setScore(80.0); // B = 3.0
        grade2.setLetterGrade("B");

        Grade grade3 = new Grade();
        Student student3 = new Student();
        student3.setId(1L);
        Course course3 = new Course();
        course3.setId(3L);
        grade3.setStudent(student3);
        grade3.setCourse(course3);
        grade3.setScore(70.0); // C = 2.0
        grade3.setLetterGrade("C");
        // Mock repository to return grades for student ID 1
        when(gradeRepository.findByStudentId(1L)).thenReturn(Arrays.asList(grade1, grade2, grade3));

        // Act
        double gpa = gradeService.calculateGPA("1");

        // Assert
        assertEquals(3.0, gpa, 0.001); // (4.0 + 3.0 + 2.0) / 3 = 3.0
    }

    @Test
    @DisplayName("Should return 0.0 GPA for student with no grades")
    void testCalculateGPAForStudentWithNoGrades() {
        // Arrange - Mock repository to return empty list for student with no grades
        when(gradeRepository.findByStudentId(999L)).thenReturn(Arrays.asList());

        // Act
        double gpa = gradeService.calculateGPA("999");

        // Assert
        assertEquals(0.0, gpa, 0.001);

        verify(gradeRepository).findByStudentId(999L);
    }

    @Test
    @DisplayName("Should throw exception when calculating GPA with null student ID")
    void testCalculateGPAWithNullStudentId() {
        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class,
                () -> gradeService.calculateGPA(null));
        assertEquals("Student ID cannot be null or empty", exception.getMessage());
    }
}

