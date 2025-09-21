package com.rca.demo_course.service;

import com.rca.demo_course.domain.Student;
import com.rca.demo_course.service.impl.StudentServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class StudentServiceImplTest {

    @InjectMocks
    private StudentServiceImpl studentService;

    @Test
    void testCreate_givenValues_returnCreated(){
        // Arrange
        Student student = new Student("abc_123","Shema","John",20);
        // Act
        Student created  = studentService.create(student);
        // Assert
        Assertions.assertNotNull(created.getId());
        Assertions.assertEquals(student.getId(),created.getId());
    }
}
