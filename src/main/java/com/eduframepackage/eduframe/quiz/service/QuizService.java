package com.eduframepackage.eduframe.quiz.service;

import com.eduframepackage.eduframe.quiz.dto.QuizForm;
import com.eduframepackage.eduframe.quiz.entity.Quiz;
import com.eduframepackage.eduframe.quiz.entity.QuizStatus;
import com.eduframepackage.eduframe.quiz.entity.User;
import com.eduframepackage.eduframe.quiz.repository.QuizAttemptRepository;
import com.eduframepackage.eduframe.quiz.repository.QuizRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@RequiredArgsConstructor
public class QuizService {

    private final QuizRepository quizRepository;
    private final QuizAttemptRepository quizAttemptRepository;

    @Transactional
    public Quiz createQuiz(QuizForm form, User teacher) {
        Quiz quiz = Quiz.builder()
                .title(form.getTitle())
                .description(form.getDescription())
                .courseId(form.getCourseId())
                .timerMinutes(form.getTimerMinutes())
                .passMarkPercentage(form.getPassMarkPercentage())
                .status(QuizStatus.DRAFT)
                .createdBy(teacher)
                .build();
        return quizRepository.save(quiz);
    }

    @Transactional(readOnly = true)
    public Quiz getQuizById(Long id) {
        return quizRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Quiz not found: " + id));
    }

    @Transactional(readOnly = true)
    public List<Quiz> getQuizzesByTeacher(User teacher) {
        return quizRepository.findByCreatedBy(teacher);
    }

    @Transactional(readOnly = true)
    public List<Quiz> getAllQuizzes() {
        return quizRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Quiz> getPublishedQuizzes() {
        return quizRepository.findByStatusOrderByCreatedAtDesc(QuizStatus.PUBLISHED);
    }

    @Transactional
    public Quiz updateQuiz(Long id, QuizForm form) {
        Quiz quiz = getQuizById(id);
        quiz.setTitle(form.getTitle());
        quiz.setDescription(form.getDescription());
        quiz.setCourseId(form.getCourseId());
        quiz.setTimerMinutes(form.getTimerMinutes());
        quiz.setPassMarkPercentage(form.getPassMarkPercentage());
        return quizRepository.save(quiz);
    }

    @Transactional
    public Quiz publishQuiz(Long id) {
        Quiz quiz = getQuizById(id);
        if (quiz.getQuestions().isEmpty()) {
            throw new IllegalStateException("Cannot publish a quiz with no questions");
        }
        quiz.setStatus(QuizStatus.PUBLISHED);
        return quizRepository.save(quiz);
    }

    @Transactional
    public Quiz closeQuiz(Long id) {
        Quiz quiz = getQuizById(id);
        quiz.setStatus(QuizStatus.CLOSED);
        return quizRepository.save(quiz);
    }

    @Transactional
    public void deleteQuiz(Long id) {
        Quiz quiz = getQuizById(id);
        long attemptCount = quizAttemptRepository.countByQuiz(quiz);
        if (attemptCount > 0) {
            throw new IllegalStateException(
                    "This quiz has " + attemptCount + " student attempt(s) on record and can't be deleted, "
                            + "to preserve those results and any issued certificates. "
                            + "Close the quiz instead to stop new attempts.");
        }
        quizRepository.deleteById(id);
    }
}