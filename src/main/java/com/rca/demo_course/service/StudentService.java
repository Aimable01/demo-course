package com.rca.demo_course.service;

import com.rca.demo_course.domain.Student;

public interface StudentService {
    Student create(Student student);

    Student getStudentByEmail(String email);
}
