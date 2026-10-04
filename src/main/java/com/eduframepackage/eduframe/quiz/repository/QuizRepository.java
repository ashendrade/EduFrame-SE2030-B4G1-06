package com.eduframepackage.eduframe.quiz.repository;

import com.eduframepackage.eduframe.quiz.entity.Quiz;
import com.eduframepackage.eduframe.quiz.entity.QuizStatus;
import com.eduframepackage.eduframe.quiz.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuizRepository extends JpaRepository<Quiz, Long> {
    List<Quiz> findByCreatedBy(User createdBy);
    List<Quiz> findByStatus(QuizStatus status);
    List<Quiz> findByStatusOrderByCreatedAtDesc(QuizStatus status);
}
