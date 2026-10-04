package com.eduframepackage.eduframe.service;

import com.eduframepackage.eduframe.model.Course;
import com.eduframepackage.eduframe.model.CourseEnrollment;
import com.eduframepackage.eduframe.model.CourseModule;
import com.eduframepackage.eduframe.repository.CourseEnrollmentRepository;
import com.eduframepackage.eduframe.repository.CourseModuleRepository;
import com.eduframepackage.eduframe.repository.CourseRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CourseService {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private CourseModuleRepository moduleRepository;

    @Autowired
    private CourseEnrollmentRepository enrollmentRepository;

    @PostConstruct
    public void seedInitialCourses() {
        if (courseRepository.count() == 0) {
            // Seed Course 1: SE2030
            Course se2030 = courseRepository.save(new Course(
                    "SE2030",
                    "Software Engineering & Architecture",
                    "Computing",
                    "Prof. Kanishka Jayasinghe",
                    "A comprehensive course covering SDLC methodologies, MVC architecture, design patterns, UML diagramming, and automated testing in modern enterprise systems.",
                    "/images/thumb-mvc.jpg",
                    "12 Weeks"
            ));
            se2030.setEnrolledCount(42);
            courseRepository.save(se2030);

            moduleRepository.save(new CourseModule(
                    se2030.getId(),
                    "Module 1: Introduction to MVC Architecture & Design Patterns",
                    "Deep dive into Model-View-Controller design pattern in modern web architectures with concrete Java examples.",
                    "/videos/mvc.mp4",
                    "45 min",
                    "SE2030_Module1_MVC_Architecture.pdf",
                    1
            ));
            moduleRepository.save(new CourseModule(
                    se2030.getId(),
                    "Module 2: Creational Patterns - Singleton & Factory",
                    "Learn thread-safe singletons, factory methods, and compile-time decoupling principles.",
                    "/videos/patterns.mp4",
                    "52 min",
                    "SE2030_Module2_Design_Patterns.pdf",
                    2
            ));

            // Seed Course 2: IT1010
            Course it1010 = courseRepository.save(new Course(
                    "IT1010",
                    "Object-Oriented Programming in Java",
                    "Computing",
                    "Dr. Tharindu Senanayake",
                    "Fundamental concepts of Object-Oriented Programming: classes, objects, encapsulation, inheritance, polymorphism, abstract classes, and interfaces.",
                    "/images/thumb-oop.jpg",
                    "10 Weeks"
            ));
            it1010.setEnrolledCount(85);
            courseRepository.save(it1010);

            moduleRepository.save(new CourseModule(
                    it1010.getId(),
                    "Module 1: Inheritance, Abstract Classes & Interfaces",
                    "Subclassing, method overriding vs overloading, dynamic binding, and contract interfaces in Java.",
                    "/videos/oop.mp4",
                    "38 min",
                    "IT1010_OOP_Java_Core.pdf",
                    1
            ));

            // Seed Course 3: EE1020
            Course ee1020 = courseRepository.save(new Course(
                    "EE1020",
                    "Digital Logic Design & Karnaugh Maps",
                    "Engineering",
                    "Dr. Priyantha Alwis",
                    "Combinational logic design, Boolean algebra simplification, K-Map minimization techniques, gates, multiplexers, and sequential logic circuits.",
                    "/images/thumb-kmaps.jpg",
                    "8 Weeks"
            ));
            ee1020.setEnrolledCount(34);
            courseRepository.save(ee1020);

            moduleRepository.save(new CourseModule(
                    ee1020.getId(),
                    "Module 1: Boolean Algebra & K-Map Circuit Minimization",
                    "Simplifying complex Boolean functions using 4-variable K-maps and implementing minimal gate circuits.",
                    "/videos/kmaps.mp4",
                    "65 min",
                    "EE1020_Digital_Logic_Kmaps.pdf",
                    1
            ));
        }
    }

    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    public List<Course> getCoursesByCategory(String category) {
        if (category == null || category.equalsIgnoreCase("All") || category.isEmpty()) {
            return getAllCourses();
        }
        return courseRepository.findByCategoryIgnoreCase(category);
    }

    public List<Course> searchCourses(String query, String category) {
        List<Course> list = getCoursesByCategory(category);
        if (query == null || query.trim().isEmpty()) {
            return list;
        }
        String q = query.toLowerCase().trim();
        return list.stream()
                .filter(c -> c.getTitle().toLowerCase().contains(q) ||
                        c.getCode().toLowerCase().contains(q) ||
                        c.getInstructor().toLowerCase().contains(q) ||
                        c.getDescription().toLowerCase().contains(q))
                .collect(Collectors.toList());
    }

    public Optional<Course> getCourseById(Long id) {
        return courseRepository.findById(id);
    }

    public List<CourseModule> getModulesForCourse(Long courseId) {
        return moduleRepository.findByCourseIdOrderBySequenceOrderAsc(courseId);
    }

    @Transactional
    public Course createCourse(String code, String title, String category, String instructor, String description, String duration, String moduleTitle, String moduleSummary) {
        Course course = new Course(code, title, category, instructor, description, "/images/thumb-mvc.jpg", duration != null ? duration : "10 Weeks");
        Course saved = courseRepository.save(course);

        if (moduleTitle != null && !moduleTitle.trim().isEmpty()) {
            CourseModule module = new CourseModule(
                    saved.getId(),
                    moduleTitle.trim(),
                    moduleSummary != null ? moduleSummary.trim() : "Initial module overview and lecture video.",
                    "/videos/mvc.mp4",
                    "45 min",
                    code.replaceAll("\\s+", "_") + "_Module1.pdf",
                    1
            );
            moduleRepository.save(module);
        }

        return saved;
    }

    @Transactional
    public Course updateCourse(Long id, String code, String title, String category, String instructor, String description, String duration) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Course not found with ID: " + id));

        course.setCode(code.trim());
        course.setTitle(title.trim());
        course.setCategory(category.trim());
        if (instructor != null && !instructor.trim().isEmpty()) {
            course.setInstructor(instructor.trim());
        }
        course.setDescription(description.trim());
        if (duration != null && !duration.trim().isEmpty()) {
            course.setDuration(duration.trim());
        }
        return courseRepository.save(course);
    }

    @Transactional
    public boolean enrollStudent(String userEmail, Long courseId) {
        if (userEmail == null || userEmail.trim().isEmpty() || courseId == null) {
            return false;
        }
        if (enrollmentRepository.existsByUserEmailAndCourseId(userEmail, courseId)) {
            return true; // Already enrolled
        }
        CourseEnrollment enrollment = new CourseEnrollment(userEmail.trim(), courseId);
        enrollmentRepository.save(enrollment);

        courseRepository.findById(courseId).ifPresent(c -> {
            c.setEnrolledCount(c.getEnrolledCount() + 1);
            courseRepository.save(c);
        });
        return true;
    }

    public boolean isStudentEnrolled(String userEmail, Long courseId) {
        if (userEmail == null || courseId == null) {
            return false;
        }
        return enrollmentRepository.existsByUserEmailAndCourseId(userEmail, courseId);
    }

    public List<Course> getEnrolledCoursesForStudent(String userEmail) {
        if (userEmail == null || userEmail.trim().isEmpty()) {
            return new ArrayList<>();
        }
        List<CourseEnrollment> enrollments = enrollmentRepository.findByUserEmail(userEmail.trim());
        List<Long> courseIds = enrollments.stream().map(e -> e.getCourseId()).collect(Collectors.toList());
        return courseRepository.findAllById(courseIds);
    }

    public int getTotalEnrolledStudentsCount() {
        List<Course> courses = courseRepository.findAll();
        int sumFromCourses = courses.stream().mapToInt(c -> c.getEnrolledCount()).sum();
        long sumFromEnrollments = enrollmentRepository.count();
        return (int) Math.max(sumFromCourses, sumFromEnrollments);
    }

    // Module Management & Re-ordering methods

    @Transactional
    public boolean moveModuleUp(Long moduleId) {
        Optional<CourseModule> opt = moduleRepository.findById(moduleId);
        if (opt.isEmpty()) return false;
        CourseModule current = opt.get();
        List<CourseModule> modules = moduleRepository.findByCourseIdOrderBySequenceOrderAsc(current.getCourseId());

        int index = -1;
        for (int i = 0; i < modules.size(); i++) {
            if (modules.get(i).getId().equals(moduleId)) {
                index = i;
                break;
            }
        }
        if (index > 0) {
            CourseModule previous = modules.get(index - 1);
            int currentSeq = current.getSequenceOrder();
            int prevSeq = previous.getSequenceOrder();
            if (currentSeq == prevSeq) {
                currentSeq = index + 1;
                prevSeq = index;
            }
            current.setSequenceOrder(prevSeq);
            previous.setSequenceOrder(currentSeq);
            moduleRepository.save(current);
            moduleRepository.save(previous);
            return true;
        }
        return false;
    }

    @Transactional
    public boolean moveModuleDown(Long moduleId) {
        Optional<CourseModule> opt = moduleRepository.findById(moduleId);
        if (opt.isEmpty()) return false;
        CourseModule current = opt.get();
        List<CourseModule> modules = moduleRepository.findByCourseIdOrderBySequenceOrderAsc(current.getCourseId());

        int index = -1;
        for (int i = 0; i < modules.size(); i++) {
            if (modules.get(i).getId().equals(moduleId)) {
                index = i;
                break;
            }
        }
        if (index >= 0 && index < modules.size() - 1) {
            CourseModule next = modules.get(index + 1);
            int currentSeq = current.getSequenceOrder();
            int nextSeq = next.getSequenceOrder();
            if (currentSeq == nextSeq) {
                currentSeq = index + 1;
                nextSeq = index + 2;
            }
            current.setSequenceOrder(nextSeq);
            next.setSequenceOrder(currentSeq);
            moduleRepository.save(current);
            moduleRepository.save(next);
            return true;
        }
        return false;
    }

    @Transactional
    public CourseModule addModuleToCourse(Long courseId, String title, String summary, String videoUrl, String duration, String notesUrl) {
        List<CourseModule> existing = moduleRepository.findByCourseIdOrderBySequenceOrderAsc(courseId);
        int nextOrder = existing.isEmpty() ? 1 : existing.get(existing.size() - 1).getSequenceOrder() + 1;

        CourseModule module = new CourseModule(
                courseId,
                title.trim(),
                summary != null ? summary.trim() : "",
                videoUrl != null && !videoUrl.trim().isEmpty() ? videoUrl.trim() : "/videos/mvc.mp4",
                duration != null && !duration.trim().isEmpty() ? duration.trim() : "45 min",
                notesUrl != null && !notesUrl.trim().isEmpty() ? notesUrl.trim() : "Module_Notes.pdf",
                nextOrder
        );
        return moduleRepository.save(module);
    }

    @Transactional
    public void deleteModule(Long moduleId) {
        moduleRepository.deleteById(moduleId);
    }

    public Optional<CourseModule> getModuleById(Long moduleId) {
        return moduleRepository.findById(moduleId);
    }
}
