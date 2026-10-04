package com.eduframepackage.eduframe.quiz.service;

import com.eduframepackage.eduframe.quiz.dto.QuestionForm;
import com.eduframepackage.eduframe.quiz.entity.Question;
import com.eduframepackage.eduframe.quiz.entity.QuestionOption;
import com.eduframepackage.eduframe.quiz.entity.QuestionType;
import com.eduframepackage.eduframe.quiz.entity.Quiz;
import com.eduframepackage.eduframe.quiz.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Full CRUD service for {@link Question} (Entity 2/4, the question bank).
 * Create - addQuestion | Read - getQuestionsForQuiz | Update - updateQuestion | Delete - deleteQuestion
 */
@Service
@RequiredArgsConstructor
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final QuizService quizService;

    @Transactional
    public Question addQuestion(QuestionForm form) {
        Quiz quiz = quizService.getQuizById(form.getQuizId());

        Question question = Question.builder()
                .questionText(form.getQuestionText())
                .questionType(form.getQuestionType())
                .marks(form.getMarks())
                .correctAnswer(normaliseAnswer(form))
                .sequence((int) questionRepository.countByQuiz(quiz) + 1)
                .build();

        quiz.addQuestion(question);
        attachOptions(question, form);

        questionRepository.save(question);
        return question;
    }

    @Transactional(readOnly = true)
    public List<Question> getQuestionsForQuiz(Quiz quiz) {
        return questionRepository.findByQuizOrderBySequenceAsc(quiz);
    }

    @Transactional(readOnly = true)
    public Question getQuestionById(Long id) {
        return questionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Question not found: " + id));
    }

    @Transactional
    public Question updateQuestion(Long id, QuestionForm form) {
        Question question = getQuestionById(id);
        question.setQuestionText(form.getQuestionText());
        question.setQuestionType(form.getQuestionType());
        question.setMarks(form.getMarks());
        question.setCorrectAnswer(normaliseAnswer(form));

        question.getOptions().clear();
        attachOptions(question, form);

        return questionRepository.save(question);
    }

    @Transactional
    public void deleteQuestion(Long id) {
        questionRepository.deleteById(id);
    }

    private String normaliseAnswer(QuestionForm form) {
        if (form.getQuestionType() == QuestionType.TRUE_FALSE) {
            return form.getCorrectAnswer().trim().equalsIgnoreCase("true") ? "TRUE" : "FALSE";
        }
        return form.getCorrectAnswer().trim();
    }

    private void attachOptions(Question question, QuestionForm form) {
        if (form.getQuestionType() != QuestionType.MCQ || form.getOptions() == null) {
            return;
        }
        int seq = 1;
        for (String optionText : form.getOptions()) {
            if (optionText == null || optionText.isBlank()) {
                continue;
            }
            QuestionOption option = QuestionOption.builder()
                    .optionText(optionText.trim())
                    .sequence(seq++)
                    .build();
            question.addOption(option);
        }
    }
}
