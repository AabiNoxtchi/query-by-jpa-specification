package com.example.jpaSpecifications.services;

import com.example.jpaSpecifications.models.entity.Grade;
import com.example.jpaSpecifications.models.entity.Student;
import com.example.jpaSpecifications.models.filter.StudentFilter;
import com.example.jpaSpecifications.repositories.CourseRepository;
import com.example.jpaSpecifications.repositories.StudentRepository;
import com.example.jpaSpecifications.service.StudentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.example.jpaSpecifications.InitialTestData.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class StudentServiceTest {

    @Autowired
    StudentRepository studentRepository;

    @Autowired
    StudentService studentService;

    @Autowired
    CourseRepository courseRepository;

    private final Random rand = new Random();
    private List<Student> dbStudents = new ArrayList<>();

    @BeforeEach
    public void setup() {
        if(studentRepository.count() == 0) {
            List<Student> students = initStudents();
            studentRepository.saveAll(students);
            courseRepository.deleteAll();
            courseRepository.saveAll(initCoursesWithEnrollments(students));
        }
        if(dbStudents.isEmpty()) {
            dbStudents = studentRepository.findByEnrollmentsNotNull();
        }
    }

    @Test
    public void filterStudents_providingFullName_ShouldPass() {
        // create
        int rnd = rand.nextInt(dbStudents.size());
        String nameToFilter = dbStudents.get(rnd).getName();
        int count = (int) dbStudents.stream().filter(student -> student.getName().contains(nameToFilter)).count();

        StudentFilter filter = new StudentFilter();
        filter.setName(nameToFilter);

        // test
        List<Student> students = studentService.findAll(filter);

        // assert
        assertEquals(count, students.size());
    }

    @Test
    public void filterStudents_providingPartNameAndPartEmail_ShouldPass() {
        // create
        String partNameToFilter = STUDENT_NAMES[RANDOM.nextInt(STUDENT_NAMES.length)].toLowerCase();
        String partEmailToFilter = EMAIL_VENDORS[RANDOM.nextInt(EMAIL_VENDORS.length)].toLowerCase();
        int count = (int) dbStudents.stream()
                .filter(student -> student.getName().toLowerCase().contains(partNameToFilter) &&
                        student.getEmail().toLowerCase().contains(partEmailToFilter))
                .count();

        StudentFilter filter = new StudentFilter();
        filter.setName(partNameToFilter);
        filter.setEmail(partEmailToFilter);

        // test
        List<Student> students = studentService.findAll(filter);

        // assert
        assertEquals(count, students.size());
    }

    @Test
    public void filterStudents_providingAgeBoundaries_ShouldPass() {
        // create
        int minAge = 20;
        int maxAge = 30;
        int count = (int) dbStudents.stream()
                .filter(student -> student.getAge() > minAge && student.getAge() < maxAge)
                .count();

        StudentFilter filter = new StudentFilter();
        filter.setAgeGreaterThan(minAge);
        filter.setAgeLessThan(maxAge);

        // test
        List<Student> students = studentService.findAll(filter);

        // assert
        assertEquals(count, students.size());
    }

    @Test
    public void filterStudents_byEnrollmentCounts_ShouldPass() {
        // create
        int minCount = 3;
        int maxCount = 6;
        int count = (int) dbStudents.stream()
                .filter(student -> student.getEnrollments().size() > minCount &&
                        student.getEnrollments().size() < maxCount)
                .count();

        StudentFilter filter = new StudentFilter();
        filter.setEnrollmentsCountGreaterThan(minCount);
        filter.setEnrollmentsCountLessThan(maxCount);

        // test
        List<Student> students = studentService.findAll(filter);

        // assert
        assertEquals(count, students.size());
    }

    @Test
    public void filterStudents_enrolledInCourse_ShouldPass() {
        // create
        String randomCourseName = COURSE_NAMES[RANDOM.nextInt(COURSE_NAMES.length)];

        int count = (int) dbStudents.stream()
                .filter(student -> student.getEnrollments().stream()
                        .anyMatch(enrollment -> enrollment.getCourse().getName().toLowerCase().contains(randomCourseName.toLowerCase())))
                .count();

        StudentFilter filter = new StudentFilter();
        filter.setCourseName(randomCourseName);

        // test
        List<Student> students = studentService.findAll(filter);

        // assert
        assertEquals(count, students.size());
    }

    @Test
    public void filterStudents_hasAnyGradeEqualsGiven_ShouldPass() {
        // create
        Grade randomGrade = Grade.C;

        int count = (int) dbStudents.stream()
                .filter(student -> student.getEnrollments().stream()
                        .anyMatch(enrollment -> randomGrade.equals(enrollment.getGrade())))
                .count();

        StudentFilter filter = new StudentFilter();
        filter.setCourseGrade(randomGrade);

        // test
        List<Student> students = studentService.findAll(filter);

        // assert
        assertEquals(count, students.size());
    }

    @Test
    public void filterStudents_bySpecificGradeInGivenCourse_ShouldPass() {
        // create
        String randomCourseName = COURSE_NAMES[RANDOM.nextInt(COURSE_NAMES.length)];
        Grade randomGrade = Grade.B;

        int count = (int) dbStudents.stream()
                .filter(student -> student.getEnrollments().stream()
                        .anyMatch(enrollment -> randomGrade.equals(enrollment.getGrade()) &&
                                enrollment.getCourse().getName().toLowerCase().contains(randomCourseName.toLowerCase())))
                .count();

        StudentFilter filter = new StudentFilter();
        filter.setCourseName(randomCourseName);
        filter.setCourseGrade(randomGrade);

        // test
        List<Student> students = studentService.findAll(filter);

        // assert
        assertEquals(count, students.size());
    }


    /*** catch edge cases ***/

    @Test
    public void filterStudents_emptyFilter_ShouldReturnAllStudents() {
        StudentFilter filter = new StudentFilter(); // Empty filter

        List<Student> students = studentService.findAll(filter);

        assertEquals(dbStudents.size(), students.size());
    }

    @Test
    public void filterStudents_nullFilter_ShouldReturnAllStudents() {
        List<Student> students = studentService.findAll(null);

        assertEquals(dbStudents.size(), students.size());
    }

    @Test
    public void filterStudents_minimumAge_ShouldPass() {
        StudentFilter filter = new StudentFilter();
        filter.setAgeGreaterThan(Integer.MIN_VALUE);

        List<Student> students = studentService.findAll(filter);

        assertEquals(dbStudents.size(), students.size()); // All students match minimum age
    }

    @Test
    public void filterStudents_invalidCourseName_ShouldReturnNoResults() {
        StudentFilter filter = new StudentFilter();
        filter.setCourseName("Nonexistent Course");

        List<Student> students = studentService.findAll(filter);

        assertEquals(0, students.size()); // No matches
    }

    @Test
    public void filterStudents_invalidEmail_ShouldReturnNoResults() {
        StudentFilter filter = new StudentFilter();
        filter.setEmail("nonexistent-email@example.com");

        List<Student> students = studentService.findAll(filter);

        assertEquals(0, students.size()); // No matches
    }

}
