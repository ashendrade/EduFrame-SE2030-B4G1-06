package com.eduframepackage.eduframe.quiz.service.grading;

import com.eduframepackage.eduframe.quiz.entity.QuestionType;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;

/**
 * Resolves the correct {@link GradingStrategy} for a given
 * {@link QuestionType} at runtime. Works together with the Strategy
 * pattern above so new question types only need one new strategy class
 * plus one line here.
 */
@Component
public class GradingStrategyFactory {

    private final Map<QuestionType, GradingStrategy> strategies = new EnumMap<>(QuestionType.class);

    public GradingStrategyFactory(McqGradingStrategy mcq,
                                   TrueFalseGradingStrategy trueFalse,
                                   ShortAnswerGradingStrategy shortAnswer) {
        strategies.put(QuestionType.MCQ, mcq);
        strategies.put(QuestionType.TRUE_FALSE, trueFalse);
        strategies.put(QuestionType.SHORT_ANSWER, shortAnswer);
    }

    public GradingStrategy getStrategy(QuestionType type) {
        GradingStrategy strategy = strategies.get(type);
        if (strategy == null) {
            throw new IllegalArgumentException("No grading strategy registered for type: " + type);
        }
        return strategy;
    }
}
