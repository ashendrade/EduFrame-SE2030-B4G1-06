package com.eduframepackage.eduframe.repository;

import com.eduframepackage.eduframe.model.CourseModule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for managing CourseModule entities in EduFrame.
 * Provides data access abstractions and custom query methods for course modules.
 */
@Repository
public interface CourseModuleRepository extends JpaRepository<CourseModule, Long> {
    
    /**
     * Retrieves all course modules belonging to a specific course, ordered by their sequence order ascending.
     * 
     * @param courseId The unique identifier of the course.
     * @return List of ordered course modules for the given course.
     */
    List<CourseModule> findByCourseIdOrderBySequenceOrderAsc(Long courseId);
}
