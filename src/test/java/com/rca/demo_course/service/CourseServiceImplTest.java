package com.rca.demo_course.service;

import com.rca.demo_course.domain.Course;
import com.rca.demo_course.exception.CourseNotFoundException;
import com.rca.demo_course.exception.DuplicateResourceException;
import com.rca.demo_course.exception.ValidationException;
import com.rca.demo_course.repository.CourseRepository;
import com.rca.demo_course.service.impl.CourseServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Course Service Implementation Tests")
public class CourseServiceImplTest {

    @Mock
    private CourseRepository courseRepository;

    @InjectMocks
    private CourseServiceImpl courseService;

    private Course testCourse;

    @BeforeEach
    void setUp() {
        testCourse = new Course();
        testCourse.setId(1L);
        testCourse.setName("Introduction to Programming");
        testCourse.setCode("CS101");
        testCourse.setCredits(3);
    }

    @Test
    @DisplayName("Should create course with valid data")
    void testCreateCourseWithValidData() {
        // Arrange
        when(courseRepository.existsByCode("CS101")).thenReturn(false);
        when(courseRepository.save(any(Course.class))).thenReturn(testCourse);

        // Act
        Course created = courseService.create(testCourse);

        // Assert
        assertNotNull(created);
        assertEquals(testCourse.getId(), created.getId());
        assertEquals("Introduction to Programming", created.getName());
        assertEquals("CS101", created.getCode());
        assertEquals(3, created.getCredits());

        verify(courseRepository).existsByCode("CS101");
        verify(courseRepository).save(testCourse);
    }

    @Test
    @DisplayName("Should throw exception when course is null")
    void testCreateCourseWithNullCourse() {
        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class,
                () -> courseService.create(null));
        assertEquals("Course cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when course name is null")
    void testCreateCourseWithNullName() {
        // Arrange
        Course course = new Course();
        course.setName(null);
        course.setCode("CS101");
        course.setCredits(3);

        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class,
                () -> courseService.create(course));
        assertEquals("Course name cannot be null or empty", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when course name is empty")
    void testCreateCourseWithEmptyName() {
        // Arrange
        Course course = new Course();
        course.setName("");
        course.setCode("CS101");
        course.setCredits(3);

        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class,
                () -> courseService.create(course));
        assertEquals("Course name cannot be null or empty", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when course code is null")
    void testCreateCourseWithNullCode() {
        // Arrange
        Course course = new Course();
        course.setName("Introduction to Programming");
        course.setCode(null);
        course.setCredits(3);

        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class,
                () -> courseService.create(course));
        assertEquals("Course code cannot be null or empty", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when course code is empty")
    void testCreateCourseWithEmptyCode() {
        // Arrange
        Course course = new Course();
        course.setName("Introduction to Programming");
        course.setCode("");
        course.setCredits(3);

        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class,
                () -> courseService.create(course));
        assertEquals("Course code cannot be null or empty", exception.getMessage());
    }

    @ParameterizedTest(name = "Should throw exception for invalid credits: {0}")
    @ValueSource(ints = {0, -1, -5})
    @DisplayName("Should throw exception for invalid credit values")
    void testCreateCourseWithInvalidCredits(int invalidCredits) {
        // Arrange
        Course course = new Course();
        course.setName("Introduction to Programming");
        course.setCode("CS101");
        course.setCredits(invalidCredits);

        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class,
                () -> courseService.create(course));
        assertEquals("Course credits must be positive", exception.getMessage());
    }

    @Test
    @DisplayName("Should find course by ID")
    void testFindCourseById() {
        // Arrange
        when(courseRepository.findById(1L)).thenReturn(Optional.of(testCourse));

        // Act
        Course found = courseService.findById("1");

        // Assert
        assertNotNull(found);
        assertEquals(testCourse.getId(), found.getId());
        assertEquals("Introduction to Programming", found.getName());

        verify(courseRepository).findById(1L);
    }

    @Test
    @DisplayName("Should return null when course not found by ID")
    void testFindCourseByIdNotFound() {
        // Arrange
        when(courseRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        Course found = courseService.findById("999");

        // Assert
        assertNull(found);

        verify(courseRepository).findById(999L);
    }

    @Test
    @DisplayName("Should throw exception when finding by null ID")
    void testFindCourseByNullId() {
        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class,
                () -> courseService.findById(null));
        assertEquals("Course ID cannot be null or empty", exception.getMessage());
    }

    @Test
    @DisplayName("Should return all courses")
    void testFindAllCourses() {
        // Arrange
        Course course2 = new Course();
        course2.setId(2L);
        course2.setName("Data Structures");
        course2.setCode("CS201");
        course2.setCredits(4);

        List<Course> courses = Arrays.asList(testCourse, course2);
        when(courseRepository.findAll()).thenReturn(courses);

        // Act
        var allCourses = courseService.findAll();

        // Assert
        assertNotNull(allCourses);
        assertEquals(2, allCourses.size());
        assertEquals("Introduction to Programming", allCourses.get(0).getName());
        assertEquals("Data Structures", allCourses.get(1).getName());

        verify(courseRepository).findAll();
    }

    @Test
    @DisplayName("Should update existing course")
    void testUpdateCourse() {
        // Arrange
        when(courseRepository.existsById(1L)).thenReturn(true);
        when(courseRepository.save(any(Course.class))).thenReturn(testCourse);

        Course updatedCourse = new Course();
        updatedCourse.setId(1L);
        updatedCourse.setName("Advanced Programming");
        updatedCourse.setCode("CS101");
        updatedCourse.setCredits(3);

        // Act
        Course updated = courseService.update(updatedCourse);

        // Assert
        assertNotNull(updated);
        assertEquals("Introduction to Programming", updated.getName());
        assertEquals("CS101", updated.getCode());

        verify(courseRepository).existsById(1L);
        verify(courseRepository).save(updatedCourse);
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent course")
    void testUpdateNonExistentCourse() {
        // Arrange
        when(courseRepository.existsById(999L)).thenReturn(false);

        Course course = new Course();
        course.setId(999L);
        course.setName("Introduction to Programming");
        course.setCode("CS101");
        course.setCredits(3);

        // Act & Assert
        CourseNotFoundException exception = assertThrows(CourseNotFoundException.class,
                () -> courseService.update(course));
        assertEquals("Course not found with ID: 999", exception.getMessage());
    }

    @Test
    @DisplayName("Should delete existing course")
    void testDeleteCourse() {
        // Arrange
        when(courseRepository.existsById(1L)).thenReturn(true);
        doNothing().when(courseRepository).deleteById(1L);

        // Act
        courseService.delete("1");

        // Assert
        verify(courseRepository).existsById(1L);
        verify(courseRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent course")
    void testDeleteNonExistentCourse() {
        // Arrange
        when(courseRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        CourseNotFoundException exception = assertThrows(CourseNotFoundException.class,
                () -> courseService.delete("999"));
        assertEquals("Course not found with ID: 999", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when creating course with duplicate code")
    void testCreateCourseWithDuplicateCode() {
        // Arrange
        when(courseRepository.existsByCode("CS101")).thenReturn(true);

        // Act & Assert
        DuplicateResourceException exception = assertThrows(DuplicateResourceException.class,
                () -> courseService.create(testCourse));
        assertEquals("Course code already exists: CS101", exception.getMessage());

        verify(courseRepository).existsByCode("CS101");
        verify(courseRepository, never()).save(any(Course.class));
    }
}

