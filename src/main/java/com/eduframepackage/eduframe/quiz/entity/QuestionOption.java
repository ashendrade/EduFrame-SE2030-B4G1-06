package com.eduframepackage.eduframe.quiz.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * A single selectable option for an MCQ {@link Question}.
 * Supporting/child table of Question - not counted as one of the
 * subsystem's 4 major entities (Quiz, Question, QuizAttempt, Certificate).
 */
@Entity
@Table(name = "question_option")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "question")
public class QuestionOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(nullable = false, length = 300)
    private String optionText;

    private Integer sequence;
}
