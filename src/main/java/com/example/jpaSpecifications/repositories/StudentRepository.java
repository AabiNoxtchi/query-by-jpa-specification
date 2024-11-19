package com.example.jpaSpecifications.repositories;

import com.example.jpaSpecifications.models.entity.Student;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long>, JpaSpecificationExecutor<Student> {

    @EntityGraph(attributePaths = {"enrollments", "enrollments.course"})
    List<Student> findByEnrollmentsNotNull();

}

