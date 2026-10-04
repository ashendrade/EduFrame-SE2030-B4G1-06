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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity 1/4 - Quiz configuration.
 *
 * A Quiz is the aggregate root of the module: it owns a set of
 * {@link Question}s and is the target of {@link QuizAttempt}s made by
 * students. CRUD: Create/Read/Update/Delete are all exposed through
 * TeacherQuizController.
 */
@Entity
@Table(name = "quiz")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "questions")
@EqualsAndHashCode(of = "id")
public class Quiz {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, length = 150)
    private String title;

    @Column(length = 1000)
    private String description;

    /** Optional link to a Course module owned by a teammate (loosely coupled by id only). */
    private Long courseId;

    @Min(1)
    @Column(nullable = false)
    private Integer timerMinutes;

    @Min(0)
    @Column(nullable = false)
    private Integer passMarkPercentage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private QuizStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @Builder.Default
    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Question> questions = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = QuizStatus.DRAFT;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /** Total marks available across every question in this quiz. */
    @Transient
    public int getTotalMarks() {
        return questions.stream().mapToInt(q -> q.getMarks() != null ? q.getMarks() : 0).sum();
    }

    public void addQuestion(Question question) {
        questions.add(question);
        question.setQuiz(this);
    }
}
