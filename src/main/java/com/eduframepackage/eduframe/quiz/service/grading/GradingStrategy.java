package com.eduframepackage.eduframe.quiz.service.grading;

import com.eduframepackage.eduframe.quiz.entity.Question;

/**
 * STRATEGY PATTERN.
 *
 * Each {@link com.eduframepackage.eduframe.quiz.entity.QuestionType} is graded differently
 * (exact option match, boolean match, fuzzy text match). Rather than a long
 * if/else chain in the service layer, each question type gets its own
 * interchangeable grading algorithm behind this common interface.
 * {@link GradingStrategyFactory} picks the right implementation at runtime.
 */
public interface GradingStrategy {
    GradingResult grade(Question question, String studentAnswer);
}
