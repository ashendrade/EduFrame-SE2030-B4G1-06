package com.eduframepackage.eduframe.quiz.service;

import com.eduframepackage.eduframe.quiz.entity.Role;
import com.eduframepackage.eduframe.quiz.entity.User;
import com.eduframepackage.eduframe.quiz.repository.QuizUserRepository;
import com.eduframepackage.eduframe.repository.UserRepository;
import com.eduframepackage.eduframe.model.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService {

    private final QuizUserRepository userRepository;
    private final UserRepository mainUserRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User register(String fullName, String email, String rawPassword, Role role) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("An account with this email already exists");
        }
        User user = User.builder()
                .fullName(fullName)
                .email(email)
                .password(passwordEncoder.encode(rawPassword))
                .role(role)
                .createdAt(LocalDateTime.now())
                .build();
        User savedUser = userRepository.save(user);

        try {
            if (!mainUserRepository.existsByEmail(email)) {
                String username = email.contains("@") ? email.split("@")[0] : email;
                UserRole mainRole = (role == Role.ADMIN) ? UserRole.ADMIN :
                        (role == Role.TEACHER) ? UserRole.TEACHER : UserRole.STUDENT;

                com.eduframepackage.eduframe.model.User mainUser = new com.eduframepackage.eduframe.model.User(
                        username, email, savedUser.getPassword(), fullName, mainRole
                );
                mainUserRepository.save(mainUser);
            }
        } catch (Exception e) {
            // Log warning if main user sync has any constraint variation
            System.err.println("Main user sync note: " + e.getMessage());
        }

        return savedUser;
    }

    public User getByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("No user found with email: " + email));
    }

    @Transactional
    public User updateProfile(String currentEmail, String fullName, String newEmail, String currentPassword, String newPassword) {
        User user = getByEmail(currentEmail);

        if (currentPassword != null && !currentPassword.trim().isEmpty()) {
            if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
                throw new IllegalArgumentException("Current password does not match.");
            }
        }

        if (newEmail != null && !newEmail.equalsIgnoreCase(currentEmail)) {
            if (userRepository.existsByEmail(newEmail)) {
                throw new IllegalArgumentException("The new email is already registered by another account.");
            }
            user.setEmail(newEmail);
        }

        if (fullName != null && !fullName.trim().isEmpty()) {
            user.setFullName(fullName);
        }

        if (newPassword != null && !newPassword.trim().isEmpty()) {
            user.setPassword(passwordEncoder.encode(newPassword));
        }

        User updatedUser = userRepository.save(user);

        // Sync with main user repository
        try {
            mainUserRepository.findByEmail(currentEmail).ifPresent(mainUser -> {
                if (fullName != null && !fullName.trim().isEmpty()) {
                    mainUser.setFullName(fullName);
                }
                if (newEmail != null && !newEmail.equalsIgnoreCase(currentEmail)) {
                    mainUser.setEmail(newEmail);
                    mainUser.setUsername(newEmail.contains("@") ? newEmail.split("@")[0] : newEmail);
                }
                if (newPassword != null && !newPassword.trim().isEmpty()) {
                    mainUser.setPassword(updatedUser.getPassword());
                }
                mainUserRepository.save(mainUser);
            });
        } catch (Exception e) {
            System.err.println("Main user sync update note: " + e.getMessage());
        }

        return updatedUser;
    }
}


