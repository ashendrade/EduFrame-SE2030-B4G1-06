package com.eduframepackage.eduframe.quiz.repository;

import com.eduframepackage.eduframe.quiz.entity.Question;
import com.eduframepackage.eduframe.quiz.entity.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findByQuizOrderBySequenceAsc(Quiz quiz);
    long countByQuiz(Quiz quiz);
}
