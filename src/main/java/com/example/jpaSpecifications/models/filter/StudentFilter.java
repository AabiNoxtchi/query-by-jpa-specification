package com.example.jpaSpecifications.models.filter;

import com.example.jpaSpecifications.models.entity.Grade;
import com.example.jpaSpecifications.models.entity.Student;
import jakarta.persistence.criteria.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class StudentFilter {

    private Long id;

    private String name;
    private String email;

    private Integer ageGreaterThan;
    private Integer ageLessThan;

    private Integer enrollmentsCountGreaterThan;
    private Integer enrollmentsCountLessThan;

    private String courseName;
    private Grade courseGrade;

    public Specification<Student> toSpecification() {
        return (root, query, cb) -> {
            // Dynamic predicates
            query.distinct(true);

            List<Predicate> predicates = new ArrayList<>();

            if (id != null) {
                predicates.add(cb.equal(root.get("id"), id));
            }
            if (name != null) {
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
            }
            if (email != null) {
                predicates.add(cb.like(cb.lower(root.get("email")), "%" + email.toLowerCase() + "%"));
            }
            if (ageGreaterThan != null) {
                predicates.add(cb.greaterThan(root.get("age"), ageGreaterThan));
            }
            if (ageLessThan != null) {
                predicates.add(cb.lessThan(root.get("age"), ageLessThan));
            }
            if (courseName != null || courseGrade != null) {
                Join<Object, Object> enrollments = root.join("enrollments", JoinType.LEFT);
                if (courseName != null) {
                    predicates.add(cb.like(cb.lower(enrollments.get("course").get("name")), "%" + courseName.toLowerCase() + "%"));
                }
                if (courseGrade != null) {
                    predicates.add(cb.equal(enrollments.get("grade"), courseGrade));
                }
            }

            // Enrollment Count Greater Than
            if (enrollmentsCountGreaterThan != null) {
                predicates.add(cb.greaterThan(getEnrollmentsCountSubquery(root, query, cb), (long)enrollmentsCountGreaterThan));
            }

            // Enrollment Count Less Than
            if (enrollmentsCountLessThan != null) {
                predicates.add(cb.lessThan(getEnrollmentsCountSubquery(root, query, cb), (long)enrollmentsCountLessThan));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private Subquery<Long> getEnrollmentsCountSubquery(Root<Student> root,
                                                       jakarta.persistence.criteria.CriteriaQuery<?> query,
                                                       jakarta.persistence.criteria.CriteriaBuilder cb) {
        Subquery<Long> subquery = query.subquery(Long.class);
        Root<Student> subRoot = subquery.from(Student.class);
        subquery.select(cb.count(subRoot.join("enrollments"))).where(cb.equal(subRoot, root));
        return subquery;
    }

}
