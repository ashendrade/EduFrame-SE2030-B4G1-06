package com.eduframepackage.eduframe.quiz.entity;

/**
 * Application roles. Kept minimal on purpose - the full role-based login
 * system belongs to a teammate's module; this is just enough to let the
 * Quiz & Assessment Subsystem be tested end-to-end as both a Teacher and
 * a Student.
 */
public enum Role {
    STUDENT,
    TEACHER,
    ADMIN
}
