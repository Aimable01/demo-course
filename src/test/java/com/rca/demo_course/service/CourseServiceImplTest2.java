package com.rca.demo_course.service;

import com.rca.demo_course.domain.Course;
import com.rca.demo_course.exception.ValidationException;
import com.rca.demo_course.repository.CourseRepository;
import com.rca.demo_course.service.impl.CourseServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CourseServiceImplTest2 {

    @Mock
    private CourseRepository courseRepository;

    @InjectMocks
    private CourseServiceImpl courseService;

    @Test
    @DisplayName("Should create course with valid data")
    void givenValidData_whenCreateCourse_thenCourseIsCreated() {
        // Arrange
        Course course = new Course();
        course.setCode("CS101");
        course.setName("Introduction to Computer Science");
        course.setCredits(3);

        Course savedCourse = new Course();
        savedCourse.setId(1L);
        savedCourse.setCode("CS101");
        savedCourse.setName("Introduction to Computer Science");
        savedCourse.setCredits(3);

        when(courseRepository.existsByCode("CS101")).thenReturn(false);
        when(courseRepository.save(course)).thenReturn(course);

        // Act
        Course createdCourse = courseService.create(course);

        // Assert
        assertNotNull(createdCourse);
        assertEquals(savedCourse.getId(), createdCourse.getId());
    }

    @Test
    @DisplayName("Should throw exception when course code is null")
    void testCreateCourse_whenCourseCodeIsNull_thenThrowException() {
        Course course = new Course();
        course.setCode(null);
        course.setName("Introduction to Computer Science");
        course.setCredits(3);

        ValidationException exception = assertThrows(ValidationException.class,
                () -> courseService.create(course));
        assertEquals("Course code cannot be null or empty", exception.getMessage());
    }
}
