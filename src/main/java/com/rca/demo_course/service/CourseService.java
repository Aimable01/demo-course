package com.rca.demo_course.service;

import com.rca.demo_course.domain.Course;
import java.util.List;

public interface CourseService {
    Course create(Course course);
    Course findById(String id);
    List<Course> findAll();
    Course update(Course course);
    void delete(String id);
}
