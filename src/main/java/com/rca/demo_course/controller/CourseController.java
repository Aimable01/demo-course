package com.rca.demo_course.controller;

import com.rca.demo_course.domain.Course;
import com.rca.demo_course.dto.CourseDTO;
import com.rca.demo_course.mapper.CourseMapper;
import com.rca.demo_course.service.CourseService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/courses")
@CrossOrigin(origins = "*")
public class CourseController {

    @Autowired
    private CourseService courseService;

    @Autowired
    private CourseMapper courseMapper;

    @PostMapping
    public ResponseEntity<CourseDTO> createCourse(@Valid @RequestBody CourseDTO courseDTO) {
        Course course = courseMapper.toEntity(courseDTO);
        Course createdCourse = courseService.create(course);
        CourseDTO responseDTO = courseMapper.toDTO(createdCourse);
        return new ResponseEntity<>(responseDTO, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CourseDTO> getCourseById(@PathVariable String id) {
        Course course = courseService.findById(id);
        if (course != null) {
            CourseDTO courseDTO = courseMapper.toDTO(course);
            return new ResponseEntity<>(courseDTO, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping
    public ResponseEntity<List<CourseDTO>> getAllCourses() {
        List<Course> courses = courseService.findAll();
        List<CourseDTO> courseDTOs = courses.stream()
                .map(courseMapper::toDTO)
                .collect(Collectors.toList());
        return new ResponseEntity<>(courseDTOs, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CourseDTO> updateCourse(@PathVariable String id, @Valid @RequestBody CourseDTO courseDTO) {
        courseDTO.setId(Long.parseLong(id));
        Course course = courseMapper.toEntity(courseDTO);
        Course updatedCourse = courseService.update(course);
        CourseDTO responseDTO = courseMapper.toDTO(updatedCourse);
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCourse(@PathVariable String id) {
        courseService.delete(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
