package com.eduframepackage.eduframe.controller;

import com.eduframepackage.eduframe.model.Course;
import com.eduframepackage.eduframe.model.CourseModule;
import com.eduframepackage.eduframe.quiz.entity.Quiz;
import com.eduframepackage.eduframe.quiz.service.QuizService;
import com.eduframepackage.eduframe.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class CourseController {

    @Autowired
    private CourseService courseService;

    @Autowired
    private QuizService quizService;

    @GetMapping("/courses/{id}")
    public String viewCourseDetail(@PathVariable("id") Long id, Authentication auth, Model model) {
        Course course = courseService.getCourseById(id)
                .orElseThrow(() -> new IllegalArgumentException("Course not found with ID: " + id));

        List<CourseModule> modules = courseService.getModulesForCourse(id);

        boolean isEnrolled = false;
        String userEmail = null;
        if (auth != null) {
            userEmail = auth.getName();
            isEnrolled = courseService.isStudentEnrolled(userEmail, id);
        }

        // Get linked published quizzes for this course
        List<Quiz> quizzes = quizService.getPublishedQuizzes();

        model.addAttribute("course", course);
        model.addAttribute("modules", modules);
        model.addAttribute("quizzes", quizzes);
        model.addAttribute("isEnrolled", isEnrolled);
        model.addAttribute("userEmail", userEmail);

        return "course-detail";
    }

    @PostMapping("/courses/{id}/enroll")
    public String enrollInCourse(@PathVariable("id") Long id, Authentication auth, RedirectAttributes redirectAttributes) {
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }

        String userEmail = auth.getName();
        boolean success = courseService.enrollStudent(userEmail, id);

        if (success) {
            redirectAttributes.addFlashAttribute("successMessage", "You have successfully enrolled in this course!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to enroll. Please try again.");
        }

        return "redirect:/courses/" + id;
    }

    @PostMapping("/teacher/courses/create")
    public String createCourse(@RequestParam("code") String code,
                               @RequestParam("title") String title,
                               @RequestParam("category") String category,
                               @RequestParam(value = "instructor", required = false) String instructor,
                               @RequestParam("description") String description,
                               @RequestParam(value = "duration", required = false) String duration,
                               @RequestParam(value = "moduleTitle", required = false) String moduleTitle,
                               @RequestParam(value = "moduleSummary", required = false) String moduleSummary,
                               Authentication auth,
                               RedirectAttributes redirectAttributes) {

        String lecturerName = (instructor != null && !instructor.trim().isEmpty()) ? instructor.trim() :
                (auth != null ? auth.getName() : "Prof. Kanishka Jayasinghe");

        Course course = courseService.createCourse(
                code.trim(),
                title.trim(),
                category.trim(),
                lecturerName,
                description.trim(),
                duration != null ? duration.trim() : "10 Weeks",
                moduleTitle,
                moduleSummary
        );

        redirectAttributes.addFlashAttribute("successMessage", "Course '" + course.getTitle() + "' published successfully!");
        return "redirect:/courses/" + course.getId();
    }

    @GetMapping("/teacher/courses/{id}/edit")
    public String editCourseForm(@PathVariable("id") Long id, Model model) {
        Course course = courseService.getCourseById(id)
                .orElseThrow(() -> new IllegalArgumentException("Course not found with ID: " + id));
        List<CourseModule> modules = courseService.getModulesForCourse(id);
        model.addAttribute("course", course);
        model.addAttribute("modules", modules);
        return "course-edit";
    }

    @PostMapping("/teacher/courses/{id}/update")
    public String updateCourse(@PathVariable("id") Long id,
                               @RequestParam("code") String code,
                               @RequestParam("title") String title,
                               @RequestParam("category") String category,
                               @RequestParam(value = "instructor", required = false) String instructor,
                               @RequestParam("description") String description,
                               @RequestParam(value = "duration", required = false) String duration,
                               RedirectAttributes redirectAttributes) {

        Course updated = courseService.updateCourse(id, code, title, category, instructor, description, duration);
        redirectAttributes.addFlashAttribute("successMessage", "Course details for '" + updated.getTitle() + "' updated successfully!");
        return "redirect:/teacher/courses/" + id + "/edit";
    }

    @PostMapping("/teacher/courses/{id}/modules/add")
    public String addModule(@PathVariable("id") Long id,
                            @RequestParam("moduleTitle") String moduleTitle,
                            @RequestParam(value = "summary", required = false) String summary,
                            @RequestParam(value = "videoUrl", required = false) String videoUrl,
                            @RequestParam(value = "duration", required = false) String duration,
                            @RequestParam(value = "notesUrl", required = false) String notesUrl,
                            RedirectAttributes redirectAttributes) {
        courseService.addModuleToCourse(id, moduleTitle, summary, videoUrl, duration, notesUrl);
        redirectAttributes.addFlashAttribute("successMessage", "New course module added successfully!");
        return "redirect:/teacher/courses/" + id + "/edit";
    }

    @PostMapping("/teacher/modules/{moduleId}/move-up")
    public String moveModuleUp(@PathVariable("moduleId") Long moduleId, RedirectAttributes redirectAttributes) {
        Long courseId = courseService.getModuleById(moduleId).map(m -> m.getCourseId()).orElse(null);
        courseService.moveModuleUp(moduleId);
        redirectAttributes.addFlashAttribute("successMessage", "Module order updated successfully!");
        return "redirect:/teacher/courses/" + (courseId != null ? courseId : "") + "/edit";
    }

    @PostMapping("/teacher/modules/{moduleId}/move-down")
    public String moveModuleDown(@PathVariable("moduleId") Long moduleId, RedirectAttributes redirectAttributes) {
        Long courseId = courseService.getModuleById(moduleId).map(m -> m.getCourseId()).orElse(null);
        courseService.moveModuleDown(moduleId);
        redirectAttributes.addFlashAttribute("successMessage", "Module order updated successfully!");
        return "redirect:/teacher/courses/" + (courseId != null ? courseId : "") + "/edit";
    }

    @PostMapping("/teacher/modules/{moduleId}/delete")
    public String deleteModule(@PathVariable("moduleId") Long moduleId, RedirectAttributes redirectAttributes) {
        Long courseId = courseService.getModuleById(moduleId).map(m -> m.getCourseId()).orElse(null);
        courseService.deleteModule(moduleId);
        redirectAttributes.addFlashAttribute("successMessage", "Module removed successfully!");
        return "redirect:/teacher/courses/" + (courseId != null ? courseId : "") + "/edit";
    }
}

