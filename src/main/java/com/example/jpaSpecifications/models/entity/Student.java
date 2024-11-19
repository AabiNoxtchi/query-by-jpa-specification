package com.example.jpaSpecifications.models.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private int age;
    private String email;

    @OneToMany(mappedBy = "student", cascade = CascadeType.REMOVE)
    private Set<Enrollment> enrollments;

}

