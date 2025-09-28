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
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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

    @BeforeEach
    void setUp() {
        // Reset mocks before each test
        reset(gradeRepository, studentRepository, courseRepository);
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
        grade.setScore(new BigDecimal("85.5"));

        Grade savedGrade = new Grade();
        savedGrade.setId(1L);
        savedGrade.setStudent(student);
        savedGrade.setCourse(course);
        savedGrade.setScore(new BigDecimal("85.5"));
        savedGrade.setLetterGrade("B");

        // Mock repository responses
        when(studentRepository.existsById(1L)).thenReturn(true);
        when(courseRepository.existsById(1L)).thenReturn(true);
        when(gradeRepository.save(any(Grade.class))).thenReturn(savedGrade);

        // Act
        Grade created = gradeService.create(grade);

        // Assert
        assertNotNull(created);
        assertNotNull(created.getId());
        assertEquals(student, created.getStudent());
        assertEquals(course, created.getCourse());
        assertEquals(new BigDecimal("85.5"), created.getScore());
        assertEquals("B", created.getLetterGrade());
        verify(studentRepository).existsById(1L);
        verify(courseRepository).existsById(1L);
        verify(gradeRepository).save(grade);
    }

    @Test
    @DisplayName("Should throw exception when grade is null")
    void testCreateGradeWithNullGrade() {
        // Act & Assert
        com.rca.demo_course.exception.ValidationException exception = assertThrows(com.rca.demo_course.exception.ValidationException.class,
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
        grade.setScore(new BigDecimal("85.5"));

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
        Student student = new Student();
        student.setId(1L);
        grade.setStudent(student);
        grade.setCourse(null);
        grade.setScore(new BigDecimal("85.5"));

        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class,
                () -> gradeService.create(grade));
        assertEquals("Course cannot be null", exception.getMessage());
    }

    @ParameterizedTest(name = "Should throw exception for invalid score: {0}")
    @ValueSource(strings = {"-1.0", "101.0", "-0.1", "100.1"})
    @DisplayName("Should throw exception for invalid score values")
    void testCreateGradeWithInvalidScore(String invalidScoreStr) {
        // Arrange
        Grade grade = new Grade();
        Student student = new Student();
        student.setId(1L);
        Course course = new Course();
        course.setId(1L);
        grade.setStudent(student);
        grade.setCourse(course);
        grade.setScore(new BigDecimal(invalidScoreStr));

        // Act & Assert
        InvalidGradeException exception = assertThrows(InvalidGradeException.class,
                () -> gradeService.create(grade));
        assertTrue(exception.getMessage().contains("Invalid grade score"));
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
        assertTrue(exception.getMessage().contains("Invalid grade score"));
    }

    @Test
    @DisplayName("Should find grade by ID")
    void testFindGradeById() {
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
        
        when(gradeRepository.findById(1L)).thenReturn(Optional.of(grade));

        // Act
        Grade found = gradeService.findById("1");

        // Assert
        assertNotNull(found);
        assertEquals(grade.getId(), found.getId());
        assertEquals(student, found.getStudent());
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
        grade1.setId(1L);
        Student student1 = new Student();
        student1.setId(1L);
        Course course1 = new Course();
        course1.setId(1L);
        grade1.setStudent(student1);
        grade1.setCourse(course1);
        grade1.setScore(new BigDecimal("85.5"));

        Grade grade2 = new Grade();
        grade2.setId(2L);
        Student student2 = new Student();
        student2.setId(1L);
        Course course2 = new Course();
        course2.setId(2L);
        grade2.setStudent(student2);
        grade2.setCourse(course2);
        grade2.setScore(new BigDecimal("92.0"));

        List<Grade> grades = Arrays.asList(grade1, grade2);
        when(gradeRepository.findByStudentId(1L)).thenReturn(grades);

        // Act
        var studentGrades = gradeService.findByStudentId("1");

        // Assert
        assertNotNull(studentGrades);
        assertEquals(2, studentGrades.size());
        assertTrue(studentGrades.stream().allMatch(g -> g.getStudent().getId().equals(1L)));
        verify(gradeRepository).findByStudentId(1L);
    }

    @Test
    @DisplayName("Should find grades by course ID")
    void testFindGradesByCourseId() {
        // Arrange
        Grade grade1 = new Grade();
        grade1.setId(1L);
        Student student1 = new Student();
        student1.setId(1L);
        Course course1 = new Course();
        course1.setId(1L);
        grade1.setStudent(student1);
        grade1.setCourse(course1);
        grade1.setScore(new BigDecimal("85.5"));

        Grade grade2 = new Grade();
        grade2.setId(2L);
        Student student2 = new Student();
        student2.setId(2L);
        Course course2 = new Course();
        course2.setId(1L);
        grade2.setStudent(student2);
        grade2.setCourse(course2);
        grade2.setScore(new BigDecimal("92.0"));

        List<Grade> grades = Arrays.asList(grade1, grade2);
        when(gradeRepository.findByCourseId(1L)).thenReturn(grades);

        // Act
        var courseGrades = gradeService.findByCourseId("1");

        // Assert
        assertNotNull(courseGrades);
        assertEquals(2, courseGrades.size());
        assertTrue(courseGrades.stream().allMatch(g -> g.getCourse().getId().equals(1L)));
        verify(gradeRepository).findByCourseId(1L);
    }

    @Test
    @DisplayName("Should update existing grade")
    void testUpdateGrade() {
        // Arrange
        Grade grade = new Grade();
        grade.setId(1L);
        Student student = new Student();
        student.setId(1L);
        Course course = new Course();
        course.setId(1L);
        grade.setStudent(student);
        grade.setCourse(course);
        grade.setScore(new BigDecimal("92.0"));
        
        when(gradeRepository.existsById(1L)).thenReturn(true);
        when(gradeRepository.save(any(Grade.class))).thenReturn(grade);

        // Act
        Grade updated = gradeService.update(grade);

        // Assert
        assertNotNull(updated);
        assertEquals(new BigDecimal("92.0"), updated.getScore());
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
        Student student = new Student();
        student.setId(1L);
        Course course = new Course();
        course.setId(1L);
        grade.setStudent(student);
        grade.setCourse(course);
        grade.setScore(new BigDecimal("85.5"));
        grade.setLetterGrade("B");
        
        when(gradeRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        GradeNotFoundException exception = assertThrows(GradeNotFoundException.class,
                () -> gradeService.update(grade));
        assertEquals("Grade not found with ID: 999", exception.getMessage());
        verify(gradeRepository).existsById(999L);
    }

    @Test
    @DisplayName("Should delete existing grade")
    void testDeleteGrade() {
        // Arrange
        Long gradeId = 1L;
        when(gradeRepository.existsById(gradeId)).thenReturn(true);
        doNothing().when(gradeRepository).deleteById(gradeId);

        // Act
        gradeService.delete("1");

        // Assert
        verify(gradeRepository).existsById(gradeId);
        verify(gradeRepository).deleteById(gradeId);
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
        verify(gradeRepository).existsById(999L);
    }

    @Test
    @DisplayName("Should calculate correct GPA for student")
    void testCalculateGPA() {
        // Arrange
        Grade grade1 = new Grade();
        grade1.setId(1L);
        Student student1 = new Student();
        student1.setId(1L);
        Course course1 = new Course();
        course1.setId(1L);
        grade1.setStudent(student1);
        grade1.setCourse(course1);
        grade1.setScore(new BigDecimal("90.0")); // A = 4.0
        grade1.setLetterGrade("A");

        Grade grade2 = new Grade();
        grade2.setId(2L);
        Student student2 = new Student();
        student2.setId(1L);
        Course course2 = new Course();
        course2.setId(2L);
        grade2.setStudent(student2);
        grade2.setCourse(course2);
        grade2.setScore(new BigDecimal("80.0")); // B = 3.0
        grade2.setLetterGrade("B");

        Grade grade3 = new Grade();
        grade3.setId(3L);
        Student student3 = new Student();
        student3.setId(1L);
        Course course3 = new Course();
        course3.setId(3L);
        grade3.setStudent(student3);
        grade3.setCourse(course3);
        grade3.setScore(new BigDecimal("70.0")); // C = 2.0
        grade3.setLetterGrade("C");
        
        List<Grade> grades = Arrays.asList(grade1, grade2, grade3);
        when(gradeRepository.findByStudentId(1L)).thenReturn(grades);

        // Act
        double gpa = gradeService.calculateGPA("1");

        // Assert
        assertEquals(3.0, gpa, 0.001); // (4.0 + 3.0 + 2.0) / 3 = 3.0
        verify(gradeRepository).findByStudentId(1L);
    }

    @Test
    @DisplayName("Should return 0.0 GPA for student with no grades")
    void testCalculateGPAForStudentWithNoGrades() {
        // Arrange
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

