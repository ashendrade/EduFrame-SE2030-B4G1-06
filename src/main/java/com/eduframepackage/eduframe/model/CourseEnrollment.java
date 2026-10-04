package com.eduframepackage.eduframe.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "student_course_enrollment", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"userEmail", "courseId"})
})
public class CourseEnrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String userEmail;

    @Column(nullable = false)
    private Long courseId;

    @Column(nullable = false)
    private LocalDateTime enrolledAt;

    public CourseEnrollment() {
    }

    public CourseEnrollment(String userEmail, Long courseId) {
        this.userEmail = userEmail;
        this.courseId = courseId;
        this.enrolledAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (this.enrolledAt == null) {
            this.enrolledAt = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public LocalDateTime getEnrolledAt() {
        return enrolledAt;
    }

    public void setEnrolledAt(LocalDateTime enrolledAt) {
        this.enrolledAt = enrolledAt;
    }
}
