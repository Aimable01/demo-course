package com.rca.demo_course.repository;

import com.rca.demo_course.domain.Grade;
import com.rca.demo_course.domain.Student;
import com.rca.demo_course.domain.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GradeRepository extends JpaRepository<Grade, Long> {
    
    List<Grade> findByStudent(Student student);
    
    List<Grade> findByStudentId(Long studentId);
    
    List<Grade> findByCourse(Course course);
    
    List<Grade> findByCourseId(Long courseId);
    
    List<Grade> findByStudentAndCourse(Student student, Course course);
    
    @Query("SELECT g FROM Grade g WHERE g.student.id = :studentId AND g.course.id = :courseId")
    List<Grade> findByStudentIdAndCourseId(@Param("studentId") Long studentId, @Param("courseId") Long courseId);
    
    @Query("SELECT AVG(g.score) FROM Grade g WHERE g.student.id = :studentId")
    Double findAverageScoreByStudentId(@Param("studentId") Long studentId);
    
    @Query("SELECT AVG(g.score) FROM Grade g WHERE g.course.id = :courseId")
    Double findAverageScoreByCourseId(@Param("courseId") Long courseId);
    
    @Query("SELECT COUNT(g) FROM Grade g WHERE g.student.id = :studentId")
    Long countByStudentId(@Param("studentId") Long studentId);
}

