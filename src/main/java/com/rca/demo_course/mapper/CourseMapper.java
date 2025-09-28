package com.rca.demo_course.mapper;

import com.rca.demo_course.domain.Course;
import com.rca.demo_course.dto.CourseDTO;
import org.springframework.stereotype.Component;

@Component
public class CourseMapper {

    public CourseDTO toDTO(Course course) {
        if (course == null) {
            return null;
        }

        return new CourseDTO(
            course.getId(),
            course.getName(),
            course.getCode(),
            course.getCredits()
        );
    }

    public Course toEntity(CourseDTO courseDTO) {
        if (courseDTO == null) {
            return null;
        }

        Course course = new Course();
        course.setId(courseDTO.getId());
        course.setName(courseDTO.getName());
        course.setCode(courseDTO.getCode());
        course.setCredits(courseDTO.getCredits());

        return course;
    }
}
