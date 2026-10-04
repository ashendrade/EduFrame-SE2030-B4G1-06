package com.eduframepackage.eduframe.quiz.security;

import com.eduframepackage.eduframe.quiz.entity.User;
import com.eduframepackage.eduframe.quiz.repository.QuizUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CurrentUserProvider {

    private final QuizUserRepository userRepository;

    public User getCurrentUser(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new IllegalStateException("User is not authenticated");
        }
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Logged-in user not found: " + email));
    }
}
