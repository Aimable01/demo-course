package com.rca.demo_course.service.impl;

import com.rca.demo_course.domain.Course;
import com.rca.demo_course.exception.CourseNotFoundException;
import com.rca.demo_course.exception.DuplicateResourceException;
import com.rca.demo_course.exception.ValidationException;
import com.rca.demo_course.repository.CourseRepository;
import com.rca.demo_course.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CourseServiceImpl implements CourseService {

    @Autowired
    private CourseRepository courseRepository;

    @Override
    public Course create(Course course) {
        if (course == null) {
            throw new ValidationException("Course cannot be null");
        }
        if (course.getName() == null || course.getName().trim().isEmpty()) {
            throw new ValidationException("Course name cannot be null or empty");
        }
        if (course.getCode() == null || course.getCode().trim().isEmpty()) {
            throw new ValidationException("Course code cannot be null or empty");
        }
        if (course.getCredits() == null || course.getCredits() <= 0) {
            throw new ValidationException("Course credits must be positive");
        }
        if (courseRepository.existsByCode(course.getCode())) {
            throw new DuplicateResourceException("Course code already exists: " + course.getCode());
        }

        return courseRepository.save(course);
    }

    @Override
    public Course findById(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new ValidationException("Course ID cannot be null or empty");
        }
        try {
            Long courseId = Long.parseLong(id);
            Optional<Course> course = courseRepository.findById(courseId);
            return course.orElse(null);
        } catch (NumberFormatException e) {
            throw new ValidationException("Invalid course ID format: " + id);
        }
    }

    @Override
    public List<Course> findAll() {
        return courseRepository.findAll();
    }

    @Override
    public Course update(Course course) {
        if (course == null) {
            throw new ValidationException("Course cannot be null");
        }
        if (course.getId() == null) {
            throw new ValidationException("Course ID cannot be null");
        }
        if (!courseRepository.existsById(course.getId())) {
            throw new CourseNotFoundException(course.getId());
        }

        return courseRepository.save(course);
    }

    @Override
    public void delete(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new ValidationException("Course ID cannot be null or empty");
        }
        try {
            Long courseId = Long.parseLong(id);
            if (!courseRepository.existsById(courseId)) {
                throw new CourseNotFoundException(courseId);
            }
            courseRepository.deleteById(courseId);
        } catch (NumberFormatException e) {
            throw new ValidationException("Invalid course ID format: " + id);
        }
    }
}
