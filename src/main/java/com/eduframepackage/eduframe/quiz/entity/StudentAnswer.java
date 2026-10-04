package com.eduframepackage.eduframe.quiz.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * One answer given by a student for one question, within one attempt.
 * Supporting/child table of QuizAttempt - not counted as one of the
 * subsystem's 4 major entities (Quiz, Question, QuizAttempt, Certificate).
 */
@Entity
@Table(name = "student_answer")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"attempt", "question"})
public class StudentAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attempt_id", nullable = false)
    private QuizAttempt attempt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(length = 1000)
    private String answerText;

    private Boolean correct;

    private Integer marksAwarded;
}
