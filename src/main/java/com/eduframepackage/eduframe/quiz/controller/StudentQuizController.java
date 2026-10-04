package com.eduframepackage.eduframe.quiz.controller;

import com.eduframepackage.eduframe.quiz.entity.*;
import com.eduframepackage.eduframe.quiz.security.CurrentUserProvider;
import com.eduframepackage.eduframe.quiz.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * Student-facing screens: browse published quizzes, take a quiz within
 * its timer, submit for auto-grading, and view results / certificates.
 */
@Controller
@RequestMapping("/student")
@RequiredArgsConstructor
public class StudentQuizController {

    private final QuizService quizService;
    private final QuestionService questionService;
    private final QuizAttemptService quizAttemptService;
    private final CertificateService certificateService;
    private final CurrentUserProvider currentUserProvider;
    private final com.eduframepackage.eduframe.service.CourseService courseService;

    @GetMapping("/dashboard")
    public String dashboard(Authentication auth, Model model) {
        User student = currentUserProvider.getCurrentUser(auth);
        List<Quiz> availableQuizzes = quizService.getPublishedQuizzes();
        List<QuizAttempt> myAttempts = quizAttemptService.getAttemptsForStudent(student);
        List<com.eduframepackage.eduframe.model.Course> enrolledCourses = courseService.getEnrolledCoursesForStudent(student.getEmail());

        model.addAttribute("student", student);
        model.addAttribute("quizzes", availableQuizzes);
        model.addAttribute("myAttempts", myAttempts);
        model.addAttribute("enrolledCourses", enrolledCourses);
        return "student/dashboard";
    }

    @PostMapping("/quizzes/{id}/start")
    public String startQuiz(@PathVariable Long id, Authentication auth, Model model) {
        User student = currentUserProvider.getCurrentUser(auth);
        try {
            QuizAttempt attempt = quizAttemptService.startAttempt(id, student);
            return "redirect:/student/attempts/" + attempt.getId();
        } catch (IllegalStateException ex) {
            model.addAttribute("error", ex.getMessage());
            return "redirect:/student/dashboard";
        }
    }

    @GetMapping("/attempts/{attemptId}")
    public String takeQuiz(@PathVariable Long attemptId, Model model) {
        QuizAttempt attempt = quizAttemptService.getAttemptById(attemptId);

        if (attempt.getStatus() != AttemptStatus.IN_PROGRESS) {
            return "redirect:/student/attempts/" + attemptId + "/result";
        }

        Quiz quiz = attempt.getQuiz();
        List<Question> questions = questionService.getQuestionsForQuiz(quiz);

        model.addAttribute("attempt", attempt);
        model.addAttribute("quiz", quiz);
        model.addAttribute("questions", questions);
        return "student/quiz-take";
    }

    @PostMapping("/attempts/{attemptId}/submit")
    public String submitQuiz(@PathVariable Long attemptId,
                             @RequestParam Map<String, String> allParams) {
        Map<Long, String> answers = new HashMap<>();
        allParams.forEach((key, value) -> {
            if (key.startsWith("answer_")) {
                Long questionId = Long.valueOf(key.substring("answer_".length()));
                answers.put(questionId, value);
            }
        });

        quizAttemptService.submitAttempt(attemptId, answers);
        return "redirect:/student/attempts/" + attemptId + "/result";
    }

    @GetMapping("/attempts/{attemptId}/result")
    public String result(@PathVariable Long attemptId, Model model) {
        QuizAttempt attempt = quizAttemptService.getAttemptById(attemptId);

        // Safety net: certificate issuance normally happens asynchronously via
        // QuizPassedEvent (Observer pattern) right after the attempt is graded.
        // If that event failed to fire or its handler errored (Spring silently
        // logs and swallows exceptions thrown from an AFTER_COMMIT listener),
        // this guarantees a passed student still gets their certificate the
        // moment they view their result. issueCertificateForAttempt() is
        // idempotent - safe to call even if one already exists.
        if (Boolean.TRUE.equals(attempt.getPassed())) {
            try {
                certificateService.issueCertificateForAttempt(attemptId);
            } catch (Exception ex) {
                System.err.println("[Certificate] Failed to issue certificate for attempt "
                        + attemptId + ": " + ex);
                ex.printStackTrace();
            }
        }

        model.addAttribute("attempt", attempt);
        model.addAttribute("quiz", attempt.getQuiz());
        return "student/quiz-result";
    }

    @GetMapping("/certificates")
    public String certificates(Authentication auth, Model model) {
        User student = currentUserProvider.getCurrentUser(auth);
        List<Certificate> certificates = certificateService.getCertificatesForStudent(student);
        model.addAttribute("certificates", certificates);
        return "student/certificates";
    }
}