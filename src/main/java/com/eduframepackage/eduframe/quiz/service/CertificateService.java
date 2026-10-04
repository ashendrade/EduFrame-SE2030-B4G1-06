package com.eduframepackage.eduframe.quiz.service;

import com.eduframepackage.eduframe.quiz.entity.Certificate;
import com.eduframepackage.eduframe.quiz.entity.QuizAttempt;
import com.eduframepackage.eduframe.quiz.entity.User;
import com.eduframepackage.eduframe.quiz.repository.CertificateRepository;
import com.eduframepackage.eduframe.quiz.repository.QuizAttemptRepository;
import com.eduframepackage.eduframe.quiz.service.certificate.CertificatePdfBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Full CRUD service for {@link Certificate} (Entity 4/4).
 * Create - issueCertificateForAttempt (invoked automatically by the
 * Observer listener when a quiz is passed) | Read - getCertificatesForStudent /
 * getPdf | Update - not applicable, certificates are immutable once issued |
 * Delete - revokeCertificate
 */
@Service
@RequiredArgsConstructor
public class CertificateService {

    private final CertificateRepository certificateRepository;
    private final QuizAttemptRepository quizAttemptRepository;

    @Transactional
    public Certificate issueCertificateForAttempt(Long attemptId) {
        if (certificateRepository.findByAttemptId(attemptId).isPresent()) {
            return certificateRepository.findByAttemptId(attemptId).get();
        }

        QuizAttempt attempt = quizAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new IllegalArgumentException("Attempt not found: " + attemptId));

        if (!Boolean.TRUE.equals(attempt.getPassed())) {
            throw new IllegalStateException("Cannot issue a certificate for a failed or ungraded attempt");
        }

        String code = "EDF-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        LocalDateTime issuedAt = LocalDateTime.now();

        byte[] pdf = new CertificatePdfBuilder()
                .withStudentName(attempt.getStudent().getFullName())
                .withQuizTitle(attempt.getQuiz().getTitle())
                .withCertificateCode(code)
                .withScorePercentage(attempt.getScorePercentage())
                .withIssuedDate(issuedAt)
                .build();

        Certificate certificate = Certificate.builder()
                .certificateCode(code)
                .student(attempt.getStudent())
                .quiz(attempt.getQuiz())
                .attempt(attempt)
                .issuedAt(issuedAt)
                .pdfContent(pdf)
                .build();

        return certificateRepository.save(certificate);
    }

    @Transactional(readOnly = true)
    public List<Certificate> getCertificatesForStudent(User student) {
        return certificateRepository.findByStudentOrderByIssuedAtDesc(student);
    }

    @Transactional(readOnly = true)
    public Certificate getById(Long id) {
        return certificateRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Certificate not found: " + id));
    }

    @Transactional
    public void revokeCertificate(Long id) {
        certificateRepository.deleteById(id);
    }
}
