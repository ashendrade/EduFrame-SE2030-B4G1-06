package com.eduframepackage.eduframe.controller;

import com.eduframepackage.eduframe.dto.TicketReceiptDTO;
import com.eduframepackage.eduframe.model.Video;
import com.eduframepackage.eduframe.service.TicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.stream.Collectors;

@Controller
public class PageController {

    @Autowired
    private TicketService ticketService;

    @Autowired
    private com.eduframepackage.eduframe.quiz.service.UserService userService;

    @Autowired
    private com.eduframepackage.eduframe.service.VideoService videoService;

    @GetMapping("/")
    public String index(Model model) {
        // Pass top/recent videos to home page
        model.addAttribute("featuredVideos", videoService.getFeaturedVideos());
        model.addAttribute("recentVideos", videoService.getAllVideos());
        return "index";
    }

    @Autowired
    private com.eduframepackage.eduframe.service.CourseService courseService;

    @GetMapping("/browse")
    public String browse(@RequestParam(value = "search", required = false) String search,
                         @RequestParam(value = "category", required = false) String category,
                         @RequestParam(value = "level", required = false) String level,
                         Model model) {
        List<Video> filtered = videoService.searchVideos(search, category);

        List<com.eduframepackage.eduframe.model.Course> catalogCourses = courseService.searchCourses(search, category);
        
        List<com.eduframepackage.eduframe.model.Course> allCourses = courseService.getAllCourses();
        long computingCount = allCourses.stream().filter(c -> "Computing".equalsIgnoreCase(c.getCategory())).count();
        long engineeringCount = allCourses.stream().filter(c -> "Engineering".equalsIgnoreCase(c.getCategory())).count();
        long businessCount = allCourses.stream().filter(c -> "Business".equalsIgnoreCase(c.getCategory())).count();

        model.addAttribute("videos", filtered);
        model.addAttribute("courses", catalogCourses);
        model.addAttribute("searchQuery", search);
        model.addAttribute("selectedCategory", category != null ? category : "All");
        model.addAttribute("selectedLevel", level != null ? level : "All");

        model.addAttribute("totalCount", allCourses.size());
        model.addAttribute("computingCount", computingCount);
        model.addAttribute("engineeringCount", engineeringCount);
        model.addAttribute("businessCount", businessCount);
        model.addAttribute("undergradCount", allCourses.size()); // Default undergraduate catalog
        model.addAttribute("postgradCount", 0);
        return "browse";
    }

    @GetMapping("/play/{id}")
    public String play(@PathVariable("id") String id, Model model) {
        List<Video> allVideos = videoService.getAllVideos();
        Video currentVideo = videoService.getVideoById(id)
            .orElse(allVideos.isEmpty() ? new Video() : allVideos.get(0));

        // Related videos from the same category (excluding current)
        List<Video> relatedVideos = allVideos.stream()
            .filter(v -> !v.getId().equals(currentVideo.getId()) && v.getCategory().equalsIgnoreCase(currentVideo.getCategory()))
            .collect(Collectors.toList());

        // Fallback related if none in same category
        if (relatedVideos.isEmpty()) {
            relatedVideos = allVideos.stream()
                .filter(v -> !v.getId().equals(currentVideo.getId()))
                .collect(Collectors.toList());
        }

        model.addAttribute("video", currentVideo);
        model.addAttribute("playlist", relatedVideos);
        return "play";
    }

    @GetMapping("/upload")
    public String upload(org.springframework.security.core.Authentication auth, Model model) {
        if (auth != null && auth.isAuthenticated()) {
            boolean isTeacherOrAdmin = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_TEACHER") || a.getAuthority().equals("ROLE_ADMIN"));
            if (!isTeacherOrAdmin) {
                return "redirect:/student/dashboard";
            }
        } else {
            return "redirect:/login";
        }

        String fullName = "T. D. Adikari";
        String email = "teacher@eduframe.lk";
        if (auth != null && auth.isAuthenticated()) {
            email = auth.getName();
            try {
                com.eduframepackage.eduframe.quiz.entity.User user = userService.getByEmail(email);
                if (user != null && user.getFullName() != null && !user.getFullName().trim().isEmpty()) {
                    fullName = user.getFullName();
                }
            } catch (Exception e) {
                fullName = email;
            }
        }

        String initials = "TA";
        if (fullName != null && !fullName.trim().isEmpty()) {
            String[] parts = fullName.trim().split("\\s+");
            if (parts.length >= 2) {
                initials = (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
            } else {
                initials = fullName.substring(0, Math.min(2, fullName.length())).toUpperCase();
            }
        }

        model.addAttribute("lecturerName", fullName);
        model.addAttribute("lecturerEmail", email);
        model.addAttribute("lecturerInitials", initials);
        model.addAttribute("guestVideos", videoService.getAllVideos());
        return "upload";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @PostMapping("/register")
    public String handleRegistration(@RequestParam("fullName") String fullName,
                                     @RequestParam("email") String email,
                                     @RequestParam("password") String password,
                                     Model model) {
        try {
            userService.register(fullName, email, password, com.eduframepackage.eduframe.quiz.entity.Role.STUDENT);
            model.addAttribute("successMessage", "Registration successful! You can now log in with your credentials.");
        } catch (Exception ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            return "register";
        }
        return "register";
    }

    @GetMapping("/events")
    public String events() {
        return "events";
    }

    @GetMapping("/announcements")
    public String announcements() {
        return "announcements";
    }

    @GetMapping("/advertisements")
    public String advertisementsPage() {
        return "advertisements";
    }

    @GetMapping("/dashboard")
    public String dashboard(org.springframework.security.core.Authentication auth, Model model) {
        String role = "STUDENT";
        if (auth != null) {
            boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            boolean isTeacher = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_TEACHER"));
            if (isAdmin) {
                role = "ADMIN";
            } else if (isTeacher) {
                role = "TEACHER";
            }
            model.addAttribute("username", auth.getName());
        }
        model.addAttribute("userRole", role);
        return "dashboard";
    }

    @GetMapping("/support")
    public String support() {
        return "support";
    }

    @GetMapping("/staff/helpdesk")
    public String staffHelpDesk() {
        return "staff-helpdesk";
    }

    @GetMapping({"/staff/tickets/{ticketId}", "/staff/helpdesk/review/{ticketId}"})
    public String staffTicketReview(@PathVariable("ticketId") String ticketId, Model model) {
        try {
            TicketReceiptDTO ticket = ticketService.getTicketReceipt(ticketId);
            model.addAttribute("ticket", ticket);
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Ticket not found with ID: " + ticketId);
        }
        model.addAttribute("ticketId", ticketId);
        return "staff-ticket-detail";
    }

    @GetMapping("/staff/login")
    public String staffLogin() {
        return "redirect:/login?role=staff";
    }

    @GetMapping("/profile")
    public String userProfile(org.springframework.security.core.Authentication auth, Model model) {
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }
        String email = auth.getName();
        try {
            com.eduframepackage.eduframe.quiz.entity.User user = userService.getByEmail(email);
            model.addAttribute("user", user);
        } catch (Exception e) {
            model.addAttribute("errorMessage", "User profile details could not be retrieved.");
        }
        return "profile";
    }

    @PostMapping("/profile")
    public String updateProfile(@RequestParam("fullName") String fullName,
                                @RequestParam("email") String newEmail,
                                @RequestParam(value = "currentPassword", required = false) String currentPassword,
                                @RequestParam(value = "newPassword", required = false) String newPassword,
                                org.springframework.security.core.Authentication auth,
                                Model model) {
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }
        String currentEmail = auth.getName();
        try {
            com.eduframepackage.eduframe.quiz.entity.User updatedUser = userService.updateProfile(
                    currentEmail, fullName, newEmail, currentPassword, newPassword
            );
            model.addAttribute("user", updatedUser);
            model.addAttribute("successMessage", "Profile details updated successfully!");
        } catch (Exception ex) {
            try {
                com.eduframepackage.eduframe.quiz.entity.User user = userService.getByEmail(currentEmail);
                model.addAttribute("user", user);
            } catch (Exception ignored) {}
            model.addAttribute("errorMessage", ex.getMessage());
        }
        return "profile";
    }
}

