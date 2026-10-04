package com.eduframepackage.eduframe.quiz.repository;

import com.eduframepackage.eduframe.quiz.entity.QuizAttempt;
import com.eduframepackage.eduframe.quiz.entity.StudentAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StudentAnswerRepository extends JpaRepository<StudentAnswer, Long> {
    List<StudentAnswer> findByAttempt(QuizAttempt attempt);
}
