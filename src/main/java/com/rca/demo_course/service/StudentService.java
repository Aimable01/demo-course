package com.rca.demo_course.service;

import com.rca.demo_course.domain.Student;
import java.util.List;

public interface StudentService {
    Student create(Student student);
    Student findById(String id);
    Student findByEmail(String email);
    List<Student> findAll();
    Student update(Student student);
    void delete(String id);
}
