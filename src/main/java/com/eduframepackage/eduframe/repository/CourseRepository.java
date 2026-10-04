package com.eduframepackage.eduframe.repository;

import com.eduframepackage.eduframe.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findByCategoryIgnoreCase(String category);
    List<Course> findByTitleContainingIgnoreCaseOrCodeContainingIgnoreCaseOrInstructorContainingIgnoreCase(String title, String code, String instructor);
}
