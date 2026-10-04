package com.eduframepackage.eduframe.quiz.service.grading;

import com.eduframepackage.eduframe.quiz.entity.Question;
import org.springframework.stereotype.Component;

/** Concrete strategy: true/false questions are graded by boolean-string match. */
@Component
public class TrueFalseGradingStrategy implements GradingStrategy {

    @Override
    public GradingResult grade(Question question, String studentAnswer) {
        if (studentAnswer == null) {
            return new GradingResult(false, 0);
        }
        boolean correct = studentAnswer.trim().equalsIgnoreCase(question.getCorrectAnswer().trim());
        return new GradingResult(correct, correct ? question.getMarks() : 0);
    }
}
