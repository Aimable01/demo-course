package com.rca.demo_course.repository;

import com.rca.demo_course.domain.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    
    Optional<Course> findByCode(String code);
    
    List<Course> findByNameContainingIgnoreCase(String name);
    
    List<Course> findByCredits(Integer credits);
    
    @Query("SELECT c FROM Course c WHERE c.credits >= :minCredits")
    List<Course> findByCreditsGreaterThanEqual(@Param("minCredits") Integer minCredits);

    boolean existsByCode(String code);
}

