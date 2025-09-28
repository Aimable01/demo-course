package com.rca.demo_course.service;

import com.rca.demo_course.domain.Grade;
import java.util.List;

public interface GradeService {
    Grade create(Grade grade);
    Grade findById(String id);
    List<Grade> findByStudentId(String studentId);
    List<Grade> findByCourseId(String courseId);
    Grade update(Grade grade);
    void delete(String id);
    String calculateLetterGrade(double score);
    double calculateGPA(String studentId);
}

