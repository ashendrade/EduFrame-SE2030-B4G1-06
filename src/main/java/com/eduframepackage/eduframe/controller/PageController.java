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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class PageController {

    @Autowired
    private TicketService ticketService;

    @Autowired
    private com.eduframepackage.eduframe.quiz.service.UserService userService;

    private final List<Video> mockVideos = new ArrayList<>();

    public PageController() {
        // Initialize sample videos with YouTube embeds (new uploads will use uploaded file path)
        mockVideos.add(new Video(
            "se2030-l1",
            "SE2030: Software Engineering - Introduction to MVC Architecture",
            "A comprehensive lecture explaining the Model-View-Controller design pattern in modern web architectures with concrete Java examples.",
            "Prof. Kanishka Jayasinghe",
            "45:12",
            "Computing",
            "2026-08-10",
            124,
            "/images/thumb-mvc.jpg",
            "https://www.youtube.com/embed/pTb0U4xW6h8"
        ));
        mockVideos.add(new Video(
            "se2030-l2",
            "SE2030: Design Patterns - Singleton & Factory Patterns",
            "Deep dive into creational design patterns. Learn how to write thread-safe singletons and compile-time decoupling using factories.",
            "Prof. Kanishka Jayasinghe",
            "52:30",
            "Computing",
            "2026-08-12",
            89,
            "/images/thumb-patterns.jpg",
            "https://www.youtube.com/embed/v9ejT8FO-7I"
        ));
        mockVideos.add(new Video(
            "it1010-l5",
            "IT1010: Object Oriented Programming in Java - Inheritance & Polymorphism",
            "In this session we cover base and subclass relationships, overriding, overloading, dynamic binding, and interface implementation.",
            "Dr. Tharindu Senanayake",
            "38:15",
            "Computing",
            "2026-08-05",
            345,
            "/images/thumb-oop.jpg",
            "https://www.youtube.com/embed/3W983z2697g"
        ));
        mockVideos.add(new Video(
            "ee1020-l1",
            "EE1020: Digital Logic Design - Boolean Algebra & K-Maps",
            "Simplifying Boolean expressions using laws and theorems. Graphical minimization of logic circuits using Karnaugh Maps.",
            "Dr. Priyantha Alwis",
            "1:05:40",
            "Engineering",
            "2026-08-01",
            78,
            "/images/thumb-kmaps.jpg",
            "https://www.youtube.com/embed/RO5alU6CMwE"
        ));
        mockVideos.add(new Video(
            "bm1010-l3",
            "BM1010: Principles of Marketing - Market Segmentation",
            "Analyzing how businesses divide broad target markets into consumer subsets based on demographic, geographic, and psychographic characteristics.",
            "Ms. Sanduni Perera",
            "28:40",
            "Business",
            "2026-08-09",
            156,
            "/images/thumb-marketing.jpg",
            "https://www.youtube.com/embed/hJ814j2o4Q4"
        ));
        mockVideos.add(new Video(
            "cs3020-l4",
            "CS3020: Operating Systems - CPU Scheduling Algorithms",
            "An analytical review of CPU scheduling mechanisms including First-Come-First-Served, Shortest-Job-First, Round Robin, and Priority Scheduling.",
            "Dr. Tharindu Senanayake",
            "58:20",
            "Computing",
            "2026-08-08",
            210,
            "/images/thumb-os.jpg",
            "https://www.youtube.com/embed/EWkfqT_8e0k"
        ));
    }

    @GetMapping("/")
    public String index(Model model) {
        // Pass top/recent videos to home page
        model.addAttribute("featuredVideos", mockVideos.subList(0, 3));
        model.addAttribute("recentVideos", mockVideos);
        return "index";
    }

    @Autowired
    private com.eduframepackage.eduframe.service.CourseService courseService;

    @GetMapping("/browse")
    public String browse(@RequestParam(value = "search", required = false) String search,
                         @RequestParam(value = "category", required = false) String category,
                         @RequestParam(value = "level", required = false) String level,
                         Model model) {
        List<Video> filtered = mockVideos;

        if (category != null && !category.isEmpty() && !category.equalsIgnoreCase("All")) {
            filtered = filtered.stream()
                .filter(v -> v.getCategory().equalsIgnoreCase(category))
                .collect(Collectors.toList());
        }

        if (search != null && !search.isEmpty()) {
            String q = search.toLowerCase();
            filtered = filtered.stream()
                .filter(v -> v.getTitle().toLowerCase().contains(q) || 
                             v.getDescription().toLowerCase().contains(q) || 
                             v.getLecturer().toLowerCase().contains(q))
                .collect(Collectors.toList());
        }

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
        Video currentVideo = mockVideos.stream()
            .filter(v -> v.getId().equals(id))
            .findFirst()
            .orElse(mockVideos.get(0)); // default to first video if not found

        // Related videos from the same category (excluding current)
        List<Video> relatedVideos = mockVideos.stream()
            .filter(v -> !v.getId().equals(currentVideo.getId()) && v.getCategory().equals(currentVideo.getCategory()))
            .collect(Collectors.toList());

        // Fallback related if none in same category
        if (relatedVideos.isEmpty()) {
            relatedVideos = mockVideos.stream()
                .filter(v -> !v.getId().equals(currentVideo.getId()))
                .collect(Collectors.toList());
        }

        model.addAttribute("video", currentVideo);
        model.addAttribute("playlist", relatedVideos);
        return "play";
    }

    @GetMapping("/upload")
    public String upload(org.springframework.security.core.Authentication auth, Model model) {
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

