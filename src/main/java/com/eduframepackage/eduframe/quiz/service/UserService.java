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
        if (userRepository.existsByEmail(email) || mainUserRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("An account with this email already exists");
        }
        User user = User.builder()
                .fullName(fullName != null && !fullName.trim().isEmpty() ? fullName : email)
                .email(email)
                .password(passwordEncoder.encode(rawPassword))
                .role(role != null ? role : Role.STUDENT)
                .createdAt(LocalDateTime.now())
                .build();
        User savedUser = userRepository.save(user);

        UserRole mainRole = (role == Role.ADMIN) ? UserRole.ADMIN :
                (role == Role.TEACHER) ? UserRole.TEACHER : UserRole.STUDENT;

        com.eduframepackage.eduframe.model.User mainUser = new com.eduframepackage.eduframe.model.User(
                email, savedUser.getPassword(), fullName, mainRole
        );
        mainUserRepository.save(mainUser);

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
                    String[] parts = fullName.trim().split(" ", 2);
                    mainUser.setFirstName(parts[0]);
                    mainUser.setLastName(parts.length > 1 ? parts[1] : "");
                }
                if (newEmail != null && !newEmail.equalsIgnoreCase(currentEmail)) {
                    mainUser.setEmail(newEmail);
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

    public java.util.List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));
    }

    @Transactional
    public User updateRole(Long userId, Role newRole) {
        User user = getUserById(userId);
        user.setRole(newRole);
        User updated = userRepository.save(user);

        try {
            mainUserRepository.findByEmail(user.getEmail()).ifPresent(mainUser -> {
                UserRole mainRole = (newRole == Role.ADMIN) ? UserRole.ADMIN :
                        (newRole == Role.TEACHER) ? UserRole.TEACHER : UserRole.STUDENT;
                mainUser.setRole(mainRole);
                mainUserRepository.save(mainUser);
            });
        } catch (Exception e) {
            System.err.println("Main user role update sync note: " + e.getMessage());
        }

        return updated;
    }

    @Transactional
    public User adminUpdateUser(Long userId, String fullName, String newEmail, String newPassword, Role newRole) {
        User user = getUserById(userId);
        String oldEmail = user.getEmail();

        if (newEmail != null && !newEmail.equalsIgnoreCase(oldEmail)) {
            if (userRepository.existsByEmail(newEmail) || mainUserRepository.existsByEmail(newEmail)) {
                throw new IllegalArgumentException("The email address is already in use by another account.");
            }
            user.setEmail(newEmail);
        }

        if (fullName != null && !fullName.trim().isEmpty()) {
            user.setFullName(fullName);
        }

        if (newRole != null) {
            user.setRole(newRole);
        }

        if (newPassword != null && !newPassword.trim().isEmpty()) {
            user.setPassword(passwordEncoder.encode(newPassword));
        }

        User updatedUser = userRepository.save(user);

        try {
            mainUserRepository.findByEmail(oldEmail).ifPresent(mainUser -> {
                if (fullName != null && !fullName.trim().isEmpty()) {
                    String[] parts = fullName.trim().split(" ", 2);
                    mainUser.setFirstName(parts[0]);
                    mainUser.setLastName(parts.length > 1 ? parts[1] : "");
                }
                if (newEmail != null && !newEmail.equalsIgnoreCase(oldEmail)) {
                    mainUser.setEmail(newEmail);
                }
                if (newRole != null) {
                    UserRole mainRole = (newRole == Role.ADMIN) ? UserRole.ADMIN :
                            (newRole == Role.TEACHER) ? UserRole.TEACHER : UserRole.STUDENT;
                    mainUser.setRole(mainRole);
                }
                if (newPassword != null && !newPassword.trim().isEmpty()) {
                    mainUser.setPassword(updatedUser.getPassword());
                }
                mainUserRepository.save(mainUser);
            });
        } catch (Exception e) {
            System.err.println("Main user sync edit note: " + e.getMessage());
        }

        return updatedUser;
    }

    @Transactional
    public void deleteUser(Long userId) {
        User user = getUserById(userId);
        String email = user.getEmail();
        userRepository.deleteById(userId);

        try {
            mainUserRepository.findByEmail(email).ifPresent(mainUser -> {
                mainUserRepository.delete(mainUser);
            });
        } catch (Exception e) {
            System.err.println("Main user delete sync note: " + e.getMessage());
        }
    }
}



