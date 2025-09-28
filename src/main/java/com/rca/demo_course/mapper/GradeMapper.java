package com.rca.demo_course.mapper;

import com.rca.demo_course.domain.Grade;
import com.rca.demo_course.domain.Student;
import com.rca.demo_course.domain.Course;
import com.rca.demo_course.dto.GradeDTO;
import org.springframework.stereotype.Component;

@Component
public class GradeMapper {

    public GradeDTO toDTO(Grade grade) {
        if (grade == null) {
            return null;
        }

        Long studentId = grade.getStudent() != null ? grade.getStudent().getId() : null;
        Long courseId = grade.getCourse() != null ? grade.getCourse().getId() : null;

        return new GradeDTO(
            grade.getId(),
            studentId,
            courseId,
            grade.getScore(),
            grade.getLetterGrade()
        );
    }

    public Grade toEntity(GradeDTO gradeDTO) {
        if (gradeDTO == null) {
            return null;
        }

        Grade grade = new Grade();
        grade.setId(gradeDTO.getId());
        grade.setScore(gradeDTO.getScore());
        grade.setLetterGrade(gradeDTO.getLetterGrade());

        // Set student and course references
        if (gradeDTO.getStudentId() != null) {
            Student student = new Student();
            student.setId(gradeDTO.getStudentId());
            grade.setStudent(student);
        }

        if (gradeDTO.getCourseId() != null) {
            Course course = new Course();
            course.setId(gradeDTO.getCourseId());
            grade.setCourse(course);
        }

        return grade;
    }
}
