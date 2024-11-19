package com.example.jpaSpecifications;

import com.example.jpaSpecifications.models.entity.Course;
import com.example.jpaSpecifications.models.entity.Enrollment;
import com.example.jpaSpecifications.models.entity.Grade;
import com.example.jpaSpecifications.models.entity.Student;
import com.example.jpaSpecifications.repositories.CourseRepository;
import com.example.jpaSpecifications.repositories.EnrollmentRepository;
import com.example.jpaSpecifications.repositories.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.IntStream;

@Slf4j
@Component
@RequiredArgsConstructor
public class InitialTestData implements CommandLineRunner {

    public static final Random RANDOM = new Random();

    public static final String[] STUDENT_NAMES =
            {"John Doe", "Bob Doe",  "Jane Smith", "Alice Brown", "Bob White", "Charlie Black"};
    public static final String[] EMAIL_VENDORS =
            {"@gmail.com", "@yahoo.com", "@outlook.com", "@icloud.com", "@aol.com", "@zoho.com", "@protonmail.com"};

    public static final String[] COURSE_NAMES =
            {"Intro to Chemistry", "Advanced Physics", "Fundamentals of Computer Science", "Principles of Economics",
                    "Topics in Philosophy", "Advanced Maths", "Fundamentals of Economics", "Intro to Databases"};
    public static final int[] CREDITS = {1, 2, 3, 4, 5, 6, 7, 8};

    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;

    @Override
    public void run(String... args) throws Exception {
        if(studentRepository.count() == 0) {
            List<Student> students = initStudents();
            studentRepository.saveAll(students);
            courseRepository.deleteAll();
            courseRepository.saveAll(initCoursesWithEnrollments(students));
        }

        log.info("student repository count = {}", studentRepository.count());
        log.info("course repository count = {}", courseRepository.count());
        log.info("enrollment repository count = {}", enrollmentRepository.count());
    }

    public static List<Student> initStudents() {
        List<Student> students = new ArrayList<>();
        IntStream.range(1, 100).forEach(index -> {
            String name =
                    STUDENT_NAMES[RANDOM.nextInt(STUDENT_NAMES.length)]+ " " + index;
            String email =
                    name.replace(" ", "").toLowerCase() +
                            EMAIL_VENDORS[RANDOM.nextInt(EMAIL_VENDORS.length)];
            int age = RANDOM.nextInt(32) + 18; // from 18 to 50

            students.add(new Student(null, name, age, email, null));
        });
        return students;
    }

    public static List<Course> initCoursesWithEnrollments(List<Student> students) {
        List<Course> courses = new ArrayList<>();
        IntStream.range(0, COURSE_NAMES.length).forEach(index -> {
            Course course = new Course();
            course.setName(COURSE_NAMES[index]);
            course.setCredits(CREDITS[index]); // CREDITS.length is the same as COURSE_NAMES.length
            courses.add(course);
        });

        return initEnrollmentsForAllStudentsInCourses(courses, students);
    }

    private static List<Course> initEnrollmentsForAllStudentsInCourses(List<Course> courses, List<Student> students) {
        Grade[] grades = Grade.values();

        IntStream.range(0, students.size()).forEach(index -> {
            Student student = students.get(index);
            Set<Course> chosenCourses = new HashSet<>();
            IntStream.range(0, courses.size()).forEach(innerIndex -> {
                chosenCourses.add(courses.get(RANDOM.nextInt(courses.size())));
            });
            chosenCourses.forEach(course -> {
                course.getEnrollments().add(new Enrollment(
                        null,
                        student,
                        course,
                        grades[RANDOM.nextInt(grades.length)],
                        "some notes"));
            });
        });
        return courses;
    }

}
