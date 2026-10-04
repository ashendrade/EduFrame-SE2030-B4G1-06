package com.eduframepackage.eduframe.quiz.service.grading;

import com.eduframepackage.eduframe.quiz.entity.Question;
import org.springframework.stereotype.Component;

/**
 * Concrete strategy: short-answer questions are auto-graded using a
 * normalised, case/whitespace/punctuation-insensitive "close match"
 * against the reference answer (per the "auto-grade by exact/close text
 * match" requirement). This keeps grading fully automatic while still
 * tolerating minor differences in capitalisation or spacing.
 */
@Component
public class ShortAnswerGradingStrategy implements GradingStrategy {

    @Override
    public GradingResult grade(Question question, String studentAnswer) {
        if (studentAnswer == null || studentAnswer.isBlank()) {
            return new GradingResult(false, 0);
        }
        String normalisedStudent = normalise(studentAnswer);
        String normalisedCorrect = normalise(question.getCorrectAnswer());

        boolean correct = normalisedStudent.equals(normalisedCorrect)
                || normalisedStudent.contains(normalisedCorrect)
                || normalisedCorrect.contains(normalisedStudent);

        return new GradingResult(correct, correct ? question.getMarks() : 0);
    }

    private String normalise(String text) {
        return text.trim()
                .toLowerCase()
                .replaceAll("[^a-z0-9 ]", "")
                .replaceAll("\\s+", " ");
    }
}
