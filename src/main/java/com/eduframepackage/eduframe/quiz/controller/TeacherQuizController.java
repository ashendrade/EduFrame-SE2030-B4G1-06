package com.eduframepackage.eduframe.quiz.controller;

import com.eduframepackage.eduframe.quiz.dto.QuestionForm;
import com.eduframepackage.eduframe.quiz.dto.QuizAttemptSummary;
import com.eduframepackage.eduframe.quiz.dto.QuizForm;
import com.eduframepackage.eduframe.quiz.entity.Question;
import com.eduframepackage.eduframe.quiz.entity.Quiz;
import com.eduframepackage.eduframe.quiz.entity.QuizAttempt;
import com.eduframepackage.eduframe.quiz.entity.User;
import com.eduframepackage.eduframe.quiz.security.CurrentUserProvider;
import com.eduframepackage.eduframe.quiz.service.QuestionService;
import com.eduframepackage.eduframe.quiz.service.QuizAttemptService;
import com.eduframepackage.eduframe.quiz.service.QuizService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Teacher-facing CRUD screens for the Quiz & Assessment Subsystem.
 * Covers Quiz CRUD, Question CRUD (question bank), publish/close, and
 * the results/analytics view over student submissions.
 */
@Controller
@RequestMapping("/teacher")
@RequiredArgsConstructor
public class TeacherQuizController {

    private final QuizService quizService;
    private final QuestionService questionService;
    private final QuizAttemptService quizAttemptService;
    private final CurrentUserProvider currentUserProvider;
    private final com.eduframepackage.eduframe.service.CourseService courseService;

    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");

    //  Dashboard (Unified Course & Quiz Management)

    @GetMapping("/dashboard")
    public String dashboard(Authentication auth, Model model) {
        User teacher = currentUserProvider.getCurrentUser(auth);
        List<Quiz> quizzes = quizService.getQuizzesByTeacher(teacher);
        List<com.eduframepackage.eduframe.model.Course> courses = courseService.getAllCourses();

        int totalEnrolled = courseService.getTotalEnrolledStudentsCount();

        model.addAttribute("teacher", teacher);
        model.addAttribute("quizzes", quizzes);
        model.addAttribute("courses", courses);
        model.addAttribute("totalCourses", courses.size());
        model.addAttribute("totalQuizzes", quizzes.size());
        model.addAttribute("totalEnrolled", totalEnrolled);

        return "teacher/dashboard";
    }

    //  Quiz CRUD

    @GetMapping("/quizzes/new")
    public String newQuizForm(Model model) {
        model.addAttribute("quizForm", new QuizForm());
        return "teacher/quiz-form";
    }

    @PostMapping("/quizzes")
    public String createQuiz(@Valid @ModelAttribute("quizForm") QuizForm form,
                              BindingResult bindingResult,
                              Authentication auth) {
        if (bindingResult.hasErrors()) {
            return "teacher/quiz-form";
        }
        User teacher = currentUserProvider.getCurrentUser(auth);
        Quiz quiz = quizService.createQuiz(form, teacher);
        return "redirect:/teacher/quizzes/" + quiz.getId();
    }

    @GetMapping("/quizzes/{id}")
    public String viewQuiz(@PathVariable Long id, Model model) {
        Quiz quiz = quizService.getQuizById(id);
        List<Question> questions = questionService.getQuestionsForQuiz(quiz);
        model.addAttribute("quiz", quiz);
        model.addAttribute("questions", questions);
        model.addAttribute("questionForm", new QuestionForm());
        return "teacher/quiz-detail";
    }

    @GetMapping("/quizzes/{id}/edit")
    public String editQuizForm(@PathVariable Long id, Model model) {
        Quiz quiz = quizService.getQuizById(id);
        QuizForm form = new QuizForm();
        form.setId(quiz.getId());
        form.setTitle(quiz.getTitle());
        form.setDescription(quiz.getDescription());
        form.setCourseId(quiz.getCourseId());
        form.setTimerMinutes(quiz.getTimerMinutes());
        form.setPassMarkPercentage(quiz.getPassMarkPercentage());
        model.addAttribute("quizForm", form);
        return "teacher/quiz-form";
    }

    @PostMapping("/quizzes/{id}")
    public String updateQuiz(@PathVariable Long id,
                              @Valid @ModelAttribute("quizForm") QuizForm form,
                              BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "teacher/quiz-form";
        }
        quizService.updateQuiz(id, form);
        return "redirect:/teacher/quizzes/" + id;
    }

    @PostMapping("/quizzes/{id}/publish")
    public String publishQuiz(@PathVariable Long id) {
        quizService.publishQuiz(id);
        return "redirect:/teacher/quizzes/" + id;
    }

    @PostMapping("/quizzes/{id}/close")
    public String closeQuiz(@PathVariable Long id) {
        quizService.closeQuiz(id);
        return "redirect:/teacher/quizzes/" + id;
    }

    @PostMapping("/quizzes/{id}/delete")
    public String deleteQuiz(@PathVariable Long id) {
        quizService.deleteQuiz(id);
        return "redirect:/teacher/dashboard";
    }

    //  Question CRUD (question bank)

    @PostMapping("/quizzes/{quizId}/questions")
    public String addQuestion(@PathVariable Long quizId,
                               @Valid @ModelAttribute("questionForm") QuestionForm form,
                               BindingResult bindingResult,
                               Model model) {
        form.setQuizId(quizId);
        if (bindingResult.hasErrors()) {
            Quiz quiz = quizService.getQuizById(quizId);
            model.addAttribute("quiz", quiz);
            model.addAttribute("questions", questionService.getQuestionsForQuiz(quiz));
            return "teacher/quiz-detail";
        }
        questionService.addQuestion(form);
        return "redirect:/teacher/quizzes/" + quizId;
    }

    @GetMapping("/questions/{id}/edit")
    public String editQuestionForm(@PathVariable Long id, Model model) {
        Question question = questionService.getQuestionById(id);
        QuestionForm form = new QuestionForm();
        form.setId(question.getId());
        form.setQuizId(question.getQuiz().getId());
        form.setQuestionText(question.getQuestionText());
        form.setQuestionType(question.getQuestionType());
        form.setMarks(question.getMarks());
        form.setCorrectAnswer(question.getCorrectAnswer());
        form.setOptions(question.getOptions().stream().map(o -> o.getOptionText()).toList());
        model.addAttribute("questionForm", form);
        model.addAttribute("quiz", question.getQuiz());
        return "teacher/question-form";
    }

    @PostMapping("/questions/{id}")
    public String updateQuestion(@PathVariable Long id,
                                  @Valid @ModelAttribute("questionForm") QuestionForm form,
                                  BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "teacher/question-form";
        }
        Question updated = questionService.updateQuestion(id, form);
        return "redirect:/teacher/quizzes/" + updated.getQuiz().getId();
    }

    @PostMapping("/questions/{id}/delete")
    public String deleteQuestion(@PathVariable Long id) {
        Question question = questionService.getQuestionById(id);
        Long quizId = question.getQuiz().getId();
        questionService.deleteQuestion(id);
        return "redirect:/teacher/quizzes/" + quizId;
    }

    // ---------- Results / analytics (Read) ----------

    @GetMapping("/quizzes/{id}/results")
    public String viewResults(@PathVariable Long id, Model model) {
        Quiz quiz = quizService.getQuizById(id);
        List<QuizAttempt> attempts = quizAttemptService.getAttemptsForQuiz(quiz);

        List<QuizAttemptSummary> summaries = attempts.stream()
                .map(a -> new QuizAttemptSummary(
                        a.getId(),
                        a.getStudent().getFullName(),
                        a.getStudent().getEmail(),
                        a.getScoreObtained(),
                        quiz.getTotalMarks(),
                        a.getScorePercentage(),
                        a.getPassed(),
                        a.getStatus().name(),
                        a.getSubmittedAt() != null ? a.getSubmittedAt().format(DTF) : "-"
                ))
                .toList();

        model.addAttribute("quiz", quiz);
        model.addAttribute("attempts", summaries);
        return "teacher/quiz-results";
    }
}
