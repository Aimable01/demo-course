package com.rca.demo_course.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Course Model Tests")
public class CourseTest {

    @Test
    @DisplayName("Should create course with all fields")
    void testCreateCourseWithAllFields() {
        // Arrange
        Long id = 1L;
        String name = "Introduction to Programming";
        String code = "CS101";
        Integer credits = 3;

        // Act
        Course course = new Course();
        course.setId(id);
        course.setName(name);
        course.setCode(code);
        course.setCredits(credits);

        // Assert
        assertNotNull(course);
        assertEquals(id, course.getId());
        assertEquals(name, course.getName());
        assertEquals(code, course.getCode());
        assertEquals(credits, course.getCredits());
    }

    @Test
    @DisplayName("Should create course with no-args constructor")
    void testCreateCourseWithNoArgsConstructor() {
        // Act
        Course course = new Course();

        // Assert
        assertNotNull(course);
        assertNull(course.getId());
        assertNull(course.getName());
        assertNull(course.getCode());
        assertNull(course.getCredits());
    }

    @Test
    @DisplayName("Should set and get course properties")
    void testSetAndGetCourseProperties() {
        // Arrange
        Course course = new Course();
        Long id = 2L;
        String name = "Data Structures";
        String code = "CS201";
        Integer credits = 4;

        // Act
        course.setId(id);
        course.setName(name);
        course.setCode(code);
        course.setCredits(credits);

        // Assert
        assertEquals(id, course.getId());
        assertEquals(name, course.getName());
        assertEquals(code, course.getCode());
        assertEquals(credits, course.getCredits());
    }

    @Test
    @DisplayName("Should handle zero credits")
    void testZeroCredits() {
        // Arrange & Act
        Course course = new Course(1L, "Workshop", "WS001", 0, null);

        // Assert
        assertEquals(0, course.getCredits());
    }

    @Test
    @DisplayName("Should handle large credit values")
    void testLargeCredits() {
        // Arrange & Act
        Course course = new Course(1L, "Thesis", "TH999", 6, null);

        // Assert
        assertEquals(6, course.getCredits());
    }

    @Test
    @DisplayName("Should generate correct toString")
    void testToString() {
        // Arrange
        Course course = new Course(1L, "Introduction to Programming", "CS101", 3, null);

        // Act
        String result = course.toString();

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("Introduction to Programming"));
        assertTrue(result.contains("CS101"));
    }
}

