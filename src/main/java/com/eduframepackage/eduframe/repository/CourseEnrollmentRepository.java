package com.eduframepackage.eduframe.repository;

import com.eduframepackage.eduframe.model.CourseEnrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseEnrollmentRepository extends JpaRepository<CourseEnrollment, Long> {
    boolean existsByUserEmailAndCourseId(String userEmail, Long courseId);
    Optional<CourseEnrollment> findByUserEmailAndCourseId(String userEmail, Long courseId);
    List<CourseEnrollment> findByUserEmail(String userEmail);
}
