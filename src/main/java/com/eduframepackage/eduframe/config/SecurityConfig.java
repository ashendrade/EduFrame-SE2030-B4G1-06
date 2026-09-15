package com.eduframepackage.eduframe.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder encoder) {
        UserDetails admin = User.builder()
                .username("admin")
                .password(encoder.encode("password123"))
                .roles("ADMIN")
                .build();

        UserDetails teacher = User.builder()
                .username("teacher")
                .password(encoder.encode("password123"))
                .roles("TEACHER")
                .build();

        UserDetails student = User.builder()
                .username("student")
                .password(encoder.encode("password123"))
                .roles("STUDENT")
                .build();

        return new InMemoryUserDetailsManager(admin, teacher, student);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/dashboard/admin").hasRole("ADMIN")
                .requestMatchers("/dashboard/teacher").hasRole("TEACHER")
                .requestMatchers("/dashboard/student").hasRole("STUDENT")
                .requestMatchers("/", "/browse", "/play", "/play/**", "/upload", "/login", "/support", "/announcements", "/events", "/events/**", "/dashboard", "/api/announcements/**", "/api/tickets/**", "/h2-console/**", "/css/**", "/js/**", "/images/**").permitAll()
                .anyRequest().authenticated()
            )
            .headers(headers -> headers.frameOptions(frame -> frame.disable()))
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/dashboard", true)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/")
                .permitAll()
            )
            .csrf(csrf -> csrf.disable());

        return http.build();
    }
}
