package com.rca.demo_course.service;

import com.rca.demo_course.domain.Course;
import com.rca.demo_course.service.impl.CourseServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Course Service Implementation Tests")
public class CourseServiceImplTest {

    private CourseServiceImpl courseService;

    @BeforeEach
    void setUp() {
        courseService = new CourseServiceImpl();
    }

    @Test
    @DisplayName("Should create course with valid data")
    void testCreateCourseWithValidData() {
        // Arrange
        Course course = new Course();
        course.setName("Introduction to Programming");
        course.setCode("CS101");
        course.setCredits(3);

        // Act
        Course created = courseService.create(course);

        // Assert
        assertNotNull(created);
        assertNotNull(created.getId());
        assertEquals("Introduction to Programming", created.getName());
        assertEquals("CS101", created.getCode());
        assertEquals(3, created.getCredits());
    }

    @Test
    @DisplayName("Should throw exception when course is null")
    void testCreateCourseWithNullCourse() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
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
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
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
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
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
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
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
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
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
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> courseService.create(course));
        assertEquals("Course credits must be positive", exception.getMessage());
    }

    @Test
    @DisplayName("Should find course by ID")
    void testFindCourseById() {
        // Arrange
        Course course = new Course();
        course.setName("Introduction to Programming");
        course.setCode("CS101");
        course.setCredits(3);
        Course created = courseService.create(course);

        // Act
        Course found = courseService.findById(created.getId().toString());

        // Assert
        assertNotNull(found);
        assertEquals(created.getId(), found.getId());
        assertEquals("Introduction to Programming", found.getName());
    }

    @Test
    @DisplayName("Should return null when course not found by ID")
    void testFindCourseByIdNotFound() {
        // Act
        Course found = courseService.findById("999");

        // Assert
        assertNull(found);
    }

    @Test
    @DisplayName("Should throw exception when finding by null ID")
    void testFindCourseByNullId() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> courseService.findById(null));
        assertEquals("Course ID cannot be null or empty", exception.getMessage());
    }

    @Test
    @DisplayName("Should return all courses")
    void testFindAllCourses() {
        // Arrange
        Course course1 = new Course();
        course1.setName("Introduction to Programming");
        course1.setCode("CS101");
        course1.setCredits(3);

        Course course2 = new Course();
        course2.setName("Data Structures");
        course2.setCode("CS201");
        course2.setCredits(4);
        courseService.create(course1);
        courseService.create(course2);

        // Act
        var allCourses = courseService.findAll();

        // Assert
        assertNotNull(allCourses);
        assertEquals(2, allCourses.size());
    }

    @Test
    @DisplayName("Should update existing course")
    void testUpdateCourse() {
        // Arrange
        Course course = new Course();
        course.setName("Introduction to Programming");
        course.setCode("CS101");
        course.setCredits(3);
        Course created = courseService.create(course);
        created.setName("Advanced Programming");

        // Act
        Course updated = courseService.update(created);

        // Assert
        assertNotNull(updated);
        assertEquals("Advanced Programming", updated.getName());
        assertEquals("CS101", updated.getCode());
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent course")
    void testUpdateNonExistentCourse() {
        // Arrange
        Course course = new Course();
        course.setId(999L);
        course.setName("Introduction to Programming");
        course.setCode("CS101");
        course.setCredits(3);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> courseService.update(course));
        assertEquals("Course not found with ID: 999", exception.getMessage());
    }

    @Test
    @DisplayName("Should delete existing course")
    void testDeleteCourse() {
        // Arrange
        Course course = new Course();
        course.setName("Introduction to Programming");
        course.setCode("CS101");
        course.setCredits(3);
        Course created = courseService.create(course);

        // Act
        courseService.delete(created.getId().toString());

        // Assert
        Course found = courseService.findById(created.getId().toString());
        assertNull(found);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent course")
    void testDeleteNonExistentCourse() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> courseService.delete("999"));
        assertEquals("Course not found with ID: 999", exception.getMessage());
    }
}

