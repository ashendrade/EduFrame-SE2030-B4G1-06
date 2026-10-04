package com.eduframepackage.eduframe.quiz.service.grading;

/** Outcome of grading a single student answer. */
public record GradingResult(boolean correct, int marksAwarded) {
}
