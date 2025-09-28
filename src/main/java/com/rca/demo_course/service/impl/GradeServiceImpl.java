package com.rca.demo_course.service.impl;

import com.rca.demo_course.domain.Grade;
import com.rca.demo_course.domain.Student;
import com.rca.demo_course.domain.Course;
import com.rca.demo_course.exception.CourseNotFoundException;
import com.rca.demo_course.exception.GradeNotFoundException;
import com.rca.demo_course.exception.InvalidGradeException;
import com.rca.demo_course.exception.StudentNotFoundException;
import com.rca.demo_course.exception.ValidationException;
import com.rca.demo_course.repository.GradeRepository;
import com.rca.demo_course.repository.StudentRepository;
import com.rca.demo_course.repository.CourseRepository;
import com.rca.demo_course.service.GradeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class GradeServiceImpl implements GradeService {

    @Autowired
    private GradeRepository gradeRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Override
    public Grade create(Grade grade) {
        if (grade == null) {
            throw new ValidationException("Grade cannot be null");
        }
        if (grade.getStudent() == null) {
            throw new ValidationException("Student cannot be null");
        }
        if (grade.getCourse() == null) {
            throw new ValidationException("Course cannot be null");
        }
        if (grade.getScore() == null || grade.getScore().compareTo(BigDecimal.ZERO) < 0 || grade.getScore().compareTo(new BigDecimal("100")) > 0) {
            throw new InvalidGradeException(grade.getScore().doubleValue());
        }

        // Verify student and course exist
        if (!studentRepository.existsById(grade.getStudent().getId())) {
            throw new StudentNotFoundException(grade.getStudent().getId());
        }
        if (!courseRepository.existsById(grade.getCourse().getId())) {
            throw new CourseNotFoundException(grade.getCourse().getId());
        }

        grade.setLetterGrade(calculateLetterGrade(grade.getScore().doubleValue()));
        return gradeRepository.save(grade);
    }

    @Override
    public Grade findById(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new ValidationException("Grade ID cannot be null or empty");
        }
        try {
            Long gradeId = Long.parseLong(id);
            Optional<Grade> grade = gradeRepository.findById(gradeId);
            return grade.orElse(null);
        } catch (NumberFormatException e) {
            throw new ValidationException("Invalid grade ID format: " + id);
        }
    }

    @Override
    public List<Grade> findByStudentId(String studentId) {
        if (studentId == null || studentId.trim().isEmpty()) {
            throw new ValidationException("Student ID cannot be null or empty");
        }
        try {
            Long id = Long.parseLong(studentId);
            return gradeRepository.findByStudentId(id);
        } catch (NumberFormatException e) {
            throw new ValidationException("Invalid student ID format: " + studentId);
        }
    }

    @Override
    public List<Grade> findByCourseId(String courseId) {
        if (courseId == null || courseId.trim().isEmpty()) {
            throw new ValidationException("Course ID cannot be null or empty");
        }
        try {
            Long id = Long.parseLong(courseId);
            return gradeRepository.findByCourseId(id);
        } catch (NumberFormatException e) {
            throw new ValidationException("Invalid course ID format: " + courseId);
        }
    }

    @Override
    public Grade update(Grade grade) {
        if (grade == null) {
            throw new ValidationException("Grade cannot be null");
        }
        if (grade.getId() == null) {
            throw new ValidationException("Grade ID cannot be null");
        }
        if (!gradeRepository.existsById(grade.getId())) {
            throw new GradeNotFoundException(grade.getId());
        }
        if (grade.getScore() == null || grade.getScore().compareTo(BigDecimal.ZERO) < 0 || grade.getScore().compareTo(new BigDecimal("100")) > 0) {
            throw new InvalidGradeException(grade.getScore().doubleValue());
        }

        grade.setLetterGrade(calculateLetterGrade(grade.getScore().doubleValue()));
        return gradeRepository.save(grade);
    }

    @Override
    public void delete(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new ValidationException("Grade ID cannot be null or empty");
        }
        try {
            Long gradeId = Long.parseLong(id);
            if (!gradeRepository.existsById(gradeId)) {
                throw new GradeNotFoundException(gradeId);
            }
            gradeRepository.deleteById(gradeId);
        } catch (NumberFormatException e) {
            throw new ValidationException("Invalid grade ID format: " + id);
        }
    }

    @Override
    public String calculateLetterGrade(double score) {
        if (score < 0 || score > 100) {
            throw new InvalidGradeException(score);
        }

        if (score >= 90) return "A";
        if (score >= 80) return "B";
        if (score >= 70) return "C";
        if (score >= 60) return "D";
        return "F";
    }

    @Override
    public double calculateGPA(String studentId) {
        if (studentId == null || studentId.trim().isEmpty()) {
            throw new ValidationException("Student ID cannot be null or empty");
        }

        try {
            Long id = Long.parseLong(studentId);
            List<Grade> studentGrades = gradeRepository.findByStudentId(id);
            if (studentGrades.isEmpty()) {
                return 0.0;
            }

            double totalPoints = 0.0;
            for (Grade grade : studentGrades) {
                totalPoints += getGradePoints(grade.getLetterGrade());
            }

            return totalPoints / studentGrades.size();
        } catch (NumberFormatException e) {
            throw new ValidationException("Invalid student ID format: " + studentId);
        }
    }

    private double getGradePoints(String letterGrade) {
        switch (letterGrade) {
            case "A": return 4.0;
            case "B": return 3.0;
            case "C": return 2.0;
            case "D": return 1.0;
            case "F": return 0.0;
            default: return 0.0;
        }
    }
}
