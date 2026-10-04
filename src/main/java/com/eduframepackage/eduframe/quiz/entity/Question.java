package com.eduframepackage.eduframe.quiz.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

/**
 * Entity 2/4 - Question bank.
 *
 * Belongs to exactly one {@link Quiz}. Supports three question types
 * (MCQ, TRUE_FALSE, SHORT_ANSWER) covered by dedicated grading
 * strategies (see service.grading). For MCQ questions, options are
 * stored as a simple ordered list of {@link QuestionOption} rows.
 */
@Entity
@Table(name = "question")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"quiz", "options"})
@EqualsAndHashCode(of = "id")
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @NotBlank
    @Column(nullable = false, length = 1000)
    private String questionText;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private QuestionType questionType;

    @Min(1)
    @Column(nullable = false)
    private Integer marks;

    /**
     * Correct answer text.
     * - MCQ: matches one option's optionText
     * - TRUE_FALSE: "TRUE" or "FALSE"
     * - SHORT_ANSWER: the reference answer used for auto exact/close matching
     */
    @Column(nullable = false, length = 500)
    private String correctAnswer;

    /** Display order within the quiz. */
    private Integer sequence;

    @Builder.Default
    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QuestionOption> options = new ArrayList<>();

    public void addOption(QuestionOption option) {
        options.add(option);
        option.setQuestion(this);
    }
}
