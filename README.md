# Getting Started  

### JPA Specifications and Criteria API: Documentation and Examples ###  

**Overview**  

Spring Data JPA provides two powerful tools for creating dynamic and complex queries:  

- Specifications: A high-level abstraction built on top of the Criteria API, allowing for reusable, modular query filters.
- Criteria API: A low-level, programmatic way of constructing queries that provides full control over query structure.  

___________________________________________________________________________________________________
1. **JPA Specifications**  

- The Specification interface encapsulates query logic in reusable, modular components. It's primarily used for filtering data in Spring Data JPA repositories.
- Key Features: 
  - Encapsulates filtering logic in reusable methods.
  - Dynamically chains multiple conditions with and/or.
  - Simplifies query building for repositories.
<br><br>
- **Implementation :**
- Basic Structure
```java
public interface Specification<T> {
    Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb);
}
```  
      
- Example: Filter Students by Name  
```java

public class StudentSpecifications {
    public static Specification<Student> hasName(String name) {
        return (root, query, cb) -> cb.equal(root.get("name"), name);
    }
}
```  

- Combine Filters Dynamically  
```java
Specification<Student> spec = Specification.where(hasName("John"))
                                           .and(isOlderThan(18));
List<Student> students = studentRepository.findAll(spec);
```  

___________________________________________________________________________
- **Advanced Features**
- Joins  
```java
public static Specification<Student> enrolledInCourse(String courseName) {
    return (root, query, cb) -> {
        Join<Student, Enrollment> enrollments = root.join("enrollments");
        return cb.equal(enrollments.get("course").get("name"), courseName);
    };
}
```  

- Subqueries  
```java
public static Specification<Student> hasEnrollmentsGreaterThan(int count) {
    return (root, query, cb) -> {
        Subquery<Long> subquery = query.subquery(Long.class);
        Root<Student> subRoot = subquery.from(Student.class);
        subquery.select(cb.count(subRoot.join("enrollments")))
                .where(cb.equal(subRoot, root));
        return cb.greaterThan(subquery, count);
    };
}

```
_________________________________________________________________________

- **Repository Integration**  
- Extend JpaSpecificationExecutor to enable specification-based queries:  
```java
public interface StudentRepository extends JpaRepository<Student, Long>, JpaSpecificationExecutor<Student> {}
```  
- Query with specifications:
```java
Specification<Student> spec = Specification.where(hasName("John")).and(enrolledInCourse("Math"));
List<Student> students = studentRepository.findAll(spec);
```  
--------------------------------------------------------------  

2. **JPA Criteria API**
- The Criteria API is a lower-level, programmatic API for constructing dynamic queries. It is part of the JPA specification and provides full control over query construction.

- Key Features
  - Fine-grained query construction for projections, groupings, and aggregations.
  - Supports complex subqueries and joins.
  - Independent of Spring Data, usable in pure JPA contexts. e.g.(Java SE Application, Jakarta EE Application)
<br><br>
- **Implementation**  
- Example: Filter Students by Name
```java
CriteriaBuilder cb = entityManager.getCriteriaBuilder();
CriteriaQuery<Student> query = cb.createQuery(Student.class);
Root<Student> root = query.from(Student.class);

query.select(root).where(cb.equal(root.get("name"), "John"));
List<Student> students = entityManager.createQuery(query).getResultList();
```
- Adding Joins  
```java
Join<Student, Enrollment> enrollmentJoin = root.join("enrollments");
query.where(cb.equal(enrollmentJoin.get("course").get("name"), "Math"));
```  
- Subqueries
```java
Subquery<Long> subquery = query.subquery(Long.class);
Root<Student> subRoot = subquery.from(Student.class);
subquery.select(cb.count(subRoot.join("enrollments")))
        .where(cb.equal(subRoot, root));

query.where(cb.greaterThan(subquery, 3));
```  

- Combining Conditions  
```java
Predicate namePredicate = cb.equal(root.get("name"), "John");
Predicate enrollmentCountPredicate = cb.greaterThan(subquery, 3);
query.where(cb.and(namePredicate, enrollmentCountPredicate));
```
-----------------------------------------------

3. **Complex Query Example**
**Use Case:**  
"Find all students named 'John' who are enrolled in a course named 'Math' and have more than 3 enrollments."

- Using Criteria API  
```java
CriteriaBuilder cb = entityManager.getCriteriaBuilder();
CriteriaQuery<Student> query = cb.createQuery(Student.class);
Root<Student> studentRoot = query.from(Student.class);
Join<Student, Enrollment> enrollmentJoin = studentRoot.join("enrollments");

// Subquery for enrollment count
Subquery<Long> subquery = query.subquery(Long.class);
Root<Student> subStudent = subquery.from(Student.class);
subquery.select(cb.count(subStudent.join("enrollments")))
        .where(cb.equal(subStudent, studentRoot));

// Query predicates
Predicate namePredicate = cb.equal(studentRoot.get("name"), "John");
Predicate coursePredicate = cb.equal(enrollmentJoin.get("course").get("name"), "Math");
Predicate enrollmentCountPredicate = cb.greaterThan(subquery, 3);

query.select(studentRoot)
     .where(cb.and(namePredicate, coursePredicate, enrollmentCountPredicate))
     .distinct(true);

List<Student> students = entityManager.createQuery(query).getResultList();
```  

- Using Specifications  
```java
public static Specification<Student> hasName(String name) {
    return (root, query, cb) -> cb.equal(root.get("name"), name);
}

public static Specification<Student> enrolledInCourse(String courseName) {
    return (root, query, cb) -> {
        Join<Student, Enrollment> enrollments = root.join("enrollments");
        return cb.equal(enrollments.get("course").get("name"), courseName);
    };
}

public static Specification<Student> hasEnrollmentsGreaterThan(int count) {
    return (root, query, cb) -> {
        Subquery<Long> subquery = query.subquery(Long.class);
        Root<Student> subRoot = subquery.from(Student.class);
        subquery.select(cb.count(subRoot.join("enrollments")))
                .where(cb.equal(subRoot, root));
        return cb.greaterThan(subquery, count);
    };
}

Specification<Student> spec = Specification.where(hasName("John"))
                                           .and(enrolledInCourse("Math"))
                                           .and(hasEnrollmentsGreaterThan(3));

List<Student> students = studentRepository.findAll(spec);
```  
-------------------------------


4. When to Use Which  
- Specifications
  - Simple or modular filters.
  - Reusable filters for repositories.
- Criteria API
  - Complex aggregations or grouping.
  - Fine-grained query control.

-------------------------------------------

<br><br>  

### Reference Documentation
For further reference, please consider the following sections:

* [Official Gradle documentation](https://docs.gradle.org)
* [Spring Boot Gradle Plugin Reference Guide](https://docs.spring.io/spring-boot/3.3.5/gradle-plugin)
* [Create an OCI image](https://docs.spring.io/spring-boot/3.3.5/gradle-plugin/packaging-oci-image.html)
* [Spring Data JPA](https://docs.spring.io/spring-boot/3.3.5/reference/data/sql.html#data.sql.jpa-and-spring-data)

### Guides
The following guides illustrate how to use some features concretely:

* [Accessing Data with JPA](https://spring.io/guides/gs/accessing-data-jpa/)

### Additional Links
These additional references should also help you:

* [Gradle Build Scans – insights for your project's build](https://scans.gradle.com#gradle)

