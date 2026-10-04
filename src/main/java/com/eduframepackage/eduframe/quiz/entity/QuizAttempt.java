package com.eduframepackage.eduframe.quiz.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity 3/4 - Student submission / attempt.
 *
 * Records one student's attempt at a quiz: when it started/ended, the
 * answers given (child {@link StudentAnswer} rows), and the resulting
 * score once graded. A passed, graded attempt triggers a
 * {@link com.eduframepackage.eduframe.quiz.event.QuizPassedEvent} which the certificate
 * listener (Observer pattern) picks up to auto-issue a Certificate.
 */
@Entity
@Table(name = "quiz_attempt")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"quiz", "student", "answers"})
@EqualsAndHashCode(of = "id")
public class QuizAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @Column(nullable = false)
    private LocalDateTime startedAt;

    private LocalDateTime submittedAt;

    private Integer scoreObtained;

    private Integer scorePercentage;

    private Boolean passed;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AttemptStatus status;

    @Builder.Default
    @OneToMany(mappedBy = "attempt", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StudentAnswer> answers = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (this.startedAt == null) {
            this.startedAt = LocalDateTime.now();
        }
        if (this.status == null) {
            this.status = AttemptStatus.IN_PROGRESS;
        }
    }

    public void addAnswer(StudentAnswer answer) {
        answers.add(answer);
        answer.setAttempt(this);
    }
}
