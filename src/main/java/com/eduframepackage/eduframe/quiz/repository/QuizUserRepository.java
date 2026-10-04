package com.eduframepackage.eduframe.quiz.repository;

import com.eduframepackage.eduframe.quiz.entity.Role;
import com.eduframepackage.eduframe.quiz.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface QuizUserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    List<User> findByRole(Role role);
}
