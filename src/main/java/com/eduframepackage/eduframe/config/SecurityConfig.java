package com.eduframepackage.eduframe.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import com.eduframepackage.eduframe.quiz.security.CustomUserDetailsService;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(CustomUserDetailsService customUserDetailsService, PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(customUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler() {
        return (request, response, authentication) -> {
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            boolean isTeacher = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_TEACHER"));
            boolean isStaff = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_STAFF"));
            
            if (isAdmin) {
                response.sendRedirect("/dashboard");
            } else if (isTeacher) {
                response.sendRedirect("/teacher/dashboard");
            } else if (isStaff) {
                response.sendRedirect("/staff/helpdesk");
            } else {
                response.sendRedirect("/student/dashboard");
            }
        };
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authorize -> authorize
                // Publicly accessible pages & static assets
                .requestMatchers("/", "/browse", "/play", "/play/**", "/login", "/register", "/events", "/announcements", "/api/announcements/**", "/css/**", "/js/**", "/images/**", "/img/**", "/uploads/**", "/videos/**").permitAll()
                
                // Admin endpoints & user management
                .requestMatchers("/admin/**", "/api/admin/**").hasRole("ADMIN")
                
                // Teacher / Instructor & Admin secured endpoints
                .requestMatchers("/teacher/**", "/upload", "/upload/**", "/advertisements").hasAnyRole("TEACHER", "ADMIN")
                .requestMatchers("/api/advertisements/**").permitAll()
                
                // Student secured endpoints
                .requestMatchers("/student/**").hasAnyRole("STUDENT", "ADMIN")
                
                // General dashboard & support portal secured endpoints
                .requestMatchers("/dashboard", "/support").authenticated()
                .requestMatchers("/staff/**").hasAnyRole("STAFF", "ADMIN", "TEACHER")
                
                .anyRequest().authenticated()
            )
            .headers(headers -> headers.frameOptions(frame -> frame.disable()))
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .successHandler(authenticationSuccessHandler())
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            )
            .csrf(csrf -> csrf.disable());

        return http.build();
    }
}
