package com.eduframepackage.eduframe.quiz.repository;

import com.eduframepackage.eduframe.quiz.entity.Certificate;
import com.eduframepackage.eduframe.quiz.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CertificateRepository extends JpaRepository<Certificate, Long> {
    List<Certificate> findByStudentOrderByIssuedAtDesc(User student);
    Optional<Certificate> findByAttemptId(Long attemptId);
    Optional<Certificate> findByCertificateCode(String certificateCode);
}
