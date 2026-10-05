package com.eduframepackage.eduframe.quiz.security;

import com.eduframepackage.eduframe.quiz.entity.User;
import com.eduframepackage.eduframe.quiz.repository.QuizUserRepository;
import com.eduframepackage.eduframe.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final QuizUserRepository quizUserRepository;
    private final UserRepository mainUserRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // First look in QuizUserRepository
        var quizOpt = quizUserRepository.findByEmail(email);
        if (quizOpt.isPresent()) {
            User user = quizOpt.get();
            return org.springframework.security.core.userdetails.User
                    .withUsername(user.getEmail())
                    .password(user.getPassword())
                    .authorities(List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())))
                    .build();
        }

        // Fallback to main UserRepository (dbo.Users)
        var mainOpt = mainUserRepository.findByEmail(email);
        if (mainOpt.isPresent()) {
            var user = mainOpt.get();
            String roleName = user.getRole() != null ? user.getRole().name().toUpperCase() : "STUDENT";
            return org.springframework.security.core.userdetails.User
                    .withUsername(user.getEmail())
                    .password(user.getPassword())
                    .authorities(List.of(new SimpleGrantedAuthority("ROLE_" + roleName)))
                    .build();
        }

        throw new UsernameNotFoundException("No account found for " + email);
    }
}
