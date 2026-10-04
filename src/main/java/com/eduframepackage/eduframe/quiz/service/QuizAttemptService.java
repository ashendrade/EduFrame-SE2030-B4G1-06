package com.eduframepackage.eduframe.quiz.service;

import com.eduframepackage.eduframe.quiz.entity.*;
import com.eduframepackage.eduframe.quiz.event.QuizPassedEvent;
import com.eduframepackage.eduframe.quiz.repository.QuizAttemptRepository;
import com.eduframepackage.eduframe.quiz.service.grading.GradingResult;
import com.eduframepackage.eduframe.quiz.service.grading.GradingStrategy;
import com.eduframepackage.eduframe.quiz.service.grading.GradingStrategyFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * Full CRUD service for {@link QuizAttempt} (Entity 3/4, submissions).
 * Create - startAttempt | Read - getAttemptById/getAttemptsForQuiz |
 * Update - submitAttempt (grades + updates status) | Delete - deleteAttempt
 *
 * Grading delegates to {@link GradingStrategyFactory} (Strategy pattern).
 * A passing grade publishes {@link QuizPassedEvent} (Observer pattern),
 * which {@code CertificateEventListener} reacts to by issuing a PDF
 * certificate - this service knows nothing about certificates directly.
 */
@Service
@RequiredArgsConstructor
public class QuizAttemptService {

    private final QuizAttemptRepository quizAttemptRepository;
    private final QuizService quizService;
    private final QuestionService questionService;
    private final GradingStrategyFactory gradingStrategyFactory;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public QuizAttempt startAttempt(Long quizId, User student) {
        Quiz quiz = quizService.getQuizById(quizId);
        if (quiz.getStatus() != QuizStatus.PUBLISHED) {
            throw new IllegalStateException("This quiz is not currently open for attempts");
        }

        quizAttemptRepository
                .findFirstByQuizAndStudentAndStatusOrderByStartedAtDesc(quiz, student, AttemptStatus.IN_PROGRESS)
                .ifPresent(existing -> {
                    throw new IllegalStateException("You already have an attempt in progress for this quiz");
                });

        QuizAttempt attempt = QuizAttempt.builder()
                .quiz(quiz)
                .student(student)
                .status(AttemptStatus.IN_PROGRESS)
                .build();
        return quizAttemptRepository.save(attempt);
    }

    @Transactional(readOnly = true)
    public QuizAttempt getAttemptById(Long id) {
        return quizAttemptRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Attempt not found: " + id));
    }

    @Transactional(readOnly = true)
    public List<QuizAttempt> getAttemptsForQuiz(Quiz quiz) {
        return quizAttemptRepository.findByQuizOrderBySubmittedAtDesc(quiz);
    }

    @Transactional(readOnly = true)
    public List<QuizAttempt> getAttemptsForStudent(User student) {
        return quizAttemptRepository.findByStudentOrderByStartedAtDesc(student);
    }

    /**
     * Grades and finalises an attempt. answersByQuestionId maps questionId -> raw student answer text.
     */
    @Transactional
    public QuizAttempt submitAttempt(Long attemptId, Map<Long, String> answersByQuestionId) {
        QuizAttempt attempt = getAttemptById(attemptId);
        if (attempt.getStatus() != AttemptStatus.IN_PROGRESS) {
            throw new IllegalStateException("This attempt has already been submitted");
        }

        Quiz quiz = attempt.getQuiz();
        List<Question> questions = questionService.getQuestionsForQuiz(quiz);

        int totalScored = 0;
        for (Question question : questions) {
            String rawAnswer = answersByQuestionId.get(question.getId());

            GradingStrategy strategy = gradingStrategyFactory.getStrategy(question.getQuestionType());
            GradingResult result = strategy.grade(question, rawAnswer);

            StudentAnswer answer = StudentAnswer.builder()
                    .question(question)
                    .answerText(rawAnswer)
                    .correct(result.correct())
                    .marksAwarded(result.marksAwarded())
                    .build();
            attempt.addAnswer(answer);

            totalScored += result.marksAwarded();
        }

        int totalMarks = quiz.getTotalMarks();
        int percentage = totalMarks == 0 ? 0 : Math.round((totalScored * 100f) / totalMarks);
        boolean passed = percentage >= quiz.getPassMarkPercentage();

        attempt.setScoreObtained(totalScored);
        attempt.setScorePercentage(percentage);
        attempt.setPassed(passed);
        attempt.setStatus(AttemptStatus.GRADED);
        attempt.setSubmittedAt(java.time.LocalDateTime.now());

        QuizAttempt saved = quizAttemptRepository.save(attempt);

        if (passed) {
            eventPublisher.publishEvent(new QuizPassedEvent(this, saved));
        }

        return saved;
    }

    @Transactional
    public void deleteAttempt(Long id) {
        quizAttemptRepository.deleteById(id);
    }
}
