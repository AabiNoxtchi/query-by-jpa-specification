package com.example.jpaSpecifications.service;

import com.example.jpaSpecifications.models.entity.Student;
import com.example.jpaSpecifications.models.filter.StudentFilter;
import com.example.jpaSpecifications.repositories.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository studentRepository;

    public List<Student> findAll(StudentFilter filter) {
      return filter == null ? studentRepository.findAll() :
              studentRepository.findAll(filter.toSpecification());
    }
}



