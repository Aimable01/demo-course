package com.rca.demo_course.service.impl;

import com.rca.demo_course.domain.Student;
import com.rca.demo_course.exception.DuplicateResourceException;
import com.rca.demo_course.exception.StudentNotFoundException;
import com.rca.demo_course.exception.ValidationException;
import com.rca.demo_course.repository.StudentRepository;
import com.rca.demo_course.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class StudentServiceImpl implements StudentService {

    @Autowired
    private StudentRepository studentRepository;

    @Override
    public Student create(Student student) {
        if (student == null) {
            throw new ValidationException("Student cannot be null");
        }
        if (student.getFirstName() == null || student.getFirstName().trim().isEmpty()) {
            throw new ValidationException("First name cannot be null or empty");
        }
        if (student.getLastName() == null || student.getLastName().trim().isEmpty()) {
            throw new ValidationException("Last name cannot be null or empty");
        }
        if (student.getEmail() == null || student.getEmail().trim().isEmpty()) {
            throw new ValidationException("Email cannot be null or empty");
        }
        if (!isValidEmail(student.getEmail())) {
            throw new ValidationException("Invalid email format");
        }
        if (studentRepository.existsByEmail(student.getEmail())) {
            throw new DuplicateResourceException("Email already exists: " + student.getEmail());
        }

        return studentRepository.save(student);
    }

    @Override
    public Student findById(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new ValidationException("Student ID cannot be null or empty");
        }
        try {
            Long studentId = Long.parseLong(id);
            Optional<Student> student = studentRepository.findById(studentId);
            return student.orElse(null);
        } catch (NumberFormatException e) {
            throw new ValidationException("Invalid student ID format: " + id);
        }
    }

    @Override
    public Student findByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new ValidationException("Email cannot be null or empty");
        }
        Optional<Student> student = studentRepository.findByEmail(email);
        return student.orElse(null);
    }

    @Override
    public List<Student> findAll() {
        return studentRepository.findAll();
    }

    @Override
    public Student update(Student student) {
        if (student == null) {
            throw new ValidationException("Student cannot be null");
        }
        if (student.getId() == null) {
            throw new ValidationException("Student ID cannot be null");
        }
        if (!studentRepository.existsById(student.getId())) {
            throw new StudentNotFoundException(student.getId());
        }

        return studentRepository.save(student);
    }

    @Override
    public void delete(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new ValidationException("Student ID cannot be null or empty");
        }
        try {
            Long studentId = Long.parseLong(id);
            if (!studentRepository.existsById(studentId)) {
                throw new StudentNotFoundException(studentId);
            }
            studentRepository.deleteById(studentId);
        } catch (NumberFormatException e) {
            throw new ValidationException("Invalid student ID format: " + id);
        }
    }

    private boolean isValidEmail(String email) {
        return email.contains("@") && email.contains(".");
    }
}
