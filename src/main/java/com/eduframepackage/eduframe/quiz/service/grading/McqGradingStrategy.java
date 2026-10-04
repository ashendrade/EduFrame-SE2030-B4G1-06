package com.eduframepackage.eduframe.quiz.service.grading;

import com.eduframepackage.eduframe.quiz.entity.Question;
import org.springframework.stereotype.Component;

/** Concrete strategy: multiple-choice questions are graded by exact option match. */
@Component
public class McqGradingStrategy implements GradingStrategy {

    @Override
    public GradingResult grade(Question question, String studentAnswer) {
        if (studentAnswer == null) {
            return new GradingResult(false, 0);
        }
        boolean correct = studentAnswer.trim().equalsIgnoreCase(question.getCorrectAnswer().trim());
        return new GradingResult(correct, correct ? question.getMarks() : 0);
    }
}
