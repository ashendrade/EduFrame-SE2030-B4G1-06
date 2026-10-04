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

/**
 * Entity 4/4 - Completion certificate.
 *
 * Auto-generated (via the Observer pattern - see event package) the
 * moment a graded {@link QuizAttempt} is marked as passed. Stores the
 * generated PDF as a byte array so the certificate can be downloaded
 * on demand without depending on external file storage.
 */
@Entity
@Table(name = "certificate")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"student", "quiz", "attempt", "pdfContent"})
@EqualsAndHashCode(of = "id")
public class Certificate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String certificateCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attempt_id", nullable = false, unique = true)
    private QuizAttempt attempt;

    @Column(nullable = false)
    private LocalDateTime issuedAt;

    @Lob
    @Column(name = "pdf_content")
    private byte[] pdfContent;

    @PrePersist
    protected void onCreate() {
        this.issuedAt = LocalDateTime.now();
    }
}
