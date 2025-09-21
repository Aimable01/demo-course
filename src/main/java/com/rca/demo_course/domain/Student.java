package com.rca.demo_course.domain;

import lombok.Data;

@Data
public class Student {
    private String id;
    private String name;
    private String surname;
    private int age;

    public Student(String id, String name, String surname, int age) {
        this.id = id;
        this.name = name;
        this.surname = surname;
        this.age = age;
    }
}
