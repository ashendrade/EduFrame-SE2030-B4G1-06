package com.eduframepackage.eduframe.quiz.entity;

/**
 * Supported question formats in the Quiz & Assessment Subsystem.
 * Each type has its own grading behaviour - see the Strategy pattern
 * implementations under service.grading.
 */
public enum QuestionType {
    MCQ,
    TRUE_FALSE,
    SHORT_ANSWER
}
