package com.eduframepackage.eduframe.quiz.repository;

import com.eduframepackage.eduframe.quiz.entity.Quiz;
import com.eduframepackage.eduframe.quiz.entity.QuizAttempt;
import com.eduframepackage.eduframe.quiz.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {
    List<QuizAttempt> findByQuizOrderBySubmittedAtDesc(Quiz quiz);
    List<QuizAttempt> findByStudentOrderByStartedAtDesc(User student);
    Optional<QuizAttempt> findFirstByQuizAndStudentAndStatusOrderByStartedAtDesc(
            Quiz quiz, User student, com.eduframepackage.eduframe.quiz.entity.AttemptStatus status);
    long countByQuiz(Quiz quiz);
}
