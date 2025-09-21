package com.rca.demo_course.service.impl;

import com.rca.demo_course.domain.Student;
import com.rca.demo_course.service.StudentService;

public class StudentServiceImpl implements StudentService {
    @Override
    public Student create(Student student) {
        return student;
    }

    @Override
    public Student getStudentByEmail(String surname) {
        if(surname != null && surname.length() > 0){
            return new Student("def_456","Hirwa","Peace",20);
        }
        throw new RuntimeException("Surname not provided");
    }
}
